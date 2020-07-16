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
package org.openjdk.jmc.console.ext.agent.manager.model;

import org.openjdk.jmc.console.ext.agent.AgentPlugin;

import java.io.File;
import java.io.IOException;

public class PresetRepositoryFactory {
	static final String PRESET_FILE_EXTENSION = ".xml"; // $NON-NLS-1$
	private static final File PRESET_STORAGE_DIR = AgentPlugin.getDefault().getStateLocation()
			.append(".presets") // $NON-NLS-1$
			.toFile();

	public static PresetRepository create() {
		PresetRepository repository = new PresetRepository();
		initiate(repository);
		return repository;
	}

	protected static void initiate(PresetRepository repository) {
		addLocalPresetTo(repository);
	}

	private static void addLocalPresetTo(PresetRepository repository) {
		File localDir = PRESET_STORAGE_DIR;
		if (!localDir.isDirectory()) {
			return;
		}

		File[] files = localDir.listFiles((dir, name) -> name.endsWith(PRESET_FILE_EXTENSION));
		if (files == null) {
			return;
		}

		for (File file : files) {
			if (file.length() == 0) {
				// FIXME: delete or just ignore empty files? 
			} else {
				IPreset preset;
				IPresetStorageDelegate delegate = LocalStorageDelegate.getDelegate(file);
				preset = new Preset(repository, delegate);
				try {
					repository.addPreset(preset);
				} catch (IOException e) {
					// TODO: log exception
				}
			}
		}
	}

	public static File getCreatedStorageDir() throws IOException {
		if (!PRESET_STORAGE_DIR.isDirectory()) {
			// Since the parent directory should exist, we explicitly avoid "mkdirs()".
			if (!PRESET_STORAGE_DIR.mkdir()) {
				throw new IOException("Could not create the directory " + PRESET_STORAGE_DIR.toString()); //$NON-NLS-1$
			}
		}
		return PRESET_STORAGE_DIR;
	}
}
