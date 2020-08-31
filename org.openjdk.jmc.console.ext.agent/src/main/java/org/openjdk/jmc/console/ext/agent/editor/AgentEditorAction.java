package org.openjdk.jmc.console.ext.agent.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openjdk.jmc.ui.UIPlugin;

public class AgentEditorAction extends Action {
	private static final String MESSAGE_REFRESH = "Refresh";
	private static final String MESSAGE_LOAD_PRESET = "Load a preset...";
	private static final String MESSAGE_SAVE_AS_PRESET = "Save as a preset...";

	private final AgentEditorActionType actionType;
	private Runnable runnable = () -> {
	};

	AgentEditorAction(AgentEditorActionType actionType) {
		super(actionType.message, actionType.action);
		this.actionType = actionType;
		setToolTipText(actionType.message);
		setImageDescriptor(actionType.imageDescriptor);
	}

	@Override
	public void run() {
		runnable.run();
	}

	public void setRunnable(Runnable callback) {
		runnable = callback;
	}

	public AgentEditorActionType getType() {
		return actionType;
	}

	enum AgentEditorActionType {
		REFRESH(MESSAGE_REFRESH, IAction.AS_PUSH_BUTTON, UIPlugin.getDefault()
				.getMCImageDescriptor(UIPlugin.ICON_REFRESH)), // 
		LOAD_PRESET(MESSAGE_LOAD_PRESET, IAction.AS_PUSH_BUTTON, UIPlugin.getDefault()
				.getMCImageDescriptor(UIPlugin.ICON_CHANGE)), //
		SAVE_AS_PRESET(MESSAGE_SAVE_AS_PRESET, IAction.AS_PUSH_BUTTON, UIPlugin.getDefault()
				.getMCImageDescriptor(UIPlugin.ICON_SAVE));

		private final String message;
		private final int action;
		private final ImageDescriptor imageDescriptor;

		AgentEditorActionType(String message, int action, ImageDescriptor imageDescriptor) {
			this.message = message;
			this.action = action;
			this.imageDescriptor = imageDescriptor;
		}
	}
}
