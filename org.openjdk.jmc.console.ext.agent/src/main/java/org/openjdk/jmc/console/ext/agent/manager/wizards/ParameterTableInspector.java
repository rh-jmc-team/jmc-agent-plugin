/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, Red Hat Inc. All rights reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.console.ext.agent.manager.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodReturnValue;
import org.openjdk.jmc.console.ext.agent.manager.model.INamedCapturedValue;
import org.openjdk.jmc.ui.column.ColumnBuilder;
import org.openjdk.jmc.ui.column.ColumnManager;
import org.openjdk.jmc.ui.column.IColumn;
import org.openjdk.jmc.ui.misc.OptimisticComparator;
import org.openjdk.jmc.ui.misc.TreeStructureContentProvider;

public class ParameterTableInspector {

	public static final String PARAMETER_TREE_NAME = "EventEditingWizardParameterPage.ParamtersTree"; //$NON-NLS-1$

	private final TreeViewer viewer;

	private final ColumnLabelProvider nameLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof ICapturedValue) {
				return ((INamedCapturedValue) ((ICapturedValue) element)).getName();
			}
			return element.toString();
		}
	};

	private final ColumnLabelProvider indexLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IMethodParameter) {
				return (String.valueOf(((IMethodParameter) element).getIndex()));
			} else if (element instanceof IMethodReturnValue) {
				return ("ReturnValue");
			}
			return element.toString();
		}
	};

	private final ColumnLabelProvider descriptionLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof ICapturedValue) {
				return ((ICapturedValue) element).getDescription();
			}
			return element.toString();
		}
	};

	public ParameterTableInspector(Composite parent) {
		Tree tree = new Tree(parent,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TreeStructureContentProvider());
		ColumnViewerToolTipSupport.enableFor(viewer);

		tree.setData("name", PARAMETER_TREE_NAME); //$NON-NLS-1$
		List<IColumn> columns = new ArrayList<>();
		columns.add(new ColumnBuilder("Index", "index", indexLabelProvider).comparator( //$NON-NLS-1$
				new OptimisticComparator(indexLabelProvider)).build());
		columns.add(new ColumnBuilder("Name", "name", nameLabelProvider).comparator( //$NON-NLS-1$
				new OptimisticComparator(nameLabelProvider)).build());
		columns.add(new ColumnBuilder("Description", "description", descriptionLabelProvider).comparator(//$NON-NLS-1$
				new OptimisticComparator(descriptionLabelProvider)).build());
		ColumnManager.build(viewer, columns, null);
	}

	public void setInput(Object input) {
		viewer.setInput(input);
	}

	public TreeViewer getViewer() {
		return viewer;
	}

}
