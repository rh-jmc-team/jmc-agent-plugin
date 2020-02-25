package org.openjdk.jmc.console.ext.agent.views;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ui.editor.IConsolePageContainer;
import org.openjdk.jmc.console.ui.editor.IConsolePageStateHandler;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

public class AgentTab implements IConsolePageStateHandler {

	@Inject
	protected void createPageContent(IConsolePageContainer page, IManagedForm managedForm, IConnectionHandle handle) {
		// Create SectionPartManager
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Composite container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());

		toolkit.createLabel(container, "to be populated");
	}

	@Override
	public boolean saveState(IMemento state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
