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

import java.net.URI;
import java.net.URISyntaxException;

import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;

public class MethodParameter implements IMethodParameter {
	private static final String DEFAULT_STRING_FIELD = "";
	private static final Object DEFAULT_OBJECT_TYPE = null;
	private static final String DEFAULT_PARAMETER_NAME = "myParameterName"; // $NON-NLS-1$
	private static final int DEFAULT_INDEX = 0;
	private static final String ERROR_CANNOT_BE_EMPTY = "Field cannot be empty";
	private static final String ERROR_INCORRECT_SYNTAX = "Field has incorrect syntax";
	private static final String ERROR_LESS_THAN_ZERO = "Index cannot be less than zero";
	private static final String ERROR_CANNOT_BE_NULL = "Field cannot be null";

	private String name;
	private String description;
	private ContentType contentType;
	private String relationKey;
	private String converter;
	private int index;

	public MethodParameter() {
		name = DEFAULT_PARAMETER_NAME;
		description = DEFAULT_STRING_FIELD;
		contentType = (ContentType) DEFAULT_OBJECT_TYPE;
		relationKey = DEFAULT_STRING_FIELD;
		converter = DEFAULT_STRING_FIELD;
		index = DEFAULT_INDEX;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_NULL);
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		}
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public String getRelationKey() {
		return relationKey;
	}

	public void setRelationKey(String relationKey) {
		if (relationKey != null) {
			try {
				new URI(relationKey);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(ERROR_INCORRECT_SYNTAX);
			}
		}
		this.relationKey = relationKey;
	}

	public String getConverter() {
		return converter;
	}

	public void setConverter(String converter) {
		this.converter = converter;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException(ERROR_LESS_THAN_ZERO);
		}
		this.index = index;
	}

}
