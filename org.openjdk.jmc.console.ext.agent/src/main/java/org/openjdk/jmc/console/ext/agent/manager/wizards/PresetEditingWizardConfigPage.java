package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;

public class PresetEditingWizardConfigPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_TITLE = "Editing Preset Global Configurations";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION = "Global configurations are defaults which applies to any event missing a per-even configuration.";

	private final IPreset preset;

	protected PresetEditingWizardConfigPage(IPreset preset) {
		super(PAGE_NAME);

		this.preset = preset;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		// TODO: create manager control here
		container.setLayout(new FillLayout());
		new Label(container, SWT.NONE).setText("TODO: create config page control here");

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}
}
