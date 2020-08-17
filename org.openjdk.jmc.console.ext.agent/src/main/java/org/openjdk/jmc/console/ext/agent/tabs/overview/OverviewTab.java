package org.openjdk.jmc.console.ext.agent.tabs.overview;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditorBak;
import org.openjdk.jmc.console.ext.agent.editor.AgentFormPage;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

import com.sun.tools.attach.VirtualMachine;

public class OverviewTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.overview.OverviewTab";
	private static final String TITLE = "Overview";
	private VirtualMachine vm;
	private AgentJmxHelper agentJMXHelper;
	private Composite container;

	public OverviewTab(AgentEditorBak editor) {
		super(editor, ID, TITLE);
		// TODO Auto-generated constructor stub
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, AgentJmxHelper agentJMXHelper) {
		this.agentJMXHelper = agentJMXHelper;

		ScrolledForm form = managedForm.getForm();

		this.container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());

		Label l = new Label(container, SWT.NONE);
		l.setText("Agent is loaded");
	}

	private void loadAgentListener() {
		Label l = new Label(container, SWT.NONE);
		l.setText("Agent is loaded");
		container.layout();
	}

}
