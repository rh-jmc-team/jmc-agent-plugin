package org.openjdk.jmc.console.ext.agent.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.rjmx.IConnectionListener;
import org.openjdk.jmc.rjmx.IServerHandle;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.WorkbenchToolkit;
import org.openjdk.jmc.ui.misc.CompositeToolkit;

public class AgentEditor extends EditorPart implements IConnectionListener {
	public static final String EDITOR_ID = "org.openjdk.jmc.console.ext.agent.editor.AgentEditor"; //$NON-NLS-1$

	private static final String AGENT_EDITOR_TITLE = "Agent Live Config";
	private static final String AGENT_EDITOR_ACTION_REFRESH = "Refresh";
	private static final String CONNECTION_LOST = "Connection Lost";

	private volatile IConnectionHandle connection;

	private Composite parentComposite;
	private FormToolkit formToolkit;
	private StackLayout stackLayout;
	private AgentEditorUi agentEditorUi;
	private Form form;

	@Override
	public void onConnectionChange(IConnectionHandle connection) {
		boolean serverDisposed = getAgentEditorInput().getServerHandle().getState() == IServerHandle.State.DISPOSED;
		if (serverDisposed) {
			WorkbenchToolkit.asyncCloseEditor(AgentEditor.this);
			return;
		}

		if (!connection.isConnected() && form != null) {
			form.setMessage(CONNECTION_LOST, IMessageProvider.ERROR);
		}
	}

	@Override
	public void doSave(IProgressMonitor iProgressMonitor) {
		// intentionally empty
	}

	@Override
	public void doSaveAs() {
		// intentionally empty
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		parentComposite.setFocus();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		try {
			getAgentEditorInput();
		} catch (Exception e) {
			throw new PartInitException(e.getMessage(), e);
		}

		setPartName(getAgentEditorInput().getName());

		connection = getAgentEditorInput().getConnectionHandle();
		getAgentEditorInput().getAgentJmxHelper().addConnectionChangedListener(this);
	}

	protected AgentEditorInput getAgentEditorInput() {
		AgentEditorInput aei;
		IEditorInput input = super.getEditorInput();
		if (input instanceof AgentEditorInput) {
			aei = (AgentEditorInput) input;
		} else {
			aei = input.getAdapter(AgentEditorInput.class);
		}

		if (aei == null) {
			// Not likely to be null, but guard just in case
			throw new RuntimeException("The agent editor cannot handle the provided editor input"); //$NON-NLS-1$
		}

		return (AgentEditorInput) super.getEditorInput();
	}

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		stackLayout = new StackLayout();
		parentComposite.setLayout(stackLayout);

		formToolkit = new FormToolkit(FlightRecorderUI.getDefault().getFormColors(Display.getCurrent()));
		formToolkit.setBorderStyle(SWT.NULL);

		stackLayout.topControl = formToolkit.createComposite(parent);
		ProgressIndicator progressIndicator = CompositeToolkit
				.createWaitIndicator((Composite) stackLayout.topControl, formToolkit);
		progressIndicator.beginTask(1);

		createAgentEditorUi(parent);
	}

	private void createAgentEditorUi(Composite parent) {
		form = formToolkit.createForm(parent);
		form.setText(AGENT_EDITOR_TITLE);
		form.setImage(getTitleImage());
		formToolkit.decorateFormHeading(form);

		IToolBarManager manager = form.getToolBarManager();
		// TODO: optimize action design
		manager.add((new Action(AGENT_EDITOR_ACTION_REFRESH) {
			{
				setImageDescriptor(UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_REFRESH));
			}

			@Override
			public void run() {
				// TODO: refresh event list
			}
		}));
		form.updateToolBar();

		Composite body = form.getBody();
		body.setLayout(new FillLayout());

		agentEditorUi = new AgentEditorUi(this);
		agentEditorUi.createContent(form, formToolkit);
		agentEditorUi.refresh(() -> {
			stackLayout.topControl.dispose();
			stackLayout.topControl = form;
			parentComposite.layout();
		});
	}
}
