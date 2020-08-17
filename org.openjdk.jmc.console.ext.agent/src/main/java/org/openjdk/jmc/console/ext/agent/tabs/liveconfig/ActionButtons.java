/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, Red Hat Inc. All rights reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.console.ext.agent.tabs.liveconfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;

import javax.xml.transform.stream.StreamSource;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepositoryFactory;
import org.openjdk.jmc.console.ext.agent.tabs.presets.internal.ProbeValidator;
import org.openjdk.jmc.console.ext.agent.tabs.presets.internal.ValidationResult;
import org.openjdk.jmc.console.ext.agent.wizards.BaseWizardPage;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;
import org.xml.sax.SAXException;

public class ActionButtons extends Composite {
	private static final String LABEL_SAVE_TO_PRESET_BUTTON = "Save to Preset";
	private static final String LABEL_SAVE_TO_FILE_BUTTON = "Save to File";
	private static final String LABEL_APPLY_PRESET_BUTTON = "Apply Preset";
	private static final String LABEL_APPLY_LOCAL_CONFIG_BUTTON = "Apprivate final PresetRepository presetRepository = PresetRepositoryFactory.createSingleton();ply Local Config";
	private static final String ERROR_PAGE_TITLE = "Error in Configuration";
	private static final String FILE_OPEN_FILTER_PATH = "file.open.filter.path"; // $NON-NLS-1$
	private static final String PRESET_XML_EXTENSION = "*.xml"; // $NON-NLS-1$
	private static final String MESSAGE_APPLY_LOCAL_CONFIG = "Apply a local configuration";

	private AgentJmxHelper helper;
	private PresetRepository repository;
	private EventTreeSection eventTree;

	private Button saveToPresetButton;
	private Button saveToFileButton;
	private Button applyPresetButton;
	private Button applyLocalConfigButton;

	public ActionButtons(Composite parent, PresetRepository repository, AgentJmxHelper helper,
			EventTreeSection eventTree) {
		super(parent, SWT.NONE);

		this.repository = repository;
		this.helper = helper;
		this.eventTree = eventTree;

		setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, true));
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		createButtons(this);
		bindListeners();
	}

	private void createButtons(Composite parent) {
		saveToPresetButton = createButton(parent, LABEL_SAVE_TO_PRESET_BUTTON);
		saveToFileButton = createButton(parent, LABEL_SAVE_TO_FILE_BUTTON);
		applyPresetButton = createButton(parent, LABEL_APPLY_PRESET_BUTTON);
		applyLocalConfigButton = createButton(parent, LABEL_APPLY_LOCAL_CONFIG_BUTTON);
	}

	private final Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return button;
	}

	private void bindListeners() {
		saveToPresetButton.addListener(SWT.Selection, e -> onSaveToPreset());
		saveToFileButton.addListener(SWT.Selection, e -> onSaveToFile());
		applyLocalConfigButton.addListener(SWT.Selection, e -> onApplyLocalConfig());
		applyPresetButton.addListener(SWT.Selection, e -> onApplyPreset());
	}

	private void onSaveToPreset() {
		IPreset preset = repository.createPreset();
		try {
			preset.deserialize(helper.retrieveEventProbes());
			repository.addPreset(preset);
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}
		DialogToolkit.openConfirmOnUiThread("Saved to Preset", "Configuration saved to " + preset.getFileName());
	}

	private void onSaveToFile() {
		String[] files = openFileDialog("Save to File", new String[] {PRESET_XML_EXTENSION}, SWT.SAVE | SWT.SINGLE);
		if (files == null || files.length == 0) {
			return;
		}
		IPreset preset = repository.createPreset();
		try {
			preset.deserialize(helper.retrieveEventProbes());
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}

		File file = new File(files[0]);
		try {
			repository.exportPreset(preset, file);
		} catch (IOException e) {
			DialogToolkit.openConfirmOnUiThread("unable to save to file", e.getLocalizedMessage());
		}
	}

	private void onApplyLocalConfig() {
		String[] path = openFileDialog(MESSAGE_APPLY_LOCAL_CONFIG, new String[] {PRESET_XML_EXTENSION},
				SWT.OPEN | SWT.SINGLE);
		if (path != null && path.length != 0) {
			applyConfig(path[0]);
		}
		eventTree.refreshTree();
		;
	}

	private void onApplyPreset() {
		PresetSelectorWizardPage presetSelector = new PresetSelectorWizardPage();
		while (new OnePageWizardDialog(Display.getCurrent().getActiveShell(), presetSelector).open() == Window.OK) {
			String filePath = null;
			try {
				String parentPath = PresetRepositoryFactory.getCreatedStorageDir().getAbsolutePath();
				filePath = parentPath + "/" + presetSelector.getSelectedPreset().getFileName();
			} catch (IOException e) {
				AgentPlugin.getDefault().getLogger().log(Level.WARNING,
						"Could not find the file " + presetSelector.getSelectedPreset().getFileName(), e);
			}
			applyConfig(filePath);
			break;
		}
		eventTree.refreshTree();
	}

	private class PresetSelectorWizardPage extends BaseWizardPage {
		private static final String PAGE_NAME = "Apply Preset";
		private static final String MESSAGE_PAGE_TITLE = "Apply Preset";
		private static final String MESSAGE_PAGE_DESCRIPTION = "Select a preset to apply";
		private static final String ID_PRESET = "preset"; // $NON-NLS-1$
		private static final String MESSAGE_EVENTS = "event(s)";

		private TableInspector tableInspector;
		private IPreset selectedPreset;

		public PresetSelectorWizardPage() {
			super(PAGE_NAME);
		}

		@Override
		public void createControl(Composite parent) {
			initializeDialogUnits(parent);

			setTitle(MESSAGE_PAGE_TITLE);
			setDescription(MESSAGE_PAGE_DESCRIPTION);

			ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
			Composite container = new Composite(sc, SWT.NONE);
			sc.setContent(container);

			container.setLayout(new FillLayout());
			createPresetTableContainer(container);

			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);
			sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			setControl(sc);

		}

		private Composite createPresetTableContainer(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new FillLayout());

			tableInspector = new TableInspector(container, TableInspector.MULTI) {
				@Override
				protected void addColumns() {
					addColumn(ID_PRESET, new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							if (!(element instanceof IPreset)) {
								throw new IllegalArgumentException("element must be an IPreset"); // $NON-NLS-1$
							}

							IPreset preset = (IPreset) element;
							return preset.getFileName() + " - " + preset.getEvents().length + " " + MESSAGE_EVENTS;
						}

						@Override
						public Image getImage(Object element) {
							return AgentPlugin.getDefault().getImage(AgentPlugin.ICON_AGENT); // TODO: replace the icon in the future
						}
					});
				}
			};
			tableInspector.setContentProvider(new PresetTableContentProvider());
			tableInspector.getViewer().addSelectionChangedListener(
					e -> selectedPreset = (IPreset) e.getStructuredSelection().getFirstElement());
			tableInspector.setInput(repository);

			return container;
		}

		public IPreset getSelectedPreset() {
			return selectedPreset;
		}

	}

	private static class PresetTableContentProvider extends AbstractStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof PresetRepository)) {
				throw new IllegalArgumentException("input element must be a PresetRepository"); // $NON-NLS-1$
			}

			PresetRepository repository = (PresetRepository) inputElement;
			return repository.listPresets();
		}
	}

	private String[] openFileDialog(String title, String[] extensions, int style) {
		String filterPath = FlightRecorderUI.getDefault().getDialogSettings().get(FILE_OPEN_FILTER_PATH);
		if (filterPath != null && Files.notExists(Paths.get(filterPath))) {
			filterPath = System.getProperty("user.home", "./"); // $NON-NLS-1$ $NON-NLS-2$
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		FileDialog dialog = new FileDialog(window.getShell(), style);
		dialog.setFilterPath(filterPath);
		dialog.setText(title);
		dialog.setFilterExtensions(extensions);

		if (dialog.open() == null) {
			return new String[0];
		}

		return Arrays.stream(dialog.getFileNames()).map(name -> dialog.getFilterPath() + File.separator + name)
				.toArray(String[]::new);
	}

	private void applyConfig(String path) {
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(path));
			String validationMessage = validateProbeDefinition(new String(bytes, StandardCharsets.UTF_8));
			if (!validationMessage.isEmpty()) {
				DialogToolkit.openConfirmOnUiThread(ERROR_PAGE_TITLE, validationMessage);
				return;
			}
			helper.defineEventProbes(new String(bytes, StandardCharsets.UTF_8));
		} catch (IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not apply XML config", e);
		}
	}

	private String validateProbeDefinition(String configuration) {
		ProbeValidator validator = new ProbeValidator();
		try {
			validator.validate(
					new StreamSource(new ByteArrayInputStream(configuration.getBytes(StandardCharsets.UTF_8))));
		} catch (IOException e) {
			return e.getMessage();
		} catch (SAXException e) {
			// noop
		}

		ValidationResult result = validator.getValidationResult();
		StringBuilder sb = new StringBuilder();
		if (result.getFatalError() != null) {
			sb.append("[FATAL]\t").append(result.getFatalError().getMessage()).append('\n');
		}

		for (SAXException error : result.getErrors()) {
			sb.append("[ERROR]\t").append(error.getMessage()).append('\n');
		}

		for (SAXException warning : result.getErrors()) {
			sb.append("[WARN]\t").append(warning.getMessage()).append('\n');
		}

		return sb.toString();
	}
}
