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
package org.jboss.tools.vpe.editor.mapping;

import static org.jboss.tools.vpe.xulrunner.util.XPCOM.queryInterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.context.VpePageContext;
import org.jboss.tools.vpe.xulrunner.editor.XulRunnerEditor;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.xpcom.XPCOMException;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VpeDomMapping {
	private Map<Node, VpeNodeMapping> sourceMap = new HashMap<Node, VpeNodeMapping>();
	private Map<nsIDOMNode, VpeNodeMapping> visualMap = new HashMap<nsIDOMNode, VpeNodeMapping>();
	private VpePageContext pageContext;
	
	public VpeDomMapping(VpePageContext pageContext) {
		this.pageContext = pageContext;
	}

	public void mapNodes(VpeNodeMapping nodeMapping) {
		sourceMap.put(nodeMapping.getSourceNode(), nodeMapping);
		if (nodeMapping.getVisualNode() != null) {
			visualMap.put(nodeMapping.getVisualNode(), nodeMapping);
		}
	}
	
	/**
	 * Remap visual node in DOM Mapping and in the VisualMap. 
	 * 
	 * @param visualNode old visual node
	 * @param registeredVisualNewNode node, that've been  actually registered in the DOM  
	 */
	public void remapVisualNode(nsIDOMNode visualNode, nsIDOMNode registeredVisualNewNode) {
		if ((visualNode != null) && (registeredVisualNewNode != null)) {
			VpeNodeMapping nodeMapping = visualMap.get(visualNode);
			if (nodeMapping != null) {
				nodeMapping.setVisualNode(registeredVisualNewNode);
				visualMap.remove(visualNode);
				visualMap.put(registeredVisualNewNode, nodeMapping);
			}
		}
	}
	
	/**
	 * Clear maps of source and visual nodes with their mappings,
	 * except for the specified visual node.  
	 * 
	 * @param except the node with its mapping to remain in visual map.
	 */
	public void clear(nsIDOMNode except) {
		sourceMap.clear();
		VpeNodeMapping exceptMapping = visualMap.get(except);
		visualMap.clear();
		if (exceptMapping != null) {
			visualMap.put(except, exceptMapping);
		}
	}
	
	public VpeNodeMapping getNodeMapping(Node node) {
		return getNodeMappingAtSourceNode(node);
	}

	public VpeNodeMapping getNodeMapping(nsIDOMNode node) {
		return getNodeMappingAtVisualNode(node);
	}
	
	public VpeNodeMapping getNodeMappingAtSourceNode(Node sourceNode) {
		VpeNodeMapping nodeMapping = null;
		if (sourceNode != null) {
			nodeMapping = sourceMap.get(sourceNode);
		}
		return nodeMapping;
	}
	
	public VpeNodeMapping getNodeMappingAtVisualNode(nsIDOMNode visualNode) {
		VpeNodeMapping nodeMapping = null;
		/*
		 * https://issues.jboss.org/browse/JBIDE-9932
		 * Replaced with map.
		 */
		if (visualNode != null) {
			/*
			 * https://issues.jboss.org/browse/JBIDE-10600
			 * visualNode could be a nsIDOMElement or other,
			 * to get right mapping it should be exactly nsIDOMNode.
			 */
			try {
				nodeMapping = visualMap.get(queryInterface(visualNode, nsIDOMNode.class));
			} catch (XPCOMException e) {
				VpePlugin.getDefault().logError("Cannot cast visualNode to nsIDOMNode type", e); //$NON-NLS-1$
			}
		}
		return nodeMapping;
	}
	
	public nsIDOMNode getVisualNode(Node sourceNode) {
		VpeNodeMapping nodeMapping = getNodeMapping(sourceNode);
		if (nodeMapping != null) {
			return nodeMapping.getVisualNode();
		}
		return null;
	}
	
	public Node getSourceNode(nsIDOMNode visualNode) {
		VpeNodeMapping nodeMapping = getNodeMapping(visualNode);
		if (nodeMapping != null) {
			return nodeMapping.getSourceNode();
		}
		return null;
	}
	
	public VpeNodeMapping getNearNodeMapping(Node node) {
		return getNearNodeMappingAtSourceNode(node);
	}
	
	public VpeNodeMapping getNearNodeMapping(nsIDOMNode node) {
		return getNearNodeMappingAtVisualNode(node);
	}

	
	public VpeNodeMapping getNearNodeMappingAtSourceNode(Node sourceNode) {
		VpeNodeMapping nodeMapping = getNodeMappingAtSourceNode(sourceNode);
		
		while (sourceNode != null && nodeMapping == null) {
			if (sourceNode instanceof Attr) {
				sourceNode = ((Attr) sourceNode).getOwnerElement();
			} else {
				sourceNode = sourceNode.getParentNode();
			}
			nodeMapping = getNodeMappingAtSourceNode(sourceNode);
			
			if(sourceNode!=null && nodeMapping != null) {			
				nsIDOMNode nearVisualNode = nodeMapping.getVisualNode();
				if(nearVisualNode instanceof nsIDOMElement){	
					nsIDOMElement visualElement = (nsIDOMElement) nearVisualNode;
					visualElement.setAttribute(XulRunnerEditor.VPE_INVISIBLE_ELEMENT, 
							Boolean.TRUE.toString());
				}
			}
		}

		if(sourceNode!=null && nodeMapping != null) {			
			nsIDOMNode nearVisualNode = nodeMapping.getVisualNode();
			if(nearVisualNode instanceof nsIDOMElement){			
				nsIDOMElement visualElement = (nsIDOMElement) nearVisualNode;
				visualElement.removeAttribute(XulRunnerEditor.VPE_INVISIBLE_ELEMENT);
			}
		}

		return nodeMapping;
	}

	public VpeNodeMapping getNearNodeMappingAtVisualNode(nsIDOMNode visualNode) {
		VpeNodeMapping nodeMapping = getNodeMappingAtVisualNode(visualNode);
		while (visualNode != null && nodeMapping == null) {
			visualNode = visualNode.getParentNode();
			nodeMapping = getNodeMappingAtVisualNode(visualNode);
		}
		return nodeMapping;
	}
	
	public VpeNodeMapping getNearParentMapping(Node sourceNode) {
		VpeNodeMapping nodeMapping = null;
		if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
			nodeMapping = getNearNodeMapping(sourceNode);
		} else if (sourceNode.getNodeType() == Node.TEXT_NODE) {
			sourceNode = sourceNode.getParentNode();
			nodeMapping = getNodeMapping(sourceNode);
			while (sourceNode != null && sourceNode.getNodeType() != Node.DOCUMENT_NODE && nodeMapping == null) {
				sourceNode = sourceNode.getParentNode();
				nodeMapping = getNodeMapping(sourceNode);
			}
		}
		return nodeMapping;
	}

	public VpeNodeMapping getParentMapping(Node sourceNode) {
		VpeNodeMapping nodeMapping = null;
		sourceNode = sourceNode.getParentNode();
		nodeMapping = getNodeMapping(sourceNode);
		while (sourceNode != null && sourceNode.getNodeType() != Node.DOCUMENT_NODE && nodeMapping == null) {
			sourceNode = sourceNode.getParentNode();
			nodeMapping = getNodeMapping(sourceNode);
		}
		return nodeMapping;
	}

	public VpeElementMapping getNearElementMapping(Node node) {
		return getNearElementMappingAtSourceNode(node);
	}

	public VpeElementMapping getNearElementMapping(nsIDOMNode node) {
		return getNearElementMappingAtVisualNode(node);
	}
	
	public VpeElementMapping getNearElementMappingAtSourceNode(Node sourceNode) {
		VpeNodeMapping nodeMapping = getNearNodeMappingAtSourceNode(sourceNode);
		if (nodeMapping != null) {
		
//			switch (nodeMapping.getType()) {
//			case VpeNodeMapping.TEXT_MAPPING:
//				return getNearElementMappingAtSourceNode(nodeMapping.getSourceNode().getParentNode());
//			case VpeNodeMapping.ELEMENT_MAPPING:
//				return (VpeElementMapping)nodeMapping;
//			}
			if(nodeMapping instanceof VpeElementMapping) {
				return (VpeElementMapping)nodeMapping;
			} else {
				return getNearElementMappingAtSourceNode(nodeMapping.getSourceNode().getParentNode());
			}
		}
		return null;
	}

	public VpeElementMapping getNearElementMappingAtVisualNode(nsIDOMNode visualNode) {
		VpeNodeMapping nodeMapping = getNearNodeMappingAtVisualNode(visualNode);
		if (nodeMapping != null) {
//			switch (nodeMapping.getType()) {
//			case VpeNodeMapping.TEXT_MAPPING:
//				return getNearElementMappingAtSourceNode(nodeMapping.getSourceNode().getParentNode());
//			case VpeNodeMapping.ELEMENT_MAPPING:
//				return (VpeElementMapping)nodeMapping;
//			}
			if(nodeMapping instanceof VpeElementMapping) {
				return (VpeElementMapping)nodeMapping;
			} else {
				return getNearElementMappingAtSourceNode(nodeMapping.getSourceNode().getParentNode());
			}
		}
		return null;
	}
	
	public nsIDOMNode getNearVisualNode_(Node sourceNode) {
		VpeNodeMapping nodeMapping = getNearNodeMapping(sourceNode);
		if (nodeMapping != null) {
			return nodeMapping.getVisualNode();
		}
		return null;
	}
	
	/**
	 * Returns the nearest visual element for the source node 
	 * 
	 * @param sourceNode the source node
	 * @return nearest visual element
	 */
	public nsIDOMElement getNearVisualElement(Node sourceNode) {
		nsIDOMElement element = null;
		VpeNodeMapping nodeMapping = getNearNodeMappingAtSourceNode(sourceNode);
		if (sourceNode != null) {
			if (nodeMapping != null) {
				nsIDOMNode visualNode = nodeMapping.getVisualNode();
				if (visualNode != null) {
					try {
						element = queryInterface(visualNode, nsIDOMElement.class);
					} catch (XPCOMException xpcomException) {
						if (sourceNode.getPreviousSibling() != null) {
							element = getNearVisualElement(sourceNode.getPreviousSibling());
						} else {
							element = getNearVisualElement(sourceNode.getParentNode());
						}
					}
				}
			} else {
				if (sourceNode.getPreviousSibling() != null) {
					element = getNearVisualElement(sourceNode.getPreviousSibling());
				} else {
					element = getNearVisualElement(sourceNode.getParentNode());
				}
			}
		}
		return element;
	}
	
	public nsIDOMNode getNearVisualNode(Node sourceNode) {
		if (sourceNode == null) return null;
		VpeNodeMapping nodeMapping = getNearNodeMappingAtSourceNode(sourceNode);
		if (nodeMapping != null) {
			if (nodeMapping.getVisualNode() == null) {
				return getNearVisualNode(sourceNode.getParentNode());
			} else {
				return nodeMapping.getVisualNode();
			}
		}
		return null;
	}
	
	/**
	 * Returns the nearest source element for the visual node 
	 * 
	 * @param visualNode the visual node
	 * @return nearest source element
	 */
	public ElementImpl getNearSourceElementImpl(nsIDOMNode visualNode) {
		ElementImpl element = null;
		VpeNodeMapping nodeMapping = getNearNodeMappingAtVisualNode(visualNode);
		if (visualNode != null) {
			if ((nodeMapping != null) && (nodeMapping.getSourceNode() != null) 
					&& (nodeMapping.getSourceNode() instanceof ElementImpl)) {
				/*
				 * This visual node is OK and src node is Element
				 */
				element = (ElementImpl) nodeMapping.getSourceNode();
			} else {
				/*
				 * Else continue searching
				 */
				if (visualNode.getNextSibling() != null) {
					element = getNearSourceElementImpl(visualNode.getNextSibling());
				} else {
					element = getNearSourceElementImpl(visualNode.getParentNode());
				}
			}
		}
//		System.out.println("--near src element = " + element);
		return element;
	}
	
	public Node getNearSourceNode(nsIDOMNode visualNode) {
		VpeNodeMapping nodeMapping = getNearNodeMapping(visualNode);
		if (nodeMapping != null) {
			return nodeMapping.getSourceNode();
		}
		return null;
	}
	
	public nsIDOMNode remove(Node sourceNode) {
		nsIDOMNode visualNode = getVisualNode(sourceNode);
//		if (visualNode != null) {
			removeImpl(sourceNode);
//		}
		return visualNode;
	}
	
	public void removeChildren(Node sourceNode) {
		NodeList sourceChildren = sourceNode.getChildNodes();
		if (sourceChildren != null) {
			int len = sourceChildren.getLength();
			for (int i = 0; i < len; i++) {
				removeImpl(sourceChildren.item(i));
			}
		}
	}
	
	private VpeNodeMapping removeImpl(Node sourceNode) {
		nsIDOMNode visualNode = null;
		VpeNodeMapping nodeMapping = (VpeNodeMapping)sourceMap.remove(sourceNode);
		if (nodeMapping != null) {
			visualNode = nodeMapping.getVisualNode();
			if (visualNode != null) {
				visualMap.remove(visualNode);
			}
			if (nodeMapping instanceof VpeElementMapping) { 
				VpeElementMapping elementMapping = (VpeElementMapping)nodeMapping;
//				Map xmlnsMap = elementMapping.getXmlnsMap();
//				if (xmlnsMap != null) {
//					for (Iterator iter = xmlnsMap.values().iterator(); iter.hasNext();) {
//						pageContext.setTaglib(((Integer)iter.next()).intValue(), null, null, true);
//					}
//					elementMapping.setXmlnsMap(null);
//				}
				elementMapping.getTemplate().beforeRemove(pageContext, elementMapping.getSourceNode(), elementMapping.getVisualNode(), elementMapping.getData());
			}
		}
		removeChildren(sourceNode);
		return nodeMapping;
	}
	
	//for debug
	public void printMapping() {
		System.out.println("Source DOM Mapping ------------------------------------"); //$NON-NLS-1$
		Set entrySet = sourceMap.entrySet();
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			VpeNodeMapping nodeMapping = (VpeNodeMapping)entry.getValue(); 
			Node sourceNode = nodeMapping.getSourceNode();
			nsIDOMNode visualNode = nodeMapping.getVisualNode(); 
			System.out.println("sourceNode: " + sourceNode.getNodeName() + " (" + sourceNode.hashCode() + ")    visualNode: " + (visualNode != null ? visualNode.getNodeName() + " (" + visualNode.hashCode() + ")" : null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		System.out.println("Visual DOM Mapping ------------------------------------"); //$NON-NLS-1$
		entrySet = visualMap.entrySet();
		iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			VpeNodeMapping nodeMapping = (VpeNodeMapping)entry.getValue(); 
			Node sourceNode = nodeMapping.getSourceNode();
			nsIDOMNode visualNode = nodeMapping.getVisualNode(); 
			System.out.println("sourceNode: " + (sourceNode != null ? sourceNode.getNodeName() + " (" + sourceNode.hashCode() + ")" : null) + "    visualNode: " + visualNode.getNodeName() + " (" + visualNode.hashCode() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
	}

	public Map<nsIDOMNode, VpeNodeMapping> getVisualMap() {
		return visualMap;
	}
	
	public Map<Node, VpeNodeMapping> getSourceMap() {
		return sourceMap;
	}
}
