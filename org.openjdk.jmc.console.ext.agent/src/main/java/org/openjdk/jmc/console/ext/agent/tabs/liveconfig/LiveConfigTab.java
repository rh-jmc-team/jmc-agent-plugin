package org.openjdk.jmc.console.ext.agent.tabs.liveconfig;

import javax.inject.Inject;
import javax.management.MBeanServerConnection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ext.agent.AgentJMXHelper;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.editor.AgentFormPage;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

public class LiveConfigTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.liveconfig.LiveConfigTab";
	private static final String TITLE = "Live Config";
	private EventTreeSection eventTree;

	public LiveConfigTab(AgentEditor editor) {
		super(editor, ID, TITLE);
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, IConnectionHandle handle) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Composite container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());

		MBeanServerConnection mbeanServer = handle.getServiceOrDummy(MBeanServerConnection.class);
		eventTree = new EventTreeSection(container, toolkit);
		AgentJMXHelper agentJMXHelper = new AgentJMXHelper(mbeanServer);
		eventTree.setAgentJMXHelper(agentJMXHelper);
	}

}
