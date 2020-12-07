package org.openjdk.jmc.console.ext.agent.tabs.overview;

import java.io.IOException;
import java.util.logging.Level;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.editor.AgentFormPage;
import org.openjdk.jmc.console.ext.agent.messages.internal.Messages;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class OverviewTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.overview.OverviewTab"; //$NON-NLS-1$
	private VirtualMachine vm;
	private AgentJmxHelper agentJMXHelper;
	private Composite container;

	public OverviewTab(AgentEditor editor) {
		super(editor, ID, Messages.OverviewTab_TITLE);
		// TODO Auto-generated constructor stub
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, AgentJmxHelper agentJMXHelper) {
		this.agentJMXHelper = agentJMXHelper;

		ScrolledForm form = managedForm.getForm();

		this.container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.OverviewTab_MESSAGE_AGENT_LOADED);
	}

	private void loadAgentListener() {
		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.OverviewTab_MESSAGE_AGENT_LOADED);
		container.layout();
	}

}
