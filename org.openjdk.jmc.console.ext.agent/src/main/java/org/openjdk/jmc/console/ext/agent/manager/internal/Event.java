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
package org.openjdk.jmc.console.ext.agent.manager.internal;

import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodReturnValue;

public class Event implements IEvent {
	private static final String DEFAULT_STRING_FIELD = "";
	private static final boolean DEFAULT_BOOLEAN_FIELD = false;
	private static final Location DEFAULT_LOCATION = Location.WRAP;

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
		id = DEFAULT_STRING_FIELD;
		name = DEFAULT_STRING_FIELD;
		clazz = DEFAULT_STRING_FIELD;
		description = DEFAULT_STRING_FIELD;
		path = DEFAULT_STRING_FIELD;
		recordStackTrace = DEFAULT_BOOLEAN_FIELD;
		useRethrow = DEFAULT_BOOLEAN_FIELD;
		methodName = DEFAULT_STRING_FIELD;
		methodDescriptor = DEFAULT_STRING_FIELD;
		location = DEFAULT_LOCATION;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(String clazz) {
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
		this.methodName = methodName;
	}

	@Override
	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	@Override
	public void setMethodDescriptor(String methodDescriptor) {
		this.methodDescriptor = methodDescriptor;

	}

	@Override
	public IMethodParameter[] getMethodParameters() {
		return parameters.toArray(new IMethodParameter[0]);
	}

	@Override
	public void addMethodParameter(IMethodParameter methodParameter) {
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

}
