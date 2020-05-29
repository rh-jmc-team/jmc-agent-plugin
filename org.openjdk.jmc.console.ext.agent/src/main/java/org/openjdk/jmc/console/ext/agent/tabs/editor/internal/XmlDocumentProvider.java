package org.openjdk.jmc.console.ext.agent.tabs.editor.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

public class XmlDocumentProvider extends StorageDocumentProvider {

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(new XmlPartitionScanner(),
					new String[] {XmlPartitionScanner.XML_TAG, XmlPartitionScanner.XML_COMMENT});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {
		// TODO: Prototype implementation that most likely requires revise
		IStorageEditorInput ei = (IStorageEditorInput) element;
		File out = ei.getStorage().getFullPath().toFile();

		if (!out.exists()) {
			try {
				out.createNewFile();
			} catch (IOException e) {
				IStatus s = new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, e.getMessage(), e);
				throw new CoreException(s);
			}
		}

		try (FileOutputStream fos = new FileOutputStream(out)) {
			fos.write(document.get().getBytes()); // TODO: encoding?
		} catch (IOException e) {
			IStatus s = new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, e.getMessage(), e);
			throw new CoreException(s);
		}
	}
}
