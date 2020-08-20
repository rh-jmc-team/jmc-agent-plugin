package org.openjdk.jmc.console.ext.agent.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.editor.sections.EventDetailSection;
import org.openjdk.jmc.console.ext.agent.editor.sections.EventListSection;
import org.openjdk.jmc.console.ext.agent.editor.sections.GlobalConfigSection;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepositoryFactory;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.DisplayToolkit;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentEditorUi {
	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	private final AgentEditor editor;
	private final AgentJmxHelper helper;
	private final IConnectionHandle handle;
	private final PresetRepository presetRepository = PresetRepositoryFactory.createSingleton();

	private GlobalConfigSection globalConfigSection;
	private EventListSection eventListSection;
	private EventDetailSection eventDetailSection;

	public AgentEditorUi(AgentEditor editor) {
		this.editor = editor;

		helper = editor.getAgentEditorInput().getAgentJmxHelper();
		handle = editor.getAgentEditorInput().getConnectionHandle();
	}

	protected void createContent(Form form, FormToolkit toolkit) {
		Composite body = form.getBody();
		body.setLayout(MCLayoutFactory.createFormPageLayout());

		Composite container = new Composite(body, SWT.NONE);
		container.setLayout(new FillLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm hSashForm = new SashForm(container, SWT.HORIZONTAL);
		SashForm vSashForm = new SashForm(hSashForm, SWT.VERTICAL);

		globalConfigSection = new GlobalConfigSection(vSashForm, toolkit);
		eventListSection = new EventListSection(vSashForm, toolkit);
		eventDetailSection = new EventDetailSection(hSashForm, toolkit);

		eventListSection.addSelectionChangedListener(selectionChangedEvent -> eventDetailSection
				.setInput((IEvent) selectionChangedEvent.getStructuredSelection().getFirstElement()));

		hSashForm.setWeights(new int[] {3, 7});
		vSashForm.setWeights(new int[] {1, 4});
	}

	protected void refresh(Runnable done) {
		EXECUTOR_SERVICE.submit(() -> {
			String probes = helper.retrieveEventProbes();
			IPreset[] preset = new IPreset[] {null};
			if (probes != null && !probes.isEmpty()) {
				preset[0] = presetRepository.createPreset();
				try {
					preset[0].deserialize(probes);
				} catch (IOException | SAXException e) {
					// TODO: display error dialog
					e.printStackTrace();
				}
			}

			DisplayToolkit.inDisplayThread().execute(() -> {
				globalConfigSection.setInput(preset[0]);
				eventListSection.setInput(preset[0]);
				eventDetailSection.setInput(null);
				done.run();
			});
		});
	}

	private void refresh() {
		refresh(() -> {
			// noop
		});
	}
}
