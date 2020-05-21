package org.openjdk.jmc.console.ext.agent.ui;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.sun.tools.attach.VirtualMachine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.ide.IDE;
import org.openjdk.jmc.console.ext.agent.editor.XmlEditor;
import org.openjdk.jmc.ui.MCPathEditorInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public class EditAgentSection extends Composite {
	private static final String MESSAGE_ENTER_PATH = "Enter Path...";
	private static final String MESSAGE_AGENT_XML_PATH = "XML Path: ";
	private static final String MESSAGE_BROWSE = "Browse";
	private static final String MESSAGE_EDIT = "Edit";
	private static final String MESSAGE_VALIDATE = "Validate";
	private static final String MESSAGE_APPLY = "Apply";

	private AgentJMXHelper agentJMXHelper = null;

	public EditAgentSection(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		{
			Composite row = new Composite(this, SWT.NO_BACKGROUND);
			row.setLayout(new GridLayout(3, false));

			GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
			gridData.widthHint = 100;

			Label label = new Label(row, SWT.NONE);
			label.setText(MESSAGE_AGENT_XML_PATH);
			label.setLayoutData(gridData);

			Text text = new Text(row, SWT.LEFT | SWT.BORDER);
			text.setLayoutData(gridData);
			text.setText(MESSAGE_ENTER_PATH);

			Button browse = new Button(row, SWT.PUSH);
			browse.setText(MESSAGE_BROWSE);
			browse.setLayoutData(gridData);
			browse.addListener(SWT.Selection, e -> {
				FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell());
				fd.setFilterExtensions(new String[] {"*.xml"});
				String filename = fd.open();
				if (filename != null) {
					text.setText(fd.getFilterPath() + File.separator + fd.getFileName());
				}
			});

			row = new Composite(this, SWT.NO_BACKGROUND);
			row.setLayout(new GridLayout(3, false));
			
			Button edit = new Button(row, SWT.PUSH);
			edit.setText(MESSAGE_EDIT);
			edit.setLayoutData(gridData);
			edit.addListener(SWT.Selection, event -> {
				IEditorInput input = new MCPathEditorInput(new File(text.getText()), false);
				input = XmlEditor.convertInput(input);
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, XmlEditor.EDITOR_ID);
				} catch (PartInitException e) {
					AgentUi.getLogger().log(Level.WARNING, "Could not open XML editor", e);
				}
			});
			
			Button validate = new Button(row, SWT.PUSH);
			validate.setText(MESSAGE_VALIDATE);
			validate.setLayoutData(gridData);
			validate.addListener(SWT.Selection, event -> {
				// TODO Auto-generated method stub
			});
			
			Button apply = new Button(row, SWT.PUSH);
			apply.setText(MESSAGE_APPLY);
			apply.setLayoutData(gridData);
			apply.addListener(SWT.Selection, event -> {
				try {
					byte[] bytes = Files.readAllBytes(Paths.get(text.getText()));
					agentJMXHelper.defineEventProbes(new String(bytes));
				} catch (IOException e) {
					AgentUi.getLogger().log(Level.WARNING, "Could not apply XML config", e);
				}
			});
		}
	}

	/*package-private*/ void setAgentJMXHelper(AgentJMXHelper agentJMXHelper) {
		this.agentJMXHelper = agentJMXHelper;
	}
}
