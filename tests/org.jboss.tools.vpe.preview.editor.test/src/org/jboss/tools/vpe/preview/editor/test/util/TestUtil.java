/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor.test.util;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.xpl.DisplayDelayHelper;
import org.jboss.tools.test.util.xpl.DisplayHelper;
import org.jboss.tools.vpe.base.test.ProjectsLoader;
import org.jboss.tools.vpe.preview.editor.VpvEditor;
import org.jboss.tools.vpe.preview.editor.VpvEditorController;
import org.jboss.tools.vpe.preview.editor.VpvEditorPart;
import org.w3c.dom.Node;

/**
 * 
 * @author Konstantin Marmalyukov (kmarmaliykov)
 *
 */

public class TestUtil {
    /** Editor in which we open visual page. */
    protected final static String EDITOR_ID = "org.jboss.tools.jst.jsp.jspeditor.JSPTextEditor"; //$NON-NLS-1$

	/** The Constant MAX_IDLE. */
	public static final long MAX_IDLE = 5*1000L;
	private static final long STANDARD_DELAY = 50L;
	
	/**
     * Gets visual page editor controller.
     * 
     * @param part the part
     * 
     * @return {@link VpvEditorController}
     */
    public static VpvEditorController getVpvEditorController(JSPMultiPageEditor part) {
        VpvEditorPart visualEditor = (VpvEditorPart) part.getVisualEditor();
        DisplayHelper helper = new ControllerDisplayHelper(visualEditor);
        helper.waitForCondition(Display.getCurrent(), 5000);
        return visualEditor.getController();
    }

    
    /**
	 * Gets the component path.
	 * 
	 * @param componentPage the component page
	 * @param projectName the project name
	 * 
	 * @return the component path
	 * 
	 * @throws CoreException the core exception
	 * @throws IOException 
	 */
	public static IResource getComponentPath(String componentPage, String projectName) throws CoreException, IOException {
		IProject project = ProjectsLoader.getInstance().getProject(projectName);
		if (project != null) {
			return project.getFolder("src/main/webapp").findMember(componentPage); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Gets the component path.
	 * 
	 * @param componentPage the component page
	 * @param projectName the project name
	 * 
	 * @return the component path
	 * 
	 * @throws CoreException the core exception
	 * @throws IOException 
	 */
	public static IResource getComponentFileByFullPath(String componentPage,
			String projectName) throws CoreException, IOException {
		IProject project = ProjectsLoader.getInstance().getProject(projectName);
		if (project != null) {
			return project.findMember(componentPage);
		}
		return null;
	}

	public static IResource getResource(String path, String projectName) 
			throws CoreException, IOException {
		IProject project = ProjectsLoader.getInstance().getProject(projectName);
		if (project != null) {
			return project.findMember(path);
		}
		return null;
	}

	/**
	 * @param xmlScheme
	 * @param xmlSchemesRoot
	 * @return
	 */
	public static File getXmlTestFile(String xmlTestPath, String xmlTestsRoot) {
		return new File(xmlTestsRoot + File.separator + xmlTestPath);
	}

	/**
	 * Process UI input but do not return for the {@link TestUtil#STANDARD_DELAY} interval.
	 */
	public static void delay() {
		delay(STANDARD_DELAY);
	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis the number of milliseconds
	 */
	public static void delay(long waitTimeMillis) {
		JobUtils.delay(waitTimeMillis);
	}

	/**
	 * Wait until all background tasks are complete.
	 */
	public static void waitForJobs() {
		waitForIdle();
	}

	/**
	 * Wait for idle.
	 */
	public static void waitForIdle(long maxIdle) {
		JobUtils.waitForIdle(STANDARD_DELAY, maxIdle);
	}
	
	public static void waitForIdle() {
		waitForIdle(MAX_IDLE);
	}

	/**
	 * Utility function which returns node mapping by source position(line and position in line).
	 * 
	 * @param linePosition the line position
	 * @param lineIndex the line index
	 * @param itextViewer the itext viewer
	 * 
	 * @return node for specified src position
	 */
	@SuppressWarnings("restriction")
    public static Node getNodeMappingBySourcePosition(ITextViewer itextViewer, int lineIndex, int linePosition) {
		int offset = getLinePositionOffcet(itextViewer, lineIndex, linePosition);
		IndexedRegion treeNode = ContentAssistUtils.getNodeAt(itextViewer, offset);
		return (Node) treeNode;
	}
	
	/**
	 * Utility function which is used to calculate offcet in document by line number and character position.
	 * 
	 * @param linePosition the line position
	 * @param textViewer the text viewer
	 * @param lineIndex the line index
	 * 
	 * @return offcet in document
	 * 
	 * @throws IllegalArgumentException 	 */
	public static final int getLinePositionOffcet(ITextViewer textViewer, int lineIndex, int linePosition) {		
		int resultOffcet = 0;
		
		if(textViewer == null) {				
			throw new IllegalArgumentException("Text viewer shouldn't be a null"); //$NON-NLS-1$
		}	
		//lineIndex-1 becose calculating of line begibns in eclipse from one, but should be form zero
		resultOffcet=textViewer.getTextWidget().getOffsetAtLine(lineIndex-1);
		//here we get's tabs length
		//for more example you can see code org.eclipse.ui.texteditor.AbstractTextEditor@getCursorPosition() and class $PositionLabelValue
		int tabWidth = textViewer.getTextWidget().getTabs();
		int characterOffset=0;
		String currentString = textViewer.getTextWidget().getLine(lineIndex-1);
		int pos=1;
		for (int i= 0; (i < currentString.length())&&(pos<linePosition); i++) {
			if ('\t' == currentString.charAt(i)) {
				
				characterOffset += (tabWidth == 0 ? 0 : 1);
				pos+=tabWidth;
			}else{
				pos++;
				characterOffset++;
			}
		}
		resultOffcet+=characterOffset;
		if(textViewer.getTextWidget().getLineAtOffset(resultOffcet)!=(lineIndex-1)) {				
			throw new IllegalArgumentException("Incorrect character position in line"); //$NON-NLS-1$
		}
		return resultOffcet;
	}
	
	public static void replaceText(JSPMultiPageEditor editor, int end, int start, String text) throws BadLocationException {
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		assertNotNull(document);
		document.replace(end, start, text);
	}
	
	public static ActionContributionItem getAction(VpvEditor visualEditor, String action) {
		for (IContributionItem ic : visualEditor.getToolBarManager().getItems()) {
			if (ic instanceof ActionContributionItem
					&& ((ActionContributionItem) ic).getAction().getText().equals(action)) {
				return (ActionContributionItem) ic;
			}
		}
		return null;
	}
}

class ControllerDisplayHelper extends DisplayHelper {
	private VpvEditorPart visualEditor;
	public ControllerDisplayHelper(VpvEditorPart visualEditor) {
		this.visualEditor = visualEditor;
	}
	
	@Override
	protected boolean condition() {
		return visualEditor.getController() != null;
	}
	
}
