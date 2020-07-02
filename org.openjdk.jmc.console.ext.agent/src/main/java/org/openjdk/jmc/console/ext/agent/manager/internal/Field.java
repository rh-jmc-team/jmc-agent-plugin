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

import org.openjdk.jmc.console.ext.agent.manager.model.IField;

public class Field implements IField {

	private static final String DEFAULT_STRING_FIELD = "";
	private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.NONE;

	private String name;
	private String description;
	private ContentType contentType;
	private String relationKey;
	private String converter;
	private String expression;

	public Field() {
		name = DEFAULT_STRING_FIELD;
		expression = DEFAULT_STRING_FIELD;
		description = DEFAULT_STRING_FIELD;
		contentType = DEFAULT_CONTENT_TYPE;
		relationKey = DEFAULT_STRING_FIELD;
		converter = DEFAULT_STRING_FIELD;
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
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getRelationKey() {
		return relationKey;
	}

	@Override
	public void setRelationKey(String relationKey) {
		this.relationKey = relationKey;
	}

	@Override
	public String getConverter() {
		return converter;
	}

	@Override
	public void setConverter(String converter) {
		this.converter = converter;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public void setExpression(String expression) {
		this.expression = expression;
	}

}
