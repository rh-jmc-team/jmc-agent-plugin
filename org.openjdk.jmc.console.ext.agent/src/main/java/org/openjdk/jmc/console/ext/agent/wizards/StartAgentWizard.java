package org.openjdk.jmc.console.ext.agent.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;

public class StartAgentWizard extends Wizard {

	private final AgentJmxHelper helper;

	public StartAgentWizard(AgentJmxHelper helper) {
		this.helper = helper;
	}

	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public void addPages() {
		addPage(new StartAgentWizardPage(helper));
	}
}
