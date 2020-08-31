package org.openjdk.jmc.console.ext.agent.editor.sections;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.manager.model.Event;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.MCSectionPart;

public class EventListSection extends MCSectionPart {
	private static final String SECTION_TITLE = "Event List";
	private static final String NO_EVENT_REGISTERED = "No event registered";

	private final Composite stack;
	private final StackLayout stackLayout;
	private final Composite message;
	private final TableViewer viewer;

	public EventListSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, DEFAULT_TITLE_STYLE);

		getSection().setText(SECTION_TITLE);

		Composite body = createSectionBody(MCLayoutFactory.createMarginFreeFormPageLayout());
		stack = toolkit.createComposite(body);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);

		message = toolkit.createComposite(stack);
		message.setLayout(new GridLayout(1, false));
		toolkit.createLabel(message, NO_EVENT_REGISTERED)
				.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		viewer = createViewer(stack, toolkit);
		viewer.getControl()
				.setLayoutData(MCLayoutFactory.createFormPageLayoutData(SWT.DEFAULT, SWT.DEFAULT, true, true));
	}

	private TableViewer createViewer(Composite parent, FormToolkit formToolkit) {
		Table table = formToolkit.createTable(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		TableViewer viewer = new TableViewer(table);
		viewer.setContentProvider(new ListContentProvider());

		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Event) element).getName();
			}
		});

		return viewer;
	}

	public void setInput(IPreset preset) {
		viewer.setInput(preset);
		stackLayout.topControl = preset == null || preset.getEvents().length == 0 ? message : viewer.getControl();
		stack.layout();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.removeSelectionChangedListener(listener);
	}

	private static class ListContentProvider extends AbstractStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) {
				return new Object[0];
			}

			if (!(inputElement instanceof IPreset)) {
				throw new IllegalArgumentException("input element must be an IPreset"); // $NON-NLS-1$
			}

			return ((IPreset) inputElement).getEvents();
		}
	}
}
