package org.openjdk.jmc.console.ext.agent.ui.tabs.overview;

import java.io.IOException;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.management.MBeanServerConnection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ext.agent.ui.AgentJMXHelper;
import org.openjdk.jmc.console.ext.agent.ui.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.ui.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.ui.editor.AgentFormPage;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class OverviewTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.ui.tabs.overview";
	private static final String TITLE = "Overview";
	private LoadAgentSection loadAgentSection;
	private VirtualMachine vm;
	private AgentJMXHelper agentJMXHelper;
	private Composite container;

	public OverviewTab(AgentEditor editor) {
		super(editor, ID, TITLE);
		// TODO Auto-generated constructor stub
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, IConnectionHandle handle) {
		ScrolledForm form = managedForm.getForm();

		this.container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());
		String pid = handle.getServerDescriptor().getJvmInfo().getPid().toString();
		vm  = initVM(pid);
		MBeanServerConnection mbsc = handle.getServiceOrDummy(MBeanServerConnection.class);

		agentJMXHelper = new AgentJMXHelper(mbsc);
		if (agentJMXHelper.isMXBeanRegistered()) {
			Label l = new Label(container, SWT.NONE);
			l.setText("Agent is loaded");
		} else {
			loadAgentSection = new LoadAgentSection(container, vm);
			loadAgentSection.setLoadAgentListener(() -> loadAgentListener());
		}
	}

	private VirtualMachine initVM(String pid) {
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(pid);
		} catch (AttachNotSupportedException | IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.SEVERE, "Could not attatch process with pid " + pid + " and create a VirtualMachine", e);
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
