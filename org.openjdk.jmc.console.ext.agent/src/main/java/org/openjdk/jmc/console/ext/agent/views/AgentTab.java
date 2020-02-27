package org.openjdk.jmc.console.ext.agent.views;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.common.io.IOToolkit;
import org.openjdk.jmc.console.ui.editor.IConsolePageContainer;
import org.openjdk.jmc.console.ui.editor.IConsolePageStateHandler;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

public class AgentTab implements IConsolePageStateHandler {
	private static final String NO_EVENT_PROBES_XML = "no-event-probes.xml";
	private static final String TEMP_DIR_NAME = "eventProbes";

	@Inject
	protected void createPageContent(IConsolePageContainer page, IManagedForm managedForm, IConnectionHandle handle) {
		// Create SectionPartManager
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Composite container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());

		Composite chartLabelContainer = toolkit.createComposite(container);
		chartLabelContainer.setLayout(new GridLayout(2, false));
		Label label = new Label(chartLabelContainer, SWT.NULL);
		label.setText("Agent jar Path: ");
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.widthHint = 100;
		label.setLayoutData(gridData);
		Text agentJarPath = new Text(chartLabelContainer, SWT.LEFT | SWT.BORDER);
		agentJarPath.setLayoutData(gridData);
		agentJarPath.setText("Enter path...");

		Button button = new Button(container, SWT.PUSH);
		button.setText("Load agent");
		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String pid = handle.getServerDescriptor().getJvmInfo().getPid().toString();
				if (loadAgent(agentJarPath.getText(), pid)) {
					button.setVisible(false);
				}
			}
		});
	}

	private boolean loadAgent(String agentJar, String pid) {
		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			File tempFile = materialize(TEMP_DIR_NAME, NO_EVENT_PROBES_XML, AgentTab.class);
			vm.loadAgent(agentJar,tempFile.getPath());
			vm.detach();
		} catch (AgentInitializationException e) {
			System.err.println("ERROR: Could not access jdk.internal.misc.Unsafe! Rerun your application with '--add-opens java.base/jdk.internal.misc=ALL-UNNAMED'.");
			return false;
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
		return true;
	}

	private static File materialize(String dir, String file, Class<?> clazz) {
		File matDir;
		try {
			matDir = materialize(clazz, dir, file);
			File matFile = new File(matDir, file);
			return matFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File materialize(Class<?> clazz, String directoryName, String fileName) throws IOException {
		File directory = File.createTempFile(directoryName, ".dir");
		materialize(clazz, fileName, directory);
		return directory;
	}

	private static void materialize(Class<?> clazz, String fileName, File directory)
			throws IOException {
		if (fileName == null) {
			throw new IOException("Must specify file name to materialize");
		}
		if (!directory.delete()) {
			throw new IOException("Could not delete directory: " + directory.getAbsolutePath());
		}
		if (!directory.mkdirs()) {
			throw new IOException("Could not create directory: " + directory.getAbsolutePath());
		}
		InputStream in = null;
		try {
			in = clazz.getResourceAsStream(fileName);
			if (in != null) {
				FileOutputStream os = null;
				try {
					File file = new File(directory, fileName);
					os = new FileOutputStream(file);
					IOToolkit.copy(in, os);
					os.close();
				} finally {
					IOToolkit.closeSilently(os);
				}
			}
		} finally {
			IOToolkit.closeSilently(in);
		}
	}

	@Override
	public boolean saveState(IMemento state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
