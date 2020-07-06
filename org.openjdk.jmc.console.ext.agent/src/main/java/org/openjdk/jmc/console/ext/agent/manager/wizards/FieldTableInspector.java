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
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.ui.column.ColumnBuilder;
import org.openjdk.jmc.ui.column.ColumnManager;
import org.openjdk.jmc.ui.column.IColumn;
import org.openjdk.jmc.ui.misc.OptimisticComparator;
import org.openjdk.jmc.ui.misc.TreeStructureContentProvider;

public class FieldTableInspector {
	private static final String NAME_COLUMN_ID = "name";
	private static final String NAME_COLUMN_NAME = "Name";
	private static final String EXPRESSION_COLUMN_ID = "expression";
	private static final String EXPRESSION_COLUMN_NAME = "Expression";
	private static final String DESCRIPTION_COLUMN_ID = "description";
	private static final String DESCRIPTION_COLUMN_NAME = "Description";

	private final TableViewer viewer;

	private final ColumnLabelProvider nameLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IField) {
				return ((IField) element).getName();
			}
			return element.toString();
		}
	};

	private final ColumnLabelProvider expressionLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IField) {
				return ((IField) element).getExpression();
			}
			return element.toString();
		}
	};

	private final ColumnLabelProvider descriptionLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IField) {
				return ((IField) element).getDescription();
			}
			return element.toString();
		}
	};

	public FieldTableInspector(Composite parent) {
		viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new TreeStructureContentProvider());
		viewer.getTable().setHeaderVisible(true);

		List<IColumn> columns = new ArrayList<>();
		columns.add(new ColumnBuilder(NAME_COLUMN_NAME, NAME_COLUMN_ID, nameLabelProvider).comparator( //$NON-NLS-1$
				new OptimisticComparator(nameLabelProvider)).build());
		columns.add(new ColumnBuilder(EXPRESSION_COLUMN_NAME, EXPRESSION_COLUMN_ID, expressionLabelProvider).comparator( //$NON-NLS-1$
				new OptimisticComparator(expressionLabelProvider)).build());
		columns.add(
				new ColumnBuilder(DESCRIPTION_COLUMN_NAME, DESCRIPTION_COLUMN_ID, descriptionLabelProvider).comparator(//$NON-NLS-1$
						new OptimisticComparator(descriptionLabelProvider)).build());
		ColumnManager.build(viewer, columns, null);
	}

	public void setInput(Object input) {
		viewer.setInput(input);
	}

	public TableViewer getViewer() {
		return viewer;
	}

}
