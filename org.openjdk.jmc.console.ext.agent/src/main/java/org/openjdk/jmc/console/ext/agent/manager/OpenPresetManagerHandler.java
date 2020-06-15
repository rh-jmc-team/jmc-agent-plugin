package org.openjdk.jmc.console.ext.agent.manager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepositoryFactory;

public class OpenPresetManagerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = Display.getCurrent().getActiveShell();
		PresetRepository repository = PresetRepositoryFactory.create();

		Dialog dialog = PresetManager.createFor(shell, repository);
		dialog.open();
		return null;
	}

}
