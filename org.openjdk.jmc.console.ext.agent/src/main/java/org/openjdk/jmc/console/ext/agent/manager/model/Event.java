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
package org.openjdk.jmc.console.ext.agent.manager.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Event implements IEvent {
	private static final String DEFAULT_STRING_FIELD = ""; // $NON-NLS-1$
	private static final boolean DEFAULT_BOOLEAN_FIELD = false;
	private static final Object DEFAULT_OBJECT_FIELD = null;
	private static final String DEFAULT_EVENT_ID = "event.id"; // $NON-NLS-1$
	private static final String DEFAULT_EVENT_NAME = "New Custom Event"; // $NON-NLS-1$
	private static final String DEFAULT_EVENT_CLAZZ = "com.company.project.MyClass"; // $NON-NLS-1$
	private static final String DEFAULT_METHOD_NAME = "myMethod"; // $NON-NLS-1$
	private static final String DEFAULT_METHOD_DESCRIPTOR = "()V"; // $NON-NLS-1$
	private static final String CLAZZ_REGEX = "([a-zA-Z_$][a-zA-Z0-9_$]*\\.)*([a-zA-Z_$][a-zA-Z0-9_$]*)"; // $NON-NLS-1$
	private static final String PATH_REGEX = "([^/]+/)*([^/]*)"; // $NON-NLS-1$
	private static final String METHOD_NAME_REGEX = "[a-zA-Z_$][a-zA-Z0-9_$]*"; // $NON-NLS-1$
	private static final String METHOD_DESCRIPTOR_REGEX = "\\((\\[*([BCDFIJSZ]|L([a-zA-Z_$][a-zA-Z0-9_$]*/)*[a-zA-Z_$][a-zA-Z0-9_$]*;))*\\)(V|\\[*([BCDFIJSZ]|L([a-zA-Z_$][a-zA-Z0-9_$]*/)*[a-zA-Z_$][a-zA-Z0-9_$]*;))"; // $NON-NLS-1$

	private static final String ERROR_ID_CANNOT_BE_EMPTY_OR_NULL = "ID cannot be empty or null.";
	private static final String ERROR_NAME_CANNOT_BE_EMPTY_OR_NULL = "Name cannot be empty or null.";
	private static final String ERROR_CLASS_CANNOT_BE_EMPTY_OR_NULL = "Class cannot be empty or null.";
	private static final String ERROR_METHOD_NAME_CANNOT_BE_EMPTY_OR_NULL = "Method name cannot be empty or null.";
	private static final String ERROR_METHOD_DESCRIPTOR_CANNOT_BE_EMPTY_OR_NULL = "Method descriptor cannot be empty or null.";
	private static final String ERROR_METHOD_PARAMETER_CANNOT_BE_NULL = "Method parameter cannot be null.";
	private static final String ERROR_FIELD_CANNOT_BE_NULL = "Field cannot be null.";
	private static final String ERROR_CLASS_HAS_INCORRECT_SYNTAX = "Class has incorrect syntax.";
	private static final String ERROR_PATH_HAS_INCORRECT_SYNTAX = "Path has incorrect syntax.";
	private static final String ERROR_METHOD_NAME_HAS_INCORRECT_SYNTAX = "Method name has incorrect syntax.";
	private static final String ERROR_METHOD_DESCRIPTOR_HAS_INCORRECT_SYNTAX = "Method descriptor has incorrect syntax.";
	private static final String ERROR_INDEX_MUST_BE_UNIQUE = "Method parameter index must be unique.";

	private static final Pattern NAME_WITH_COUNT_PATTERN = Pattern.compile("^(.*)\\s*\\((\\d+)\\)$"); // $NON-NLS-1$
	private static final Pattern COUNT_SUFFIX_PATTERN = Pattern.compile("^\\s*\\((\\d+)\\)$"); // $NON-NLS-1$

	private final Preset preset;
	private final List<IMethodParameter> parameters = new ArrayList<>();
	private final List<IField> fields = new ArrayList<>();

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
	private IMethodReturnValue returnValue;

	Event(Preset preset) {
		this.preset = preset;

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
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException(ERROR_ID_CANNOT_BE_EMPTY_OR_NULL);
		}

		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException(ERROR_NAME_CANNOT_BE_EMPTY_OR_NULL);
		}

		this.name = name;
	}

	@Override
	public String getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(String clazz) {
		if (clazz == null || clazz.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CLASS_CANNOT_BE_EMPTY_OR_NULL);
		}

		clazz = clazz.trim();
		if (!clazz.matches(CLAZZ_REGEX)) {
			throw new IllegalArgumentException(ERROR_CLASS_HAS_INCORRECT_SYNTAX);
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
			path = path.trim();
			if (!path.matches(PATH_REGEX)) {
				throw new IllegalArgumentException(ERROR_PATH_HAS_INCORRECT_SYNTAX);
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
		if (methodName == null || methodName.isEmpty()) {
			throw new IllegalArgumentException(ERROR_METHOD_NAME_CANNOT_BE_EMPTY_OR_NULL);
		}

		methodName = methodName.trim();
		if (!methodName.matches(METHOD_NAME_REGEX)) {
			throw new IllegalArgumentException(ERROR_METHOD_NAME_HAS_INCORRECT_SYNTAX);
		}

		this.methodName = methodName;
	}

	@Override
	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	@Override
	public void setMethodDescriptor(String methodDescriptor) {
		if (methodDescriptor == null || methodDescriptor.isEmpty()) {
			throw new IllegalArgumentException(ERROR_METHOD_DESCRIPTOR_CANNOT_BE_EMPTY_OR_NULL);
		}

		methodDescriptor = methodDescriptor.trim();
		if (!methodDescriptor.matches(METHOD_DESCRIPTOR_REGEX)) {
			throw new IllegalArgumentException(ERROR_METHOD_DESCRIPTOR_HAS_INCORRECT_SYNTAX);
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
			throw new IllegalArgumentException(ERROR_METHOD_PARAMETER_CANNOT_BE_NULL);
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
	public boolean containsMethodParameter(IMethodParameter methodParameter) {
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
			throw new IllegalArgumentException(ERROR_FIELD_CANNOT_BE_NULL);
		}
		fields.add(field);
	}

	@Override
	public void removeField(IField field) {
		fields.remove(field);
	}

	@Override
	public boolean containsField(IField field) {
		return fields.contains(field);
	}

	@Override
	public Event createWorkingCopy() {
		Event copy = new Event(preset);
		copy.id = id;
		copy.name = name;
		copy.clazz = clazz;
		copy.description = description;
		copy.path = path;
		copy.recordStackTrace = recordStackTrace;
		copy.useRethrow = useRethrow;
		copy.methodName = methodName;
		copy.methodDescriptor = methodDescriptor;
		copy.location = location;

		if (returnValue != null) {
			copy.returnValue = returnValue.createWorkingCopy();
		}

		copy.parameters
				.addAll(parameters.stream().map(IMethodParameter::createWorkingCopy).collect(Collectors.toList()));
		copy.fields.addAll(fields.stream().map(IField::createWorkingCopy).collect(Collectors.toList()));

		return copy;
	}

	@Override
	public IEvent createDuplicate() {
		Event duplicate = createWorkingCopy();
		duplicate.id = preset.nextUniqueEventId(id);
		duplicate.name = preset.nextUniqueEventName(name);

		return duplicate;
	}

	@Override
	public int nextUniqueParameterIndex() {
		List<IMethodParameter> sorted = parameters.stream().sorted(Comparator.comparingInt(IMethodParameter::getIndex))
				.collect(Collectors.toList());

		int index = 0;
		if (sorted.isEmpty()) {
			return index;
		}

		for (IMethodParameter parameter : sorted) {
			if (parameter.getIndex() > index) {
				return index;
			}

			index = parameter.getIndex() + 1;
		}

		return index;
	}

	@Override
	public String nextUniqueParameterName(String originalName) {
		originalName = originalName.trim();

		// First, extract a base name and a count of the original name.
		String baseName = originalName;
		// Use count -1 to mean that no count should be appended, the baseName suffices.
		long proposedCount = -1;
		Matcher matcher = NAME_WITH_COUNT_PATTERN.matcher(originalName);
		if (matcher.matches()) {
			try {
				long count = Long.parseLong(matcher.group(2));
				// Valid match, use the shorter base and this count.
				baseName = matcher.group(1).trim();
				proposedCount = count;
			} catch (NumberFormatException e) {
				// Too large number. => Use the entire name as base.
				// (Yes, we could have used BigInteger, but which sane person would want such names?)
			}
		}

		// Second, find any existing templates matching the proposed baseName pattern,
		// with or without count, and make sure the proposed count is greater.
		int baseLen = baseName.length();
		for (IMethodParameter parameter : parameters) {
			String tempName = parameter.getName().trim();
			if (tempName.startsWith(baseName)) {
				if (tempName.equals(baseName) && (proposedCount < 1)) {
					proposedCount = 1;
				} else {
					// Note that this pattern must ignore leading whitespace.
					Matcher tempMatch = COUNT_SUFFIX_PATTERN.matcher(tempName.substring(baseLen));
					if (tempMatch.matches()) {
						try {
							long count = Long.parseLong(tempMatch.group(1));
							if (count < Long.MAX_VALUE) {
								// Valid match, use a count greater than this, unless the proposed was greater.
								proposedCount = Math.max(proposedCount, count + 1);
							}
						} catch (NumberFormatException e) {
							// Too large number, pretend we didn't see this template.
						}
					}
				}
			}
		}
		if (proposedCount == -1) {
			return baseName;
		} else {
			return baseName + " (" + proposedCount + ')'; // $NON-NLS-1$
		}
	}

	@Override
	public String nextUniqueFieldName(String originalName) {
		originalName = originalName.trim();

		// First, extract a base name and a count of the original name.
		String baseName = originalName;
		// Use count -1 to mean that no count should be appended, the baseName suffices.
		long proposedCount = -1;
		Matcher matcher = NAME_WITH_COUNT_PATTERN.matcher(originalName);
		if (matcher.matches()) {
			try {
				long count = Long.parseLong(matcher.group(2));
				// Valid match, use the shorter base and this count.
				baseName = matcher.group(1).trim();
				proposedCount = count;
			} catch (NumberFormatException e) {
				// Too large number. => Use the entire name as base.
				// (Yes, we could have used BigInteger, but which sane person would want such names?)
			}
		}

		// Second, find any existing templates matching the proposed baseName pattern,
		// with or without count, and make sure the proposed count is greater.
		int baseLen = baseName.length();
		for (IField field : fields) {
			String tempName = field.getName().trim();
			if (tempName.startsWith(baseName)) {
				if (tempName.equals(baseName) && (proposedCount < 1)) {
					proposedCount = 1;
				} else {
					// Note that this pattern must ignore leading whitespace.
					Matcher tempMatch = COUNT_SUFFIX_PATTERN.matcher(tempName.substring(baseLen));
					if (tempMatch.matches()) {
						try {
							long count = Long.parseLong(tempMatch.group(1));
							if (count < Long.MAX_VALUE) {
								// Valid match, use a count greater than this, unless the proposed was greater.
								proposedCount = Math.max(proposedCount, count + 1);
							}
						} catch (NumberFormatException e) {
							// Too large number, pretend we didn't see this template.
						}
					}
				}
			}
		}
		if (proposedCount == -1) {
			return baseName;
		} else {
			return baseName + " (" + proposedCount + ')'; // $NON-NLS-1$
		}
	}

	@Override
	public MethodReturnValue createMethodReturnValue() {
		return new MethodReturnValue(this);
	}

	@Override
	public MethodParameter createMethodParameter() {
		MethodParameter parameter = new MethodParameter(this);

		parameter.setName(nextUniqueParameterName(parameter.getName()));
		parameter.setIndex(nextUniqueParameterIndex());
		return parameter;
	}

	@Override
	public void updateMethodParameter(IMethodParameter original, IMethodParameter workingCopy) {
		if (parameters.remove(original)) {
			parameters.add(workingCopy);
		}
	}

	@Override
	public Field createField() {
		Field field = new Field(this);

		field.setName(nextUniqueFieldName(field.getName()));
		return field;
	}

	@Override
	public void updateField(IField original, IField workingCopy) {
		if (fields.remove(original)) {
			fields.add(workingCopy);
		}
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
