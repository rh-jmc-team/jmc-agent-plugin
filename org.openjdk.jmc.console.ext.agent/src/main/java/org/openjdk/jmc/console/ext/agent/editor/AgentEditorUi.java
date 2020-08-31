package org.openjdk.jmc.console.ext.agent.editor;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.editor.sections.EventDetailSection;
import org.openjdk.jmc.console.ext.agent.editor.sections.EventListSection;
import org.openjdk.jmc.console.ext.agent.editor.sections.GlobalConfigSection;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepositoryFactory;
import org.openjdk.jmc.console.ext.agent.utils.ProbeValidator;
import org.openjdk.jmc.console.ext.agent.utils.ValidationResult;
import org.openjdk.jmc.console.ext.agent.wizards.BaseWizardPage;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;
import org.openjdk.jmc.ui.misc.DisplayToolkit;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Stream;

public class AgentEditorUi {
	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	private final AgentJmxHelper helper;
	private final PresetRepository presetRepository = PresetRepositoryFactory.createSingleton();

	private final AgentEditorAction[] actions;

	private GlobalConfigSection globalConfigSection;
	private EventListSection eventListSection;
	private EventDetailSection eventDetailSection;

	public AgentEditorUi(AgentEditor editor, AgentEditorAction[] actions) {
		helper = editor.getAgentEditorInput().getAgentJmxHelper();
		this.actions = Arrays.copyOf(actions, actions.length);

		bindAgentEditorActions();
	}

	protected void createContent(Form form, FormToolkit toolkit) {
		Composite body = form.getBody();
		body.setLayout(MCLayoutFactory.createFormPageLayout());

		Composite container = new Composite(body, SWT.NONE);
		container.setLayout(new FillLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm hSashForm = new SashForm(container, SWT.HORIZONTAL);
		SashForm vSashForm = new SashForm(hSashForm, SWT.VERTICAL);

		globalConfigSection = new GlobalConfigSection(vSashForm, toolkit);
		eventListSection = new EventListSection(vSashForm, toolkit);
		eventDetailSection = new EventDetailSection(hSashForm, toolkit);

		eventListSection.addSelectionChangedListener(selectionChangedEvent -> eventDetailSection
				.setInput((IEvent) selectionChangedEvent.getStructuredSelection().getFirstElement()));

		hSashForm.setWeights(new int[] {3, 7});
		vSashForm.setWeights(new int[] {1, 4});
	}

	protected void refresh(Runnable done) {
		EXECUTOR_SERVICE.submit(() -> {
			String probes = helper.retrieveEventProbes();
			IPreset[] preset = new IPreset[] {null};
			if (probes != null && !probes.isEmpty()) {
				preset[0] = presetRepository.createPreset();
				try {
					preset[0].deserialize(probes);
				} catch (IOException | SAXException e) {
					// TODO: display error dialog
					e.printStackTrace();
				}
			}

			DisplayToolkit.inDisplayThread().execute(() -> {
				globalConfigSection.setInput(preset[0]);
				eventListSection.setInput(preset[0]);
				eventDetailSection.setInput(null);
				done.run();
				Stream.of(actions).forEach((action) -> action.setEnabled(true));
			});
		});
	}

	private void refresh() {
		Stream.of(actions).forEach((action) -> action.setEnabled(false));
		refresh(() -> {
		});
	}

	private void loadPreset() {
		Stream.of(actions).forEach((action) -> action.setEnabled(false));

		PresetSelectorWizardPage presetSelector = new PresetSelectorWizardPage();
		if (new OnePageWizardDialog(Display.getCurrent().getActiveShell(), presetSelector).open() != Window.OK) {
			Stream.of(actions).forEach((action) -> action.setEnabled(true));
		}

		try {
			String parentPath = PresetRepositoryFactory.getCreatedStorageDir().getAbsolutePath();
			String filePath = parentPath + File.separator + presetSelector.getSelectedPreset().getFileName();
			applyConfig(filePath);
			refresh(() -> {
			});
		} catch (IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.WARNING,
					"Could not find the file " + presetSelector.getSelectedPreset().getFileName(), e);
		} finally {
			Stream.of(actions).forEach((action) -> action.setEnabled(true));
		}
	}

	private void savePreset() {
		IPreset preset = presetRepository.createPreset();
		try {
			preset.deserialize(helper.retrieveEventProbes());
			presetRepository.addPreset(preset);
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}
		DialogToolkit.openConfirmOnUiThread("Saved to Preset", "Configuration saved to " + preset.getFileName());
	}

	private void bindAgentEditorActions() {
		Stream.of(actions).forEach((action) -> {
			switch (action.getType()) {
			case REFRESH:
				action.setRunnable(this::refresh);
				break;
			case LOAD_PRESET:
				action.setRunnable(this::loadPreset);
				break;
			case SAVE_AS_PRESET:
				action.setRunnable(this::savePreset);
			}

		});
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
			tableInspector.setInput(presetRepository);

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

	private static final String ERROR_PAGE_TITLE = "Error in Configuration";

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
