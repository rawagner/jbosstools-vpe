/*******************************************************************************
 * Copyright (c) 2007-2009 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.dnd;

import static org.jboss.tools.vpe.xulrunner.util.XPCOM.queryInterface;

import org.eclipse.swt.graphics.Point;
import org.jboss.tools.vpe.editor.mozilla.MozillaEditor;
import org.jboss.tools.vpe.editor.util.HTML;
import org.jboss.tools.vpe.editor.util.VpeStyleUtil;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMMouseEvent;
import org.mozilla.interfaces.nsIDOMNode;

/**
 * Class responsible for showing the drag icon and the node being dragged. 
 * 
 * @author Yahor Radtsevich (yradtsevich)
 */
public class DraggablePattern {
	private static final int ICON_HEIGHT = 20;
	private static final String DRAG_ICON_ID = "dragIcon";		  //$NON-NLS-1$
	private static final String DRAG_ICON_FILE = "dragIcon.gif";  //$NON-NLS-1$
	
	/** @see #getTransparentDiv() */
	private static final String TRANSPARENT_DIV_ID = "transparentDragDiv"; //$NON-NLS-1$
	/** @see #getTransparentDiv() */
	private static final int TRANSPARENT_DIV_SIZE = 360;
	/** @see #getTransparentDiv() */
	private static final String TRANSPARENT_DIV_STYLE
			= "background-color: rgba(255, 0, 0, 0.0);"	//$NON-NLS-1$
				+ "height:"+TRANSPARENT_DIV_SIZE+"px;"//$NON-NLS-1$//$NON-NLS-2$
				+ "width:"+TRANSPARENT_DIV_SIZE+"px;" //$NON-NLS-1$//$NON-NLS-2$
				+ "position:absolute;";					//$NON-NLS-1$

	private static final String DRAG_ICON_STYLE
			= "display:none;"						//$NON-NLS-1$
			+ "position: absolute;"					//$NON-NLS-1$
			+ "cursor: move;";						//$NON-NLS-1$
	private int offsetX;
	private int offsetY;
	private boolean sessionStarted;
	private nsIDOMElement draggableFragmentRepresentation;
	private IDraggableFragment draggableFragment;
	private final MozillaEditor mozillaEditor;

	public DraggablePattern(MozillaEditor mozillaEditor) {
		sessionStarted = false;
		this.mozillaEditor = mozillaEditor;
	}

	public void showDragIcon(IDraggableFragment draggableFragment) {
		this.draggableFragment = draggableFragment;
		Point position = draggableFragment.getPosition();
		nsIDOMElement dragIcon = getDragIcon();

		VpeStyleUtil.setElementVisible(dragIcon, true);
		VpeStyleUtil.moveElementTo(dragIcon, position.x, position.y - ICON_HEIGHT);
	}

	public void hideDragIcon() {
		this.draggableFragment = null;
		VpeStyleUtil.setElementVisible(getDragIcon(), false);
	}

	private nsIDOMElement getDragIcon() {
		nsIDOMElement dragIconElement = mozillaEditor.getDomDocument()
				.getElementById(DRAG_ICON_ID);
		if (dragIconElement == null) {
			dragIconElement = mozillaEditor.getDomDocument()
					.createElement(HTML.TAG_IMG);
			DndUtil.setTemporaryDndElement(dragIconElement, true);
			nsIDOMNode bodyNode = mozillaEditor.getDomDocument().getElementsByTagName(HTML.TAG_BODY)
					.item(0);
			if (bodyNode != null) {
				bodyNode.appendChild(dragIconElement);
			}
			dragIconElement.setAttribute(HTML.ATTR_ID, DRAG_ICON_ID);
			dragIconElement.setAttribute(HTML.ATTR_SRC,
					VpeStyleUtil.getAbsoluteResourcePathUrl(DRAG_ICON_FILE));
			dragIconElement.setAttribute(HTML.ATTR_STYLE, DRAG_ICON_STYLE);
		}
		return dragIconElement; 
	}
	
	
	/**
	 * This transparent DIV is needed to move the draggable pattern
	 * over empty areas. Without it the dragover event is not fired by
	 * XULRunner.
	 */
	private nsIDOMElement getTransparentDiv() {
		nsIDOMElement transparentDiv = mozillaEditor.getDomDocument()
				.getElementById(TRANSPARENT_DIV_ID);
		if (transparentDiv == null) {
			transparentDiv = mozillaEditor.getDomDocument()
					.createElement(HTML.TAG_DIV);
			DndUtil.setTemporaryDndElement(transparentDiv, true);
			mozillaEditor.getDomDocument().getElementsByTagName(HTML.TAG_BODY)
					.item(0).appendChild(transparentDiv);
			transparentDiv.setAttribute(HTML.ATTR_ID, TRANSPARENT_DIV_ID);
			transparentDiv.setAttribute(HTML.ATTR_STYLE, TRANSPARENT_DIV_STYLE);
		}
		
		return transparentDiv;
	}

	public boolean isDragIconClicked(nsIDOMMouseEvent mouseEvent) {
		nsIDOMElement targetElement = queryInterface(mouseEvent.getTarget(), nsIDOMElement.class);
		if (targetElement != null) {
			return DRAG_ICON_ID.equals(targetElement.getAttribute(HTML.ATTR_ID));
		} else {
			return false;
		}
	}

	public void startSession(int mouseStartX, int mouseStartY) {
		if (sessionStarted) {
			new IllegalStateException(
					"Session is already started."); //$NON-NLS-1$
		}
		if (draggableFragment == null) {
			new IllegalStateException(
			"No node to drag."); //$NON-NLS-1$
		}

		Point fragmentPosition = draggableFragment.getPosition();
		offsetX = fragmentPosition.x - mouseStartX;
		offsetY = fragmentPosition.y - mouseStartY;
		
		draggableFragmentRepresentation = draggableFragment.createRepresentation();

		//TODO: remove the following line
		//setVisible(false);
		moveTo(mouseStartX, mouseStartY);
		setVisible(true);
		sessionStarted = true;
	}

	public void closeSession() {
		if (sessionStarted) {
			setVisible(false);
			nsIDOMNode parent = draggableFragmentRepresentation.getParentNode();
			if (parent != null) {
				parent.removeChild(draggableFragmentRepresentation);
			}
			
			hideDragIcon();
			draggableFragmentRepresentation = null;
			sessionStarted = false;
		}
	}

	public void setVisible(boolean visible) {
		VpeStyleUtil.setElementVisible(draggableFragmentRepresentation, visible);
		VpeStyleUtil.setElementVisible(getTransparentDiv(), visible);
		VpeStyleUtil.setElementVisible(getDragIcon(), visible);
	}

	public void moveTo(int mouseX, int mouseY) {
		VpeStyleUtil.moveElementTo(draggableFragmentRepresentation, offsetX + mouseX, offsetY + mouseY);
		VpeStyleUtil.moveElementTo(getTransparentDiv(),
				offsetX + mouseX - TRANSPARENT_DIV_SIZE / 2,
				offsetY + mouseY - TRANSPARENT_DIV_SIZE / 2);
		VpeStyleUtil.moveElementTo(getDragIcon(),
				offsetX + mouseX, offsetY + mouseY - ICON_HEIGHT);
	}
}
