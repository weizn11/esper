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

import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBeanComplexProps;
import com.espertech.esper.regressionlib.support.bean.SupportBeanDupProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventBeanPropertyResolutionCaseInsensitive implements RegressionExecution {

    public void run(RegressionEnvironment env) {
        env.compileDeploy("@name('s0') select MYPROPERTY, myproperty, myProperty, MyProperty from SupportBeanDupProperty");
        env.addListener("s0");

        env.sendEventBean(new SupportBeanDupProperty("lowercamel", "uppercamel", "upper", "lower"));
        env.assertEventNew("s0", result -> {
            assertEquals("upper", result.get("MYPROPERTY"));
            assertEquals("lower", result.get("myproperty"));
            assertTrue(result.get("myProperty").equals("lowercamel") || result.get("myProperty").equals("uppercamel")); // JDK6 versus JDK7 JavaBean inspector
            assertEquals("upper", result.get("MyProperty"));
        });
        env.undeployAll();

        env.compileDeploy("@name('s0') select " +
            "NESTED.NESTEDVALUE as val1, " +
            "ARRAYPROPERTY[0] as val2, " +
            "MAPPED('keyOne') as val3, " +
            "INDEXED[0] as val4 " +
            " from SupportBeanComplexProps").addListener("s0");

        env.sendEventBean(SupportBeanComplexProps.makeDefaultBean());
        env.assertEventNew("s0", theEvent -> {
            assertEquals("nestedValue", theEvent.get("val1"));
            assertEquals(10, theEvent.get("val2"));
            assertEquals("valueOne", theEvent.get("val3"));
            assertEquals(1, theEvent.get("val4"));
        });

        env.undeployAll();
    }
}
