package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;

public class EventEditingWizard extends Wizard {

	private final IEvent event;

	protected EventEditingWizard(IEvent event) {
		this.event = event;
	}

	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public void addPages() {
		addPage(new EventEditingWizardConfigPage(event));
		addPage(new EventEditingWizardParameterPage(event));
		addPage(new EventEditingWizardFieldPage(event));
	}
}
