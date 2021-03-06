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
package com.espertech.esper.regressionlib.suite.epl.subselect;

import com.espertech.esper.common.client.soda.*;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.common.internal.support.SupportBean_S1;
import com.espertech.esper.common.internal.support.SupportBean_S2;
import com.espertech.esper.common.internal.util.SerializableObjectCopier;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class EPLSubselectFiltered {

    public static List<RegressionExecution> executions() {
        List<RegressionExecution> execs = new ArrayList<>();
        execs.add(new EPLSubselectHavingNoAggNoFilterNoWhere());
        execs.add(new EPLSubselectHavingNoAggWWhere());
        execs.add(new EPLSubselectHavingNoAggWFilterWWhere());
        execs.add(new EPLSubselectSameEventCompile());
        execs.add(new EPLSubselectSameEventOM());
        execs.add(new EPLSubselectSameEvent());
        execs.add(new EPLSubselectSelectSceneOne());
        execs.add(new EPLSubselectSelectWildcard());
        execs.add(new EPLSubselectSelectWildcardNoName());
        execs.add(new EPLSubselectWhereConstant());
        execs.add(new EPLSubselectWherePrevious());
        execs.add(new EPLSubselectWherePreviousOM());
        execs.add(new EPLSubselectWherePreviousCompile());
        execs.add(new EPLSubselectSelectWithWhereJoined());
        execs.add(new EPLSubselectSelectWhereJoined2Streams());
        execs.add(new EPLSubselectSelectWhereJoined3Streams());
        execs.add(new EPLSubselectSelectWhereJoined3SceneTwo());
        execs.add(new EPLSubselectSelectWhereJoined4Coercion());
        execs.add(new EPLSubselectSelectWhereJoined4BackCoercion());
        execs.add(new EPLSubselectSelectWithWhere2Subqery());
        execs.add(new EPLSubselectJoinFilteredOne());
        execs.add(new EPLSubselectJoinFilteredTwo());
        execs.add(new EPLSubselectSubselectMixMax());
        execs.add(new EPLSubselectSubselectPrior());
        execs.add(new EPLSubselectWhereClauseMultikeyWArrayPrimitive());
        execs.add(new EPLSubselectWhereClauseMultikeyWArray2Field());
        execs.add(new EPLSubselectWhereClauseMultikeyWArrayComposite());
        return execs;
    }

    private static class EPLSubselectWhereClauseMultikeyWArrayComposite implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select (select id from SupportEventWithManyArray#keepall as sm " +
                "where sm.intOne = se.array and sm.value > se.value) as value from SupportEventWithIntArray as se";
            env.compileDeploy(epl).addListener("s0");

            sendManyArray(env, "MA1", new int[] {1, 2}, 100);
            sendManyArray(env, "MA2", new int[] {1, 2}, 200);
            sendManyArray(env, "MA3", new int[] {1}, 300);
            sendManyArray(env, "MA4", new int[] {1, 2}, 400);

            env.milestone(0);

            sendIntArrayAndAssert(env, "IA2", new int[] {1, 2}, 250, "MA4");
            sendIntArrayAndAssert(env, "IA3", new int[] {1, 2}, 0, null);
            sendIntArrayAndAssert(env, "IA4", new int[] {1}, 299, "MA3");
            sendIntArrayAndAssert(env, "IA5", new int[] {1, 2}, 500, null);

            env.undeployAll();
        }
    }

    private static class EPLSubselectWhereClauseMultikeyWArray2Field implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select (select id from SupportEventWithManyArray#keepall as sm " +
                "where sm.intOne = se.array and sm.value = se.value) as value from SupportEventWithIntArray as se";
            env.compileDeploy(epl).addListener("s0");

            sendManyArray(env, "MA1", new int[] {1, 2}, 10);
            sendManyArray(env, "MA2", new int[] {1, 2}, 11);
            sendManyArray(env, "MA3", new int[] {1}, 12);

            env.milestone(0);

            sendIntArrayAndAssert(env, "IA1", new int[] {1}, 12, "MA3");
            sendIntArrayAndAssert(env, "IA2", new int[] {1, 2}, 11, "MA2");
            sendIntArrayAndAssert(env, "IA3", new int[] {1, 2}, 10, "MA1");
            sendIntArrayAndAssert(env, "IA4", new int[] {1}, 10, null);
            sendIntArrayAndAssert(env, "IA5", new int[] {1, 2}, 12, null);

            env.undeployAll();
        }
    }

    private static class EPLSubselectWhereClauseMultikeyWArrayPrimitive implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select (select id from SupportEventWithManyArray#keepall as sm where sm.intOne = se.array) as value from SupportEventWithIntArray as se";
            env.compileDeploy(epl).addListener("s0");

            sendManyArray(env, "MA1", new int[] {1, 2});
            sendIntArrayAndAssert(env, "IA1", new int[] {1, 2}, "MA1");

            sendManyArray(env, "MA2", new int[] {1, 2});
            sendManyArray(env, "MA3", new int[] {1});
            sendManyArray(env, "MA4", new int[] {});
            sendManyArray(env, "MA5", null);

            env.milestone(0);

            sendIntArrayAndAssert(env, "IA2", new int[] {}, "MA4");
            sendIntArrayAndAssert(env, "IA3", new int[] {1}, "MA3");
            sendIntArrayAndAssert(env, "IA4", null, "MA5");
            sendIntArrayAndAssert(env, "IA5", new int[] {1, 2}, null);

            env.undeployAll();
        }
    }

    private static class EPLSubselectSameEventCompile implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select * from SupportBean_S1#length(1000)) as events1 from SupportBean_S1";
            env.eplToModelCompileDeploy(stmtText).addListener("s0");

            env.assertStatement("s0", statement -> assertEquals(SupportBean_S1.class, statement.getEventType().getPropertyType("events1")));

            Object theEvent = new SupportBean_S1(-1, "Y");
            env.sendEventBean(theEvent);
            env.assertEventNew("s0", event -> assertSame(theEvent, event.get("events1")));

            env.undeployAll();
        }
    }

    private static class EPLSubselectSameEventOM implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            EPStatementObjectModel subquery = new EPStatementObjectModel();
            subquery.setSelectClause(SelectClause.createWildcard());
            subquery.setFromClause(FromClause.create(FilterStream.create("SupportBean_S1").addView(View.create("length", Expressions.constant(1000)))));

            EPStatementObjectModel model = new EPStatementObjectModel();
            model.setFromClause(FromClause.create(FilterStream.create("SupportBean_S1")));
            model.setSelectClause(SelectClause.create().add(Expressions.subquery(subquery), "events1"));
            model = SerializableObjectCopier.copyMayFail(model);

            String stmtText = "select (select * from SupportBean_S1#length(1000)) as events1 from SupportBean_S1";
            assertEquals(stmtText, model.toEPL());

            model.setAnnotations(Collections.singletonList(AnnotationPart.nameAnnotation("s0")));
            env.compileDeploy(model).addListener("s0");

            env.assertStatement("s0", statement -> assertEquals(SupportBean_S1.class, statement.getEventType().getPropertyType("events1")));

            Object theEvent = new SupportBean_S1(-1, "Y");
            env.sendEventBean(theEvent);
            env.assertEventNew("s0", event -> assertSame(theEvent, event.get("events1")));

            env.undeployAll();
        }
    }

    private static class EPLSubselectSameEvent implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select * from SupportBean_S1#length(1000)) as events1 from SupportBean_S1";
            env.compileDeploy(stmtText).addListener("s0");

            env.assertStatement("s0", statement -> assertEquals(SupportBean_S1.class, statement.getEventType().getPropertyType("events1")));

            Object theEvent = new SupportBean_S1(-1, "Y");
            env.sendEventBean(theEvent);
            env.assertEventNew("s0", event -> assertSame(theEvent, event.get("events1")));

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWildcard implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select * from SupportBean_S1#length(1000)) as events1 from SupportBean_S0";
            env.compileDeploy(stmtText).addListener("s0");

            env.assertStatement("s0", statement -> assertEquals(SupportBean_S1.class, statement.getEventType().getPropertyType("events1")));

            Object theEvent = new SupportBean_S1(-1, "Y");
            env.sendEventBean(theEvent);
            env.sendEventBean(new SupportBean_S0(0));

            env.assertEventNew("s0", event -> assertSame(theEvent, event.get("events1")));

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWildcardNoName implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select * from SupportBean_S1#length(1000)) from SupportBean_S0";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            env.assertStatement("s0", statement -> assertEquals(SupportBean_S1.class, statement.getEventType().getPropertyType("subselect_1")));

            Object theEvent = new SupportBean_S1(-1, "Y");
            env.sendEventBean(theEvent);
            env.sendEventBean(new SupportBean_S0(0));

            env.assertEventNew("s0", event -> assertSame(theEvent, event.get("subselect_1")));

            env.undeployAll();
        }
    }

    private static class EPLSubselectWhereConstant implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();

            // single-column constant
            String stmtText = "@name('s0') select (select id from SupportBean_S1#length(1000) where p10='X') as ids1 from SupportBean_S0";
            env.compileDeployAddListenerMile(stmtText, "s0", milestone.getAndIncrement());

            env.sendEventBean(new SupportBean_S1(-1, "Y"));
            env.sendEventBean(new SupportBean_S0(0));
            env.assertEqualsNew("s0", "ids1", null);

            env.sendEventBean(new SupportBean_S1(1, "X"));
            env.sendEventBean(new SupportBean_S1(2, "Y"));
            env.sendEventBean(new SupportBean_S1(3, "Z"));

            env.sendEventBean(new SupportBean_S0(0));
            env.assertEqualsNew("s0", "ids1", 1);

            env.sendEventBean(new SupportBean_S0(1));
            env.assertEqualsNew("s0", "ids1", 1);

            env.sendEventBean(new SupportBean_S1(2, "X"));
            env.sendEventBean(new SupportBean_S0(2));
            env.assertEqualsNew("s0", "ids1", null);
            env.undeployAll();

            // two-column constant
            stmtText = "@name('s0') select (select id from SupportBean_S1#length(1000) where p10='X' and p11='Y') as ids1 from SupportBean_S0";
            env.compileDeployAddListenerMile(stmtText, "s0", milestone.getAndIncrement());

            env.sendEventBean(new SupportBean_S1(1, "X", "Y"));
            env.sendEventBean(new SupportBean_S0(0));
            env.assertEqualsNew("s0", "ids1", 1);
            env.undeployAll();

            // single range
            stmtText = "@name('s0') select (select theString from SupportBean#lastevent where intPrimitive between 10 and 20) as ids1 from SupportBean_S0";
            env.compileDeployAddListenerMile(stmtText, "s0", milestone.getAndIncrement());

            env.sendEventBean(new SupportBean("E1", 15));
            env.sendEventBean(new SupportBean_S0(0));
            env.assertEqualsNew("s0", "ids1", "E1");

            env.undeployAll();
        }
    }

    private static class EPLSubselectWherePrevious implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select prev(1, id) from SupportBean_S1#length(1000) where id=s0.id) as value from SupportBean_S0 as s0";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            runWherePrevious(env);
            env.undeployAll();
        }
    }

    private static class EPLSubselectWherePreviousOM implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            EPStatementObjectModel subquery = new EPStatementObjectModel();
            subquery.setSelectClause(SelectClause.create().add(Expressions.previous(1, "id")));
            subquery.setFromClause(FromClause.create(FilterStream.create("SupportBean_S1").addView(View.create("length", Expressions.constant(1000)))));
            subquery.setWhereClause(Expressions.eqProperty("id", "s0.id"));

            EPStatementObjectModel model = new EPStatementObjectModel();
            model.setFromClause(FromClause.create(FilterStream.create("SupportBean_S0", "s0")));
            model.setSelectClause(SelectClause.create().add(Expressions.subquery(subquery), "value"));
            model = SerializableObjectCopier.copyMayFail(model);

            String stmtText = "select (select prev(1,id) from SupportBean_S1#length(1000) where id=s0.id) as value from SupportBean_S0 as s0";
            assertEquals(stmtText, model.toEPL());

            model.setAnnotations(Collections.singletonList(AnnotationPart.nameAnnotation("s0")));
            env.compileDeploy(model).addListener("s0").milestone(0);

            runWherePrevious(env);

            env.undeployAll();
        }
    }

    private static class EPLSubselectWherePreviousCompile implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select prev(1,id) from SupportBean_S1#length(1000) where id=s0.id) as value from SupportBean_S0 as s0";
            env.eplToModelCompileDeploy(stmtText).addListener("s0").milestone(0);

            runWherePrevious(env);

            env.undeployAll();
        }
    }

    public static class EPLSubselectSelectSceneOne implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"s0price", "s1price"};
            String text = "@name('s0') select irstream s0.price as s0price, " +
                " (select price from SupportMarketDataBean(symbol='S1')#length(10) s1" +
                " where s0.volume = s1.volume) as s1price " +
                " from  SupportMarketDataBean(symbol='S0')#length(2) s0";
            env.compileDeployAddListenerMileZero(text, "s0");

            env.sendEventBean(makeMarketDataEvent("S0", 100, 1));
            env.assertPropsPerRowIRPairFlattened("s0", fields, new Object[][]{{100.0, null}}, null);

            env.milestone(1);

            env.sendEventBean(makeMarketDataEvent("S1", -10, 2));
            env.assertListenerNotInvoked("s0");

            env.milestone(2);

            env.sendEventBean(makeMarketDataEvent("S0", 200, 2));
            env.assertPropsPerRowIRPairFlattened("s0", fields, new Object[][]{{200.0, -10.0}}, null);

            env.milestone(3);

            env.sendEventBean(makeMarketDataEvent("S1", -20, 3));
            env.assertListenerNotInvoked("s0");

            env.milestone(4);

            env.sendEventBean(makeMarketDataEvent("S0", 300, 3));
            env.assertPropsPerRowIRPairFlattened("s0", fields, new Object[][]{{300.0, -20.0}}, new Object[][]{{100.0, null}});

            env.milestone(5);

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWithWhereJoined implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select id from SupportBean_S1#length(1000) where p10=s0.p00) as ids1 from SupportBean_S0 as s0";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            env.sendEventBean(new SupportBean_S0(0));
            env.assertEqualsNew("s0", "ids1", null);

            env.sendEventBean(new SupportBean_S1(1, "X"));
            env.sendEventBean(new SupportBean_S1(2, "Y"));
            env.sendEventBean(new SupportBean_S1(3, "Z"));

            env.sendEventBean(new SupportBean_S0(0));
            env.assertEqualsNew("s0", "ids1", null);

            env.sendEventBean(new SupportBean_S0(0, "X"));
            env.assertEqualsNew("s0", "ids1", 1);
            env.sendEventBean(new SupportBean_S0(0, "Y"));
            env.assertEqualsNew("s0", "ids1", 2);
            env.sendEventBean(new SupportBean_S0(0, "Z"));
            env.assertEqualsNew("s0", "ids1", 3);
            env.sendEventBean(new SupportBean_S0(0, "A"));
            env.assertEqualsNew("s0", "ids1", null);

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWhereJoined2Streams implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select id from SupportBean_S0#length(1000) where p00=s1.p10 and p00=s2.p20) as ids0 from SupportBean_S1#keepall as s1, SupportBean_S2#keepall as s2 where s1.id = s2.id";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            env.sendEventBean(new SupportBean_S1(10, "s0_1"));
            env.sendEventBean(new SupportBean_S2(10, "s0_1"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S0(99, "s0_1"));
            env.sendEventBean(new SupportBean_S1(11, "s0_1"));
            env.sendEventBean(new SupportBean_S2(11, "s0_1"));
            env.assertEqualsNew("s0", "ids0", 99);

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWhereJoined3Streams implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select id from SupportBean_S0#length(1000) where p00=s1.p10 and p00=s3.p30) as ids0 " +
                "from SupportBean_S1#keepall as s1, SupportBean_S2#keepall as s2, SupportBean_S3#keepall as s3 where s1.id = s2.id and s2.id = s3.id";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            env.sendEventBean(new SupportBean_S1(10, "s0_1"));
            env.sendEventBean(new SupportBean_S2(10, "s0_1"));
            env.sendEventBean(new SupportBean_S3(10, "s0_1"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S0(99, "s0_1"));
            env.sendEventBean(new SupportBean_S1(11, "s0_1"));
            env.sendEventBean(new SupportBean_S2(11, "xxx"));
            env.sendEventBean(new SupportBean_S3(11, "s0_1"));
            env.assertEqualsNew("s0", "ids0", 99);

            env.sendEventBean(new SupportBean_S0(98, "s0_2"));
            env.sendEventBean(new SupportBean_S1(12, "s0_x"));
            env.sendEventBean(new SupportBean_S2(12, "s0_2"));
            env.sendEventBean(new SupportBean_S3(12, "s0_1"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S1(13, "s0_2"));
            env.sendEventBean(new SupportBean_S2(13, "s0_2"));
            env.sendEventBean(new SupportBean_S3(13, "s0_x"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S1(14, "s0_2"));
            env.sendEventBean(new SupportBean_S2(14, "xx"));
            env.sendEventBean(new SupportBean_S3(14, "s0_2"));
            env.assertEqualsNew("s0", "ids0", 98);

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWhereJoined3SceneTwo implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select (select id from SupportBean_S0#length(1000) where p00=s1.p10 and p00=s3.p30 and p00=s2.p20) as ids0 " +
                "from SupportBean_S1#keepall as s1, SupportBean_S2#keepall as s2, SupportBean_S3#keepall as s3 where s1.id = s2.id and s2.id = s3.id";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            env.sendEventBean(new SupportBean_S1(10, "s0_1"));
            env.sendEventBean(new SupportBean_S2(10, "s0_1"));
            env.sendEventBean(new SupportBean_S3(10, "s0_1"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S0(99, "s0_1"));
            env.sendEventBean(new SupportBean_S1(11, "s0_1"));
            env.sendEventBean(new SupportBean_S2(11, "xxx"));
            env.sendEventBean(new SupportBean_S3(11, "s0_1"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S0(98, "s0_2"));
            env.sendEventBean(new SupportBean_S1(12, "s0_x"));
            env.sendEventBean(new SupportBean_S2(12, "s0_2"));
            env.sendEventBean(new SupportBean_S3(12, "s0_1"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S1(13, "s0_2"));
            env.sendEventBean(new SupportBean_S2(13, "s0_2"));
            env.sendEventBean(new SupportBean_S3(13, "s0_x"));
            env.assertEqualsNew("s0", "ids0", null);

            env.sendEventBean(new SupportBean_S1(14, "s0_2"));
            env.sendEventBean(new SupportBean_S2(14, "s0_2"));
            env.sendEventBean(new SupportBean_S3(14, "s0_2"));
            env.assertEqualsNew("s0", "ids0", 98);

            env.undeployAll();
        }
    }

    private static class EPLSubselectSelectWhereJoined4Coercion implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();

            String stmtText = "@name('s0') select " +
                "(select intPrimitive from SupportBean(theString='S')#length(1000) " +
                "  where intBoxed=s1.longBoxed and " +
                "intBoxed=s2.doubleBoxed and " +
                "doubleBoxed=s3.intBoxed" +
                ") as ids0 from " +
                "SupportBean(theString='A')#keepall as s1, " +
                "SupportBean(theString='B')#keepall as s2, " +
                "SupportBean(theString='C')#keepall as s3 " +
                "where s1.intPrimitive = s2.intPrimitive and s2.intPrimitive = s3.intPrimitive";
            trySelectWhereJoined4Coercion(env, milestone, stmtText);

            stmtText = "@name('s0') select " +
                "(select intPrimitive from SupportBean(theString='S')#length(1000) " +
                "  where doubleBoxed=s3.intBoxed and " +
                "intBoxed=s2.doubleBoxed and " +
                "intBoxed=s1.longBoxed" +
                ") as ids0 from " +
                "SupportBean(theString='A')#keepall as s1, " +
                "SupportBean(theString='B')#keepall as s2, " +
                "SupportBean(theString='C')#keepall as s3 " +
                "where s1.intPrimitive = s2.intPrimitive and s2.intPrimitive = s3.intPrimitive";
            trySelectWhereJoined4Coercion(env, milestone, stmtText);

            stmtText = "@name('s0') select " +
                "(select intPrimitive from SupportBean(theString='S')#length(1000) " +
                "  where doubleBoxed=s3.intBoxed and " +
                "intBoxed=s1.longBoxed and " +
                "intBoxed=s2.doubleBoxed" +
                ") as ids0 from " +
                "SupportBean(theString='A')#keepall as s1, " +
                "SupportBean(theString='B')#keepall as s2, " +
                "SupportBean(theString='C')#keepall as s3 " +
                "where s1.intPrimitive = s2.intPrimitive and s2.intPrimitive = s3.intPrimitive";
            trySelectWhereJoined4Coercion(env, milestone, stmtText);
        }
    }

    private static class EPLSubselectSelectWhereJoined4BackCoercion implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();

            String stmtText = "@name('s0') select " +
                "(select intPrimitive from SupportBean(theString='S')#length(1000) " +
                "  where longBoxed=s1.intBoxed and " +
                "longBoxed=s2.doubleBoxed and " +
                "intBoxed=s3.longBoxed" +
                ") as ids0 from " +
                "SupportBean(theString='A')#keepall as s1, " +
                "SupportBean(theString='B')#keepall as s2, " +
                "SupportBean(theString='C')#keepall as s3 " +
                "where s1.intPrimitive = s2.intPrimitive and s2.intPrimitive = s3.intPrimitive";
            trySelectWhereJoined4CoercionBack(env, milestone, stmtText);

            stmtText = "@name('s0') select " +
                "(select intPrimitive from SupportBean(theString='S')#length(1000) " +
                "  where longBoxed=s2.doubleBoxed and " +
                "intBoxed=s3.longBoxed and " +
                "longBoxed=s1.intBoxed " +
                ") as ids0 from " +
                "SupportBean(theString='A')#keepall as s1, " +
                "SupportBean(theString='B')#keepall as s2, " +
                "SupportBean(theString='C')#keepall as s3 " +
                "where s1.intPrimitive = s2.intPrimitive and s2.intPrimitive = s3.intPrimitive";
            trySelectWhereJoined4CoercionBack(env, milestone, stmtText);
        }
    }

    private static class EPLSubselectSelectWithWhere2Subqery implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select id from SupportBean_S0 as s0 where " +
                " id = (select id from SupportBean_S1#length(1000) where s0.id = id) or id = (select id from SupportBean_S2#length(1000) where s0.id = id)";
            env.compileDeployAddListenerMileZero(stmtText, "s0");

            env.sendEventBean(new SupportBean_S0(0));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean_S1(1));
            env.sendEventBean(new SupportBean_S0(1));
            env.assertEqualsNew("s0", "id", 1);

            env.sendEventBean(new SupportBean_S2(2));
            env.sendEventBean(new SupportBean_S0(2));
            env.assertEqualsNew("s0", "id", 2);

            env.sendEventBean(new SupportBean_S0(3));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean_S1(3));
            env.sendEventBean(new SupportBean_S0(3));
            env.assertEqualsNew("s0", "id", 3);

            env.undeployAll();
        }
    }

    private static class EPLSubselectJoinFilteredOne implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.id as s0id, s1.id as s1id, " +
                "(select p20 from SupportBean_S2#length(1000) where id=s0.id) as s2p20, " +
                "(select prior(1, p20) from SupportBean_S2#length(1000) where id=s0.id) as s2p20Prior, " +
                "(select prev(1, p20) from SupportBean_S2#length(10) where id=s0.id) as s2p20Prev " +
                "from SupportBean_S0#keepall as s0, SupportBean_S1#keepall as s1 " +
                "where s0.id = s1.id and p00||p10 = (select p20 from SupportBean_S2#length(1000) where id=s0.id)";
            tryJoinFiltered(env, stmtText);
        }
    }

    private static class EPLSubselectJoinFilteredTwo implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.id as s0id, s1.id as s1id, " +
                "(select p20 from SupportBean_S2#length(1000) where id=s0.id) as s2p20, " +
                "(select prior(1, p20) from SupportBean_S2#length(1000) where id=s0.id) as s2p20Prior, " +
                "(select prev(1, p20) from SupportBean_S2#length(10) where id=s0.id) as s2p20Prev " +
                "from SupportBean_S0#keepall as s0, SupportBean_S1#keepall as s1 " +
                "where s0.id = s1.id and (select s0.p00||s1.p10 = p20 from SupportBean_S2#length(1000) where id=s0.id)";
            tryJoinFiltered(env, stmtText);
        }
    }

    private static class EPLSubselectSubselectPrior implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "insert into Pair " +
                "select * from SupportSensorEvent(device='A')#lastevent as a, SupportSensorEvent(device='B')#lastevent as b " +
                "where a.type = b.type;\n" +
                "" +
                "insert into PairDuplicatesRemoved select * from Pair(1=2);\n" +
                "" +
                "@name('s0') insert into PairDuplicatesRemoved " +
                "select * from Pair " +
                "where a.id != coalesce((select a.id from PairDuplicatesRemoved#lastevent), -1)" +
                "  and b.id != coalesce((select b.id from PairDuplicatesRemoved#lastevent), -1);\n";
            env.compileDeployAddListenerMileZero(epl, "s0");

            env.sendEventBean(new SupportSensorEvent(1, "Temperature", "A", 51, 94.5));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportSensorEvent(2, "Temperature", "A", 57, 95.5));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportSensorEvent(3, "Humidity", "B", 29, 67.5));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportSensorEvent(4, "Temperature", "B", 55, 88.0));
            env.assertEventNew("s0", theEvent -> {
                assertEquals(2, theEvent.get("a.id"));
                assertEquals(4, theEvent.get("b.id"));
            });

            env.sendEventBean(new SupportSensorEvent(5, "Temperature", "B", 65, 85.0));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportSensorEvent(6, "Temperature", "B", 49, 87.0));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportSensorEvent(7, "Temperature", "A", 51, 99.5));
            env.assertEventNew("s0", theEvent -> {
                assertEquals(7, theEvent.get("a.id"));
                assertEquals(6, theEvent.get("b.id"));
            });

            env.undeployAll();
        }
    }

    private static class EPLSubselectSubselectMixMax implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtTextOne =
                "@name('s0') select " +
                    " (select * from SupportSensorEvent#sort(1, measurement desc)) as high, " +
                    " (select * from SupportSensorEvent#sort(1, measurement asc)) as low " +
                    " from SupportSensorEvent";
            env.compileDeployAddListenerMileZero(stmtTextOne, "s0");

            env.sendEventBean(new SupportSensorEvent(1, "Temp", "Dev1", 68.0, 96.5));
            assertHighLow(env, 68, 68);

            env.sendEventBean(new SupportSensorEvent(2, "Temp", "Dev2", 70.0, 98.5));
            assertHighLow(env, 70, 68);

            env.sendEventBean(new SupportSensorEvent(3, "Temp", "Dev2", 65.0, 99.5));
            assertHighLow(env, 70, 65);

            env.undeployAll();
        }

        private void assertHighLow(RegressionEnvironment env, double highExpected, double lowExpected) {
            env.assertEventNew("s0", theEvent -> {
                double high = ((SupportSensorEvent) theEvent.get("high")).getMeasurement();
                double low = ((SupportSensorEvent) theEvent.get("low")).getMeasurement();
                assertEquals(highExpected, high, 0d);
                assertEquals(lowExpected, low, 0d);
            });

        }
    }

    private static class EPLSubselectHavingNoAggWFilterWWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select (select intPrimitive from SupportBean(intPrimitive < 20) #keepall where intPrimitive > 15 having theString = 'ID1') as c0 from SupportBean_S0";
            env.compileDeployAddListenerMileZero(epl, "s0");

            sendS0AndAssert(env, null);
            sendSBAndS0Assert(env, "ID2", 10, null);
            sendSBAndS0Assert(env, "ID1", 11, null);
            sendSBAndS0Assert(env, "ID1", 20, null);
            sendSBAndS0Assert(env, "ID1", 19, 19);

            env.undeployAll();
        }
    }

    private static class EPLSubselectHavingNoAggWWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select (select intPrimitive from SupportBean#keepall where intPrimitive > 15 having theString = 'ID1') as c0 from SupportBean_S0";
            env.compileDeployAddListenerMileZero(epl, "s0");

            sendS0AndAssert(env, null);
            sendSBAndS0Assert(env, "ID2", 10, null);
            sendSBAndS0Assert(env, "ID1", 11, null);
            sendSBAndS0Assert(env, "ID1", 20, 20);

            env.undeployAll();
        }
    }

    private static class EPLSubselectHavingNoAggNoFilterNoWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select (select intPrimitive from SupportBean#keepall having theString = 'ID1') as c0 from SupportBean_S0";
            env.compileDeployAddListenerMileZero(epl, "s0");

            sendS0AndAssert(env, null);
            sendSBAndS0Assert(env, "ID2", 10, null);
            sendSBAndS0Assert(env, "ID1", 11, 11);

            env.undeployAll();
        }
    }

    private static void trySelectWhereJoined4CoercionBack(RegressionEnvironment env, AtomicInteger milestone, String stmtText) {
        env.compileDeployAddListenerMile(stmtText, "s0", milestone.getAndIncrement());

        sendBean(env, "A", 1, 10, 200, 3000);        // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "B", 1, 10, 200, 3000);
        sendBean(env, "C", 1, 10, 200, 3000);
        env.assertEqualsNew("s0", "ids0", null);

        sendBean(env, "S", -1, 11, 201, 0);     // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "A", 2, 201, 0, 0);
        sendBean(env, "B", 2, 0, 0, 201);
        sendBean(env, "C", 2, 0, 11, 0);
        env.assertEqualsNew("s0", "ids0", -1);

        sendBean(env, "S", -2, 12, 202, 0);     // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "A", 3, 202, 0, 0);
        sendBean(env, "B", 3, 0, 0, 202);
        sendBean(env, "C", 3, 0, -1, 0);
        env.assertEqualsNew("s0", "ids0", null);

        sendBean(env, "S", -3, 13, 203, 0);     // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "A", 4, 203, 0, 0);
        sendBean(env, "B", 4, 0, 0, 203.0001);
        sendBean(env, "C", 4, 0, 13, 0);
        env.assertEqualsNew("s0", "ids0", null);

        sendBean(env, "S", -4, 14, 204, 0);     // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "A", 5, 205, 0, 0);
        sendBean(env, "B", 5, 0, 0, 204);
        sendBean(env, "C", 5, 0, 14, 0);
        env.assertEqualsNew("s0", "ids0", null);

        env.undeployAll();
    }

    private static void trySelectWhereJoined4Coercion(RegressionEnvironment env, AtomicInteger milestone, String stmtText) {
        env.compileDeployAddListenerMile(stmtText, "s0", milestone.getAndIncrement());

        sendBean(env, "A", 1, 10, 200, 3000);        // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "B", 1, 10, 200, 3000);
        sendBean(env, "C", 1, 10, 200, 3000);
        env.assertEqualsNew("s0", "ids0", null);

        sendBean(env, "S", -2, 11, 0, 3001);
        sendBean(env, "A", 2, 0, 11, 0);        // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "B", 2, 0, 0, 11);
        sendBean(env, "C", 2, 3001, 0, 0);
        env.assertEqualsNew("s0", "ids0", -2);

        sendBean(env, "S", -3, 12, 0, 3002);
        sendBean(env, "A", 3, 0, 12, 0);        // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "B", 3, 0, 0, 12);
        sendBean(env, "C", 3, 3003, 0, 0);
        env.assertEqualsNew("s0", "ids0", null);

        sendBean(env, "S", -4, 11, 0, 3003);
        sendBean(env, "A", 4, 0, 0, 0);        // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "B", 4, 0, 0, 11);
        sendBean(env, "C", 4, 3003, 0, 0);
        env.assertEqualsNew("s0", "ids0", null);

        sendBean(env, "S", -5, 14, 0, 3004);
        sendBean(env, "A", 5, 0, 14, 0);        // intPrimitive, intBoxed, longBoxed, doubleBoxed
        sendBean(env, "B", 5, 0, 0, 11);
        sendBean(env, "C", 5, 3004, 0, 0);
        env.assertEqualsNew("s0", "ids0", null);

        env.undeployAll();
    }

    private static void tryJoinFiltered(RegressionEnvironment env, String stmtText) {
        env.compileDeployAddListenerMileZero(stmtText, "s0");

        env.sendEventBean(new SupportBean_S0(0, "X"));
        env.sendEventBean(new SupportBean_S1(0, "Y"));
        env.assertListenerNotInvoked("s0");

        env.sendEventBean(new SupportBean_S2(1, "ab"));
        env.sendEventBean(new SupportBean_S0(1, "a"));
        env.sendEventBean(new SupportBean_S1(1, "b"));
        env.assertEventNew("s0", theEvent -> {
            assertEquals(1, theEvent.get("s0id"));
            assertEquals(1, theEvent.get("s1id"));
            assertEquals("ab", theEvent.get("s2p20"));
            assertEquals(null, theEvent.get("s2p20Prior"));
            assertEquals(null, theEvent.get("s2p20Prev"));
        });

        env.sendEventBean(new SupportBean_S2(2, "qx"));
        env.sendEventBean(new SupportBean_S0(2, "q"));
        env.sendEventBean(new SupportBean_S1(2, "x"));
        env.assertEventNew("s0", theEvent -> {
            assertEquals(2, theEvent.get("s0id"));
            assertEquals(2, theEvent.get("s1id"));
            assertEquals("qx", theEvent.get("s2p20"));
            assertEquals("ab", theEvent.get("s2p20Prior"));
            assertEquals("ab", theEvent.get("s2p20Prev"));
        });

        env.undeployAll();
    }

    private static void runWherePrevious(RegressionEnvironment env) {
        env.sendEventBean(new SupportBean_S1(1));
        env.sendEventBean(new SupportBean_S0(0));
        env.assertEqualsNew("s0", "value", null);

        env.sendEventBean(new SupportBean_S1(2));
        env.sendEventBean(new SupportBean_S0(2));
        env.assertEqualsNew("s0", "value", 1);

        env.sendEventBean(new SupportBean_S1(3));
        env.sendEventBean(new SupportBean_S0(3));
        env.assertEqualsNew("s0", "value", 2);
    }

    private static void sendBean(RegressionEnvironment env, String theString, int intPrimitive, int intBoxed, long longBoxed, double doubleBoxed) {
        SupportBean bean = new SupportBean();
        bean.setTheString(theString);
        bean.setIntPrimitive(intPrimitive);
        bean.setIntBoxed(intBoxed);
        bean.setLongBoxed(longBoxed);
        bean.setDoubleBoxed(doubleBoxed);
        env.sendEventBean(bean);
    }

    private static void sendSBAndS0Assert(RegressionEnvironment env, String theString, int intPrimitive, Integer expected) {
        env.sendEventBean(new SupportBean(theString, intPrimitive));
        sendS0AndAssert(env, expected);
    }

    private static void sendS0AndAssert(RegressionEnvironment env, Integer expected) {
        env.sendEventBean(new SupportBean_S0(0));
        env.assertEqualsNew("s0", "c0", expected);
    }

    private static SupportMarketDataBean makeMarketDataEvent(String symbol, double price, long volume) {
        return new SupportMarketDataBean(symbol, price, volume, null);
    }

    private static void sendManyArray(RegressionEnvironment env, String id, int[] ints, int value) {
        env.sendEventBean(new SupportEventWithManyArray(id).withIntOne(ints).withValue(value));
    }

    private static void sendIntArrayAndAssert(RegressionEnvironment env, String id, int[] array, int value, String expected) {
        env.sendEventBean(new SupportEventWithIntArray(id, array, value));
        env.assertEqualsNew("s0", "value", expected);
    }

    private static void sendIntArray(RegressionEnvironment env, String id, int[] array) {
        env.sendEventBean(new SupportEventWithIntArray(id, array));
    }

    private static void sendManyArray(RegressionEnvironment env, String id, int[] ints) {
        env.sendEventBean(new SupportEventWithManyArray(id).withIntOne(ints));
    }

    private static void sendIntArrayAndAssert(RegressionEnvironment env, String id, int[] array, String expected) {
        env.sendEventBean(new SupportEventWithIntArray(id, array));
        env.assertEqualsNew("s0", "value", expected);
    }
}
