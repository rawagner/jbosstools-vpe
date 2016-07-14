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

import static org.jboss.tools.vpe.preview.core.preferences.VpvPreferencesInitializer.REFRESH_ON_CHANGE_PREFERENCES;
import static org.jboss.tools.vpe.preview.core.preferences.VpvPreferencesInitializer.REFRESH_ON_SAVE_PREFERENCES;
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
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.core.util.Messages;
import org.jboss.tools.vpe.preview.editor.VpvEditor;
import org.jboss.tools.vpe.preview.editor.VpvEditorController;
import org.jboss.tools.vpe.preview.editor.test.util.ActionIsEnabledCondition;
import org.jboss.tools.vpe.preview.editor.test.util.TestUtil;
import org.junit.Test;

@SuppressWarnings("restriction")
public class VpvToolBarTest extends RefreshTest {

	private static final String PROJECT_NAME = "html5-test"; //$NON-NLS-1$
	private static final String PAGE_NAME = "replace.html"; //$NON-NLS-1$

	private JSPMultiPageEditor editor;
	private VpvEditor visualEditor;
	private static IPreferenceStore preferences = WebUiPlugin.getDefault().getPreferenceStore();

	private void openTestPage() throws CoreException, IOException {
		try {
			final IFile elementPageFile = (IFile) TestUtil.getComponentPath(PAGE_NAME, PROJECT_NAME);
			editor = openEditor(elementPageFile);
			editor.pageChange(IVisualEditor.VISUALSOURCE_MODE);

			VpvEditorController controller = TestUtil.getVpvEditorController(editor);
			visualEditor = controller.getPageContext().getEditPart().getVisualEditor();
			assertNotNull(visualEditor);

			TestUtil.waitForJobs();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testPreferencesAction() throws Exception{
		openTestPage();
		ActionContributionItem action = TestUtil.getAction(visualEditor,VpeUIMessages.PREFERENCES);
		checkBasicAction(action);
	}
	
	@Test
	public void testPageDesignAction() throws Exception{
		openTestPage();
		ActionContributionItem action = TestUtil.getAction(visualEditor,VpeUIMessages.PAGE_DESIGN_OPTIONS);
		checkBasicAction(action);
	}
	
	@Test
	public void testEditorSplittingAction() throws Exception{
		openTestPage();
		ActionContributionItem action = TestUtil.getAction(visualEditor,VpeUIMessages.VISUAL_SOURCE_EDITORS_SPLITTING);
		checkBasicAction(action);
		assertEquals(VpeUIMessages.SPLITTING_VERT_TOP_SOURCE_TOOLTIP, action.getAction().getToolTipText());
		assertEquals(SWT.VERTICAL, getCurrentEditorOrientantion());
		
		action.getAction().run();
		assertTrue(action.isEnabled());
		assertEquals(VpeUIMessages.SPLITTING_HORIZ_LEFT_VISUAL_TOOLTIP, action.getAction().getToolTipText());
		assertEquals(4, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
		assertEquals(SWT.HORIZONTAL, getCurrentEditorOrientantion());
		
		action.getAction().run();
		assertTrue(action.isEnabled());
		assertEquals(VpeUIMessages.SPLITTING_VERT_TOP_VISUAL_TOOLTIP, action.getAction().getToolTipText());
		assertEquals(2, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
		assertEquals(SWT.VERTICAL, getCurrentEditorOrientantion());
		
		action.getAction().run();
		assertTrue(action.isEnabled());
		assertEquals(VpeUIMessages.SPLITTING_HORIZ_LEFT_SOURCE_TOOLTIP, action.getAction().getToolTipText());
		assertEquals(3, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
		assertEquals(SWT.HORIZONTAL, getCurrentEditorOrientantion());
		
		action.getAction().run();
		assertTrue(action.isEnabled());
		assertEquals(VpeUIMessages.SPLITTING_VERT_TOP_SOURCE_TOOLTIP, action.getAction().getToolTipText());
		assertEquals(1, preferences.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING));
		assertEquals(SWT.VERTICAL, getCurrentEditorOrientantion());
	}	
	
	@Test
	public void testShowSelectionBarAction() throws Exception{
		openTestPage();
		ActionContributionItem action = TestUtil.getAction(visualEditor,VpeUIMessages.SHOW_SELECTION_BAR);
		checkBasicAction(action);
		assertTrue(action.getAction().isChecked());
		assertTrue(preferences.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR));
		assertTrue(editor.getSelectionBar().isRealBarVisible());
		
		action.getAction().setChecked(false);
		action.getAction().run();
		assertTrue(action.isEnabled());
		assertFalse(preferences.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR));
		assertFalse(editor.getSelectionBar().isRealBarVisible());
		
		action.getAction().setChecked(true);
		action.getAction().run();
		assertTrue(action.isEnabled());
		assertTrue(preferences.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR));
		assertTrue(editor.getSelectionBar().isRealBarVisible());
	}
	
	@Test
	public void testManualRefresh() throws Exception{
		setException(new Exception("Refresh does not happen"));
		openTestPage();
		LocationListener locationListener = new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				setLocationChanged(true);
				//refresh happens
				setException(null);
			}
		};
		visualEditor.getBrowser().addLocationListener(locationListener);
		
		ActionContributionItem action = TestUtil.getAction(visualEditor,Messages.VpvView_REFRESH);
		checkBasicAction(action);
		assertEquals(Messages.VpvView_REFRESH, action.getAction().getToolTipText());
		preferences.setValue(REFRESH_ON_SAVE_PREFERENCES, false);
		preferences.setValue(REFRESH_ON_CHANGE_PREFERENCES, false);
		TestUtil.replaceText(editor, 36, 26, "Manual replacement text");
		action.getAction().run();
		waitForRefresh();
	}
	
	@Test
	public void testBackForwardAction() throws Exception{
		String newFileUrl = ((IFile) TestUtil.getComponentPath("index.html", PROJECT_NAME))
				.getLocationURI().toString();
		BrowserLocationListener browserListener = new BrowserLocationListener();
		
		openTestPage();
		visualEditor.getBrowser().addLocationListener(browserListener);
		
		ActionContributionItem backAction = TestUtil.getAction(visualEditor,Messages.VpvView_BACK);
		waitForAction(backAction, false);
		assertFalse(backAction.isEnabled());
		assertEquals(Messages.VpvView_BACK, backAction.getAction().getToolTipText());
		ActionContributionItem forwardAction = TestUtil.getAction(visualEditor,Messages.VpvView_FORWARD);
		assertFalse(forwardAction.isEnabled());
		assertEquals(Messages.VpvView_FORWARD, forwardAction.getAction().getToolTipText());
		
		visualEditor.getBrowser().setUrl(newFileUrl);
		waitForAction(backAction, true);
		assertTrue(browserListener.pageChanged());
		assertTrue(backAction.isEnabled());
		assertFalse(forwardAction.isEnabled());
		
		backAction.getAction().run();
		waitForAction(forwardAction, true);
		assertTrue(browserListener.pageChanged());
		assertFalse(backAction.isEnabled());
		assertTrue(forwardAction.isEnabled());
		
		forwardAction.getAction().run();
		waitForAction(backAction, true);
		assertTrue(browserListener.pageChanged());
		assertTrue(backAction.isEnabled());
		assertFalse(forwardAction.isEnabled());
		
	}
	
	@Test
	public void testOpenInDefaultBrowserAction() throws Exception{
		openTestPage();
		ActionContributionItem action = TestUtil.getAction(visualEditor,Messages.VpvView_OPEN_IN_DEFAULT_BROWSER);
		checkBasicAction(action);
		assertEquals(Messages.VpvView_OPEN_IN_DEFAULT_BROWSER, action.getAction().getToolTipText());
	}
	
	private void checkBasicAction(ActionContributionItem action){
		assertNotNull(action);
		assertTrue(action.isEnabled());
		assertNotNull(action.getAction().getToolTipText());
		assertFalse(action.getAction().getToolTipText().isEmpty());
	}
	
	private int getCurrentEditorOrientantion(){
		return visualEditor.getController().getPageContext().getEditPart().getContainer().getOrientation();
	}
	
	private void waitForAction(ActionContributionItem action, boolean enabled){
		ActionIsEnabledCondition actionIsEnabled = new ActionIsEnabledCondition(action,enabled);
		actionIsEnabled.waitForCondition(Display.getCurrent(), TestUtil.MAX_IDLE);
	}
	
	class BrowserLocationListener extends LocationAdapter{
		
		private boolean pageChanged = false;
		@Override
		public void changed(LocationEvent event) {
			pageChanged = true;
			setLocationChanged(true);
		}
		
		public boolean pageChanged(){
			boolean toReturn = pageChanged;
			pageChanged = false;
			setLocationChanged(false);
			return toReturn;
		}
	}

}
