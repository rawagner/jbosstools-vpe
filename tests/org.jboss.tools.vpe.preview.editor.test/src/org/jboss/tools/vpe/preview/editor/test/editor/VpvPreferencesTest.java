/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor.test.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.editor.VpvEditor;
import org.jboss.tools.vpe.preview.editor.VpvEditorController;
import org.jboss.tools.vpe.preview.editor.VpvEditorPart;
import org.jboss.tools.vpe.preview.editor.test.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class VpvPreferencesTest extends VpvTest{
	
	private static final String PROJECT_NAME = "html5-test"; //$NON-NLS-1$
	private static final String PAGE_NAME = "replace.html"; //$NON-NLS-1$
	
	private static IPreferenceStore preferences;
	private static int defaultOrientation;
	private JSPMultiPageEditor editor;
	private VpvEditorPart visualEditorPart;
	private VpvEditor visualEditor;
	
	@BeforeClass
	public static void getDefaultEditorOrientation(){
		preferences = WebUiPlugin.getDefault().getPreferenceStore();
		defaultOrientation = Integer.parseInt(preferences.getDefaultString(
				IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
		
	}
	
	@Before
	public void openTestPage() throws CoreException, IOException {
		try {
			final IFile elementPageFile = (IFile) TestUtil.getComponentPath(PAGE_NAME, PROJECT_NAME);
			editor = openEditor(elementPageFile);
			
			VpvEditorController controller = TestUtil.getVpvEditorController(editor);
			visualEditorPart = controller.getPageContext().getEditPart();
			visualEditor = visualEditorPart.getVisualEditor();
			assertNotNull(visualEditor);

			TestUtil.waitForJobs();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEditorSplittingPreferences() throws Exception {
		try{
			for(int i=1; i<=4; i++){
				preferences.setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, new Integer(i).toString());
				visualEditorPart.updatePartAccordingToPreferences();
				ActionContributionItem action = TestUtil.getAction(visualEditor, VpeUIMessages.VISUAL_SOURCE_EDITORS_SPLITTING);
				switch(i){
				case 1: assertEquals(SWT.VERTICAL, getCurrentEditorOrientantion()); 
						assertEquals(1, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
						assertEquals(VpeUIMessages.SPLITTING_VERT_TOP_SOURCE_TOOLTIP, action.getAction().getToolTipText());
						break;
				case 2: assertEquals(SWT.VERTICAL, getCurrentEditorOrientantion()); 
						assertEquals(2, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
						assertEquals(VpeUIMessages.SPLITTING_VERT_TOP_VISUAL_TOOLTIP, action.getAction().getToolTipText());
						break;
				case 3: assertEquals(SWT.HORIZONTAL, getCurrentEditorOrientantion()); 
						assertEquals(3, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
						assertEquals(VpeUIMessages.SPLITTING_HORIZ_LEFT_SOURCE_TOOLTIP, action.getAction().getToolTipText());
						break;
				case 4: assertEquals(SWT.HORIZONTAL, getCurrentEditorOrientantion()); 
						assertEquals(4, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
						assertEquals(VpeUIMessages.SPLITTING_HORIZ_LEFT_VISUAL_TOOLTIP, action.getAction().getToolTipText());
						break;
				}
			}
		} finally {
			preferences.setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, 
					new Integer(defaultOrientation).toString());
		}
	}
	
	@Test
	public void showSelectionBarPreferences() throws Exception {
		preferences.setValue(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR, false);
		visualEditorPart.updatePartAccordingToPreferences();
		ActionContributionItem action = TestUtil.getAction(visualEditor,VpeUIMessages.SHOW_SELECTION_BAR);
		assertFalse(action.getAction().isChecked());
		assertFalse(editor.getSelectionBar().isRealBarVisible());
		
		preferences.setValue(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR, true);
		visualEditorPart.updatePartAccordingToPreferences();
		assertTrue(action.getAction().isChecked());
		assertTrue(editor.getSelectionBar().isRealBarVisible());		
	}
	
	@Test
	public void setDefaultTabPreferences() throws Exception{
		preferences.setValue(IVpePreferencesPage.DEFAULT_VPE_TAB, IVpePreferencesPage.DEFAULT_VPE_TAB_SOURCE_VALUE);
		visualEditorPart.updatePartAccordingToPreferences();
		//page should not be changed yet
		assertEquals(Integer.parseInt(IVpePreferencesPage.DEFAULT_VPE_TAB_VISUAL_SOURCE_VALUE), editor.getSelectedPageIndex());
		closeEditors();
		openEditor();
		assertEquals(Integer.parseInt(IVpePreferencesPage.DEFAULT_VPE_TAB_SOURCE_VALUE), editor.getSelectedPageIndex());
		
		preferences.setValue(IVpePreferencesPage.DEFAULT_VPE_TAB, IVpePreferencesPage.DEFAULT_VPE_TAB_PREVIEW_VALUE);
		visualEditorPart.updatePartAccordingToPreferences();
		//page should not be changed yet
		assertEquals(Integer.parseInt(IVpePreferencesPage.DEFAULT_VPE_TAB_SOURCE_VALUE), editor.getSelectedPageIndex());
		closeEditors();
		openEditor();
		assertEquals(Integer.parseInt(IVpePreferencesPage.DEFAULT_VPE_TAB_PREVIEW_VALUE), editor.getSelectedPageIndex());
		
		preferences.setValue(IVpePreferencesPage.DEFAULT_VPE_TAB, IVpePreferencesPage.DEFAULT_VPE_TAB_VISUAL_SOURCE_VALUE);
		visualEditorPart.updatePartAccordingToPreferences();
		//page should not be changed yet
		assertEquals(Integer.parseInt(IVpePreferencesPage.DEFAULT_VPE_TAB_PREVIEW_VALUE), editor.getSelectedPageIndex());
		closeEditors();
		openEditor();
		assertEquals(Integer.parseInt(IVpePreferencesPage.DEFAULT_VPE_TAB_VISUAL_SOURCE_VALUE), editor.getSelectedPageIndex());
		
	}
	
	@Test
	public void testSizeOfVisualPane(){
		preferences.setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS, 0);
		visualEditorPart.updatePartAccordingToPreferences();
		assertEquals(0,visualEditorPart.getContainer().getWeights()[1]);
		
		preferences.setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS, 1000);
		visualEditorPart.updatePartAccordingToPreferences();
		assertEquals(0,visualEditorPart.getContainer().getWeights()[0]);
		
		preferences.setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS, 500);
		visualEditorPart.updatePartAccordingToPreferences();
		assertEquals(visualEditorPart.getContainer().getWeights()[0], 
				visualEditorPart.getContainer().getWeights()[1]);
	}
	
	private int getCurrentEditorOrientantion(){
		return visualEditor.getController().getPageContext().getEditPart().getContainer().getOrientation();
	}
	
	private void openEditor() throws Exception{
		IFile elementPageFile = (IFile) TestUtil.getComponentPath(PAGE_NAME, PROJECT_NAME);
		editor = openEditor(elementPageFile);
		TestUtil.getVpvEditorController(editor);
	}
	
	


}
