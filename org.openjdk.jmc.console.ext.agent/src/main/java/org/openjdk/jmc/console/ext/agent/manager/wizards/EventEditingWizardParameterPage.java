package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;

public class EventEditingWizardParameterPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Event Editing";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_TITLE = "Editing Event Parameters";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION = "Function parameters and return values can be recorded when committing an event.";

	private final IEvent event;

	protected EventEditingWizardParameterPage(IEvent event) {
		super(PAGE_NAME);

		this.event = event;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_TITLE);
		setDescription(MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		// TODO: create parameter page control here
		container.setLayout(new FillLayout());
		new Label(container, SWT.NONE).setText("TODO: create parameter page control here");

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}
}
