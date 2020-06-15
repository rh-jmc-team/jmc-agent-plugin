package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class PresetEditingWizardEventPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_TITLE = "Editing Preset Events";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_DESCRIPTION = "Add new events to the preset, or remove/edit existing events.";

	private final IPreset preset;

	protected PresetEditingWizardEventPage(IPreset preset) {
		super(PAGE_NAME);

		this.preset = preset;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		// TODO: create event page control here
		container.setLayout(new FillLayout());
		new Label(container, SWT.NONE).setText("TODO: create event page control here");

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	// TODO: call this function when "New" or "Edit" button clicked
	private void openEventEditingWizardFor(IEvent event) {
		if (DialogToolkit.openWizardWithHelp(new EventEditingWizard(event))) {
			// TODO: save the modified event to the preset
		}
	}
}
