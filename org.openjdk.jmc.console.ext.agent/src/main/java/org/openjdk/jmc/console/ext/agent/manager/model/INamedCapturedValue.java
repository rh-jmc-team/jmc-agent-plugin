package org.openjdk.jmc.console.ext.agent.manager.model;

public interface INamedCapturedValue extends ICapturedValue {
	String getName();

	void setName(String name);
}
