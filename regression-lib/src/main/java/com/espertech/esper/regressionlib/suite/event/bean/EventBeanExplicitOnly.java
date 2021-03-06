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
package com.espertech.esper.regressionlib.suite.event.bean;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportLegacyBean;

import static org.junit.Assert.assertEquals;

public class EventBeanExplicitOnly implements RegressionExecution {

    public void run(RegressionEnvironment env) {
        String statementText = "@name('s0') select " +
            "explicitFNested.fieldNestedClassValue as fnested, " +
            "explicitMNested.readNestedClassValue as mnested" +
            " from MyLegacyEvent#length(5)";
        env.compileDeploy(statementText).addListener("s0");

        env.assertStatement("s0", statement -> {
            EventType eventType = statement.getEventType();
            assertEquals(String.class, eventType.getPropertyType("fnested"));
            assertEquals(String.class, eventType.getPropertyType("mnested"));
        });

        SupportLegacyBean legacyBean = EventBeanPublicAccessors.makeSampleEvent();
        env.sendEventBean(legacyBean, "MyLegacyEvent");

        env.assertEventNew("s0", event -> {
            assertEquals(legacyBean.fieldNested.readNestedValue(), event.get("fnested"));
            assertEquals(legacyBean.fieldNested.readNestedValue(), event.get("mnested"));
        });

        env.tryInvalidCompile("select intPrimitive from MySupportBean#length(5)", "skip");

        env.undeployAll();
    }
}
