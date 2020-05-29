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
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class OverviewTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.overview.OverviewTab";
	private static final String TITLE = "Overview";
	private LoadAgentSection loadAgentSection;
	private VirtualMachine vm;
	private AgentJmxHelper agentJMXHelper;
	private Composite container;

	public OverviewTab(AgentEditor editor) {
		super(editor, ID, TITLE);
		// TODO Auto-generated constructor stub
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, AgentJmxHelper agentJMXHelper, IConnectionHandle connectionHandle) {
		this.agentJMXHelper = agentJMXHelper;
		
		ScrolledForm form = managedForm.getForm();

		this.container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());

		if (!agentJMXHelper.isMXBeanRegistered() && agentJMXHelper.isLocalJvm()) {
			String pid = connectionHandle.getServerDescriptor().getJvmInfo().getPid().toString();
			vm = initVM(pid);
			loadAgentSection = new LoadAgentSection(container, vm);
			loadAgentSection.setLoadAgentListener(this::loadAgentListener);
			return;
		}
		
		if (!agentJMXHelper.isMXBeanRegistered() && !agentJMXHelper.isLocalJvm()) {
			Label l = new Label(container, SWT.NONE);
			l.setText("Starting agent on remote JVM is not supported");
			return;
		}

		Label l = new Label(container, SWT.NONE);
		l.setText("Agent is loaded");
	}

	private VirtualMachine initVM(String pid) {
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(pid);
		} catch (AttachNotSupportedException | IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.SEVERE,
					"Could not attach process with pid " + pid + " and create a VirtualMachine", e);
		}
		return vm;
	}

	private void loadAgentListener() {
		loadAgentSection.dispose();
		Label l = new Label(container, SWT.NONE);
		l.setText("Agent is loaded");
		container.layout();
	}

}
