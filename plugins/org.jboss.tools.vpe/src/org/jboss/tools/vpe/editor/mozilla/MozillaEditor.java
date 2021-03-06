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
package org.jboss.tools.vpe.editor.mozilla;

import static org.jboss.tools.vpe.xulrunner.util.XPCOM.queryInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.EditorPart;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.i18n.ExternalizeStringsDialog;
import org.jboss.tools.jst.web.ui.internal.editor.i18n.ExternalizeStringsUtils;
import org.jboss.tools.jst.web.ui.internal.editor.i18n.ExternalizeStringsWizard;
import org.jboss.tools.jst.web.ui.internal.editor.messages.JstUIMessages;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.VpeController;
import org.jboss.tools.vpe.editor.VpeEditorPart;
import org.jboss.tools.vpe.editor.mozilla.listener.EditorLoadWindowListener;
import org.jboss.tools.vpe.editor.mozilla.listener.MozillaResizeListener;
import org.jboss.tools.vpe.editor.mozilla.listener.MozillaTooltipListener;
import org.jboss.tools.vpe.editor.preferences.VpeEditorPreferencesPage;
import org.jboss.tools.vpe.editor.preferences.VpeResourcesDialogFactory;
import org.jboss.tools.vpe.editor.toolbar.IVpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.VpeDropDownMenu;
import org.jboss.tools.vpe.editor.toolbar.VpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.jboss.tools.vpe.editor.toolbar.format.TextFormattingToolBar;
import org.jboss.tools.vpe.editor.util.DocTypeUtil;
import org.jboss.tools.vpe.editor.util.FileUtil;
import org.jboss.tools.vpe.editor.util.HTML;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.core.exceptions.XulRunnerErrorWrapper;
import org.jboss.tools.vpe.xulrunner.editor.XulRunnerEditor;
import org.jboss.tools.vpe.xulrunner.util.XPCOM;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMNamedNodeMap;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIDOMNodeList;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIEditingSession;
import org.mozilla.interfaces.nsIEditor;
import org.mozilla.interfaces.nsIHTMLAbsPosEditor;
import org.mozilla.interfaces.nsIHTMLInlineTableEditor;
import org.mozilla.interfaces.nsIHTMLObjectResizer;
import org.mozilla.interfaces.nsIPlaintextEditor;

public class MozillaEditor extends EditorPart implements IReusableEditor {
	/**
	 * 
	 */
	protected static final File INIT_FILE = new File(VpePlugin.getDefault().getResourcePath("ve"), "init.html"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String CONTENT_AREA_ID = "__content__area__"; //$NON-NLS-1$
	
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
	public static final String ICON_SHOW_BORDER_FOR_UNKNOWN_TAGS = "icons/border.gif"; //$NON-NLS-1$
	public static final String ICON_NON_VISUAL_TAGS = "icons/non-visusal-tags.gif"; //$NON-NLS-1$
	public static final String ICON_SELECTION_BAR = "icons/selbar.gif"; //$NON-NLS-1$
	public static final String ICON_TEXT_FORMATTING = "icons/text-formatting.gif"; //$NON-NLS-1$
	public static final String ICON_BUNDLE_AS_EL= "icons/bundle-as-el.gif"; //$NON-NLS-1$
	public static final String ICON_SCROLL_LOCK= "icons/scroll_lock.gif"; //$NON-NLS-1$
	public static final String ICON_EXTERNALIZE_STRINGS= "icons/externalize.png"; //$NON-NLS-1$
	
	private XulRunnerEditor xulRunnerEditor;
	private nsIDOMElement contentArea;
	private nsIDOMNode headNode;
	private MozillaEventAdapter mozillaEventAdapter = createMozillaEventAdapter();

	private EditorLoadWindowListener editorLoadWindowListener;

	private IVpeToolBarManager vpeToolBarManager;
	private FormatControllerManager formatControllerManager = new FormatControllerManager();
	private VpeController controller;
	private boolean isRefreshPage = false;
	private String doctype;
	
	private static Map<String, String> layoutIcons;
	private static Map<String, String> layoutNames;
	private static List<String> layoutValues;
	private int currentOrientationIndex = 1;
	private Action openVPEPreferencesAction;
	private Action visualRefreshAction;
	private Action showResouceDialogAction;
	private Action rotateEditorsAction;
	private Action showBorderAction;
	private Action showNonVisualTagsAction;
	private Action showSelectionBarAction;
	private Action showTextFormattingAction;
	private Action showBundleAsELAction;
	private Action scrollLockAction;
	private Action externalizeStringsAction;
	
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
	
	/**
	 * Used for manupalation of browser in design mode,
	 * for example enable or disable readOnlyMode
	 */
	private nsIEditor editor;
	private VpeDropDownMenu dropDownMenu = null;
	private ToolBar verBar = null;
	private MozillaResizeListener resizeListener;
	private MozillaTooltipListener tooltipListener;
	private IPropertyChangeListener selectionBarCloseListener;
	
	protected XulRunnerErrorWrapper errorWrapper = new XulRunnerErrorWrapper();
	
	public void doSave(IProgressMonitor monitor) {
	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.setSite(site);
		super.setInput(input);
	}

	public void setInput(IEditorInput input) {
		boolean isVisualRefreshRequired = (getEditorInput() != null && getEditorInput() != input && controller != null);
		super.setInput(input);
		if(isVisualRefreshRequired) controller.visualRefresh();
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setController(VpeController controller){
		this.controller = controller;
		formatControllerManager.setVpeController(controller);
		controller.setToolbarFormatControllerManager(formatControllerManager);
	}
	
	public ToolBar createVisualToolbar(Composite parent) {
		final ToolBarManager toolBarManager = new ToolBarManager(SWT.VERTICAL | SWT.FLAT);
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
		 * Create VPE VISUAL REFRESH tool bar item
		 */
		visualRefreshAction = new Action(VpeUIMessages.REFRESH,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (controller != null) {
					controller.visualRefresh();
				}
			}
		};
		visualRefreshAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_REFRESH));
		visualRefreshAction.setToolTipText(VpeUIMessages.REFRESH);
		toolBarManager.add(visualRefreshAction);
		
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
				VpeResourcesDialogFactory.openVpeResourcesDialog(MozillaEditor.this);
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
				((VpeEditorPart)getController().getPageContext().getEditPart()).fillContainer(true, newOrientation);
				WebUiPlugin.getDefault().getPreferenceStore().
					setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, newOrientation);
			}
		};
		rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				layoutIcons.get(newOrientation)));
		rotateEditorsAction.setToolTipText(layoutNames.get(newOrientation));
		toolBarManager.add(rotateEditorsAction);
		
		/*
		 * Create SHOW BORDER FOR UNKNOWN TAGS tool bar item
		 */
		showBorderAction = new Action(
				VpeUIMessages.SHOW_BORDER_FOR_UNKNOWN_TAGS,
				IAction.AS_CHECK_BOX) {
		    @Override
		    public void run() {
		    	/*
		    	 * Set new value to VpeVisualDomBuilder.
		    	 */
		    	getController().getVisualBuilder().setShowBorderForUnknownTags(this.isChecked());
		        /*
				 * Update VPE
				 */
		        controller.visualRefresh();
		        WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.SHOW_BORDER_FOR_UNKNOWN_TAGS, this.isChecked());
		    }
		};
		showBorderAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_SHOW_BORDER_FOR_UNKNOWN_TAGS));
		showBorderAction.setToolTipText(VpeUIMessages.SHOW_BORDER_FOR_UNKNOWN_TAGS);
		toolBarManager.add(showBorderAction);

		/*
		 * Create SHOW INVISIBLE TAGS tool bar item
		 */
		showNonVisualTagsAction = new Action(
				VpeUIMessages.SHOW_NON_VISUAL_TAGS, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				
				/*
				 * Change flag
				 */
				controller.getVisualBuilder().setShowInvisibleTags(
						this.isChecked());
				/*
				 * Update VPE
				 */
				controller.visualRefresh();
				WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.SHOW_NON_VISUAL_TAGS, this.isChecked());
			}
		};
		showNonVisualTagsAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_NON_VISUAL_TAGS));
		showNonVisualTagsAction.setToolTipText(VpeUIMessages.SHOW_NON_VISUAL_TAGS);
		toolBarManager.add(showNonVisualTagsAction);
	
		/*
		 * Create SHOW TEXT FORMATTING tool bar item
		 */
		showTextFormattingAction = new Action(
				VpeUIMessages.SHOW_TEXT_FORMATTING, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Update Text Formatting Bar 
				 */
				vpeToolBarManager.setToolbarVisibility(this.isChecked());
				WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.SHOW_TEXT_FORMATTING, this.isChecked());
			}
		};
		showTextFormattingAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_TEXT_FORMATTING));
		showTextFormattingAction.setToolTipText(VpeUIMessages.SHOW_TEXT_FORMATTING);
		toolBarManager.add(showTextFormattingAction);

		/*
		 * Create SHOW BUNDLE'S MESSAGES AS EL tool bar item
		 */
		showBundleAsELAction = new Action(VpeUIMessages.SHOW_BUNDLES_AS_EL,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Update bundle messages. 
				 */
				controller.getPageContext().getBundle().updateShowBundleUsageAsEL(this.isChecked());
				controller.visualRefresh();
				WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.SHOW_RESOURCE_BUNDLES_USAGE_AS_EL, this.isChecked());
			}
		};
		showBundleAsELAction.setImageDescriptor(ImageDescriptor.createFromFile(
				MozillaEditor.class, ICON_BUNDLE_AS_EL));
		showBundleAsELAction.setToolTipText(VpeUIMessages.SHOW_BUNDLES_AS_EL);
		toolBarManager.add(showBundleAsELAction);
		
		/*
		 * https://issues.jboss.org/browse/JBIDE-11302
		 * Create SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES tool bar item
		 */
		scrollLockAction = new Action(
				VpeUIMessages.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Change the enabled state, listeners in VpeController will do the rest
				 */
				WebUiPlugin.getDefault().getPreferenceStore().setValue(
						IVpePreferencesPage.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES,
						this.isChecked());
			}
		};
		scrollLockAction.setImageDescriptor(ImageDescriptor.createFromFile(
				MozillaEditor.class, ICON_SCROLL_LOCK));
		scrollLockAction.setToolTipText(VpeUIMessages.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES);
		toolBarManager.add(scrollLockAction);
		
		/*
		 * Create EXTERNALIZE STRINGS tool bar item
		 */
		externalizeStringsAction = new Action(JstUIMessages.EXTERNALIZE_STRINGS,
				IAction.AS_PUSH_BUTTON) {
			
			@Override
			public void run() {
				/*
				 * Externalize strings action.
				 * Show a dialog to add properties key and value.
				 * When selection is correct show the dialog
				 * otherwise the toolbar icon will be disabled.
				 */
				ExternalizeStringsDialog dlg = new ExternalizeStringsDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						new ExternalizeStringsWizard(controller.getSourceEditor(), 
								controller.getPageContext().getBundle()));
				dlg.open();
			}
		};
		externalizeStringsAction.setImageDescriptor(ImageDescriptor.createFromFile(
				MozillaEditor.class, ICON_EXTERNALIZE_STRINGS));
		externalizeStringsAction.setToolTipText(JstUIMessages.EXTERNALIZE_STRINGS_POPUP_MENU_TITLE);
		toolBarManager.add(externalizeStringsAction);

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
				((VpeEditorPart)controller.getPageContext().getEditPart()).updateSelectionBar(this.isChecked());
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
				visualRefreshAction = null;
				showResouceDialogAction = null;
				rotateEditorsAction = null;;
				showBorderAction = null;
				showSelectionBarAction = null;
				showNonVisualTagsAction = null;
				showTextFormattingAction = null;
				showBundleAsELAction = null;
				externalizeStringsAction = null;
			}
		});
		return verBar;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {

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
		if (null != verBar) {
			// Use vpeToolBarManager to create a horizontal toolbar.
			vpeToolBarManager = new VpeToolBarManager();
			if (vpeToolBarManager != null) {
				vpeToolBarManager.createToolBarComposite(cmpEdTl);
				vpeToolBarManager.addToolBar(new TextFormattingToolBar(formatControllerManager));
			}
		}

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
			xulRunnerEditor = new XulRunnerEditor2(cmpEd, this);
			xulRunnerEditor.getBrowser().addProgressListener(new ProgressListener() {

				public void changed(ProgressEvent event) {
				}

				public void completed(ProgressEvent event) {
					if (MozillaEditor.this.getXulRunnerEditor().getWebBrowser() != null) {
						//process this code only in case when editor hasn't been disposed,
						//see https://jira.jboss.org/browse/JBIDE-6373
						MozillaEditor.this.onLoadWindow();
						xulRunnerEditor.getBrowser().removeProgressListener(this);
					}
				}
			});

			setInitialContent();
			xulRunnerEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		} catch (Throwable t) {
			/*
			 * Disable VPE toolbar
			 */
			if (verBar != null) {
				verBar.setEnabled(false);
			}
			errorWrapper.showError(cmpEd, t);
		}
	}

	/**
	 * Sets initial content to the {@link xulRunnerEditor}.
	 *
	 * @see #INIT_FILE
	 */
	protected void setInitialContent() {
		final String html = DocTypeUtil.prepareInitFile(
				INIT_FILE, getEditorInput());
		
		// Workaround of JBIDE-4345.
		// Due to a bug in org.eclipse.swt.browser.Mozilla we cannot simply
		// set initial html code as xulRunnerEditor.setText(html).
		// Instead of it we create a temporary file containing 
		// the html code and set it to the Mozilla browser as URL.
		// When we will not have to support Eclipse 3.5.0
		// all the following code should be replaced by the following line:
		// xulRunnerEditor.setText(html);
		File tmp = null;
		Writer out = null;
		try {
			tmp = File.createTempFile(
					"temp", ".html"); //$NON-NLS-1$//$NON-NLS-2$
			tmp.deleteOnExit();
			out = new FileWriter(tmp);
			out.write(html);
		} catch (IOException e) {
			VpePlugin.getPluginLog().logError(e);
		} finally {
			try {
				if (out != null) {
					out.close();
					if (tmp != null) {
						xulRunnerEditor.setURL("file://"	//$NON-NLS-1$
								+ tmp.getCanonicalPath());
					}
				}
			} catch (IOException e) {
				VpePlugin.getPluginLog().logError(e);
			} finally {
				if (tmp != null) {
					tmp.delete();
				}
			}
		}
	}

	

	public void setFocus() {
		if(xulRunnerEditor!=null) {
			xulRunnerEditor.setFocus();
		} else {
			//link.setFocus();
		}
	}

	public void dispose() {
		if(selectionBarCloseListener!=null) {
			WebUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(selectionBarCloseListener);
		}
		
		if (vpeToolBarManager != null) {
			vpeToolBarManager.dispose();
			vpeToolBarManager = null;
		}
		
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
			dropDownMenu = null;
		}

//		removeDomEventListeners();
		if(getController()!=null) {
			controller.dispose();
			controller = null;
		}
		if (xulRunnerEditor != null) {
			xulRunnerEditor.dispose();
			xulRunnerEditor = null;
		}

		formatControllerManager.setVpeController(null);
		formatControllerManager=null;
		headNode = null;
		contentArea = null;
		super.dispose();
	}

	public void setEditorLoadWindowListener(EditorLoadWindowListener listener) {
		editorLoadWindowListener = listener;
	}

	public nsIDOMDocument getDomDocument() {
			return xulRunnerEditor.getDOMDocument();
	}

	public nsIDOMElement getContentArea() {
		return contentArea;
	}
	/**
	 * Sets content area element
	 * @return
	 */
	protected void setContentArea(nsIDOMElement element) {
		 this.contentArea=element;
	}

	public nsIDOMNode getHeadNode() {
		return headNode;
	}

	public Menu getMenu() {
		return xulRunnerEditor.getBrowser().getMenu();
	}

	public Control getControl() {
		return xulRunnerEditor.getBrowser();
	}

	protected nsIDOMElement findContentArea() {
		nsIDOMElement area = null;
		nsIDOMNodeList nodeList = xulRunnerEditor.getDOMDocument().getElementsByTagName(HTML.TAG_BODY);
		long length = nodeList.getLength();
		for(long i=0; i<length; i++) {
			nsIDOMNode node = nodeList.item(i);
			if (isContentArea(node)) {
				if (node.getNodeType() != nsIDOMNode.ELEMENT_NODE) {
					throw new RuntimeException("The content area node should by element node."); //$NON-NLS-1$
				}

				area = queryInterface(node, nsIDOMElement.class);
				break;
			}
		}
		if (area == null) {
			//fix for jbide-3396, if we can't find a boody element, we should create it
			area = xulRunnerEditor.getDOMDocument().createElement(HTML.TAG_BODY);
			xulRunnerEditor.getDOMDocument().getDocumentElement().appendChild(area);
		}

		nsIDOMNode root = xulRunnerEditor.getDOMDocument().getDocumentElement();
		
		headNode = findHeadNode(root);

		return area;
	}

	private nsIDOMNode findHeadNode(nsIDOMNode root){
		nsIDOMNode headNode = findChildNode(root, HTML.TAG_HEAD); 
		return headNode;
	}

	private nsIDOMNode findChildNode(nsIDOMNode parent, String name) {
		nsIDOMNodeList list = parent.getChildNodes();
		nsIDOMNode node;
		for (int i=0;i<list.getLength();i++) {
			node = list.item(i);
			if (node.getNodeName().equalsIgnoreCase(name)) {
				return node;
			}
		}
		return null;
	}

	private boolean isContentArea(nsIDOMNode node) {
		boolean ret = false;
    	if (HTML.TAG_BODY.equalsIgnoreCase(node.getNodeName())) {
   	    	nsIDOMNamedNodeMap map = node.getAttributes();
			if (map != null) {
				long length = map.getLength();
    			for (int i = 0; i < length; i++) {
    				nsIDOMNode attr = map.item(i);
    				ret = attr.getNodeType() == nsIDOMNode.ATTRIBUTE_NODE
    						&& HTML.ATTR_ID.equalsIgnoreCase(attr.getNodeName())
							&& CONTENT_AREA_ID.equalsIgnoreCase(attr.getNodeValue());
    				if (ret) {
    	    			break;
    				}
    			}
			}
    	}
    	return ret;
	}

	private void onLoadWindow() {
		contentArea = findContentArea();
		attachMozillaEventAdapter();
		if (editorLoadWindowListener != null) {
			editorLoadWindowListener.load();
		}
	}
	
	protected MozillaEventAdapter createMozillaEventAdapter() {
		return new MozillaEventAdapter();
	}

	protected void attachMozillaEventAdapter() {
		if (contentArea != null) {
//			getContentAreaEventListener().setVisualEditor(xulRunnerEditor);
			nsIDOMWindow window = xulRunnerEditor.getWebBrowser().getContentDOMWindow();
			mozillaEventAdapter.attach(window, queryInterface(contentArea, nsIDOMEventTarget.class));
		}
	}

	void detachMozillaEventAdapter() {
		mozillaEventAdapter.detach();
//		getContentAreaEventListener().setVisualEditor(null);
	}

	public void setSelectionRectangle(/*nsIDOMElement*/List<nsIDOMNode> nodes, int resizerConstrains) {
		xulRunnerEditor.setSelectionRectangle(nodes, resizerConstrains);
	}

	/**
	 * Show resizer markers
	 */
	public void showResizer() {
		xulRunnerEditor.showResizer();
	}

	/**
	 * Hide resizer markers
	 */
	public void hideResizer() {
		xulRunnerEditor.hideResizer();
	}

	/**
	 * @return the xulRunnerEditor
	 */
	public XulRunnerEditor getXulRunnerEditor() {
		return xulRunnerEditor;
	}

	/**
	 * @param xulRunnerEditor the xulRunnerEditor to set
	 */
	protected void setXulRunnerEditor(XulRunnerEditor xulRunnerEditor) {
		this.xulRunnerEditor = xulRunnerEditor;
	}

	/**
	 * @return the controller
	 */
	public VpeController getController() {
		return controller;
	}

	public MozillaEventAdapter getMozillaEventAdapter() {
		return mozillaEventAdapter;
	}
	
	/**
	 * 
	 */
	public void onReloadWindow() {
		detachMozillaEventAdapter();
		xulRunnerEditor.removeResizeListener();
		contentArea = findContentArea();
		attachMozillaEventAdapter();
		xulRunnerEditor.addResizeListener();
		controller.reinit();
	}
	
	/**
	 * 
	 */
	public void reload() {
		
		doctype = DocTypeUtil.getDoctype(getEditorInput());
		//coused page to be refreshed
		setRefreshPage(true);
		setInitialContent();
	}
	/**
	 * Initialized design mode in visual refresh
	 */
	public void initDesingMode() {
		tearDownEditor();
		getEditor();
	}
	
	/**
	 * @return Doctype for document
	 */
	public String getDoctype() {
		return doctype;
	}
	
	public boolean isRefreshPage() {
		return isRefreshPage;
	}

	public void setRefreshPage(boolean isRefreshPage) {
		this.isRefreshPage = isRefreshPage;
	}
	
	public void reinitDesignMode() {
		tearDownEditor();
		getEditor();
	}
	
	public void tearDownEditor() {
		if (editor != null) {
			nsIEditingSession iEditingSession = (nsIEditingSession) getXulRunnerEditor().
							getComponentManager().createInstanceByContractID(XPCOM.NS_EDITINGSESSION_CONTRACTID, null, nsIEditingSession.NS_IEDITINGSESSION_IID);
			nsIDOMWindow window = getXulRunnerEditor().getWebBrowser().getContentDOMWindow();
			iEditingSession.detachFromWindow(window);
			iEditingSession.tearDownEditorOnWindow(window);
			editor = null;
		}
	}

	/**
	 * Returns Editor for This Document
	 * @return
	 */
	public nsIEditor getEditor() {
		
		if(editor==null) {
			//creating editing session
			nsIEditingSession iEditingSession = (nsIEditingSession) getXulRunnerEditor().
							getComponentManager().createInstanceByContractID(XPCOM.NS_EDITINGSESSION_CONTRACTID, null, nsIEditingSession.NS_IEDITINGSESSION_IID);
			//make window editable
			iEditingSession.makeWindowEditable(getXulRunnerEditor().getWebBrowser().getContentDOMWindow(), "html", true,true,true); //$NON-NLS-1$
			//here we setup editor for window
			iEditingSession.setupEditorOnWindow(getXulRunnerEditor().getWebBrowser().getContentDOMWindow());
			//getting some editor to disable some actions
			editor = iEditingSession.getEditorForWindow(getXulRunnerEditor().getWebBrowser().getContentDOMWindow());
			editor.setFlags(nsIPlaintextEditor.eEditorReadonlyMask);
			//here we hide nsIHTMLObjectResizers
			nsIHTMLObjectResizer htmlObjectResizer = queryInterface(editor, nsIHTMLObjectResizer.class);
			//we disable abject resizers
			htmlObjectResizer.hideResizers();
			htmlObjectResizer.setObjectResizingEnabled(false);
			//here we getting position editor and disable it's too
			nsIHTMLAbsPosEditor htmlAbsPosEditor = queryInterface(editor, nsIHTMLAbsPosEditor.class);
			htmlAbsPosEditor.setAbsolutePositioningEnabled(false);
			//here we getting inline table editor and disable it's too
			nsIHTMLInlineTableEditor inlineTableEditor = queryInterface(editor, nsIHTMLInlineTableEditor.class);
			inlineTableEditor.setInlineTableEditingEnabled(false);
			
		}
		return editor;
	}

	public VpeDropDownMenu getDropDownMenu() {
		return dropDownMenu;
	}

	/**
	 * Update Externalize Strings toolbar icon state.
	 * <p>
	 * Enables the button when suitable text is selected.
	 * Disabled otherwise.
	 */
	public void updateExternalizeStringsToolbarIconState(ISelection selection) {
		if (ExternalizeStringsUtils.isExternalizeStringsCommandEnabled(selection)) {
			externalizeStringsAction.setEnabled(true);
		} else {
			externalizeStringsAction.setEnabled(false);
		}
	}
	
	public void updateToolbarItemsAccordingToPreferences() {
		String prefsOrientation = WebUiPlugin
		.getDefault().getPreferenceStore().getString(
				IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING);
		int prefsOrientationIndex = layoutValues.indexOf(prefsOrientation);
		
		boolean prefsShowBorderForUnknownTags = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_BORDER_FOR_UNKNOWN_TAGS);
		boolean prefsShowNonVisualTags = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_NON_VISUAL_TAGS);
		boolean prefsShowTextFormatting = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_TEXT_FORMATTING);
		boolean prefsShowSelectionBar = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR);
		boolean prefsShowBundlesAsEL = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_RESOURCE_BUNDLES_USAGE_AS_EL);
		boolean scrollLockEditors = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES);
		
		if (showBorderAction != null) {
			showBorderAction.setChecked(prefsShowBorderForUnknownTags);
		}
		if (showNonVisualTagsAction != null) {
			showNonVisualTagsAction.setChecked(prefsShowNonVisualTags);
		}
		if (showSelectionBarAction != null) {
			showSelectionBarAction.setChecked(prefsShowSelectionBar);
		}
		if (showTextFormattingAction != null) {
			showTextFormattingAction.setChecked(prefsShowTextFormatting);
			if (vpeToolBarManager != null) {
				// JBIDE-14756 Selection/reset of 'Show Text Formatting Bar' in VPE preferences does not Show/Hide 'Show Text Formatting Bar' in editor
				vpeToolBarManager.setToolbarVisibility(prefsShowTextFormatting);
			}
		}
		if (showBundleAsELAction != null) {
			showBundleAsELAction.setChecked(prefsShowBundlesAsEL);
		}
		if (scrollLockAction != null) {
			scrollLockAction.setChecked(scrollLockEditors);
		}
		if (rotateEditorsAction != null) {
			currentOrientationIndex = prefsOrientationIndex;
			rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(
					MozillaEditor.class, layoutIcons.get(prefsOrientation)));
			rotateEditorsAction.setToolTipText(layoutNames.get(prefsOrientation));
		}
	}

	public void setResizeListener(MozillaResizeListener resizeListener) {
		this.resizeListener = resizeListener;
	}

	public void setTooltipListener(MozillaTooltipListener tooltipListener) {
		this.tooltipListener = tooltipListener;
	}

	public MozillaResizeListener getResizeListener() {
		return resizeListener;
	}

	public MozillaTooltipListener getTooltipListener() {
		return tooltipListener;
	}

	public IVpeToolBarManager getVpeToolBarManager() {
		return vpeToolBarManager;
	}
}
