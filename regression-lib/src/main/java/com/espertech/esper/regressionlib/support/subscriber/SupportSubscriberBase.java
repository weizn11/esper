/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.regressionlib.support.subscriber;

import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.runtime.client.EPStatement;

import java.util.ArrayList;

import static org.junit.Assert.*;

public abstract class SupportSubscriberBase {
    private final boolean requiresStatementDelivery;
    private final ArrayList<EPStatement> statements = new ArrayList<EPStatement>();

    public SupportSubscriberBase(boolean requiresStatementDelivery) {
        this.requiresStatementDelivery = requiresStatementDelivery;
    }

    protected void addStmtIndication(EPStatement statement) {
        statements.add(statement);
    }

    protected void assertStmtOneReceived(EPStatement stmt) {
        if (requiresStatementDelivery) {
            EPAssertionUtil.assertEqualsExactOrder(new EPStatement[]{stmt}, statements.toArray());
        } else {
            assertTrue(statements.isEmpty());
        }
    }

    protected void assertStmtMultipleReceived(EPStatement stmt, int size) {
        if (requiresStatementDelivery) {
            assertEquals(size, statements.size());
            for (EPStatement indicated : statements) {
                assertSame(indicated, stmt);
            }
        } else {
            assertTrue(statements.isEmpty());
        }
    }

    protected void assertStmtNoneReceived() {
        assertTrue(statements.isEmpty());
    }

    protected void resetStmts() {
        statements.clear();
    }
}
