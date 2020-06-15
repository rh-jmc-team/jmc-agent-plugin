package org.openjdk.jmc.console.ext.agent.manager.model;

public interface IEvent {

	enum Location {
		ENTRY, EXIT, WRAP,
	}

	String getName();

	void setName(String name);

	String getClazz();

	void setClazz(String clazz);

	String getDescription();

	void setDescription(String description);

	String getPath();

	void setPath(String path);

	boolean getStackTrace();

	void setStackTrace(boolean enabled);

	boolean getRethrow();

	void setRethrow(boolean enabled);

	Location getLocation();

	void setLocation(Location location);

	String getMethodName();

	void setMethodName(String methodName);

	String getMethodDescriptor();

	void setMethodDescriptor(String methodDescriptor);

	IMethodParameter[] getMethodParameters();

	void addMethodParameter(IMethodParameter methodParameter);

	void removeMethodParameter(IMethodParameter methodParameter);

	boolean containMethodParameter(IMethodParameter methodParameter);

	void setMethodReturnValue(IMethodReturnValue methodReturnValue);

	IMethodReturnValue getMethodReturnValue();

	IField[] getFields();

	void addField(IField field);

	void removeField(IField field);

	boolean containField(IField field);
}
