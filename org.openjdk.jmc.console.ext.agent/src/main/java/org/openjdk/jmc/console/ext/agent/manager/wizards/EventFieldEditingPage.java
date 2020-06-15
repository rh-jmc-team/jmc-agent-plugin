package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;

public class EventFieldEditingPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Parameter Editing";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_TITLE = "Editing a Parameter or Return Value Capturing";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION= "Define a custom evaluation and capturing with an expression";

	private final IField field;

	public EventFieldEditingPage(IField field) {
		super(PAGE_NAME);

		// The capturedValue could be a IMethodParameter or IMethodReturnValue
		this.field = field;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_MANAGER_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		// TODO: create field editor control here
		container.setLayout(new FillLayout());
		new Label(container, SWT.NONE).setText("TODO: create field editor control here");

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}
}
