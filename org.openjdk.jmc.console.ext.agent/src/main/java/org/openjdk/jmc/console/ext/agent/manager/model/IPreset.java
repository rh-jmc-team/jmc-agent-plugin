package org.openjdk.jmc.console.ext.agent.manager.model;

public interface IPreset {

	String getFileName();

	void setFileName();

	void setClassPrefix(String prefix);

	String getClassPrefix();

	void setAllowToString(String allowed);

	boolean getAllowToString();

	void setAllowConverter(boolean allowed);

	boolean getAllowConverter();

	IEvent[] getEvents();

	void addEvent(IEvent event);

	void removeEvent(IEvent event);

	boolean containEvent(IEvent event);
}
