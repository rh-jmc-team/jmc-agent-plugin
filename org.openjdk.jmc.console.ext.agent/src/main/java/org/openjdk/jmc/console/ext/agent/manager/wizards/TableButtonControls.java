package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class TableButtonControls extends Composite {
	private static final String ADD_BTN_LABEL = "Add";
	private static final String EDIT_BTN_LABEL = "Edit";
	private static final String REMOVE_BTN_LABEL = "Remove";

	private Button addBtn;
	private Button editBtn;
	private Button removeBtn;
	private Runnable addBtnLister;
	private Runnable removeBtnLister;
	private Runnable editBtnLister;
	private final TableViewer viewer;

	TableButtonControls(Composite parent, TableViewer viewer) {
		super(parent, SWT.NONE);
		this.viewer = viewer;

		setLayout(new GridLayout());
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.widthHint = 150;
		setLayoutData(gd);

		addBtn = new Button(this, SWT.PUSH);
		addBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		addBtn.setText(ADD_BTN_LABEL);

		removeBtn = new Button(this, SWT.PUSH);
		removeBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		removeBtn.setText(REMOVE_BTN_LABEL);

		editBtn = new Button(this, SWT.PUSH);
		editBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editBtn.setText(EDIT_BTN_LABEL);

		bindListeners();
		updateButtonsAccordingTo(false);

	}

	private void updateButtonsAccordingTo(boolean isSelection) {
		removeBtn.setEnabled(isSelection);
		editBtn.setEnabled(isSelection);
	}

	public void setAddButtonListener(Runnable addBtnListener) {
		this.addBtnLister = addBtnListener;
	}

	public void setEditButtonListener(Runnable editBtnListener) {
		this.editBtnLister = editBtnListener;
	}

	public void setRemoveButtonListener(Runnable removeBtnListener) {
		this.removeBtnLister = removeBtnListener;
	}

	private void bindListeners() {
		addBtn.addListener(SWT.Selection, e -> addBtnLister.run());
		removeBtn.addListener(SWT.Selection, e -> removeBtnLister.run());
		editBtn.addListener(SWT.Selection, e -> editBtnLister.run());
		viewer.addSelectionChangedListener(e -> updateButtonsAccordingTo(!e.getSelection().isEmpty()));
	}

}
