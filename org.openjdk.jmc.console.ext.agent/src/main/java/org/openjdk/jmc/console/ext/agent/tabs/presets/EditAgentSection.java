package org.openjdk.jmc.console.ext.agent.tabs.presets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import javax.xml.transform.stream.StreamSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.tabs.editor.internal.XmlEditor;
import org.openjdk.jmc.console.ext.agent.tabs.presets.internal.ProbeValidator;
import org.openjdk.jmc.console.ext.agent.tabs.presets.internal.ValidationResult;
import org.openjdk.jmc.ui.MCPathEditorInput;
import org.xml.sax.SAXException;

public class EditAgentSection extends Composite {
	private static final String MESSAGE_ENTER_PATH = "Enter Path...";
	private static final String MESSAGE_AGENT_XML_PATH = "XML Path: ";
	private static final String MESSAGE_BROWSE = "Browse";
	private static final String MESSAGE_EDIT = "Edit";
	private static final String MESSAGE_VALIDATE = "Validate";
	private static final String MESSAGE_APPLY = "Apply";
	private static final String MESSAGE_NO_WARNINGS_OR_ERRORS_FOUND = "No errors/warnings found!";

	private AgentJmxHelper agentJMXHelper = null;
	final private Text messageOutput;


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
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
							XmlEditor.EDITOR_ID);
				} catch (PartInitException e) {
					AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not open XML editor", e);
				}
			});

			Button validate = new Button(row, SWT.PUSH);
			validate.setText(MESSAGE_VALIDATE);
			validate.setLayoutData(gridData);
			validate.addListener(SWT.Selection, event -> {
				try {
					byte[] bytes = Files.readAllBytes(Paths.get(text.getText()));
					validateProbeDefinition(new String(bytes));
				} catch (IOException e) {
					AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not validate XML config", e);
				}
			});

			Button apply = new Button(row, SWT.PUSH);
			apply.setText(MESSAGE_APPLY);
			apply.setLayoutData(gridData);
			apply.addListener(SWT.Selection, event -> {
				try {
					byte[] bytes = Files.readAllBytes(Paths.get(text.getText()));
					agentJMXHelper.defineEventProbes(new String(bytes));
				} catch (IOException e) {
					AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not apply XML config", e);
				}
			});

			ScrolledComposite scRow = new ScrolledComposite(this, SWT.V_SCROLL);
			scRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			messageOutput = new Text(scRow, SWT.WRAP | SWT.READ_ONLY | SWT.NO_BACKGROUND);
			messageOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			scRow.setContent(messageOutput);
			scRow.setExpandVertical(true);
			scRow.setExpandHorizontal(true);
			scRow.setAlwaysShowScrollBars(false);
			scRow.addListener(SWT.Resize, new Listener() {

				@Override
				public void handleEvent(Event event) {
					int height = messageOutput.computeSize(getClientArea().width, SWT.DEFAULT).y;
					scRow.setMinHeight(height);
				}
			});

		}

		parent.layout(true, true);
	}

	public void setAgentJMXHelper(AgentJmxHelper agentJMXHelper) {
		this.agentJMXHelper = agentJMXHelper;
	}

	private void validateProbeDefinition(String configuration) {
		ProbeValidator validator = new ProbeValidator();
		try {
			validator.validate(new StreamSource(new ByteArrayInputStream(configuration.getBytes())));
		} catch (IOException e) {
			messageOutput.setText("[ERROR]\t" + e.getMessage());
			return;
		} catch (SAXException e) {
			// noop
		}

		ValidationResult result = validator.getValidationResult();
		StringBuilder sb = new StringBuilder();
		if (result.getFatalError() != null) {
			sb.append("[FATAL]\t").append(result.getFatalError().getMessage()).append('\n');
		}

		for (SAXException error : result.getErrors()) {
			sb.append("[ERROR]\t").append(error.getMessage()).append('\n');
		}

		for (SAXException warning : result.getErrors()) {
			sb.append("[WARN]\t").append(warning.getMessage()).append('\n');
		}

		String message = sb.toString();
		if (message.isEmpty()) {
			message = MESSAGE_NO_WARNINGS_OR_ERRORS_FOUND;
		}

		messageOutput.setText(message);
		this.layout();
	}
}
