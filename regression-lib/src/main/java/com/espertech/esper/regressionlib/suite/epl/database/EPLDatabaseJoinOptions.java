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
package com.espertech.esper.regressionlib.suite.epl.database;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import java.util.ArrayList;
import java.util.List;

import static com.espertech.esper.common.client.scopetest.ScopeTestHelper.assertEquals;

public class EPLDatabaseJoinOptions {

    public static List<RegressionExecution> executions() {
        List<RegressionExecution> execs = new ArrayList<>();
        execs.add(new EPLDatabaseNoMetaLexAnalysis());
        execs.add(new EPLDatabaseNoMetaLexAnalysisGroup());
        execs.add(new EPLDatabasePlaceholderWhere());
        return execs;
    }

    private static class EPLDatabaseNoMetaLexAnalysis implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String sql = "select mydouble from mytesttable where ${intPrimitive} = myint";
            runAssertion(env, sql);
        }
    }

    private static class EPLDatabaseNoMetaLexAnalysisGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String sql = "select mydouble, sum(myint) from mytesttable where ${intPrimitive} = myint group by mydouble";
            runAssertion(env, sql);
        }
    }

    private static class EPLDatabasePlaceholderWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String sql = "select mydouble from mytesttable ${$ESPER-SAMPLE-WHERE} where ${intPrimitive} = myint";
            runAssertion(env, sql);
        }
    }

    private static void runAssertion(RegressionEnvironment env, String sql) {
        String stmtText = "@name('s0') select mydouble from " +
            " sql:MyDBPlain ['" + sql + "'] as s0," +
            "SupportBean#length(100) as s1";
        env.compileDeploy(stmtText).addListener("s0");

        env.assertStatement("s0", statement -> assertEquals(Double.class, statement.getEventType().getPropertyType("mydouble")));

        sendSupportBeanEvent(env, 10);
        env.assertEqualsNew("s0", "mydouble", 1.2);

        sendSupportBeanEvent(env, 80);
        env.assertEqualsNew("s0", "mydouble", 8.2);

        env.undeployAll();
    }

    private static void sendSupportBeanEvent(RegressionEnvironment env, int intPrimitive) {
        SupportBean bean = new SupportBean();
        bean.setIntPrimitive(intPrimitive);
        env.sendEventBean(bean);
    }
}
