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

import static org.jboss.tools.vpe.preview.core.server.HttpConstants.ABOUT_BLANK;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.editor.mozilla.MozillaEditor;
import org.jboss.tools.vpe.editor.mozilla.listener.EditorLoadWindowListener;
import org.jboss.tools.vpe.editor.preferences.VpeEditorPreferencesPage;
import org.jboss.tools.vpe.editor.preferences.VpeResourcesDialogFactory;
import org.jboss.tools.vpe.editor.toolbar.IVpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.jboss.tools.vpe.editor.util.FileUtil;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.core.exceptions.BrowserErrorWrapper;
import org.jboss.tools.vpe.preview.core.exceptions.CannotOpenExternalFileException;
import org.jboss.tools.vpe.preview.core.exceptions.Messages;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolder;
import org.jboss.tools.vpe.preview.core.util.ActionBar;
import org.jboss.tools.vpe.preview.core.util.EditorUtil;
import org.jboss.tools.vpe.preview.core.util.NavigationUtil;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;
import org.jboss.tools.vpe.preview.editor.context.VpvPageContext;
import org.w3c.dom.Node;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */
public class VpvEditor extends DocumentListeningEditorPart implements VpvVisualModelHolder, IReusableEditor{
	/*
	 * Paths for tool bar icons
	 */
	public static final String ICON_PREFERENCE = "icons/preference.gif"; //$NON-NLS-1$
	public static final String ICON_PREFERENCE_DISABLED = "icons/preference_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_REFRESH = "icons/refresh.gif"; //$NON-NLS-1$
	public static final String ICON_REFRESH_DISABLED = "icons/refresh_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_PAGE_DESIGN_OPTIONS = "icons/point_to_css.gif"; //$NON-NLS-1$
	public static final String ICON_PAGE_DESIGN_OPTIONS_DISABLED = "icons/point_to_css_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_SOURCE_LEFT = "icons/source_left.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_SOURCE_TOP = "icons/source_top.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_VISUAL_LEFT = "icons/visual_left.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_VISUAI_TOP = "icons/visual_top.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_SOURCE_LEFT_DISABLED = "icons/source_left_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_SELECTION_BAR = "icons/selbar.gif"; //$NON-NLS-1$
	public static final String ICON_TEXT_FORMATTING = "icons/text-formatting.gif"; //$NON-NLS-1$
	public static final String ICON_SCROLL_LOCK= "icons/scroll_lock.gif"; //$NON-NLS-1$
	
	private static Map<String, String> layoutIcons;
	private static Map<String, String> layoutNames;
	private static List<String> layoutValues;
	private int currentOrientationIndex = 1;
	private Action openVPEPreferencesAction;
	private Action showResouceDialogAction;
	private Action rotateEditorsAction;
	private Action showSelectionBarAction;
	static {
		/*
		 * Values from <code>layoutValues</code> should correspond to the order
		 * when increasing the index of the array will cause 
		 * the source editor rotation 
		 */
	    layoutIcons = new HashMap<String, String>();
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_SOURCE_VALUE, ICON_ORIENTATION_SOURCE_LEFT);
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE, ICON_ORIENTATION_SOURCE_TOP);
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_VISUAL_VALUE, ICON_ORIENTATION_VISUAL_LEFT);
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_VERT_TOP_VISUAL_VALUE, ICON_ORIENTATION_VISUAI_TOP);
	    
	    layoutNames = new HashMap<String, String>();
	    layoutNames.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_SOURCE_VALUE, VpeUIMessages.SPLITTING_HORIZ_LEFT_SOURCE_TOOLTIP);
	    layoutNames.put(IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE, VpeUIMessages.SPLITTING_VERT_TOP_SOURCE_TOOLTIP);
	    layoutNames.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_VISUAL_VALUE, VpeUIMessages.SPLITTING_HORIZ_LEFT_VISUAL_TOOLTIP);
	    layoutNames.put(IVpePreferencesPage.SPLITTING_VERT_TOP_VISUAL_VALUE, VpeUIMessages.SPLITTING_VERT_TOP_VISUAL_TOOLTIP);

	    layoutValues= new ArrayList<String>();
	    layoutValues.add(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_SOURCE_VALUE);
	    layoutValues.add(IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE);
	    layoutValues.add(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_VISUAL_VALUE);
	    layoutValues.add(IVpePreferencesPage.SPLITTING_VERT_TOP_VISUAL_VALUE);

	}
	
	private final ToolBarManager toolBarManager = new ToolBarManager(SWT.VERTICAL | SWT.FLAT);
	private FormatControllerManager formatControllerManager = new FormatControllerManager();
	private VpvEditorController controller;
	private ToolBar verBar = null;
	private IPropertyChangeListener selectionBarCloseListener;
	
	private IVpeToolBarManager vpeToolBarManager;
	
	private EditorLoadWindowListener editorLoadWindowListener;
	
	private Browser browser;
	private VpvVisualModel visualModel;
	private int modelHolderId;
	private SelectionListener selectionListener;
	protected IEditorPart sourceEditor;
	
	private ActionBar actionBar;
	protected BrowserErrorWrapper errorWrapper = new BrowserErrorWrapper();
	
	public VpvEditor() {
	}
	
	public VpvEditor(IEditorPart sourceEditor) {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
		setSourceEditor(sourceEditor);
	}
	
	@Override
	public void doSave(IProgressMonitor arg0) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.setSite(site);
		super.setInput(input);
	}

	public void setInput(IEditorInput input) {
		boolean isVisualRefreshRequired = (getEditorInput() != null && getEditorInput() != input && controller != null);
		super.setInput(input);
		if(isVisualRefreshRequired) controller.visualRefresh();
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setController(VpvEditorController controller){
		this.controller = controller;
		formatControllerManager.setVpeController(controller);
		controller.setToolbarFormatControllerManager(formatControllerManager);
	}
	
	public ToolBar createVisualToolbar(Composite parent) {
		verBar = toolBarManager.createControl(parent);
		
		/*
		 * Create OPEN VPE PREFERENCES tool bar item
		 */
		openVPEPreferencesAction = new Action(VpeUIMessages.PREFERENCES,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				VpeEditorPreferencesPage.openPreferenceDialog();
			}
		};
		openVPEPreferencesAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_PREFERENCE));
		openVPEPreferencesAction.setToolTipText(VpeUIMessages.PREFERENCES);
		toolBarManager.add(openVPEPreferencesAction);
		
		/*
		 * Create SHOW RESOURCE DIALOG tool bar item
		 * 
		 * https://jira.jboss.org/jira/browse/JBIDE-3966
		 * Disabling Page Design Options for external files. 
		 */
		IEditorInput input = getEditorInput();
		IFile file = null;
		if (input instanceof IFileEditorInput) {
			file = ((IFileEditorInput) input).getFile();
		} else if (input instanceof ILocationProvider) {
			ILocationProvider provider = (ILocationProvider) input;
			IPath path = provider.getPath(input);
			if (path != null) {
			    file = FileUtil.getFile(input, path.lastSegment());
			}
		}
		boolean fileExistsInWorkspace = ((file != null) && (file.exists()));
		showResouceDialogAction = new Action(VpeUIMessages.PAGE_DESIGN_OPTIONS,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				VpeResourcesDialogFactory.openVpeResourcesDialog(VpvEditor.this);
			}
		};
		showResouceDialogAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				fileExistsInWorkspace ? ICON_PAGE_DESIGN_OPTIONS : ICON_PAGE_DESIGN_OPTIONS_DISABLED));
		if (!fileExistsInWorkspace) {
			showResouceDialogAction.setEnabled(false);
		}
		showResouceDialogAction.setToolTipText(VpeUIMessages.PAGE_DESIGN_OPTIONS);
		toolBarManager.add(showResouceDialogAction);
		
		
		/*
		 * Create ROTATE EDITORS tool bar item
		 * 
		 * https://jira.jboss.org/jira/browse/JBIDE-4152
		 * Compute initial icon state and add it to the tool bar.
		 */
		String newOrientation = WebUiPlugin
		.getDefault().getPreferenceStore().getString(
				IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING);
		currentOrientationIndex = layoutValues.indexOf(newOrientation);
		rotateEditorsAction = new Action(
				VpeUIMessages.VISUAL_SOURCE_EDITORS_SPLITTING,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				/*
				 * Rotate editors orientation clockwise.
				 */
		    	currentOrientationIndex++;
				if (currentOrientationIndex >= layoutValues.size()) {
					currentOrientationIndex = currentOrientationIndex % layoutValues.size();
				}
				String newOrientation = layoutValues.get(currentOrientationIndex);
				/*
				 * Update icon and tooltip
				 */
				this.setImageDescriptor(ImageDescriptor.createFromFile(
						MozillaEditor.class, layoutIcons.get(newOrientation)));
				
				this.setToolTipText(layoutNames.get(newOrientation));
				/*
				 * Call <code>filContainer()</code> from VpeEditorPart
				 * to redraw CustomSashForm with new layout.
				 */
				((VpvEditorPart)getController().getPageContext().getEditPart()).fillContainer(true, newOrientation);
				WebUiPlugin.getDefault().getPreferenceStore().
					setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, newOrientation);
			}
		};
		rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				layoutIcons.get(newOrientation)));
		rotateEditorsAction.setToolTipText(layoutNames.get(newOrientation));
		toolBarManager.add(rotateEditorsAction);
		
		/*
		 * Create SHOW SELECTION BAR tool bar item
		 */
		showSelectionBarAction = new Action(VpeUIMessages.SHOW_SELECTION_BAR,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Update Selection Bar 
				 */
				if(controller != null){
					VpvPageContext vpvPageContext = controller.getPageContext();
					if(vpvPageContext != null && vpvPageContext.getEditPart() != null){
						vpvPageContext.getEditPart().updateSelectionBar(this.isChecked());
					}
				}
				WebUiPlugin.getDefault().getPreferenceStore().
					setValue(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR, this.isChecked());
			}
		};
		
		selectionBarCloseListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				/*
				 * Change icon state after sel bar was closed
				 */
				if (IVpePreferencesPage.SHOW_SELECTION_TAG_BAR.equalsIgnoreCase(event.getProperty())) {
					boolean newValue = (Boolean) event.getNewValue();
					if (showSelectionBarAction.isChecked() != newValue) {
						showSelectionBarAction.setChecked(newValue);
					}
				}
			}
		};
		WebUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(selectionBarCloseListener);
		
		showSelectionBarAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_SELECTION_BAR));
		showSelectionBarAction.setToolTipText(VpeUIMessages.SHOW_SELECTION_BAR);
		toolBarManager.add(showSelectionBarAction);

		updateToolbarItemsAccordingToPreferences();
		toolBarManager.update(true);

		parent.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				toolBarManager.dispose();
				toolBarManager.removeAll();
				openVPEPreferencesAction = null;
				rotateEditorsAction = null;;
				showSelectionBarAction = null;
			}
		});
		return verBar;
	}
	
	public void addPreviewToolbarItems() {
		actionBar = new VisualEditorActionBar(browser, Activator.getDefault().getPreferenceStore(), controller);
		actionBar.fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2,false);
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		layout.verticalSpacing = 2;
		layout.horizontalSpacing = 2;
		layout.marginBottom = 0;
		parent.setLayout(layout);

		// Editors and Toolbar composite 
		Composite cmpEdTl = new Composite(parent, SWT.NONE);
		GridLayout layoutEdTl = new GridLayout(1, false);
		layoutEdTl.verticalSpacing = 0;
		layoutEdTl.marginHeight = 0;
		layoutEdTl.marginBottom = 3;
		layoutEdTl.marginWidth = 0;
		cmpEdTl.setLayout(layoutEdTl);
		cmpEdTl.setLayoutData(new GridData(GridData.FILL_BOTH));

		/*
		 * https://jira.jboss.org/jira/browse/JBIDE-4429
		 * Toolbar was moved to VpeEditorPart.
		 *  'verBar' should be created in createVisualToolbar(..) in VpeEditorPart
		 *  and only after that MozillaEditor should be created itself. 
		 */
//		if (null != verBar) {
//			// Use vpeToolBarManager to create a horizontal toolbar.
//			vpeToolBarManager = new VpeToolBarManager();
//			if (vpeToolBarManager != null) {
//				vpeToolBarManager.createToolBarComposite(cmpEdTl);
//				vpeToolBarManager.addToolBar(new TextFormattingToolBar(formatControllerManager));
//			}
//		}

		//Create a composite to the Editor
		final Composite cmpEd = new Composite (cmpEdTl, SWT.NATIVE);
		GridLayout layoutEd = new GridLayout(1, false);
		layoutEd.marginBottom = 0;
		layoutEd.marginHeight = 1;
		layoutEd.marginWidth = 0;
		layoutEd.marginRight = 0;
		layoutEd.marginLeft = 1;
		layoutEd.verticalSpacing = 0;
		layoutEd.horizontalSpacing = 0;
		cmpEd.setLayout(layoutEd);
		cmpEd.setLayoutData(new GridData(GridData.FILL_BOTH));

		//TODO Add a paintListener to cmpEd and give him a border top and left only
		Color buttonDarker = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		cmpEd.setBackground(buttonDarker);

		try {
			browser = new Browser(cmpEd, SWT.NONE);
			browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			browser.addLocationListener(new LocationAdapter() {
				@Override
				public void changed(LocationEvent event) {
					NavigationUtil.disableJsPopUps(browser);
					NavigationUtil.disableLinks(browser);
					NavigationUtil.disableInputs(browser);
					
					ISelection currentSelection = getCurrentSelection();
					NavigationUtil.updateSelectionAndScrollToIt(currentSelection, browser, visualModel);
				}
			});
			
			browser.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent event) {
					NavigationUtil.navigateToVisual(sourceEditor, browser, visualModel, event.x, event.y);
				}
				
			});
			
			if (editorLoadWindowListener != null) {
				editorLoadWindowListener.load();
			}
			
			inizializeSelectionListener();
		} catch (Throwable t) {
			//cannot create browser. show error message then
			
			/*
			 * Disable VPE toolbar
			 */
			if (verBar != null) {
				verBar.setEnabled(false);
			}
			errorWrapper.showError(cmpEd, t);
		}

	}
	
	@Override
	public void setFocus() {
		if (browser != null) {
			browser.setFocus();
		}
	}
	
	public void dispose() {
		removeDocumentListener(sourceEditor);
		if (selectionBarCloseListener != null) {
			WebUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(selectionBarCloseListener);
		}
		if (selectionListener != null) {
			getSite().getPage().removePostSelectionListener(selectionListener);
		}
		Activator.getDefault().getVisualModelHolderRegistry().unregisterHolder(this);
		
		if (vpeToolBarManager != null) {
			vpeToolBarManager.dispose();
			vpeToolBarManager = null;
		}
		
		if(controller != null) {
			controller.dispose();
			controller = null;
		}
		
		if (actionBar != null) {
			actionBar.dispose();
		}
		
		if (browser != null) {
			browser.dispose();
			browser = null;
		}

		formatControllerManager.setVpeController(null);
		formatControllerManager=null;
		super.dispose();
	}

	public void updateToolbarItemsAccordingToPreferences() {
		String prefsOrientation = WebUiPlugin
		.getDefault().getPreferenceStore().getString(
				IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING);
		int prefsOrientationIndex = layoutValues.indexOf(prefsOrientation);
		
		boolean prefsShowSelectionBar = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR);
		
		if (showSelectionBarAction != null) {
			showSelectionBarAction.setChecked(prefsShowSelectionBar);
			showSelectionBarAction.run();
		}
		if (rotateEditorsAction != null) {
			currentOrientationIndex = prefsOrientationIndex;
			rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(
					MozillaEditor.class, layoutIcons.get(prefsOrientation)));
			rotateEditorsAction.setToolTipText(layoutNames.get(prefsOrientation));
		}
	}

	public Long getCurrentSelectedElementId() {
		if(sourceEditor!=null) {
			ISelection currentSelection = sourceEditor.getEditorSite().getSelectionProvider().getSelection();
			if(currentSelection!=null) {
				Node sourceNode = EditorUtil.getNodeFromSelection((IStructuredSelection) currentSelection);
				Long currentSelectionId = NavigationUtil.getIdForSelection(sourceNode, visualModel);
				if(currentSelectionId!=null) {
					return currentSelectionId;
				}
			}
		}
		return null;
	}

	public void refresh() {
		if (browser != null && !browser.isDisposed()) { 
			if (isURLDefault(browser.getUrl())) {
				formRequestToServer();
			} else {
				browser.setUrl(NavigationUtil.fixUrl(browser));
			}
		}
	}

	/** For IE and WebKit default URL is about:blank
	 *  For XulRunner 1.9 which we ship default URL is empty string.
	 * 
	 * @return <code>true</code> if URL is default
	 */
	private boolean isURLDefault(String url) {
		return ABOUT_BLANK.equals(url) || "".equals(url); //$NON-NLS-1$
	}
	
	/**
     * @return the controller
     */
	public VpvEditorController getController() {
		return controller;
	}

	@Override
	public void setVisualModel(VpvVisualModel visualModel) {
		this.visualModel = visualModel;
	}
	
	public void setModelHolderId(int modelHolderId) {
		this.modelHolderId = modelHolderId;
	}
	
	public void setBrowser(Browser browser) {
		this.browser = browser;
	}
	
	public Browser getBrowser() {
		return browser;
	}
	
	public void setEditorLoadWindowListener(EditorLoadWindowListener listener) {
		editorLoadWindowListener = listener;
	}
	
	/**
	 * @param sourceEditor the sourceEditor to set
	 */
	protected void setSourceEditor(IEditorPart sourceEditor) {
		this.sourceEditor = sourceEditor;
		addDocumentListener(sourceEditor);
	}
	
	private void inizializeSelectionListener() {
		selectionListener = new SelectionListener();
		getSite().getPage().addPostSelectionListener(selectionListener);	
	}
	
	private ISelection getCurrentSelection() {
		Activator activator = Activator.getDefault();
		IWorkbench workbench = activator.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = workbenchWindow.getSelectionService();
		ISelection selection = selectionService.getSelection();
		return selection;
	}
	
	protected void formRequestToServer() {
        IFile ifile = EditorUtil.getFileOpenedInEditor(sourceEditor);
        if (ifile != null) {
            if (SuitableFileExtensions.contains(ifile.getFileExtension().toString())) {
                try {
                    String url = EditorUtil.formUrl(ifile, modelHolderId, Integer.toString(Activator.getDefault().getServer().getPort()));
                    browser.setUrl(url);
                } catch (UnsupportedEncodingException e) {
                    Activator.logError(e);
                }
            }
        } else {
            if (sourceEditor.getEditorInput() instanceof IPathEditorInput) { //handle external file
                try {
                    IPath externalFile = ((IPathEditorInput) sourceEditor.getEditorInput()).getPath();
                    String url = EditorUtil.formUrl(externalFile, modelHolderId, Integer.toString(Activator.getDefault().getServer().getPort()));
                    browser.setUrl(url);
                } catch (UnsupportedEncodingException e) {
                    Activator.logError(e);
                }
            } else {
                Composite parent = browser.getParent();
                browser.dispose();
                browser = null;
                errorWrapper.showError(
                        parent,
                        new CannotOpenExternalFileException(MessageFormat
                                .format(Messages.CANNOT_SHOW_EXTERNAL_FILE,
                                        VpeUIMessages.VISUAL_EDITOR)));
            }
        }
	}

	public ActionBar getActionBar() {
		return actionBar;
	}
	
	public ToolBarManager getToolBarManager(){
		return toolBarManager;
	}
	
	private class SelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection && EditorUtil.isInCurrentEditor((IStructuredSelection) selection, sourceEditor)) {
				NavigationUtil.updateSelectionAndScrollToIt(selection, browser, visualModel);
			}
		}
	}

	@Override
	protected void performAction() {
		refresh();
	}

	@Override
	protected boolean actionHappening() {
		return actionBar.isAutomaticRefreshEnabled() && controller.isVisualEditorVisible();
	}
}
