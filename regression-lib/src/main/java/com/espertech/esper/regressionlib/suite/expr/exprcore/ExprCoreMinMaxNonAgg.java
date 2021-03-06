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
package com.espertech.esper.regressionlib.suite.expr.exprcore;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.soda.*;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.util.SerializableObjectCopier;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.expreval.SupportEvalBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ExprCoreMinMaxNonAgg {
    private final static String EPL = "select max(longBoxed,intBoxed) as myMax, " +
        "max(longBoxed,intBoxed,shortBoxed) as myMaxEx, " +
        "min(longBoxed,intBoxed) as myMin, " +
        "min(longBoxed,intBoxed,shortBoxed) as myMinEx" +
        " from SupportBean#length(3)";

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ExecCoreMinMax());
        execs.add(new ExecCoreMinMaxOM());
        execs.add(new ExecCoreMinMaxCompile());
        return execs;
    }

    private static class ExecCoreMinMax implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "myMax,myMaxEx,myMin,myMinEx".split(",");
            SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean")
                .expression(fields[0], "max(longBoxed,intBoxed)")
                .expression(fields[1], "max(longBoxed,intBoxed,shortBoxed)")
                .expression(fields[2], "min(longBoxed,intBoxed)")
                .expression(fields[3], "min(longBoxed,intBoxed,shortBoxed)");

            builder.statementConsumer(stmt -> {
                EventType type = stmt.getEventType();
                assertEquals(Long.class, type.getPropertyType("myMax"));
                assertEquals(Long.class, type.getPropertyType("myMin"));
                assertEquals(Long.class, type.getPropertyType("myMinEx"));
                assertEquals(Long.class, type.getPropertyType("myMaxEx"));
            });

            builder.assertion(makeBoxedEvent(10L, 20, (short) 4)).expect(fields, 20L, 20L, 10L, 4L);
            builder.assertion(makeBoxedEvent(-10L, -20, (short) -30)).expect(fields, -10L, -10L, -20L, -30L);
            builder.assertion(makeBoxedEvent(null, null, null)).expect(fields, null, null, null, null);
            builder.assertion(makeBoxedEvent(1L, null, (short) 1)).expect(fields, null, null, null, null);

            builder.run(env);
            env.undeployAll();
        }
    }

    private static class ExecCoreMinMaxOM implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            EPStatementObjectModel model = new EPStatementObjectModel();
            model.setSelectClause(SelectClause.create()
                .add(Expressions.max("longBoxed", "intBoxed"), "myMax")
                .add(Expressions.max(Expressions.property("longBoxed"), Expressions.property("intBoxed"), Expressions.property("shortBoxed")), "myMaxEx")
                .add(Expressions.min("longBoxed", "intBoxed"), "myMin")
                .add(Expressions.min(Expressions.property("longBoxed"), Expressions.property("intBoxed"), Expressions.property("shortBoxed")), "myMinEx")
            );
            model.setFromClause(FromClause.create(FilterStream.create(SupportBean.class.getSimpleName()).addView("length", Expressions.constant(3))));
            model = SerializableObjectCopier.copyMayFail(model);
            assertEquals(EPL, model.toEPL());

            model.setAnnotations(Collections.singletonList(AnnotationPart.nameAnnotation("s0")));
            env.compileDeploy(model).addListener("s0");

            tryMinMaxWindowStats(env);

            env.undeployAll();
        }
    }

    private static class ExecCoreMinMaxCompile implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.eplToModelCompileDeploy("@name('s0') " + EPL).addListener("s0");
            tryMinMaxWindowStats(env);
            env.undeployAll();
        }
    }

    private static void tryMinMaxWindowStats(RegressionEnvironment env) {
        sendEvent(env, 10, 20, (short) 4);
        env.assertListener("s0", listener -> {
            EventBean received = listener.getAndResetLastNewData()[0];
            assertEquals(20L, received.get("myMax"));
            assertEquals(10L, received.get("myMin"));
            assertEquals(4L, received.get("myMinEx"));
            assertEquals(20L, received.get("myMaxEx"));
        });

        sendEvent(env, -10, -20, (short) -30);
        env.assertListener("s0", listener -> {
            EventBean received = listener.getAndResetLastNewData()[0];
            assertEquals(-10L, received.get("myMax"));
            assertEquals(-20L, received.get("myMin"));
            assertEquals(-30L, received.get("myMinEx"));
            assertEquals(-10L, received.get("myMaxEx"));
        });
    }

    private static void sendEvent(RegressionEnvironment env, long longBoxed, int intBoxed, short shortBoxed) {
        env.sendEventBean(makeBoxedEvent(longBoxed, intBoxed, shortBoxed));
    }

    private static SupportBean makeBoxedEvent(Long longBoxed, Integer intBoxed, Short shortBoxed) {
        SupportBean bean = new SupportBean();
        bean.setLongBoxed(longBoxed);
        bean.setIntBoxed(intBoxed);
        bean.setShortBoxed(shortBoxed);
        return bean;
    }
}
