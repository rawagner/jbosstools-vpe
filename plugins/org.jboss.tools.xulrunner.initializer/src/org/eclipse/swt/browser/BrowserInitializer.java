/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.eclipse.swt.browser;

import java.io.File;
import java.lang.reflect.Field;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

public class BrowserInitializer {

	private static final String PROPERTY_DEFAULTTYPE = "org.eclipse.swt.browser.DefaultType"; //$NON-NLS-1$
	private static final String SWT_GTK3 = "org.eclipse.swt.internal.gtk.version"; //$NON-NLS-1$
	static final String XULRUNNER_PATH = "org.eclipse.swt.browser.XULRunnerPath"; //$NON-NLS-1$
	public static final String LOAD_DEFAULT_ENGINE = "org.jboss.tools.vpe.engine.default"; //$NON-NLS-1$
	
	public static boolean isGTK3() {
		if (Platform.WS_GTK.equals(Platform.getWS())) {
			try {
				Class<?> clazz = Class.forName("org.eclipse.swt.internal.gtk.OS"); //$NON-NLS-1$
				Field field = clazz.getDeclaredField("GTK3"); //$NON-NLS-1$
				boolean gtk3 = field.getBoolean(field);
				return gtk3;
			} catch (ClassNotFoundException e) {
				return isGTK3Env();
			} catch (NoSuchFieldException e) {
				return false;
			} catch (SecurityException e) {
				return isGTK3Env();
			} catch (IllegalArgumentException e) {
				return isGTK3Env();
			} catch (IllegalAccessException e) {
				return isGTK3Env();
			}
		}
		return false;
	}

	private static boolean isGTK3Env() {
		String gtk3 = System.getProperty(SWT_GTK3);
		if(gtk3 == null){
			return false;
		}
		return gtk3.startsWith("3"); //$NON-NLS-1$
	}
	
	static {
		try {
			createMozillaProfileDirIfNotExist();
		} catch (SecurityException e) {
			System.out.println(NLS.bind(Messages.XULRunnerInitializer_Cannot_check_mozilla_profile_availability, getMozillaEclipseProfilePath()));
			e.printStackTrace();
		}
		
		/* Under Linux instantiation of WebKit should be avoided,
		 * because WebKit and XULRunner running simultaneously
		 * may cause native errors.
		 * 
		 * Also see JBIDE-9144, JBIDE-10185 and JBDS-2900. 	 */
		if (Platform.OS_LINUX.equals(Platform.getOS())) {
			String defaultType = System.getProperty(PROPERTY_DEFAULTTYPE);
			if (defaultType == null) {
				// XULRunner is not ported to GTK3
				if (isGTK3()) {
					defaultType = "webkit"; //$NON-NLS-1$
				} else {
					// check out browser mode. HTML5 mode is default
					String mode = Platform.getPreferencesService().getString("org.jboss.tools.jst.web.ui", "Use visual editor for html5 editing", "true", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if ("false".equals(mode) && XULRunnerInitializer.EMBEDDED_XULRUNNER_ENABLED) { //$NON-NLS-1$
						defaultType = "mozilla"; //$NON-NLS-1$
					} else {
						defaultType = getHTML5Browser();
					}					
				}
				System.setProperty(PROPERTY_DEFAULTTYPE, defaultType);
			}
		}
	}
	
	/**
	 * If XulRunner Path is predefined, we should use mozilla browser implementation
	 */
	private static String getHTML5Browser() {
		if (Boolean.valueOf(System.getProperty(LOAD_DEFAULT_ENGINE))) {
			return "webkit"; //$NON-NLS-1$
		}
		return System.getProperty(XULRUNNER_PATH) != null ? "mozilla" : "webkit"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Bug 472956 XULRunner crashes when profile path does not exist
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=472956">Bug 472956</a>
	 * @see <a href="https://issues.jboss.org/browse/JBIDE-20524">JBIDE-20524</a>
	 * @throws SecurityException
	 */
	private static void createMozillaProfileDirIfNotExist() {
		String path = getMozillaEclipseProfilePath();
		if (path != null) {
			File userHome = new File(System.getProperty("user.home")); //$NON-NLS-1$
			if (userHome.exists()) {
				File mozillaProfile = new File(userHome, path);
				if (!mozillaProfile.exists()) {
					mozillaProfile.mkdir();
				}
			}
		}
	}

	private static String getMozillaEclipseProfilePath() {
		String path = null;
		if (PlatformUtil.isWindows()) {
			path = "AppData/Roaming/Mozilla/eclipse"; //$NON-NLS-1$
		} else if (PlatformUtil.isLinux()) {
			path = ".mozilla/eclipse"; //$NON-NLS-1$
		}
		return path;
	}
}
