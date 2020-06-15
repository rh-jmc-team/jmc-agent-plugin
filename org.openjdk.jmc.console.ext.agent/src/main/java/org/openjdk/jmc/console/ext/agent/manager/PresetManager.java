package org.openjdk.jmc.console.ext.agent.manager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.wizards.PresetManagerPage;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;

public class PresetManager extends OnePageWizardDialog {
	private PresetManager(Shell parent, IWizardPage page) {
		super(parent, page);
	}

	public static Dialog createFor(Shell shell, PresetRepository repository) {
		PresetManager manager = new PresetManager(shell, new PresetManagerPage(repository));
		return manager;
	}
}
