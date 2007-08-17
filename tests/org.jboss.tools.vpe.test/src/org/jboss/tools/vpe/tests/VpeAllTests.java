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
package org.jboss.tools.vpe.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Class created for run tests for org.jboss.tools.vpe plugin.
 * 
 * @author Max Areshkau
 * 
 */

public class VpeAllTests extends TestCase{
	
	public static Test suite(){
		TestSuite suite = new TestSuite("Tests for vpe");
		// $JUnit-BEGIN$
		suite.addTestSuite(TemplateLoadingTest.class);
		suite.addTestSuite(TemplatesExpressionParsingTest.class);
		// $JUnit-END$
		return suite;
	}
}
