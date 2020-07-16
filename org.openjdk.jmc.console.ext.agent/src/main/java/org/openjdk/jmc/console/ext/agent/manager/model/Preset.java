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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Preset implements IPreset {
	private static final String FILE_NAME_EXTENSION = ".xml"; // $NON-NLS-1$
	private static final String DEFAULT_FILE_NAME = "new_file.xml"; // $NON-NLS-1$
	private static final String DEFAULT_CLASS_PREFIX = "__JFREvent"; // $NON-NLS-1$
	private static final boolean DEFAULT_BOOLEAN_FIELD = false;
	private static final String ERROR_FILE_NAME_CANNOT_BE_EMPTY_OR_NULL = "File name cannot be empty or null.";
	private static final String ERROR_MUST_HAVE_UNIQUE_ID = "An event with the same id already exists.";
	private static final String ERROR_MUST_HAVE_UNIQUE_EVENT_CLASS_NAME = "Event must have a unique event name per class";

	private static final Pattern ID_WITH_COUNT_PATTERN = Pattern.compile("^(.*)\\.(\\d+)$"); // $NON-NLS-1$
	private static final Pattern ID_COUNT_SUFFIX_PATTERN = Pattern.compile("^\\.(\\d+)$"); // $NON-NLS-1$
	private static final Pattern NAME_WITH_COUNT_PATTERN = Pattern.compile("^(.*)\\s*\\((\\d+)\\)$"); //$NON-NLS-1$
	private static final Pattern NAME_COUNT_SUFFIX_PATTERN = Pattern.compile("^\\s*\\((\\d+)\\)$"); //$NON-NLS-1$

	private final PresetRepository presetRepository;
	private IPresetStorageDelegate storageDelegate;
	private final List<IEvent> events = new ArrayList<>();

	private String fileName;
	private String classPrefix;
	private boolean allowToString;
	private boolean allowConverter;

	Preset(PresetRepository repository, IPresetStorageDelegate storageDelegate) {
		presetRepository = repository;
		this.storageDelegate = storageDelegate;

		fileName = DEFAULT_FILE_NAME;
		classPrefix = DEFAULT_CLASS_PREFIX;
		allowToString = DEFAULT_BOOLEAN_FIELD;
		allowConverter = DEFAULT_BOOLEAN_FIELD;

		// TODO: load and parse XMl model
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void setFileName(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException(ERROR_FILE_NAME_CANNOT_BE_EMPTY_OR_NULL);
		}

		this.fileName = fileName;
	}

	@Override
	public void setClassPrefix(String prefix) {
		if (prefix != null) {
			prefix = prefix.trim();
		}

		this.classPrefix = prefix;
	}

	@Override
	public String getClassPrefix() {
		return classPrefix;
	}

	@Override
	public void setAllowToString(boolean allowed) {
		allowToString = allowed;
	}

	@Override
	public boolean getAllowToString() {
		return allowToString;
	}

	@Override
	public void setAllowConverter(boolean allowed) {
		allowConverter = allowed;
	}

	@Override
	public boolean getAllowConverter() {
		return allowConverter;
	}

	@Override
	public IEvent[] getEvents() {
		return events.toArray(new IEvent[0]);
	}

	@Override
	public void addEvent(IEvent event) {
		if (containsId(event.getId())) {
			throw new IllegalArgumentException(ERROR_MUST_HAVE_UNIQUE_ID);
		}
		if (containsEventClassName(event)) {
			throw new IllegalArgumentException(ERROR_MUST_HAVE_UNIQUE_EVENT_CLASS_NAME);
		}

		events.add(event);
	}

	@Override
	public void removeEvent(IEvent event) {
		events.remove(event);
	}

	@Override
	public boolean containsEvent(IEvent event) {
		return events.contains(event);
	}

	@Override
	public IEvent createEvent() {
		Event event = new Event(this);

		String id = fileName;
		if (id.endsWith(FILE_NAME_EXTENSION)) {
			id = id.substring(0, id.lastIndexOf(FILE_NAME_EXTENSION)).replaceAll("\\s", "_");
		}
		id = nextUniqueEventId(id + ".event.1"); // $NON-NLS-1$

		String name = nextUniqueEventName(event.getName());
		event.setId(id);
		event.setName(name);

		return event;
	}

	@Override
	public void updateEvent(IEvent original, IEvent workingCopy) {
		if (events.remove(original)) {
			events.add(workingCopy);
		}
	}

	@Override
	public Preset createWorkingCopy() {
		Preset copy = new Preset(presetRepository, null);
		copy.fileName = fileName;
		copy.classPrefix = classPrefix;
		copy.allowToString = allowToString;
		copy.allowConverter = allowConverter;

		copy.events.addAll(events.stream().map(IEvent::createWorkingCopy).collect(Collectors.toList()));

		return copy;
	}

	@Override
	public Preset createDuplicate() {
		Preset duplicate = createWorkingCopy();
		duplicate.fileName = presetRepository.nextUniqueName(duplicate.fileName);

		return duplicate;
	}

	@Override
	public String nextUniqueEventName(String originalName) {
		originalName = originalName.trim();

		// First, extract a base name and a count of the original name.
		String baseName = originalName;
		// Use count -1 to mean that no count should be appended, the baseName suffices.
		long proposedCount = -1;
		Matcher matcher = NAME_WITH_COUNT_PATTERN.matcher(originalName);
		if (matcher.matches()) {
			try {
				long count = Long.parseLong(matcher.group(2));
				// Valid match, use the shorter base and this count.
				baseName = matcher.group(1).trim();
				proposedCount = count;
			} catch (NumberFormatException e) {
				// Too large number. => Use the entire name as base.
				// (Yes, we could have used BigInteger, but which sane person would want such names?)
			}
		}

		// Second, find any existing templates matching the proposed baseName pattern,
		// with or without count, and make sure the proposed count is greater.
		int baseLen = baseName.length();
		for (IEvent event : events) {
			String tempName = event.getName().trim();
			if (tempName.startsWith(baseName)) {
				if (tempName.equals(baseName) && (proposedCount < 1)) {
					proposedCount = 1;
				} else {
					// Note that this pattern must ignore leading whitespace.
					Matcher tempMatch = NAME_COUNT_SUFFIX_PATTERN.matcher(tempName.substring(baseLen));
					if (tempMatch.matches()) {
						try {
							long count = Long.parseLong(tempMatch.group(1));
							if (count < Long.MAX_VALUE) {
								// Valid match, use a count greater than this, unless the proposed was greater.
								proposedCount = Math.max(proposedCount, count + 1);
							}
						} catch (NumberFormatException e) {
							// Too large number, pretend we didn't see this template.
						}
					}
				}
			}
		}
		if (proposedCount == -1) {
			return baseName;
		} else {
			return baseName + " (" + proposedCount + ')'; // $NON-NLS-1$
		}
	}

	@Override
	public String nextUniqueEventId(String originalName) {
		originalName = originalName.trim();

		// First, extract a base name and a count of the original name.
		String baseName = originalName;
		// Use count -1 to mean that no count should be appended, the baseName suffices.
		long proposedCount = -1;
		Matcher matcher = ID_WITH_COUNT_PATTERN.matcher(originalName);
		if (matcher.matches()) {
			try {
				long count = Long.parseLong(matcher.group(2));
				// Valid match, use the shorter base and this count.
				baseName = matcher.group(1).trim();
				proposedCount = count;
			} catch (NumberFormatException e) {
				// Too large number. => Use the entire name as base.
				// (Yes, we could have used BigInteger, but which sane person would want such names?)
			}
		}

		// Second, find any existing templates matching the proposed baseName pattern,
		// with or without count, and make sure the proposed count is greater.
		int baseLen = baseName.length();
		for (IEvent event : events) {
			String tempName = event.getId().trim();
			if (tempName.startsWith(baseName)) {
				if (tempName.equals(baseName) && (proposedCount < 1)) {
					proposedCount = 1;
				} else {
					// Note that this pattern must ignore leading whitespace.
					Matcher tempMatch = ID_COUNT_SUFFIX_PATTERN.matcher(tempName.substring(baseLen));
					if (tempMatch.matches()) {
						try {
							long count = Long.parseLong(tempMatch.group(1));
							if (count < Long.MAX_VALUE) {
								// Valid match, use a count greater than this, unless the proposed was greater.
								proposedCount = Math.max(proposedCount, count + 1);
							}
						} catch (NumberFormatException e) {
							// Too large number, pretend we didn't see this template.
						}
					}
				}
			}
		}
		if (proposedCount == -1) {
			return baseName;
		} else {
			return baseName + '.' + proposedCount; // $NON-NLS-1$
		}
	}

	@Override
	public void setStorageDelegate(IPresetStorageDelegate storageDelegate) {
		this.storageDelegate = storageDelegate;
	}

	@Override
	public IPresetStorageDelegate getStorageDelegate() {
		return storageDelegate;
	}

	@Override
	public boolean save() {
		if (storageDelegate == null) {
			return false;
		}

		try {
			return storageDelegate.save(fileName, ""); // TODO: build file content
		} catch (IOException e) {
			// TODO: log exception
			return false;
		}
	}

	@Override
	public boolean delete() {
		if (storageDelegate == null) {
			return true;
		}

		return storageDelegate.delete();
	}

	private boolean containsId(String id) {
		for (IEvent e : events) {
			if (e.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsEventClassName(IEvent event) {
		for (IEvent e : events) {
			if (e.getClazz().equals(event.getClazz()) && e.getName().equals(event.getName())) {
				return true;
			}
		}
		return false;
	}
}
