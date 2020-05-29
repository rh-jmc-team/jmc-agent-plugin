package org.openjdk.jmc.console.ext.agent.tabs.presets.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.xml.sax.SAXParseException;

public class ValidationResult {

	private List<SAXParseException> warnings = new ArrayList<>();
	private List<SAXParseException> errors = new ArrayList<>();
	private SAXParseException fatalError;

	/* package-private */ ValidationResult(ArrayList<SAXParseException> warnings, ArrayList<SAXParseException> errors,
			SAXParseException fatalError) {
		this.warnings = Objects.requireNonNull(warnings);
		this.errors = Objects.requireNonNull(errors);
		this.fatalError = fatalError;
	}

	/* package-private */ ValidationResult() {
	}

	/* package-private */ void addError(SAXParseException error) {
		errors.add(error);
	}

	/* package-private */ void addWarning(SAXParseException warning) {
		warnings.add(warning);
	}

	/* package-private */ void setFatalError(SAXParseException fatalError) {
		this.fatalError = fatalError;
	}

	public boolean isValid() {
		return errors.isEmpty() && fatalError == null;
	}

	public List<SAXParseException> getWarnings() {
		return warnings;
	}

	public List<SAXParseException> getErrors() {
		return errors;
	}

	public SAXParseException getfatalError() {
		return fatalError;
	}
}
