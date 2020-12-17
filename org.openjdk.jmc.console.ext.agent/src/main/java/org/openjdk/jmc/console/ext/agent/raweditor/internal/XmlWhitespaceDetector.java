package org.openjdk.jmc.console.ext.agent.raweditor.internal;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class XmlWhitespaceDetector implements IWhitespaceDetector {

	@Override
	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
