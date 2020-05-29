package org.openjdk.jmc.console.ext.agent.tabs.presets.internal;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ProbeValidator extends Validator {
	private Validator validator;
	private ValidationResult validationResult = new ValidationResult();

	private static final String PROBE_SCHEMA_XSD = "jfrprobes_schema.xsd"; //$NON-NLS-1$
	private static final Schema PROBE_SCHEMA;

	static {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			PROBE_SCHEMA = factory
					.newSchema(new StreamSource(ProbeValidator.class.getResourceAsStream(PROBE_SCHEMA_XSD)));
		} catch (SAXException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public ProbeValidator() {
		validator = PROBE_SCHEMA.newValidator();
		validator.setErrorHandler(new ProbeValidatorErrorHandler());
	}

	public ValidationResult getValidationResult() {
		return this.validationResult;
	}

	@Override
	public void reset() {
		validationResult = new ValidationResult();
		validator.reset();
	}

	@Override
	public void validate(Source source, Result result) throws SAXException, IOException {
		validator.validate(source, result);
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		throw new UnsupportedOperationException("setErrorHandler is unsupported");
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return validator.getErrorHandler();
	}

	@Override
	public void setResourceResolver(LSResourceResolver resourceResolver) {
		validator.setResourceResolver(resourceResolver);
	}

	@Override
	public LSResourceResolver getResourceResolver() {
		return validator.getResourceResolver();
	}

	private class ProbeValidatorErrorHandler implements ErrorHandler {

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			validationResult.addWarning(exception);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			validationResult.addError(exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			validationResult.setFatalError(exception);
		}
	}
}
