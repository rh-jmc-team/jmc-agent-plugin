package org.openjdk.jmc.console.ext.agent.manager.model;

public interface ICapturedValue {
	enum ContentType {
		NONE, BYTES, TIMESTAMP, MILLIS, NANOS, TICKS, ADDRESS, OS_THREAD, JAVA_THREAD, STACK_TRACE, CLASS, PERCENTAGE
	}

	String getDescription();

	void setDescription(String description);

	ContentType getContentType();

	void setContentType(ContentType contentType);

	String getRelationKey();

	void setRelationKey(String relationKey);

	String getConverter();

	void setConverter(String converter);
}
