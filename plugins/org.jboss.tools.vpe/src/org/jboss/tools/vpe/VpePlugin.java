/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.vpe;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jboss.tools.common.log.BaseUIPlugin;
import org.jboss.tools.common.log.IPluginLog;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class VpePlugin extends BaseUIPlugin {
	public static final String PLUGIN_ID = "org.jboss.tools.vpe"; //$NON-NLS-1$
	
	public static final String EXTESION_POINT_VPE_TEMPLATES = "org.jboss.tools.vpe.templates"; //$NON-NLS-1$

	private static final String EDITOR_EVENT_ACTION = "editor"; //$NON-NLS-1$
	private static final String SOURCE_EVENT_LABEL = "source"; //$NON-NLS-1$
	private static final String VPE_EVENT_LABEL = "visual-vpe"; //$NON-NLS-1$
	private static final String VPV_EVENT_LABEL = "visual-vpv"; //$NON-NLS-1$

	//The shared instance.
	private static VpePlugin plugin;

	private UsageEventType editorEventType;

	/**
	 * The constructor.
	 */
	public VpePlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("vpe activaotor");
		super.start(context);
//		moved to vpe.xulrunner plug-in
//		earlyStartup();
		editorEventType = new UsageEventType(this, EDITOR_EVENT_ACTION, Messages.UsageEventTypeEditorLabelDescription, UsageEventType.HOW_MANY_TIMES_VALUE_DESCRIPTION);
		UsageReporter.getInstance().registerEvent(editorEventType);
		System.out.println("vpe activaotor stop");
	}

	public void countSourceTabEvent() {
		UsageReporter.getInstance().countEvent(editorEventType.event(SOURCE_EVENT_LABEL));
	}

	public void countVpeTabEvent() {
		UsageReporter.getInstance().countEvent(editorEventType.event(VPE_EVENT_LABEL));
	}

	public void countVpvTabEvent() {
		UsageReporter.getInstance().countEvent(editorEventType.event(VPV_EVENT_LABEL));
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static VpePlugin getDefault() {
		return plugin;
	}

	public static void reportProblem(Exception throwable) {
		if (VpeDebug.USE_PRINT_STACK_TRACE) {
			throwable.printStackTrace();
		} 
	}
	
	public String getResourcePath(String resourceName) {
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		
		if (bundle != null) {
			URL url = bundle.getEntry(resourceName);
			
			if (url != null) {
				try {
					return FileLocator.resolve(url).getPath();
				} catch (IOException ioe) {
					logError(ioe);
				}
			}
		}
		
		return null;
	}
	
	public static IPluginLog getPluginLog() {
		return getDefault();
	}

	
	public static String getPluginResourcePath() {
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		URL url = null;
		try {
			url = bundle == null ? null : FileLocator.resolve(bundle.getEntry("/ve")); //$NON-NLS-1$
		} catch (IOException e) {
			VpePlugin.getPluginLog().logError(e);
		}
		return (url == null) ? null : url.getPath();
	}

}