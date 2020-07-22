package org.openjdk.jmc.console.ext.agent.manager.model;

import java.io.IOException;
import java.io.InputStream;

public interface IPresetStorageDelegate {

	String getName();

	InputStream getContents();

	boolean save(String fileName, String fileContent) throws IOException;

	boolean delete();
}
