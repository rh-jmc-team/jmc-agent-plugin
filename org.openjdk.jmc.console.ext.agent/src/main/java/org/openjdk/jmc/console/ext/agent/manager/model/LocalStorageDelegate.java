package org.openjdk.jmc.console.ext.agent.manager.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;

public class LocalStorageDelegate implements IPresetStorageDelegate {
	private final File file;

	public static IPresetStorageDelegate getDelegate() throws IOException {
		File dir = PresetRepositoryFactory.getCreatedStorageDir();
		File file = File.createTempFile("preset-", PresetRepositoryFactory.PRESET_FILE_EXTENSION, dir);
		return new LocalStorageDelegate(file);
	}

	public static IPresetStorageDelegate getDelegate(String fileName) throws IOException {
		File dir = PresetRepositoryFactory.getCreatedStorageDir();
		File file = new File(dir, fileName);
		return new LocalStorageDelegate(file);
	}

	public static IPresetStorageDelegate getDelegate(File file) {
		return new LocalStorageDelegate(file);
	}

	private LocalStorageDelegate(File file) {
		this.file = file;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public InputStream getContents() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	public boolean save(String fileName, String fileContent) throws IOException {
		if (!file.getName().equals(fileName)) {
			if (!file.renameTo(new File(PresetRepositoryFactory.getCreatedStorageDir(), fileName))) {
				return false;
			}
		}

		try {
			try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				out.write(fileContent);
				out.flush();
			}
			return true;
		} catch (IllegalCharsetNameException | FileNotFoundException e) {
			return false;
		}
	}

	@Override
	public boolean delete() {
		return file.delete();
	}
}
