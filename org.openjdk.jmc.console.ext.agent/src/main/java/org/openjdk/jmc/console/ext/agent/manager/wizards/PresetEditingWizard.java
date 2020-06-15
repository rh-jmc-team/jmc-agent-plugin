package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;

public class PresetEditingWizard extends Wizard {

	private final IPreset preset;

	protected PresetEditingWizard(IPreset preset) {
		this.preset = preset;
	}

	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public void addPages() {
		addPage(new PresetEditingWizardConfigPage(preset));
		addPage(new PresetEditingWizardEventPage(preset));
		addPage(new PresetEditingWizardPreviewPage(preset));
	}
}
