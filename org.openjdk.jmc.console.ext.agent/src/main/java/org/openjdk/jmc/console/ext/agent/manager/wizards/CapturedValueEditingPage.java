package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue.ContentType;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodReturnValue;
import org.openjdk.jmc.console.ext.agent.wizards.BaseWizardPage;

import java.util.Locale;
import java.util.stream.Stream;

public class CapturedValueEditingPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Agent Captured Value Editing";

	private static final String MESSAGE_PARAMETER_OR_RETURN_VALUE_EDITING_PAGE_TITLE = "Edit a Parameter or Return Value";
	private static final String MESSAGE_PARAMETER_OR_RETURN_VALUE_EDITING_PAGE_DESCRIPTION = "Define a capturing of a method parameter or a return value.";
	private static final String MESSAGE_FIELD_EDITING_PAGE_TITLE = "Edit a Parameter or Return Value Capturing";
	private static final String MESSAGE_FIELD_EDITING_PAGE_DESCRIPTION = "Define a custom expression evaluation and capture its result.";

	private static final String LABEL_NAME = "Name: ";
	private static final String LABEL_INDEX = "Index: ";
	private static final String LABEL_IS_RETURN_VALUE = "This is a return value";
	private static final String LABEL_EXPRESSION = "Expression: ";
	private static final String LABEL_DESCRIPTION = "Description: ";
	private static final String LABEL_CONTENT_TYPE = "Content Type: ";
	private static final String LABEL_CLEAR = "Clear";
	private static final String LABEL_RELATIONAL_KEY = "Relational Key: ";
	private static final String LABEL_CONVERTER = "Converter: ";

	private static final String MESSAGE_NAME_OF_THE_CAPTURING = "Name of this capturing";
	private static final String MESSAGE_JAVA_PRIMARY_EXPRESSION_TO_BE_EVALUATED = "Java primary expression to be evaluated";
	private static final String MESSAGE_OPTIONAL_DESCRIPTION_OF_THIS_CAPTURING = "(Optional) Description of this capturing";
	private static final String MESSAGE_RELATIONAL_KEY_DESCRIPTION = "(Optional) Unique URI signifying a relationship";
	private static final String MESSAGE_CONVERTER_DESCRIPTION = "(Optional) fully qualified class name of a value converter";

	private final IEvent event;
	private ICapturedValue capturedValue;

	private Text nameText;
	private Spinner indexSpinner;
	private Button isReturnValueButton;
	private Text expressionText;
	private Text descriptionText;
	private Combo contentTypeCombo;
	private Button contentTypeClearButton;
	private Text relationalKeyText;
	private Text converterText;

	public CapturedValueEditingPage(IEvent event, ICapturedValue capturedValue) {
		super(PAGE_NAME);

		this.event = event;
		this.capturedValue = capturedValue;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		if (capturedValue instanceof IMethodParameter || capturedValue instanceof IMethodReturnValue) {
			setTitle(MESSAGE_PARAMETER_OR_RETURN_VALUE_EDITING_PAGE_TITLE);
			setDescription(MESSAGE_PARAMETER_OR_RETURN_VALUE_EDITING_PAGE_DESCRIPTION);
		} else if (capturedValue instanceof IField) {
			setTitle(MESSAGE_FIELD_EDITING_PAGE_TITLE);
			setDescription(MESSAGE_FIELD_EDITING_PAGE_DESCRIPTION);
		} else {
			throw new IllegalStateException(
					"captured value must be a an IMethodParameter, IMethodReturnValue or IFeild"); // $NON-NLS-1$
		}

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		createConfigContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createSeparator(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createMetaInfoContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		bindListeners();
		populateUi();

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	private Composite createConfigContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		nameText = createTextInput(container, cols, LABEL_NAME, MESSAGE_NAME_OF_THE_CAPTURING);
		if (capturedValue instanceof IField) {
			expressionText = createTextInput(container, cols, LABEL_EXPRESSION,
					MESSAGE_JAVA_PRIMARY_EXPRESSION_TO_BE_EVALUATED);
		} else {
			indexSpinner = createSpinnerInput(container, 3, LABEL_INDEX);
			isReturnValueButton = createCheckbox(container, LABEL_IS_RETURN_VALUE);
			isReturnValueButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 0));
		}
		descriptionText = createMultiTextInput(container, cols, LABEL_DESCRIPTION,
				MESSAGE_OPTIONAL_DESCRIPTION_OF_THIS_CAPTURING);

		return container;
	}

	private Composite createMetaInfoContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 8;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		contentTypeCombo = createComboInput(container, cols - 2, LABEL_CONTENT_TYPE,
				Stream.of(ContentType.values()).map(ContentType::toString).toArray(String[]::new));
		contentTypeClearButton = createButton(container, LABEL_CLEAR);
		contentTypeClearButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 0));

		relationalKeyText = createTextInput(container, cols, LABEL_RELATIONAL_KEY, MESSAGE_RELATIONAL_KEY_DESCRIPTION);

		converterText = createTextInput(container, cols, LABEL_CONVERTER, MESSAGE_CONVERTER_DESCRIPTION);

		return container;
	}

	private void bindListeners() {
		nameText.addModifyListener(
				handleExceptionIfAny((ModifyListener) e -> capturedValue.setName(nameText.getText())));
		if (indexSpinner != null) {
			indexSpinner.addModifyListener(handleExceptionIfAny(
					(ModifyListener) e -> ((IMethodParameter) capturedValue).setIndex(indexSpinner.getSelection())));
		}
		if (isReturnValueButton != null) {
			isReturnValueButton.addListener(SWT.Selection, e -> {
				indexSpinner.setEnabled(!isReturnValueButton.getSelection());
				if (isReturnValueButton.getSelection()) {
					convertParameterToReturnValue();
				} else {
					convertReturnValueToParameter();
				}
			});
		}
		if (expressionText != null) {
			expressionText.addModifyListener(handleExceptionIfAny(
					(ModifyListener) e -> ((IField) capturedValue).setExpression(expressionText.getText())));
		}
		descriptionText.addModifyListener(
				handleExceptionIfAny((ModifyListener) e -> capturedValue.setDescription(descriptionText.getText())));
		contentTypeCombo.addModifyListener(handleExceptionIfAny(
				(ModifyListener) e -> capturedValue.setContentType(contentTypeCombo.getSelectionIndex() == -1 ? null
						: ContentType.valueOf(contentTypeCombo.getText().toUpperCase(Locale.ENGLISH)))));
		contentTypeClearButton.addListener(SWT.Selection,
				handleExceptionIfAny((Listener) e -> contentTypeCombo.deselectAll()));
		relationalKeyText.addModifyListener(
				handleExceptionIfAny((ModifyListener) e -> capturedValue.setRelationKey(relationalKeyText.getText())));
		converterText.addModifyListener(
				handleExceptionIfAny((ModifyListener) e -> capturedValue.setConverter(converterText.getText())));
	}

	private void populateUi() {
		setText(nameText, capturedValue.getName());
		if (indexSpinner != null) {
			indexSpinner.setSelection(
					capturedValue instanceof IMethodParameter ? ((IMethodParameter) capturedValue).getIndex() : 0);
			indexSpinner.setEnabled(capturedValue instanceof IMethodParameter);
		}
		if (isReturnValueButton != null) {
			isReturnValueButton.setSelection(capturedValue instanceof IMethodReturnValue);
		}
		if (expressionText != null) {
			setText(expressionText, ((IField) capturedValue).getExpression());
		}
		setText(descriptionText, capturedValue.getDescription());
		setText(contentTypeCombo,
				capturedValue.getContentType() == null ? null : capturedValue.getContentType().toString());
		setText(relationalKeyText, capturedValue.getRelationKey());
		setText(converterText, capturedValue.getConverter());
	}

	public ICapturedValue getResult() {
		return capturedValue;
	}

	private void convertParameterToReturnValue() {
		IMethodParameter parameter = (IMethodParameter) capturedValue;

		// TODO: do this is in model instead
		IMethodReturnValue returnValue = event.createMethodReturnValue();
		returnValue.setName(parameter.getName());
		returnValue.setDescription(parameter.getDescription());
		returnValue.setContentType(parameter.getContentType());
		returnValue.setRelationKey(parameter.getRelationKey());
		returnValue.setConverter(parameter.getConverter());

		capturedValue = returnValue;
	}

	private void convertReturnValueToParameter() {
		IMethodReturnValue returnValue = (IMethodReturnValue) capturedValue;

		// TODO: do this is in model instead
		IMethodParameter parameter = event.createMethodParameter();
		parameter.setIndex(0);
		parameter.setName(returnValue.getName());
		parameter.setDescription(returnValue.getDescription());
		parameter.setContentType(returnValue.getContentType());
		parameter.setRelationKey(returnValue.getRelationKey());
		parameter.setConverter(returnValue.getConverter());

		capturedValue = parameter;
	}
}
