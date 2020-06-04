package org.openjdk.jmc.console.ext.agent.wizards;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditorInput;
import org.openjdk.jmc.ui.common.jvm.JVMDescriptor;
import org.openjdk.jmc.ui.misc.DialogToolkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public class StartAgentWizard extends Wizard {
	private static final String MESSAGE_FAILED_TO_START_AGENT = "Failed to start JMC Agent";
	private static final String MESSAGE_FAILED_TO_OPEN_AGENT_EDITOR = "Failed to open the JMC Agent Editor";
	private static final String MESSAGE_UNEXPECTED_ERROR_HAS_OCCURRED = "An unexpected error occurred.";
	private static final String MESSAGE_ACCESS_TO_UNSAFE_REQUIRED = "Could not access jdk.internal.misc.Unsafe! Rerun your application with '--add-opens java.base/jdk.internal.misc=ALL-UNNAMED'.";

	private final AgentJmxHelper helper;
	private final StartAgentWizardPage startAgentWizardPage;

	public StartAgentWizard(AgentJmxHelper helper) {
		this.helper = helper;
		startAgentWizardPage = new StartAgentWizardPage(helper);
	}

	@Override
	public boolean performFinish() {
		JVMDescriptor targetJvm = startAgentWizardPage.getTargetJvm();
		String agentJarPath = startAgentWizardPage.getAgentJarPath();
		String agentXmlPath = startAgentWizardPage.getAgentXmlPath();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			VirtualMachine vm = VirtualMachine.attach(targetJvm.getPid() + "");
			loadAgent(vm, agentJarPath, agentXmlPath);
			IEditorInput ei = new AgentEditorInput(helper.getServerHandle());
			window.getActivePage().openEditor(ei, AgentEditor.EDITOR_ID, true);
		} catch (AttachNotSupportedException | IOException | AgentLoadException e) {
			DialogToolkit.showException(window.getShell(), MESSAGE_FAILED_TO_START_AGENT,
					MESSAGE_UNEXPECTED_ERROR_HAS_OCCURRED, e);
			return false;
		} catch (AgentInitializationException e) {
			DialogToolkit.showException(window.getShell(), MESSAGE_FAILED_TO_START_AGENT,
					MESSAGE_ACCESS_TO_UNSAFE_REQUIRED, e);
			return false;
		} catch (PartInitException e) {
			DialogToolkit.showException(window.getShell(), MESSAGE_FAILED_TO_OPEN_AGENT_EDITOR,
					MESSAGE_UNEXPECTED_ERROR_HAS_OCCURRED, e);
			return false;
		}

		return true;
	}

	private void loadAgent(VirtualMachine vm, String agentJar, String xmlPath)
			throws IOException, AgentLoadException, AgentInitializationException {
		if (Files.notExists(Paths.get(agentJar))) {
			throw new IllegalArgumentException("the Agent JAR path does exists");
		}

		if (!xmlPath.isEmpty() && Files.notExists(Paths.get(xmlPath))) {
			throw new IllegalArgumentException("the Agent configuration path does exists");
		}

		vm.loadAgent(agentJar, xmlPath);
	}

	@Override
	public void addPages() {
		addPage(startAgentWizardPage);
	}
}
