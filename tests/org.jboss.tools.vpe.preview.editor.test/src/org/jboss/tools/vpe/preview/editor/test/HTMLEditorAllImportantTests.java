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
package org.jboss.tools.vpe.preview.editor.test;

import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.editor.util.VpePlatformUtil;
import org.jboss.tools.vpe.preview.editor.test.editor.OpenEditorTest;
import org.jboss.tools.vpe.preview.editor.test.editor.PreviewReloadTest;
import org.jboss.tools.vpe.preview.editor.test.editor.RefreshOptionsTest;
import org.jboss.tools.vpe.preview.editor.test.editor.VpvPreferencesTest;
import org.jboss.tools.vpe.preview.editor.test.editor.VpvToolBarTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author Konstantin Marmalyukov (kmarmaliykov)
 *
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
OpenEditorTest.class,
RefreshOptionsTest.class,
PreviewReloadTest.class,
VpvToolBarTest.class,
VpvPreferencesTest.class,
//ResourcesTest.class
})
public class HTMLEditorAllImportantTests {
	@BeforeClass
	public static void initialize() {
		// set this property to make VPE always opened as visual part
		System.setProperty(VpePlatformUtil.LOAD_DEFAULT_ENGINE, String.valueOf(true));
		WebUiPlugin.getDefault().getPreferenceStore().setValue(IVpePreferencesPage.USE_VISUAL_EDITOR_FOR_HTML5, Boolean.TRUE.toString());
	}
}
