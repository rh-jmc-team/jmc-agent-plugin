package org.openjdk.jmc.console.ext.agent.raweditor.internal;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class XmlConfiguration extends SourceViewerConfiguration {
	private XmlDoubleClickStrategy doubleClickStrategy;
	private XmlTagScanner tagScanner;
	private XmlScanner scanner;
	private ColorManager colorManager;

	public XmlConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {IDocument.DEFAULT_CONTENT_TYPE, XmlPartitionScanner.XML_COMMENT,
				XmlPartitionScanner.XML_TAG};
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new XmlDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected XmlScanner getXMLScanner() {
		if (scanner == null) {
			scanner = new XmlScanner(colorManager);
			scanner.setDefaultReturnToken(
					new Token(new TextAttribute(colorManager.getColor(XmlColorConstants.DEFAULT))));
		}
		return scanner;
	}

	protected XmlTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new XmlTagScanner(colorManager);
			tagScanner
					.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(XmlColorConstants.TAG))));
		}
		return tagScanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getXMLTagScanner());
		reconciler.setDamager(dr, XmlPartitionScanner.XML_TAG);
		reconciler.setRepairer(dr, XmlPartitionScanner.XML_TAG);

		dr = new DefaultDamagerRepairer(getXMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(XmlColorConstants.XML_COMMENT)));
		reconciler.setDamager(ndr, XmlPartitionScanner.XML_COMMENT);
		reconciler.setRepairer(ndr, XmlPartitionScanner.XML_COMMENT);

		return reconciler;
	}

}
