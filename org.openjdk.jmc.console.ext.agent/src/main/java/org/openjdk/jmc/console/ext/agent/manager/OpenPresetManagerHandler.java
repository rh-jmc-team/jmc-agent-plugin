package org.openjdk.jmc.console.ext.agent.manager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepositoryFactory;

public class OpenPresetManagerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = (Shell) HandlerUtil.getVariable(event.getApplicationContext(), ISources.ACTIVE_SHELL_NAME);

		PresetRepository repository = PresetRepositoryFactory.create();
		Dialog dialog = PresetManager.createFor(shell, repository);
		dialog.open();
		return null;
	}

}
