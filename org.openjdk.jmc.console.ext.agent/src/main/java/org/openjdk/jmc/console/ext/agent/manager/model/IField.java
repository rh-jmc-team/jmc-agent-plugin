package org.openjdk.jmc.console.ext.agent.manager.model;

public interface IField extends INamedCapturedValue {
	String getExpression();

	void setExpression(String expression);
}
