package org.openjdk.jmc.console.ext.agent.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepositoryFactory;
import org.openjdk.jmc.console.ext.agent.tabs.liveconfig.EventTreeSection;
import org.openjdk.jmc.console.ext.agent.tabs.liveconfig.FeatureTableSection;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.DisplayToolkit;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentEditorUi {
	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	private final AgentEditor editor;
	private final AgentJmxHelper helper;
	private final IConnectionHandle handle;
	private final PresetRepository presetRepository = PresetRepositoryFactory.createSingleton();

	public AgentEditorUi(AgentEditor editor) {
		this.editor = editor;

		helper = editor.getAgentEditorInput().getAgentJmxHelper();
		handle = editor.getAgentEditorInput().getConnectionHandle();
	}

	protected void createContent(Form form, FormToolkit toolkit) {
		Composite body = form.getBody();
		body.setLayout(MCLayoutFactory.createFormPageLayout());

		Composite pageContainer = new Composite(body, SWT.NONE);
		pageContainer.setLayout(new GridLayout(2, false));
		pageContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm sashForm = new SashForm(pageContainer, SWT.HORIZONTAL);
		sashForm.setLayoutData(MCLayoutFactory.createFormPageLayoutData());
		toolkit.adapt(sashForm, false, false);

		EventTreeSection eventTree = new EventTreeSection(sashForm, toolkit, helper);

		FeatureTableSection eventTable = new FeatureTableSection(sashForm, toolkit, handle, helper);
		eventTree.addEventSelectionListener(eventTable);
		eventTree.selectTopEvent();
		sashForm.setWeights(new int[] {3, 4});
	}

	protected void refresh(Runnable done) {
		EXECUTOR_SERVICE.submit(() -> {
			// TODO: fetch event list
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// noop
			}

			DisplayToolkit.inDisplayThread().execute(done);
		});
	}

	private void refresh() {
		refresh(() -> {
			// noop
		});
	}
}
