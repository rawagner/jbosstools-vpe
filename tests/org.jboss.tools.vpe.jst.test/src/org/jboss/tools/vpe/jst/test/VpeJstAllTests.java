/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.jst.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.test.util.ProjectImportTestSetup;
import org.jboss.tools.vpe.editor.util.VpePlatformUtil;
import org.jboss.tools.vpe.jst.angularjs.test.ca.AngularDynamicCATest;

/**
 * @author Alexey Kazakov
 */
public class VpeJstAllTests {

	public static Test suite() {
		// set this property to make VPE always opened as visual part
		System.out.println("suute");
		System.setProperty(VpePlatformUtil.LOAD_DEFAULT_ENGINE, String.valueOf(true));
		WebUiPlugin.getDefault().getPreferenceStore().setValue(IVpePreferencesPage.USE_VISUAL_EDITOR_FOR_HTML5, Boolean.TRUE.toString());
		
		TestSuite suite = new TestSuite(VpeJstAllTests.class.getName());

		TestSuite s = new TestSuite("Angular Dynamic CA project tests"); //$NON-NLS-1$
		s.addTestSuite(AngularDynamicCATest.class);
		suite.addTest(
				new ProjectImportTestSetup(s,
				"org.jboss.tools.vpe.jst.test", //$NON-NLS-1$
				new String[] { "projects/AngularPhonecat" }, //$NON-NLS-1$
				new String[] { "AngularPhonecat" })); //$NON-NLS-1$
		return suite;
	}
}