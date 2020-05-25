package org.openjdk.jmc.console.ext.agent.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class GenericErrorMessage extends Composite {
	private Label errorLabel;

	public GenericErrorMessage(Composite parent) {
		super(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 10;
		this.setLayout(gl);
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		errorLabel = new Label(this, SWT.WRAP);
		GridData errorGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		errorLabel.setLayoutData(errorGridData);
		errorLabel.setForeground(new Color(Display.getDefault(), 255, 0, 0));
	}

	public void displayError(String message) {
		errorLabel.setText(message);
		this.setVisible(true);
		this.layout();
	}
}
