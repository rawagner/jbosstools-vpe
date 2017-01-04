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
package org.jboss.tools.vpe.preview.core.util;

import static org.jboss.tools.vpe.preview.core.preferences.VpvPreferencesInitializer.REFRESH_ON_CHANGE_PREFERENCES;
import static org.jboss.tools.vpe.preview.core.preferences.VpvPreferencesInitializer.REFRESH_ON_SAVE_PREFERENCES;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.jboss.tools.vpe.preview.core.Activator;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */

public abstract class ActionBar {
	private static final String GROUP_REFRESH = "org.jboss.tools.vpv.refresh"; //$NON-NLS-1$

	private IAction refreshAction;
	private IAction goBackAction;
	private IAction goForwardAction;
	private IAction openInDefaultBrowserAction;
	private IAction enableAutomaticRefreshAction;
	private IAction enableRefreshOnSaveAction;
	
	private IExecutionListener saveListener;
	private Browser browser;
	
	private IPreferenceStore preferences;
	
	private Command saveCommand;
	private Command saveAllCommand;
	
	public ActionBar(Browser browser1, IPreferenceStore preferences) {
		this.browser = browser1;
		this.preferences = preferences;
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		saveCommand = commandService.getCommand("org.eclipse.ui.file.save"); //$NON-NLS-1$
		saveAllCommand = commandService.getCommand("org.eclipse.ui.file.saveAll"); //$NON-NLS-1$
		saveListener = new IExecutionListener() {

			@Override
			public void postExecuteSuccess(String arg0, Object arg1) {
				refresh(browser);
			}

			@Override
			public void notHandled(String arg0, NotHandledException arg1) {
			}

			@Override
			public void postExecuteFailure(String arg0, ExecutionException arg1) {
			}

			@Override
			public void preExecute(String arg0, ExecutionEvent arg1) {
			}

		};
	}
	
	public void fillLocalToolBar(IToolBarManager manager) {
		makeActions();
		
		manager.add(refreshAction);
		manager.add(goBackAction);
		manager.add(goForwardAction);
		manager.add(openInDefaultBrowserAction);
		manager.add(new Separator(GROUP_REFRESH));
		manager.appendToGroup(GROUP_REFRESH, enableAutomaticRefreshAction);
		manager.appendToGroup(GROUP_REFRESH, enableRefreshOnSaveAction);
	}
	
	private void makeActions() {
		makeRefreshAction();
		makeGoBackAction();
		makeGoForwardAction();
		makeOpenInDefaultBrowserAction();
		makeEnableAutomaticRefreshAction();
		makeEnableRefreshOnSaveAction();
		
		//initialize save listeners to make refresh options work properly 
		enableAutomaticRefreshAction.run();
		enableRefreshOnSaveAction.run();
		
		if (browser != null && !browser.isDisposed()) {
			browser.addTitleListener(new TitleListener() {
				
				@Override
				public void changed(TitleEvent event) {
					goBackAction.setEnabled(browser.isBackEnabled());
					goForwardAction.setEnabled(browser.isForwardEnabled());
				}
			});
		}
	}
	
	protected abstract void refresh(Browser browser);

	private void makeEnableAutomaticRefreshAction() {
		enableAutomaticRefreshAction = new Action(Messages.VpvView_ENABLE_AUTOMATIC_REFRESH, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (enableAutomaticRefreshAction.isChecked()) {
					enableRefreshOnSaveAction.setChecked(false);
					saveCommand.removeExecutionListener(saveListener);
					saveAllCommand.removeExecutionListener(saveListener);
				}
			}
		};

		enableAutomaticRefreshAction.setChecked(preferences.getBoolean(REFRESH_ON_CHANGE_PREFERENCES));
		enableAutomaticRefreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh_on_change.png")); //$NON-NLS-1$
	}

	private void makeEnableRefreshOnSaveAction() {
		enableRefreshOnSaveAction = new Action(Messages.VpvView_ENABLE_REFRESH_ON_SAVE, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (enableRefreshOnSaveAction.isChecked()) {
					saveCommand.addExecutionListener(saveListener);
					saveAllCommand.addExecutionListener(saveListener);

					enableAutomaticRefreshAction.setChecked(false);
				} else {
					saveCommand.removeExecutionListener(saveListener);
					saveAllCommand.removeExecutionListener(saveListener);
				}
			}
		};

		enableRefreshOnSaveAction.setChecked(preferences.getBoolean(REFRESH_ON_SAVE_PREFERENCES));
		enableRefreshOnSaveAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh_on_save.png")); //$NON-NLS-1$
	}

	private void makeOpenInDefaultBrowserAction() {
		openInDefaultBrowserAction = new Action() {
			public void run() {
				URL url;
				try {
					url = new URL(browser.getUrl()); // validate URL (to do not open 'about:blank' and similar)
					Program.launch(url.toString());
				} catch (MalformedURLException e) {
					Activator.logError(e);
				}
			}
		};

		openInDefaultBrowserAction.setText(Messages.VpvView_OPEN_IN_DEFAULT_BROWSER);
		openInDefaultBrowserAction.setToolTipText(Messages.VpvView_OPEN_IN_DEFAULT_BROWSER);
		openInDefaultBrowserAction.setImageDescriptor(Activator.getImageDescriptor("icons/open_in_default_browser.gif")); //$NON-NLS-1$
	}

	private void makeRefreshAction() {
		refreshAction = new Action() {
			public void run() {
				refresh(browser);
			}
		};
		refreshAction.setText(Messages.VpvView_REFRESH);
		refreshAction.setToolTipText(Messages.VpvView_REFRESH);
		refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh.gif")); //$NON-NLS-1$
	}
	
	private void makeGoBackAction() {
		goBackAction = new Action() {
			public void run() {
				if (browser.isBackEnabled()) {
					browser.back();
				}
			}
		};
		goBackAction.setText(Messages.VpvView_BACK);
		goBackAction.setToolTipText(Messages.VpvView_BACK);
		goBackAction.setImageDescriptor(Activator.getImageDescriptor("icons/nav_backward.gif")); //$NON-NLS-1$
	}
	
	private void makeGoForwardAction() {
		goForwardAction = new Action() {
			public void run() {
				if (browser.isForwardEnabled()) {
					browser.forward();
				}
			}
		};
		goForwardAction.setText(Messages.VpvView_FORWARD);
		goForwardAction.setToolTipText(Messages.VpvView_FORWARD);
		goForwardAction.setImageDescriptor(Activator.getImageDescriptor("icons/nav_forward.gif")); //$NON-NLS-1$
	}
	
	public boolean isAutomaticRefreshEnabled() {
		return enableAutomaticRefreshAction.isChecked();
	}
	
	public void updateRefreshItemsAccordingToPreferences() {
		enableRefreshOnSaveAction.setChecked(preferences.getBoolean(REFRESH_ON_SAVE_PREFERENCES));
		enableRefreshOnSaveAction.run();
		enableAutomaticRefreshAction.setChecked(preferences.getBoolean(REFRESH_ON_CHANGE_PREFERENCES));
		enableAutomaticRefreshAction.run();
	}
	
	public void dispose() {
		saveCommand.removeExecutionListener(saveListener);
		saveAllCommand.removeExecutionListener(saveListener);
		
		preferences.setValue(REFRESH_ON_CHANGE_PREFERENCES, enableAutomaticRefreshAction.isChecked());
		preferences.setValue(REFRESH_ON_SAVE_PREFERENCES, enableRefreshOnSaveAction.isChecked());
	}
}
