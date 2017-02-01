/*
 * *************************************************************************************
 *  Copyright (C) 2006-2015 EsperTech, Inc. All rights reserved.                       *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * Context condition that starts/initiates immediately.
 */
public class ContextDescriptorConditionNever implements ContextDescriptorCondition {

    /**
     * Ctor.
     */
    public ContextDescriptorConditionNever() {
    }

    public void toEPL(StringWriter writer, EPStatementFormatter formatter) {

    }
}
