package org.openjdk.jmc.console.ext.agent.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.ui.common.jvm.JVMDescriptor;

import java.nio.file.Files;
import java.nio.file.Paths;

public class StartAgentWizardPage extends WizardPage {
	private static final String PAGE_NAME = "Start Agent Wizard Page";
	private static final String MESSAGE_START_AGENT_WIZARD_PAGE_TITLE = "Start JMC Agent";
	private static final String MESSAGE_START_AGENT_WIZARD_PAGE_DESCRIPTION = "Enter the JMC Agent configuration details and then click Finish to start the agent.";
	private static final String LABEL_TARGET_JVM = "Target JVM: ";
	private static final String LABEL_AGENT_JAR = "Agent JAR: ";
	private static final String LABEL_AGENT_XML = "Agent XML: ";
	private static final String HINT_OPTIONAL = "(Optional)";
	private static final String BUTTON_BROWSE_TEXT = "Browser...";
	private static final String DIALOG_BROWSER_FOR_AGENT_JAR = "Browser for JMC Agent JAR";
	private static final String DIALOG_BROWSER_FOR_AGENT_CONFIG = "Browser for JMC Agent Configuration";
	private static final String FILE_OPEN_FILTER_PATH = "file.open.filter.path";

	private final AgentJmxHelper helper;

	private Text targetJvmText;
	private Text agentJarText;
	private Text agentXmlText;

	protected StartAgentWizardPage(AgentJmxHelper helper) {
		super(PAGE_NAME);

		this.helper = helper;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_START_AGENT_WIZARD_PAGE_TITLE);
		setDescription(MESSAGE_START_AGENT_WIZARD_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		{
			Composite targetJvmContainer = createTargetJvmContainer(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			targetJvmContainer.setLayoutData(gd);
		}

		{
			Control separator = createSeparator(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			separator.setLayoutData(gd);
		}

		{
			Composite templateContainer = createAgentBrowserContainer(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			templateContainer.setLayoutData(gd);
		}

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	public JVMDescriptor getTargetJvm() {
		return helper.getConnectionHandle().getServerDescriptor().getJvmInfo();
	}

	public String getAgentJarPath() {
		return agentJarText.getText();
	}

	public String getAgentXmlPath() {
		return agentXmlText.getText();
	}

	private Composite createTargetJvmContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		createTargetJvmInput(container, cols);

		return container;
	}

	private void createTargetJvmInput(Composite parent, int cols) {
		GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		Label label = createLabel(parent, LABEL_TARGET_JVM);
		label.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd2.horizontalSpan = cols - 2;
		targetJvmText = createText(parent);
		gd2.minimumWidth = 0;
		gd2.widthHint = 400;
		targetJvmText.setLayoutData(gd2);

		setTextText(targetJvmText, helper.getConnectionHandle().getServerDescriptor().getDisplayName());
	}

	private Composite createAgentBrowserContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		createAgentJarBrowserInput(container, cols);
		createAgentConfigBrowserInput(container, cols);

		return container;
	}

	private void createAgentJarBrowserInput(Composite parent, int cols) {
		GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		Label label = createLabel(parent, LABEL_AGENT_JAR);
		label.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd2.horizontalSpan = cols - 2;
		agentJarText = createText(parent);
		gd2.minimumWidth = 0;
		gd2.widthHint = 400;
		agentJarText.setLayoutData(gd2);

		GridData gd3 = new GridData(SWT.FILL, SWT.FILL, false, true);
		Button browseButton = createButton(parent, BUTTON_BROWSE_TEXT);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setTextText(agentJarText, openFileOpenerBrowser(DIALOG_BROWSER_FOR_AGENT_JAR, new String[] {"*.jar"}));
			}
		});
		browseButton.setLayoutData(gd3);

		setTextText(agentJarText, "");
	}

	private void createAgentConfigBrowserInput(Composite parent, int cols) {
		GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		Label label = createLabel(parent, LABEL_AGENT_XML);
		label.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd2.horizontalSpan = cols - 2;
		agentXmlText = createText(parent);
		agentXmlText.setMessage(HINT_OPTIONAL);
		gd2.minimumWidth = 0;
		gd2.widthHint = 400;
		agentXmlText.setLayoutData(gd2);

		GridData gd3 = new GridData(SWT.FILL, SWT.FILL, false, true);
		Button browseButton = createButton(parent, BUTTON_BROWSE_TEXT);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setTextText(agentXmlText,
						openFileOpenerBrowser(DIALOG_BROWSER_FOR_AGENT_CONFIG, new String[] {"*.xml"}));
			}
		});
		browseButton.setLayoutData(gd3);

		setTextText(agentXmlText, "");
	}

	private void setTextText(Text receiver, String text) {
		text = text == null ? "" : text;
		receiver.setText(text);
		receiver.setToolTipText(text);
	}

	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	protected Text createText(Composite parent) {
		Text text = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		text.setEnabled(false);
		return text;
	}

	protected Label createSeparator(Composite parent) {
		return new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	}

	protected Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.NONE);
		button.setText(text);
		return button;
	}

	protected String openFileOpenerBrowser(String title, String[] extensions) {
		String filterPath = FlightRecorderUI.getDefault().getDialogSettings().get(FILE_OPEN_FILTER_PATH);
		if (filterPath != null && Files.notExists(Paths.get(filterPath))) {
			filterPath = System.getProperty("user.home", "./");
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN | SWT.SINGLE);
		dialog.setFilterPath(filterPath);
		dialog.setText(title);
		dialog.setFilterExtensions(extensions);

		return dialog.open();
	}
}
