package org.openjdk.jmc.console.ext.agent.editor.sections;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.ui.column.ColumnBuilder;
import org.openjdk.jmc.ui.column.ColumnManager;
import org.openjdk.jmc.ui.column.IColumn;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.MCSectionPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailSection extends MCSectionPart {
	private static final String SECTION_TITLE = "Event Details";
	private static final String NO_EVENT_SELECTED = "No event selected";
	private static final String HEADER_KEY = "Key";
	private static final String HEADER_VALUE = "Value";

	private final Composite stack;
	private final StackLayout stackLayout;
	private final Composite message;
	private final TreeViewer viewer;

	public EventDetailSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, DEFAULT_TITLE_STYLE);

		getSection().setText(SECTION_TITLE);

		Composite body = createSectionBody(MCLayoutFactory.createMarginFreeFormPageLayout());
		stack = toolkit.createComposite(body);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);

		message = toolkit.createComposite(stack);
		message.setLayout(new GridLayout(1, false));
		toolkit.createLabel(message, NO_EVENT_SELECTED).setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		viewer = createViewer(stack, toolkit);
		viewer.getControl()
				.setLayoutData(MCLayoutFactory.createFormPageLayoutData(SWT.DEFAULT, SWT.DEFAULT, true, true));

		stackLayout.topControl = message;
	}

	private TreeViewer createViewer(Composite parent, FormToolkit formToolkit) {
		Tree tree = formToolkit.createTree(parent, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		TreeViewer viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TreeContentProvider());

		List<IColumn> columns = new ArrayList<>();
		columns.add(new ColumnBuilder(HEADER_KEY, HEADER_KEY, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Map.Entry<String, ?> entry = (Map.Entry<String, ?>) element;
				return entry.getKey();
			}
		}).build());
		columns.add(new ColumnBuilder(HEADER_VALUE, HEADER_VALUE, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Map.Entry<String, ?> entry = (Map.Entry<String, ?>) element;
				return entry.getValue() instanceof String ? (String) entry.getValue() : "";
			}
		}).build());
		ColumnManager.build(viewer, columns, null);

		return viewer;
	}

	public void setInput(IEvent event) {
		viewer.setInput(event);
		stackLayout.topControl = event == null ? message : viewer.getControl();
		stack.layout();
	}

	private static class TreeContentProvider implements ITreeContentProvider {

		private Map<String, Object> serializeEvent(IEvent event) {
			Map<String, Object> entries = new HashMap<>();
			entries.put("ID", event.getId());
			entries.put("Name", event.getName());
			entries.put("Class", event.getClazz());
			entries.put("Description", event.getDescription());
			entries.put("Path", event.getPath());
			entries.put("Stack Trace", String.valueOf(event.getStackTrace()));
			entries.put("Rethrow", String.valueOf(event.getRethrow()));
			entries.put("Location", String.valueOf(event.getLocation()));
			entries.put("Method", serializeMethod(event));
			entries.put("Fields", serializeFields(event));

			return entries;
		}

		private Map<String, Object> serializeFields(IEvent event) {
			Map<String, Object> fields = new HashMap<>();
			int i = 0;
			for (IField field : event.getFields()) {
				fields.put("[" + (i++) + "]", serializeField(field));
			}

			return fields;
		}

		private Map<String, String> serializeField(IField field) {
			Map<String, String> f = serializeCapturedValue(field);
			f.put("Expression", field.getExpression());
			return f;
		}

		private Map<String, Object> serializeMethod(IEvent event) {
			Map<String, Object> method = new HashMap<>();
			method.put("Name", event.getMethodName());
			method.put("Descriptor", event.getMethodDescriptor());
			Map<String, Object> parameters = new HashMap<>();
			int i = 0;
			for (IMethodParameter methodParameter : event.getMethodParameters()) {
				parameters.put("[" + (i++) + "]", serializeParameter(methodParameter));
			}
			method.put("Parameters", parameters);
			method.put("Return Value", event.getMethodReturnValue());
			return method;
		}

		private Map<String, String> serializeCapturedValue(ICapturedValue capturedValue) {
			Map<String, String> value = new HashMap<>();
			value.put("Name", capturedValue.getName());
			value.put("Description", capturedValue.getDescription());
			value.put("Content Type", String.valueOf(capturedValue.getContentType()));
			value.put("Relation Key", capturedValue.getRelationKey());
			value.put("Converter", capturedValue.getConverter());

			return value;
		}

		private Map<String, String> serializeParameter(IMethodParameter methodParameter) {
			Map<String, String> method = serializeCapturedValue(methodParameter);
			method.put("Index", String.valueOf(methodParameter.getIndex()));

			return method;
		}

		@Override
		public Object[] getElements(Object o) {
			if (o == null) {
				return new Object[0];
			}

			if (!(o instanceof IEvent)) {
				throw new IllegalArgumentException("input element must be an IEvent"); // $NON-NLS-1$
			}

			return serializeEvent((IEvent) o).entrySet().toArray(new Map.Entry[0]);
		}

		@Override
		public Object[] getChildren(Object o) {
			Map.Entry<String, ?> entry = (Map.Entry<String, ?>) o;
			if (entry.getValue() instanceof Map) {
				return ((Map) entry.getValue()).entrySet().toArray(new Map.Entry[0]);
			}

			return new Object[0];
		}

		@Override
		public Object getParent(Object o) {
			return null;
		}

		@Override
		public boolean hasChildren(Object o) {
			Map.Entry<String, ?> entry = (Map.Entry<String, ?>) o;
			return entry.getValue() instanceof Map;
		}
	}
}
