/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.vpe.mozilla.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class VpeMozillaAllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for " + VpeMozillaAllTests.class.getName());
		//add test cases for dom elements
		suite.addTestSuite(DOMCreatingTest.class);		
		return suite;
	}

}
