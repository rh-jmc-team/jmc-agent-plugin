package org.openjdk.jmc.console.ext.agent.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.rjmx.ui.internal.TreeNodeBuilder;
import org.openjdk.jmc.ui.common.tree.ITreeNode;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.TreeStructureContentProvider;


public class EventTreeSection extends Composite {
	private static final String EVENTS_TREE_NAME = "AgentUi.EventsTree";
	private static final String NO_TRANSFORMED_EVENTS_MSG = "No events are currently transformed";
	private static final String AGENT_OBJECT_NAME = "org.openjdk.jmc.jfr.agent:type=AgentController";
	private static final String SECTION_LABEL = "Current Transfromed Events";
	private static final String GET_EVENTS = "Get Events";
	private static final List<String> COMPOSITE_DATA_TYPES = new ArrayList<>(Arrays.asList("returnValue", "method"));
	private static final List<String> COMPOSITE_DATA_ARRAY_TYPES = new ArrayList<>(Arrays.asList("fields", "parameters"));
	private final TreeViewer viewer;
	private MBeanServerConnection mbsc;

	public EventTreeSection(Composite parent, FormToolkit toolkit) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite eventsControlContainer = new Composite(this, SWT.NO_BACKGROUND);
		eventsControlContainer.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		eventsControlContainer.setLayoutData(gridData);

		Label label = new Label(eventsControlContainer, SWT.NULL);
		label.setText(SECTION_LABEL);
		Button eventsButton = new Button(eventsControlContainer, SWT.PUSH);
		eventsButton.setText(GET_EVENTS);
		eventsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final ITreeNode[] nodes = buildTreeModel(doRetrieveCurrentTransforms());
				viewer.getControl().setRedraw(false);
				viewer.setInput(nodes);
				viewer.getControl().setRedraw(true);
				viewer.getControl().redraw();
			}
		});

		viewer = createViewer(this, toolkit);
		viewer.getControl()
				.setLayoutData(MCLayoutFactory.createFormPageLayoutData(SWT.DEFAULT, SWT.DEFAULT, true, true));
	}

	public void setMBeanServerConnection(MBeanServerConnection mbsc) {
		this.mbsc = mbsc;
	}

	private CompositeData[] doRetrieveCurrentTransforms() {
		if (mbsc == null) {
			System.err.println("ERROR: no MBeanServerConnection exists, cannot invoke retrieveCurrentTransforms");
			return null;
		}
		try {
			Object result = mbsc.invoke(new ObjectName(AGENT_OBJECT_NAME), "retrieveCurrentTransforms", new Object[0], new String[0]);
			return (CompositeData[]) result;
		} catch (InstanceNotFoundException | MalformedObjectNameException | MBeanException | ReflectionException
				| IOException e) {
			System.err.println("ERROR: Could not retrieve current transforms");
			e.printStackTrace();
		}
		return null;
	}

	private TreeViewer createViewer(Composite parent, FormToolkit formToolkit) {
		Tree tree = formToolkit.createTree(parent, SWT.NONE);
		tree.setData("name", EVENTS_TREE_NAME); //$NON-NLS-1$
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TreeViewer viewer = new TreeViewer(tree);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new TreeStructureContentProvider());
		viewer.setLabelProvider(new EventTreeLabelProvider());

		return viewer;
	}

	private ITreeNode[] buildTreeModel(CompositeData[] cds) {
		TreeNodeBuilder root = new TreeNodeBuilder();
		if (cds == null || cds.length == 0) {
			root.getUniqueChild(NO_TRANSFORMED_EVENTS_MSG);
		} else {
			for (CompositeData cd : cds) {
				TreeNodeBuilder node = root.getUniqueChild(cd.get("eventName").toString());
				buildChildNodes(cd, node);
			}
		}
		return root.getChildren(null);
	}

	private void buildChildNodes(CompositeData cd, TreeNodeBuilder rootNode) {
		Set<String> keys = cd.getCompositeType().keySet();
		for (String key : keys) {
			if (!isEmptyCompositeData(cd, key)) {
				TreeNodeBuilder parent = rootNode.get(key);
				parent.setValue(key);
				if (COMPOSITE_DATA_TYPES.contains(key)) {
					buildChildNodes((CompositeData) cd.get(key), parent);
				} else if (COMPOSITE_DATA_ARRAY_TYPES.contains(key)) {
					CompositeData[] childCds = (CompositeData[]) cd.get(key);
					for (int i = 0; i < childCds.length; i++) {
						String childKey = key + " " + i;
						TreeNodeBuilder childNode = parent.get(childKey);
						childNode.setValue(childKey);
						buildChildNodes(childCds[i], childNode);
					}
				} else {
					String value = cd.get(key).toString();
					TreeNodeBuilder child = parent.get(value);
					child.setValue(value);
				}
			}
		}
	}

	private boolean isEmptyCompositeData(CompositeData cd, String key) {
		if (cd.get(key) == null) {
			return true;
		}
		try {
			CompositeData[] cds = (CompositeData[]) cd.get(key);
			return cds.length == 0;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
