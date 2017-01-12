/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.vpe.preview.core.server.VpvServer;
import org.jboss.tools.vpe.preview.core.transform.VpvController;
import org.jboss.tools.vpe.preview.core.transform.VpvDomBuilder;
import org.jboss.tools.vpe.preview.core.transform.VpvTemplateProvider;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolderRegistry;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.vpe.preview.editor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private VpvServer server;

	private VpvVisualModelHolderRegistry visualModelHolderRegistry;

	private VpvDomBuilder domBuilder;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("vpe.preview.editor activaotor");
		super.start(context);
		plugin = this;
		
		VpvTemplateProvider templateProvider = new VpvTemplateProvider();
		domBuilder = new VpvDomBuilder(templateProvider);
		visualModelHolderRegistry = new VpvVisualModelHolderRegistry();
		VpvController vpvController = new VpvController(domBuilder, visualModelHolderRegistry);
		server = new VpvServer(vpvController);
		System.out.println("vpe.preview.editor activaotor - done");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		System.out.println("vpe.preview.editor activaotor stop");
		server.stop();
		server = null;
		visualModelHolderRegistry = null;
		domBuilder = null;
		
		plugin = null;
		super.stop(context);
		System.out.println("vpe.preview.editor activaotor stop done");
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public VpvVisualModelHolderRegistry getVisualModelHolderRegistry() {
		return visualModelHolderRegistry;
	}

	public VpvDomBuilder getDomBuilder() {
		return domBuilder;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static URL getFileUrl(String path) {
		return plugin.getBundle().getEntry(path);
	}
	
	public VpvServer getServer() {
		return server;
	}
	
	public static void logError(Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
	}
	
	public static void logInfo(String info) {
		getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, info));
	}
}
