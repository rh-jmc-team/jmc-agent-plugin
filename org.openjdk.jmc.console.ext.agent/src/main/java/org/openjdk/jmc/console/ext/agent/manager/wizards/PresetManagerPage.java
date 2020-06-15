package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class PresetManagerPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Manager";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_TITLE = "JMC Agent Configuration Preset Manager";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION = "Presets for JMC agent are useful to repeatedly apply configurations to a running JMC agent.";

	private final PresetRepository repository;

	public PresetManagerPage(PresetRepository repository) {
		super(PAGE_NAME);

		this.repository = repository;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_MANAGER_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		// TODO: create manager control here
		container.setLayout(new FillLayout());
		new Label(container, SWT.NONE).setText("TODO: create manager control here");

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	// TODO: call this function when "New" or "Edit" button clicked
	private void openPresetEditingWizardFor(IPreset preset) {
		if (DialogToolkit.openWizardWithHelp(new PresetEditingWizard(preset))) {
			// TODO: save the modified preset to the repository
		}
	}
}
