package org.openjdk.jmc.console.ext.agent.tabs.presets;

import javax.inject.Inject;
import javax.management.MBeanServerConnection;

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

public class PresetsTab extends AgentFormPage {
	private static final String ID = "org.openjdk.jmc.console.ext.agent.tabs.presets.PresetsTab";
	private static final String TITLE = "Presets";

	private PresetListSection presetListSection;
	private SashForm sashForm;

	public PresetsTab(AgentEditor editor) {
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

		presetListSection = new PresetListSection(sashForm, toolkit);
		managedForm.addPart(presetListSection);
	}

}
