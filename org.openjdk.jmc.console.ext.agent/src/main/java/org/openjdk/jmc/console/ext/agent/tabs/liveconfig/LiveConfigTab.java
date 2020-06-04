package org.openjdk.jmc.console.ext.agent.tabs.liveconfig;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.editor.AgentFormPage;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

public class LiveConfigTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.liveconfig.LiveConfigTab";
	private static final String TITLE = "Live Config";

	private SashForm sashForm;
	private EventTreeSection eventTree;
	private FeatureTableSection eventTable;

	public LiveConfigTab(AgentEditor editor) {
		super(editor, ID, TITLE);
	}

	@Inject
	protected void createPageContent(IManagedForm managedForm, AgentJmxHelper helper, IConnectionHandle handle) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Composite body = form.getBody();
		body.setLayout(MCLayoutFactory.createFormPageLayout());

		sashForm = new SashForm(body, SWT.HORIZONTAL);
		sashForm.setLayoutData(MCLayoutFactory.createFormPageLayoutData());
		toolkit.adapt(sashForm, false, false);

		eventTree = new EventTreeSection(sashForm, toolkit, helper);
		managedForm.addPart(eventTree);

		eventTable = new FeatureTableSection(sashForm, toolkit, handle, helper);
		eventTree.addEventSelectionListener(eventTable);
		eventTree.selectTopEvent();
		sashForm.setWeights(new int[] {3, 4});

	}

}
