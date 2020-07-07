package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class BaseWizardPage extends WizardPage {

	protected BaseWizardPage(String pageName) {
		super(pageName);
	}

	protected BaseWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	protected Composite createComposite(Composite parent) {
		return new Composite(parent, SWT.NONE);
	}

	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	protected Text createText(Composite parent, String hint) {
		Text text = new Text(parent, SWT.BORDER);
		text.setMessage(hint);
		text.setEnabled(true);
		return text;
	}

	protected Text createMultiText(Composite parent, String hint) {
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		// FIXME: Multi line Text field (SWT.MULTI) does not support Text.setMessage
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328832
		text.setMessage(hint);
		text.setEnabled(true);
		return text;
	}

	protected Label createSeparator(Composite parent) {
		return new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	}

	protected Combo createCombo(Composite parent, String[] items) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(items);
		return combo;
	}

	protected Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		return button;
	}

	protected Button createCheckbox(Composite parent, String text) {
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(text);
		return checkbox;
	}

	protected void setText(Text receiver, String text) {
		text = text == null ? "" : text;
		receiver.setText(text);
	}

	protected void setText(Combo receiver, String text) {
		text = text == null ? "" : text;
		receiver.setText(text);
	}

	protected Text createTextInput(Composite parent, int cols, String label, String hint) {
		Label l = createLabel(parent, label);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 0));

		Text t = createText(parent, hint);
		t.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, cols - 2, 0));

		return t;
	}

	protected Text createMultiTextInput(Composite parent, int cols, String label, String hint) {
		Label l = createLabel(parent, label);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, cols, 0));

		Text t = createMultiText(parent, hint);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true, cols, 0);
		gd.heightHint = 100;
		t.setLayoutData(gd);

		return t;
	}

	protected Text[] createMultiInputTextInput(Composite parent, int cols, String label, String[] hints) {
		Label l = createLabel(parent, label);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 0));

		Composite container = createComposite(parent);
		container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, cols - 2, 0));
		container.setLayout(new FillLayout());

		Text[] t = new Text[hints.length];
		for (int i = 0; i < hints.length; i++) {
			t[i] = createText(container, hints[i]);
		}

		return t;
	}

	protected Combo createComboInput(Composite parent, int cols, String label, String[] items) {
		Label l = createLabel(parent, label);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 0));

		Combo c = createCombo(parent, items);
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, cols - 2, 0));

		return c;
	}

	protected Button createCheckboxInput(Composite parent, int cols, String text) {
		Button b = createCheckbox(parent, text);
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, cols, 0));

		return b;
	}
}
