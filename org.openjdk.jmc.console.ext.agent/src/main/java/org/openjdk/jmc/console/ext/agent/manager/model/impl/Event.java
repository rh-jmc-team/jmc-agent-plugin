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
package org.openjdk.jmc.console.ext.agent.manager.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodReturnValue;

public class Event implements IEvent {

	private static final String DEFAULT_STRING_FIELD = ""; // $NON-NLS-1$
	private static final boolean DEFAULT_BOOLEAN_FIELD = false;
	private static final Object DEFAULT_OBJECT_FIELD = null;
	private static final String DEFAULT_EVENT_ID = "my.id"; // $NON-NLS-1$
	private static final String DEFAULT_EVENT_NAME = "MyCustomEvent"; // $NON-NLS-1$
	private static final String DEFAULT_EVENT_CLAZZ = "com.company.project.MyClass"; // $NON-NLS-1$
	private static final String DEFAULT_METHOD_NAME = "myMethod"; // $NON-NLS-1$
	private static final String DEFAULT_METHOD_DESCRIPTOR = "()V"; // $NON-NLS-1$
	private static final String CLAZZ_REGEX = "([a-zA-Z_$][a-zA-Z0-9_$]*\\.)*([a-zA-Z_$][a-zA-Z0-9_$]*)"; // $NON-NLS-1$
	private static final String PATH_REGEX = "([^/]+/)*([^/]*)"; // $NON-NLS-1$
	private static final String METHOD_NAME_REGEX = "[a-zA-Z_$][a-zA-Z0-9_$]*"; // $NON-NLS-1$
	private static final String METHOD_DESCRIPTOR_REGEX = "\\((\\[*([BCDFIJSZ]|L([a-zA-Z_$][a-zA-Z0-9_$]*/)*[a-zA-Z_$][a-zA-Z0-9_$]*;))*\\)(V|\\[*([BCDFIJSZ]|L([a-zA-Z_$][a-zA-Z0-9_$]*/)*[a-zA-Z_$][a-zA-Z0-9_$]*;))"; // $NON-NLS-1$
	private static final String ERROR_CANNOT_BE_EMPTY = "Field cannot be empty";
	private static final String ERROR_CANNOT_BE_NULL = "Field cannot be null";
	private static final String ERROR_INCORRECT_SYNTAX = "Field has incorrect syntax";
	private static final String ERROR_INDEX_MUST_BE_UNIQUE = "MethodParameter index must be unique";

	private String id;
	private String name;
	private String clazz;
	private String description;
	private String path;
	private boolean recordStackTrace;
	private boolean useRethrow;
	private Location location;
	private String methodName;
	private String methodDescriptor;
	private List<IMethodParameter> parameters = new ArrayList<>();
	private IMethodReturnValue returnValue;
	private List<IField> fields = new ArrayList<>();

	public Event() {
		id = DEFAULT_EVENT_ID;
		name = DEFAULT_EVENT_NAME;
		clazz = DEFAULT_EVENT_CLAZZ;
		description = DEFAULT_STRING_FIELD;
		path = DEFAULT_STRING_FIELD;
		recordStackTrace = DEFAULT_BOOLEAN_FIELD;
		useRethrow = DEFAULT_BOOLEAN_FIELD;
		methodName = DEFAULT_METHOD_NAME;
		methodDescriptor = DEFAULT_METHOD_DESCRIPTOR;
		location = (Location) DEFAULT_OBJECT_FIELD;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		if (id == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		if (id.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		}
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		}
		this.name = name;
	}

	@Override
	public String getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(String clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		clazz = collapseWhiteSpaces(clazz);
		if (clazz.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		} else if (!clazz.matches(CLAZZ_REGEX)) {
			throw new IllegalArgumentException(ERROR_INCORRECT_SYNTAX);
		}
		this.clazz = clazz;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		if (path != null) {
			path = collapseWhiteSpaces(path);
			if (!path.matches(PATH_REGEX)) {
				throw new IllegalArgumentException(ERROR_INCORRECT_SYNTAX);
			}
		}
		this.path = path;
	}

	@Override
	public boolean getStackTrace() {
		return recordStackTrace;
	}

	@Override
	public void setStackTrace(boolean enabled) {
		recordStackTrace = enabled;
	}

	@Override
	public boolean getRethrow() {
		return useRethrow;
	}

	@Override
	public void setRethrow(boolean enabled) {
		useRethrow = enabled;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(Location location) {
		this.location = location;

	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public void setMethodName(String methodName) {
		if (methodName == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		methodName = collapseWhiteSpaces(methodName);
		if (methodName.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		} else if (!methodName.matches(METHOD_NAME_REGEX)) {
			throw new IllegalArgumentException(ERROR_INCORRECT_SYNTAX);
		}
		this.methodName = methodName;
	}

	@Override
	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	@Override
	public void setMethodDescriptor(String methodDescriptor) {
		if (methodDescriptor == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		methodDescriptor = collapseWhiteSpaces(methodDescriptor);
		if (methodDescriptor.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		} else if (!methodDescriptor.matches(METHOD_DESCRIPTOR_REGEX)) {
			throw new IllegalArgumentException(ERROR_INCORRECT_SYNTAX);
		}
		this.methodDescriptor = methodDescriptor;

	}

	@Override
	public IMethodParameter[] getMethodParameters() {
		return parameters.toArray(new IMethodParameter[0]);
	}

	@Override
	public void addMethodParameter(IMethodParameter methodParameter) {
		if (methodParameter == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		if (containsIndex(methodParameter.getIndex())) {
			throw new IllegalArgumentException(ERROR_INDEX_MUST_BE_UNIQUE);
		}
		parameters.add(methodParameter);
	}

	@Override
	public void removeMethodParameter(IMethodParameter methodParameter) {
		parameters.remove(methodParameter);
	}

	@Override
	public boolean containMethodParameter(IMethodParameter methodParameter) {
		return parameters.contains(methodParameter);
	}

	@Override
	public void setMethodReturnValue(IMethodReturnValue methodReturnValue) {
		returnValue = methodReturnValue;
	}

	@Override
	public IMethodReturnValue getMethodReturnValue() {
		return returnValue;
	}

	@Override
	public IField[] getFields() {
		return fields.toArray(new IField[0]);
	}

	@Override
	public void addField(IField field) {
		if (field == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		fields.add(field);

	}

	@Override
	public void removeField(IField field) {
		fields.remove(field);
	}

	@Override
	public boolean containField(IField field) {
		return fields.contains(field);
	}

	private static String collapseWhiteSpaces(String stringWithSpaces) {
		return stringWithSpaces.replaceAll("\\s+", " ");
	}

	private boolean containsIndex(int index) {
		for (IMethodParameter param : parameters) {
			if (param.getIndex() == index) {
				return true;
			}
		}
		return false;
	}

}
