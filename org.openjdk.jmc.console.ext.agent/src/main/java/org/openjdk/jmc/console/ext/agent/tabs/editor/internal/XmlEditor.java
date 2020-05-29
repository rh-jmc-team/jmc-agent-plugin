package org.openjdk.jmc.console.ext.agent.tabs.editor.internal;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.TextEditor;

public class XmlEditor extends TextEditor {
	public static final String EDITOR_ID = "org.openjdk.jmc.console.ext.agent.tabs.editor.internal.XmlEditor";

	private ColorManager colorManager;

	public XmlEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XmlConfiguration(colorManager));
		setDocumentProvider(new XmlDocumentProvider());
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		System.out.println("doSetInput");
		input = convertInput(input);
		
		super.doSetInput(input);
		setDocumentProvider(input);
	}

	@Override
	protected void createActions() {
//		super.createActions();
	}

	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	
	public static IEditorInput convertInput(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput || editorInput instanceof IStorageEditorInput) {
			return editorInput;
		}

		if (editorInput instanceof IPathEditorInput) {
			IPath p = ((IPathEditorInput) editorInput).getPath();

			IStorage s = new LocalFileStorage(p.toFile()) {
				public boolean isReadOnly() {
					return false;
				};
			};

			return new IStorageEditorInput() {
				public <T> T getAdapter(Class<T> adapter) {
					if (adapter.equals(ILocationProvider.class)) {
						return (T) (ILocationProvider) element -> p;
					}
					return editorInput.getAdapter(adapter);
				}

				public boolean exists() {
					return editorInput.exists();
				}

				public ImageDescriptor getImageDescriptor() {
					return editorInput.getImageDescriptor();
				}

				public String getName() {
					return editorInput.getName();
				}

				public IPersistableElement getPersistable() {
					return editorInput.getPersistable();
				}

				public String getToolTipText() {
					return editorInput.getToolTipText();
				}

				public IStorage getStorage() {
					return s;
				}
			};
		}

		throw new UnsupportedOperationException();

	}
}
