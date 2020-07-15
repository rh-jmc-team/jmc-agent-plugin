package org.openjdk.jmc.console.ext.agent.manager.model.impl;

import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;

import java.net.URI;
import java.net.URISyntaxException;

class CapturedValue implements ICapturedValue {

	private static final String DEFAULT_STRING_FIELD = ""; // $NON-NLS-1$
	private static final Object DEFAULT_OBJECT_TYPE = null;
	private static final String DEFAULT_FIELD_NAME = "'New Captured Value'"; // $NON-NLS-1$
	private static final String CONVERTER_REGEX = "([a-zA-Z_$][a-zA-Z0-9_$]*\\.)*([a-zA-Z_$][a-zA-Z0-9_$]*)"; // $NON-NLS-1$

	private static final String ERROR_NAME_CANNOT_BE_EMPTY_OR_NULL = "Name cannot be empty or null.";
	private static final String ERROR_RELATION_KEY_HAS_INCORRECT_SYNTAX = "Relation key has incorrect syntax.";
	private static final String ERROR_CONVERTER_HAS_INCORRECT_SYNTAX = "Converter has incorrect syntax.";

	private String name;
	private String description;
	private ContentType contentType;
	private String relationKey;
	private String converter;

	CapturedValue() {
		name = DEFAULT_FIELD_NAME;
		description = DEFAULT_STRING_FIELD;
		contentType = (ContentType) DEFAULT_OBJECT_TYPE;
		relationKey = DEFAULT_STRING_FIELD;
		converter = DEFAULT_STRING_FIELD;
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
		if (relationKey != null && !relationKey.isEmpty()) {
			relationKey = relationKey.trim();
			try {
				new URI(relationKey);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(ERROR_RELATION_KEY_HAS_INCORRECT_SYNTAX);
			}
		}

		this.relationKey = relationKey;
	}

	@Override
	public String getConverter() {
		return converter;
	}

	@Override
	public void setConverter(String converter) {
		if (converter != null && !converter.isEmpty()) {
			converter = converter.trim();
			if (!converter.matches(CONVERTER_REGEX)) {
				throw new IllegalArgumentException(ERROR_CONVERTER_HAS_INCORRECT_SYNTAX);
			}
		}

		this.converter = converter;
	}
}
