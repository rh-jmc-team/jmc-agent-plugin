package org.openjdk.jmc.console.ext.agent.tabs.editor;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.editor.AgentFormPage;
import org.openjdk.jmc.console.ext.agent.messages.internal.Messages;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

public class EditorTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.editor.EditorTab"; //$NON-NLS-1$

	@Inject
	public EditorTab(AgentEditor editor) {
		super(editor, ID, Messages.EditorTab_TITLE);
		// TODO Auto-generated constructor stub
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, IConnectionHandle handle) {
		ScrolledForm form = managedForm.getForm();
		Composite container = form.getBody();
		container.setLayout(MCLayoutFactory.createFormPageLayout());
	}

}
