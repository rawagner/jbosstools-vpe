package org.jboss.tools.vpe.editor.util;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.Platform;
import org.jboss.tools.vpe.preview.core.util.PlatformUtil;

public class VpePlatformUtil {
	public static final String LOAD_XULRUNNER_ENGINE = "org.jboss.tools.vpe.engine.xulrunner"; //$NON-NLS-1$
	public static final String LOAD_DEFAULT_ENGINE = "org.jboss.tools.vpe.engine.default"; //$NON-NLS-1$
	private static final String SWT_GTK3 = "org.eclipse.swt.internal.gtk.version"; //$NON-NLS-1$
	/* XXX: these constants are duplicated
	 * in XULRunnerInitializer, see JBIDE-9188 */
	private static final String LOAD_XULRUNNER_SYSTEM_PROPERTY = "org.jboss.tools.vpe.loadxulrunner";//$NON-NLS-1$
	public static boolean EMBEDDED_XULRUNNER_ENABLED = !"false".equals(System.getProperty(LOAD_XULRUNNER_SYSTEM_PROPERTY)); //$NON-NLS-1$
	
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
	
	/**
	 * @return true if Xulrunner is forced <b>for tests</b>
	 */
	public static boolean isXulrunnerForced() {
		return Boolean.valueOf(System.getProperty(LOAD_XULRUNNER_ENGINE));
	}
	
	/**
	 * @return true if system default browser is forced <b>for tests</b>
	 */
	public static boolean isDefaultBrowserForced() {
		return Boolean.valueOf(System.getProperty(LOAD_DEFAULT_ENGINE));
	}
	
	/** 
	 * Xulrunner can be loaded on Linux if Eclipse started against GTK2 and Xulrunner is not disabled by option
	 * org.jboss.tools.vpe.loadxulrunner=false
	 * 
	 * @return true if Xulrunner can be loaded
	 */
	public static boolean xulrunnerCanBeLoadedOnLinux() {
		return PlatformUtil.isLinux() && EMBEDDED_XULRUNNER_ENABLED && !VpePlatformUtil.isGTK3();
	}
}
