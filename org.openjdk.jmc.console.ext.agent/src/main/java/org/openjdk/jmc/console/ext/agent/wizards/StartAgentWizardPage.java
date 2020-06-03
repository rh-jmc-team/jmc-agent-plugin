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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;

public class StartAgentWizardPage extends WizardPage {
	private static final String PAGE_NAME = "Start Agent Wizard Page";
	private static final String MESSAGE_START_AGENT_WIZARD_PAGE_TITLE = "Start JMC Agent";
	private static final String MESSAGE_START_AGENT_WIZARD_PAGE_DESCRIPTION = "Enter the JMC Agent configuration details and then click Finish to start the agent.";
	private static final String LABEL_TARGET_JVM = "Target JVM: ";
	private static final String LABEL_AGENT_JAR = "Agent JAR: ";
	private static final String LABEL_AGENT_XML = "Agent XML: ";
	private static final String BUTTON_BROWSE_TEXT = "Browser...";
	
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

		setTargetJvmText(helper.getConnectionHandle().getServerDescriptor().getDisplayName());
	}

	private void setTargetJvmText(String name) {
		targetJvmText.setText(name);
		targetJvmText.setToolTipText(name);
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
				super.widgetSelected(e);
			}
		});
		browseButton.setLayoutData(gd3);

		setAgentJarText("");
	}

	private void setAgentJarText(String path) {
		agentJarText.setText(path);
		agentJarText.setToolTipText(path);
	}

	private void createAgentConfigBrowserInput(Composite parent, int cols) {
		GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		Label label = createLabel(parent, LABEL_AGENT_XML);
		label.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd2.horizontalSpan = cols - 2;
		agentXmlText = createText(parent);
		agentXmlText.setMessage("(Optional)");
		gd2.minimumWidth = 0;
		gd2.widthHint = 400;
		agentXmlText.setLayoutData(gd2);

		GridData gd3 = new GridData(SWT.FILL, SWT.FILL, false, true);
		Button browseButton = createButton(parent, BUTTON_BROWSE_TEXT);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
			}
		});
		browseButton.setLayoutData(gd3);

		setAgentXmlText("");
	}

	private void setAgentXmlText(String path) {
		agentJarText.setText(path);
		agentJarText.setToolTipText(path);
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
}
