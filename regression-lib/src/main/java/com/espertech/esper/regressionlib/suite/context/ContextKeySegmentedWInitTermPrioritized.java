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
package com.espertech.esper.regressionlib.suite.context;

import com.espertech.esper.common.client.context.ContextPartitionCollection;
import com.espertech.esper.common.client.context.ContextPartitionIdentifierPartitioned;
import com.espertech.esper.common.client.context.ContextPartitionSelectorAll;
import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.client.util.DateTime;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.common.internal.support.SupportBean_S1;
import com.espertech.esper.common.internal.support.SupportBean_S2;
import com.espertech.esper.common.internal.util.CollectionUtil;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.bean.ISupportABCImpl;
import com.espertech.esper.regressionlib.support.bean.ISupportAImpl;
import com.espertech.esper.regressionlib.support.bean.ISupportBImpl;
import com.espertech.esper.regressionlib.support.context.SupportContextPropUtil;
import com.espertech.esper.regressionlib.support.filter.SupportFilterServiceHelper;

import java.util.ArrayList;
import java.util.Collection;

import static com.espertech.esper.regressionlib.support.filter.SupportFilterServiceHelper.assertFilterSvcCount;
import static org.junit.Assert.assertEquals;

public class ContextKeySegmentedWInitTermPrioritized {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ContextKeySegmentedTermByFilter(false));
        execs.add(new ContextKeySegmentedTermByFilter(true));
        execs.add(new ContextKeySegmentedTermByFilterWSubtype());
        execs.add(new ContextKeySegmentedTermByFilterWSecondType());
        execs.add(new ContextKeySegmentedTermByAfter());
        execs.add(new ContextKeySegmentedTermByCrontabOutputWhenTerminated());
        execs.add(new ContextKeySegmentedTermByPatternTwoFilters());
        execs.add(new ContextKeySegmentedTermByUnrelated());
        execs.add(new ContextKeySegmentedTermByFilter2Keys());
        execs.add(new ContextKeySegmentedFilterExprTermByFilterWExpr());
        execs.add(new ContextKeySegmentedFilterExprTermByFilter());
        execs.add(new ContextKeySegmentedTermByPattern3Partition());
        execs.add(new ContextKeySegmentedInitTermNoPartitionFilter());
        execs.add(new ContextKeySegmentedInitTermWithPartitionFilter());
        execs.add(new ContextKeySegmentedInitTermWithTwoInit(true));
        execs.add(new ContextKeySegmentedInitTermWithTwoInit(false));
        execs.add(new ContextKeySegmentedInitNoTerm(true));
        execs.add(new ContextKeySegmentedInitNoTerm(false));
        execs.add(new ContextKeySegmentedInitWCorrelatedTermFilter());
        execs.add(new ContextKeySegmentedInitWCorrelatedTermPattern());
        execs.add(new ContextKeySegmentedWithCorrelatedTermFilter(true));
        execs.add(new ContextKeySegmentedWithCorrelatedTermFilter(false));
        execs.add(new ContextKeySegmentedInvalid());
        return execs;
    }

    private static class ContextKeySegmentedWithCorrelatedTermFilter implements RegressionExecution {
        private final boolean soda;

        public ContextKeySegmentedWithCorrelatedTermFilter(boolean soda) {
            this.soda = soda;
        }

        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String epl = "@Name('ctx') @public create context CtxPartitionWCorrTerm as " +
                "partition by theString from SupportBean as sb " +
                "terminated by SupportBean(intPrimitive=sb.intPrimitive)";
            env.compileDeploy(soda, epl, path);
            env.compileDeploy("@name('s0') context CtxPartitionWCorrTerm select theString, sum(intPrimitive) as theSum from SupportBean output last when terminated", path);
            env.addListener("s0");

            String[] fields = "theString,theSum".split(",");

            env.sendEventBean(new SupportBean("A", 10));
            env.sendEventBean(new SupportBean("B", 99));
            env.sendEventBean(new SupportBean("C", -1));

            env.milestone(0);

            env.sendEventBean(new SupportBean("A", 2));
            env.sendEventBean(new SupportBean("B", 3));
            env.sendEventBean(new SupportBean("C", 4));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("A", 10));
            env.assertPropsNew("s0", fields, new Object[]{"A", 10 + 2});

            env.milestone(2);

            env.sendEventBean(new SupportBean("C", -1));
            env.assertPropsNew("s0", fields, new Object[]{"C", -1 + 4});

            env.sendEventBean(new SupportBean("A", 11));
            env.sendEventBean(new SupportBean("A", 12));

            env.milestone(3);

            env.sendEventBean(new SupportBean("B", 99));
            env.assertPropsNew("s0", fields, new Object[]{"B", 99 + 3});

            env.milestone(4);

            env.sendEventBean(new SupportBean("A", 11));
            env.assertPropsNew("s0", fields, new Object[]{"A", 11 + 12});

            assertFilterSvcCount(env, 1, "ctx");
            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "soda=" + soda +
                '}';
        }
    }

    private static class ContextKeySegmentedInitWCorrelatedTermFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String epl = "@name('ctx') @public create context CtxPartitionInitWCorrTerm " +
                "partition by theString from SupportBean " +
                "initiated by SupportBean(boolPrimitive=true) as sb " +
                "terminated by SupportBean(boolPrimitive=false, intPrimitive=sb.intPrimitive)";
            env.compileDeploy(epl, path);

            env.compileDeploy("@name('s0') context CtxPartitionInitWCorrTerm select theString, sum(longPrimitive) as theSum from SupportBean output last when terminated", path);
            env.addListener("s0");
            String[] fields = "theString,theSum".split(",");

            SupportBean initA = sendBean(env, "A", 100, 1, true);

            env.milestone(0);

            sendBean(env, "B", 99, 2, false);
            SupportBean initB = sendBean(env, "B", 200, 3, true);
            sendBean(env, "A", 0, 4, false);
            sendBean(env, "B", 0, 5, false);
            sendBean(env, "A", 0, 6, true);
            env.assertListenerNotInvoked("s0");
            assertPartitionsInitWCorrelatedTermFilter(env);
            SupportContextPropUtil.assertContextProps(env, "ctx", "CtxPartitionInitWCorrTerm", new int[]{0, 1}, "key1,sb", new Object[][]{{"A", initA}, {"B", initB}});

            env.milestone(1);

            sendBean(env, "B", 200, 7, false);
            env.assertPropsNew("s0", fields, new Object[]{"B", 3 + 5L});

            env.milestone(2);

            sendBean(env, "A", 100, 8, false);
            env.assertPropsNew("s0", fields, new Object[]{"A", 1 + 4 + 6L});

            assertFilterSvcCount(env, 1, "ctx");
            env.undeployAll();
        }

        private static void assertPartitionsInitWCorrelatedTermFilter(RegressionEnvironment env) {
            env.assertThat(() -> {
                ContextPartitionCollection partitions = env.runtime().getContextPartitionService().getContextPartitions(env.deploymentId("ctx"), "CtxPartitionInitWCorrTerm", ContextPartitionSelectorAll.INSTANCE);
                assertEquals(2, partitions.getIdentifiers().size());
                ContextPartitionIdentifierPartitioned first = (ContextPartitionIdentifierPartitioned) partitions.getIdentifiers().get(0);
                ContextPartitionIdentifierPartitioned second = (ContextPartitionIdentifierPartitioned) partitions.getIdentifiers().get(1);
                EPAssertionUtil.assertEqualsExactOrder(new Object[]{"A"}, first.getKeys());
                EPAssertionUtil.assertEqualsExactOrder(new Object[]{"B"}, second.getKeys());
            });
        }
    }

    private static class ContextKeySegmentedInitWCorrelatedTermPattern implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String epl = "@name('ctx') @public create context CtxPartitionInitWCorrTerm " +
                "partition by p20 from SupportBean_S2, p10 from SupportBean_S1, p00 from SupportBean_S0 " +
                "initiated by SupportBean_S0 as s0, SupportBean_S1 as s1 " +
                "terminated by pattern[SupportBean_S0(id=s0.id) or SupportBean_S1(id=s1.id)]";
            env.compileDeploy(epl, path);

            env.compileDeploy("@name('s0') context CtxPartitionInitWCorrTerm select context.s0 as ctx0, context.s1 as ctx1, context.s0.id as ctx0id, context.s1.id as ctx1id, p20, sum(id) as theSum from SupportBean_S2 output last when terminated", path);

            env.addListener("s0");
            String[] fields = "ctx0id,ctx1id,p20,theSum".split(",");

            env.assertStatement("s0", statement -> {
                assertEquals(SupportBean_S0.class, statement.getEventType().getPropertyType("ctx0"));
                assertEquals(SupportBean_S1.class, statement.getEventType().getPropertyType("ctx1"));
            });

            env.sendEventBean(new SupportBean_S0(1, "A"));
            env.sendEventBean(new SupportBean_S1(2, "B"));
            env.sendEventBean(new SupportBean_S2(10, "A"));
            env.sendEventBean(new SupportBean_S2(11, "A"));
            env.sendEventBean(new SupportBean_S1(1, "A"));
            env.sendEventBean(new SupportBean_S0(2, "A"));
            env.sendEventBean(new SupportBean_S2(12, "B"));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean_S0(1, "A"));
            env.assertPropsNew("s0", fields, new Object[]{1, null, "A", 21});

            env.sendEventBean(new SupportBean_S1(2, "B"));
            env.assertPropsNew("s0", fields, new Object[]{null, 2, "B", 12});

            assertFilterSvcCount(env, 2, "ctx");
            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedInitNoTerm implements RegressionExecution {
        private final boolean soda;

        public ContextKeySegmentedInitNoTerm(boolean soda) {
            this.soda = soda;
        }

        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String epl = "@public create context CtxInitS0PositiveId as " +
                "partition by p00 and p01 from SupportBean_S0 " +
                "initiated by SupportBean_S0(id>0) as s0";
            env.compileDeploy(soda, epl, path);
            env.compileDeploy("@Name('s0') context CtxInitS0PositiveId select p00, p01, context.s0 as s0, sum(id) as theSum from SupportBean_S0", path);
            env.addListener("s0");

            env.assertStatement("s0", statement -> assertEquals(SupportBean_S0.class, statement.getEventType().getPropertyType("s0")));

            sendS0AssertNone(env, 0, "A", "G1");
            sendS0AssertNone(env, -1, "B", "G1");

            env.milestone(0);

            SupportBean_S0 s0BG1 = sendS0Assert(10, null, env, 10, "B", "G1");
            sendS0Assert(9, s0BG1, env, -1, "B", "G1");

            env.milestone(1);

            SupportBean_S0 s0AG1 = sendS0Assert(2, null, env, 2, "A", "G1");

            env.milestone(2);

            SupportBean_S0 s0AG2 = sendS0Assert(3, null, env, 3, "A", "G2");

            env.milestone(3);

            sendS0Assert(7, s0AG2, env, 4, "A", "G2");
            sendS0Assert(8, s0AG1, env, 6, "A", "G1");

            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "soda=" + soda +
                '}';
        }
    }

    private static class ContextKeySegmentedInitTermWithTwoInit implements RegressionExecution {
        private final boolean soda;

        public ContextKeySegmentedInitTermWithTwoInit(boolean soda) {
            this.soda = soda;
        }

        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String epl = "@name('ctx') @public create context CtxTwoInitTerm as " +
                "partition by p01 from SupportBean_S0, p11 from SupportBean_S1, p21 from SupportBean_S2 " +
                "initiated by SupportBean_S0(p00=\"a\"), SupportBean_S1(p10=\"b\") " +
                "terminated by SupportBean_S2(p20=\"z\")";
            env.compileDeploy(soda, epl, path);
            env.compileDeploy("@name('s0') context CtxTwoInitTerm select p21, count(*) as cnt from SupportBean_S2 output last when terminated", path);

            env.addListener("s0");
            String[] fields = "p21,cnt".split(",");

            sendS2(env, "b", "A");
            sendS2(env, "a", "A");
            sendS0(env, "b", "A");
            sendS1(env, "a", "A");
            sendS2(env, "z", "A");
            sendS1(env, "b", "B");
            sendS0(env, "a", "C");
            sendS2(env, "", "B");
            env.assertListenerNotInvoked("s0");

            env.milestone(0);

            sendS2(env, "z", "B");
            env.assertPropsNew("s0", fields, new Object[]{"B", 1L});

            env.milestone(1);

            sendS2(env, "z", "C");
            env.assertPropsNew("s0", fields, new Object[]{"C", 0L});

            assertFilterSvcCount(env, 2, "ctx");
            env.undeployModuleContaining("s0");

            env.milestone(2);

            assertFilterSvcCount(env, 0, "ctx");
            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "soda=" + soda +
                '}';
        }
    }

    private static class ContextKeySegmentedInitTermWithPartitionFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "create context CtxStringZeroTo1k as " +
                "partition by theString from SupportBean(intPrimitive > 0) " +
                "initiated by SupportBean(intPrimitive=0)" +
                "terminated by SupportBean(intPrimitive=1000);\n" +
                "@name('s0') context CtxStringZeroTo1k select theString, sum(intPrimitive) as theSum from SupportBean output last when terminated;\n";
            env.compileDeploy(epl).addListener("s0");
            String[] fields = "theString,theSum".split(",");

            env.sendEventBean(new SupportBean("A", 20));
            env.sendEventBean(new SupportBean("A", 1000));
            env.sendEventBean(new SupportBean("B", 0));

            env.milestone(0);

            env.sendEventBean(new SupportBean("B", 30));
            env.sendEventBean(new SupportBean("B", -100));
            env.sendEventBean(new SupportBean("A", 1000));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("B", 1000));
            env.assertPropsNew("s0", fields, new Object[]{"B", 30});

            env.milestone(2);

            env.sendEventBean(new SupportBean("A", 0));
            env.sendEventBean(new SupportBean("A", 40));

            env.milestone(3);

            env.sendEventBean(new SupportBean("A", 50));
            env.sendEventBean(new SupportBean("A", -20));
            env.assertListenerNotInvoked("s0");

            env.milestone(4);

            env.sendEventBean(new SupportBean("A", 1000));
            env.assertPropsNew("s0", fields, new Object[]{"A", 90});

            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedInvalid implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl;

            // invalid initiated-by type
            epl = "create context InvalidCtx partition by theString from SupportBean initiated by SupportBean_S0";
            env.tryInvalidCompile(epl, "Segmented context 'InvalidCtx' requires that all of the event types that are listed in the initialized-by also appear in the partition-by, type 'SupportBean_S0' is not one of the types listed in partition-by");

            // cannot assign name in different places
            epl = "create context InvalidCtx partition by p00 from SupportBean_S0 as n1 initiated by SupportBean_S0 as n2";
            env.tryInvalidCompile(epl, "Segmented context 'InvalidCtx' requires that either partition-by or initialized-by assign stream names, but not both");

            // name assigned is already used
            String message = "Name 'a' already used for type 'SupportBean_S0'";
            epl = "create context InvalidCtx partition by p00 from SupportBean_S0, p10 from SupportBean_S1 initiated by SupportBean_S0 as a, SupportBean_S1 as a";
            env.tryInvalidCompile(epl, message);
            epl = "create context InvalidCtx partition by p00 from SupportBean_S0 as a, p10 from SupportBean_S1 as a";
            env.tryInvalidCompile(epl, message);
        }
    }

    private static class ContextKeySegmentedInitTermNoPartitionFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@name('ctx') @public create context CtxStringZeroTo1k as " +
                "partition by theString from SupportBean " +
                "initiated by SupportBean(intPrimitive=0)" +
                "terminated by SupportBean(intPrimitive=1000)", path);
            env.compileDeploy("@name('s0') context CtxStringZeroTo1k select theString, sum(intPrimitive) as theSum from SupportBean output last when terminated", path);

            env.addListener("s0");
            String[] fields = "theString,theSum".split(",");

            env.sendEventBean(new SupportBean("A", 20));
            env.sendEventBean(new SupportBean("A", 1000));

            env.milestone(0);

            env.sendEventBean(new SupportBean("B", 0));

            env.milestone(1);

            env.sendEventBean(new SupportBean("B", 30));
            env.sendEventBean(new SupportBean("A", 1000));
            env.assertListenerNotInvoked("s0");

            env.milestone(2);

            env.sendEventBean(new SupportBean("B", 1000));
            env.assertPropsNew("s0", fields, new Object[]{"B", 30});

            env.sendEventBean(new SupportBean("C", 1000));

            env.milestone(3);

            env.sendEventBean(new SupportBean("C", -1));
            env.sendEventBean(new SupportBean("C", 1000));
            env.sendEventBean(new SupportBean("C", 0));

            env.milestone(4);

            env.sendEventBean(new SupportBean("A", 0));

            env.milestone(5);

            env.sendEventBean(new SupportBean("A", 40));
            env.assertListenerNotInvoked("s0");

            env.milestone(6);

            env.sendEventBean(new SupportBean("C", 1000));
            env.assertPropsNew("s0", fields, new Object[]{"C", 0});

            env.milestone(7);

            env.sendEventBean(new SupportBean("A", 1000));
            env.assertPropsNew("s0", fields, new Object[]{"A", 40});

            assertFilterSvcCount(env, 1, "ctx");
            env.undeployModuleContaining("s0");
            assertFilterSvcCount(env, 0, "ctx");
            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByPattern3Partition implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@name('ctx') @public create context Ctx3Typed as " +
                "partition by p00 from SupportBean_S0, p10 from SupportBean_S1, p20 from SupportBean_S2 " +
                "terminated by pattern[SupportBean_S1 -> SupportBean_S2]", path);
            env.compileDeploy("@name('s0') context Ctx3Typed select p00, count(*) as cnt from SupportBean_S0 output last when terminated", path);

            env.addListener("s0");
            String[] fields = "p00,cnt".split(",");

            env.sendEventBean(new SupportBean_S0(0, "A"));
            env.sendEventBean(new SupportBean_S0(0, "B"));
            env.sendEventBean(new SupportBean_S1(0, "B"));

            env.milestone(0);

            env.sendEventBean(new SupportBean_S2(0, "A"));
            env.sendEventBean(new SupportBean_S0(0, "B"));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean_S2(0, "B"));
            env.assertPropsNew("s0", fields, new Object[]{"B", 2L});

            env.sendEventBean(new SupportBean_S1(0, "A"));
            env.assertListenerNotInvoked("s0");

            env.milestone(2);

            env.sendEventBean(new SupportBean_S0(0, "A"));
            env.sendEventBean(new SupportBean_S0(0, "A"));

            env.milestone(3);

            env.sendEventBean(new SupportBean_S2(0, "A"));
            env.assertPropsNew("s0", fields, new Object[]{"A", 3L});

            env.assertThat(() -> assertEquals(3, SupportFilterServiceHelper.getFilterSvcCountApprox(env)));
            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByFilter2Keys implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@name('ctx') @public create context TwoKeyPartition " +
                "partition by theString, intPrimitive from SupportBean terminated by SupportBean(boolPrimitive = false)", path);
            env.compileDeploy("@name('s0') context TwoKeyPartition select theString, intPrimitive, sum(longPrimitive) as thesum from SupportBean output last when terminated", path);

            env.addListener("s0");
            String[] fields = "theString,intPrimitive,thesum".split(",");

            sendBean(env, "A", 1, 10, true);
            sendBean(env, "B", 1, 11, true);
            sendBean(env, "A", 2, 12, true);
            sendBean(env, "B", 2, 13, true);

            env.milestone(0);

            sendBean(env, "B", 1, 20, true);
            sendBean(env, "A", 1, 30, true);
            sendBean(env, "A", 2, 40, true);
            sendBean(env, "B", 2, 50, true);

            sendBean(env, "A", 2, 0, false);
            env.assertPropsNew("s0", fields, new Object[]{"A", 2, 52L});

            env.milestone(1);

            sendBean(env, "B", 2, 0, false);
            env.assertPropsNew("s0", fields, new Object[]{"B", 2, 63L});

            sendBean(env, "A", 1, 0, false);
            env.assertPropsNew("s0", fields, new Object[]{"A", 1, 40L});

            env.milestone(2);

            sendBean(env, "B", 1, 0, false);
            env.assertPropsNew("s0", fields, new Object[]{"B", 1, 31L});

            assertFilterSvcCount(env, 1, "ctx");
            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedFilterExprTermByFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@audit @name('ctx') @public create context MyTermByUnrelated partition by theString from SupportBean(intPrimitive=0) terminated by SupportBean", path);
            env.compileDeploy("@name('s0') context MyTermByUnrelated select theString, count(*) as cnt from SupportBean output last when terminated", path);
            env.addListener("s0");
            String[] fields = "theString,cnt".split(",");

            env.sendEventBean(new SupportBean("A", 2));

            env.milestone(0);

            env.sendEventBean(new SupportBean("B", 1));
            env.sendEventBean(new SupportBean("B", 2));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("B", 0));
            env.sendEventBean(new SupportBean("A", 0));
            env.assertListenerNotInvoked("s0");

            env.milestone(2);

            env.sendEventBean(new SupportBean("B", 99));
            env.assertPropsPerRowLastNewAnyOrder("s0", fields, new Object[][]{{"B", 1L}});

            env.milestone(3);

            env.sendEventBean(new SupportBean("A", 0));
            env.assertPropsPerRowLastNewAnyOrder("s0", fields, new Object[][]{{"A", 1L}});

            env.undeployModuleContaining("s0");
            assertFilterSvcCount(env, 0, "ctx");
            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedFilterExprTermByFilterWExpr implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@audit @name('ctx') @public create context MyTermByUnrelated partition by theString from SupportBean(intPrimitive=0) terminated by SupportBean(intPrimitive=1)", path);
            env.compileDeploy("@name('s0') context MyTermByUnrelated select theString, count(*) as cnt from SupportBean output last when terminated", path);
            env.addListener("s0");
            String[] fields = "theString,cnt".split(",");

            env.milestone(0);

            env.sendEventBean(new SupportBean("A", 2));
            env.sendEventBean(new SupportBean("B", 1));

            env.milestone(1);

            env.sendEventBean(new SupportBean("B", 0));
            env.sendEventBean(new SupportBean("B", 2));
            env.assertListenerNotInvoked("s0");

            env.milestone(2);

            env.sendEventBean(new SupportBean("B", 1));
            env.assertPropsPerRowLastNewAnyOrder("s0", fields, new Object[][]{{"B", 1L}});

            env.milestone(3);

            env.sendEventBean(new SupportBean("A", 0));
            env.undeployModuleContaining("s0");
            assertFilterSvcCount(env, 0, "ctx");
            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByUnrelated implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@public create context MyTermByUnrelated partition by theString from SupportBean terminated by SupportBean_S0", path);
            env.compileDeploy("@name('s0') context MyTermByUnrelated select theString, count(*) as cnt from SupportBean output last when terminated", path);

            env.addListener("s0");
            String[] fields = "theString,cnt".split(",");

            env.sendEventBean(new SupportBean("A", 0));
            env.sendEventBean(new SupportBean("B", 0));

            env.milestone(0);

            env.sendEventBean(new SupportBean("A", 0));

            env.milestone(1);

            env.sendEventBean(new SupportBean_S0(-1));
            env.assertPropsPerRowLastNewAnyOrder("s0", fields, new Object[][]{{"A", 2L}, {"B", 1L}});

            env.milestone(2);

            env.sendEventBean(new SupportBean("C", 0));
            env.sendEventBean(new SupportBean("A", 0));

            env.milestone(3);

            env.sendEventBean(new SupportBean_S0(-1));
            env.assertPropsPerRowLastNewAnyOrder("s0", fields, new Object[][]{{"A", 1L}, {"C", 1L}});

            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByPatternTwoFilters implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@public create context MyTermByTimeout partition by p00 from SupportBean_S0, p10 from SupportBean_S1 terminated by pattern [SupportBean_S0(id<0) or SupportBean_S1(id<0)]", path);
            env.compileDeploy("@name('s0') context MyTermByTimeout select coalesce(s0.p00, s1.p10) as key, count(*) as cnt from pattern [every (s0=SupportBean_S0 or s1=SupportBean_S1)] output last when terminated", path);

            env.addListener("s0");
            String[] fields = "key,cnt".split(",");

            env.sendEventBean(new SupportBean_S0(0, "A"));
            env.sendEventBean(new SupportBean_S1(0, "A"));
            env.sendEventBean(new SupportBean_S1(0, "B"));

            env.milestone(0);

            env.sendEventBean(new SupportBean_S1(-1, "B")); // stop B
            env.assertPropsNew("s0", fields, new Object[]{"B", 1L});

            env.milestone(1);

            env.sendEventBean(new SupportBean_S1(0, "B"));
            env.sendEventBean(new SupportBean_S0(0, "A"));
            env.sendEventBean(new SupportBean_S0(0, "B"));

            env.milestone(2);

            env.sendEventBean(new SupportBean_S1(-1, "A")); // stop A
            env.assertPropsNew("s0", fields, new Object[]{"A", 3L});

            env.milestone(3);

            env.sendEventBean(new SupportBean_S1(-1, "A")); // stop A
            env.assertListenerNotInvoked("s0");

            env.milestone(4);

            env.sendEventBean(new SupportBean_S1(-1, "B")); // stop B
            env.assertPropsNew("s0", fields, new Object[]{"B", 2L});

            env.milestone(5);

            env.sendEventBean(new SupportBean_S1(-1, "B")); // stop B
            env.assertListenerNotInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByCrontabOutputWhenTerminated implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            sendCurrentTime(env, "2002-02-01T09:00:00.000");

            env.compileDeploy("@public create context MyTermByTimeout partition by theString from SupportBean terminated (*, *, *, *, *)", path);
            env.compileDeploy("@name('s0') context MyTermByTimeout select theString, count(*) as cnt from SupportBean output last when terminated", path);
            env.addListener("s0");

            env.sendEventBean(new SupportBean("A", 0));

            env.milestone(0);

            sendCurrentTime(env, "2002-02-01T09:00:05.000");

            env.sendEventBean(new SupportBean("B", 0));

            env.milestone(1);

            env.sendEventBean(new SupportBean("A", 0));

            sendCurrentTime(env, "2002-02-01T09:00:59.999");

            env.milestone(2);

            env.sendEventBean(new SupportBean("B", 0));
            env.sendEventBean(new SupportBean("A", 0));
            env.assertListenerNotInvoked("s0");

            sendCurrentTime(env, "2002-02-01T09:01:00.000");
            env.assertPropsPerRowLastNewAnyOrder("s0", "theString,cnt".split(","), new Object[][]{{"A", 3L}, {"B", 2L}});

            env.milestone(3);

            env.sendEventBean(new SupportBean("C", 0));
            env.sendEventBean(new SupportBean("A", 0));
            sendCurrentTime(env, "2002-02-01T09:01:30.000");
            env.sendEventBean(new SupportBean("D", 0));

            env.milestone(4);

            env.sendEventBean(new SupportBean("C", 0));

            sendCurrentTime(env, "2002-02-01T09:02:00.000");
            env.assertPropsPerRowLastNewAnyOrder("s0", "theString,cnt".split(","), new Object[][]{{"A", 1L}, {"C", 2L}, {"D", 1L}});

            sendCurrentTime(env, "2002-02-01T09:03:00.000");
            env.assertListenerNotInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByAfter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@public create context MyTermByTimeout partition by theString from SupportBean terminated after 10 seconds", path);
            env.compileDeploy("@name('s0') context MyTermByTimeout select theString, count(*) as cnt from SupportBean", path);
            env.addListener("s0");

            sendAssertSB(1, env, "A");

            env.milestone(0);

            env.advanceTime(1000);

            sendAssertSB(2, env, "A");

            env.milestone(1);

            sendAssertSB(1, env, "B");

            env.advanceTime(9999);

            sendAssertSB(2, env, "B");
            sendAssertSB(3, env, "A");

            env.milestone(2);

            env.advanceTime(10000);

            env.milestone(3);

            sendAssertSB(3, env, "B");
            sendAssertSB(1, env, "A");

            env.advanceTime(10999);

            sendAssertSB(4, env, "B");
            sendAssertSB(2, env, "A");

            env.advanceTime(11000);

            sendAssertSB(1, env, "B");

            env.milestone(4);

            sendAssertSB(3, env, "A");

            env.milestone(5);

            env.advanceTime(99999);

            sendAssertSB(1, env, "B");
            sendAssertSB(1, env, "A");

            env.undeployAll();
        }
    }

    private static class ContextKeySegmentedTermByFilterWSecondType implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@buseventtype @public create objectarray schema TypeOne(poa string);\n" +
                "@buseventtype @public create map schema TypeTwo(pmap string);\n" +
                "create context MyContextOAMap partition by poa from TypeOne, pmap from TypeTwo terminated by TypeTwo;\n" +
                "@name('s0') context MyContextOAMap select poa, count(*) as cnt from TypeOne;\n";
            env.compileDeploy(epl, new RegressionPath());

            env.addListener("s0");

            sendOAAssert(env, "A", 1L);

            env.milestone(0);

            sendOAAssert(env, "B", 1L);
            sendOAAssert(env, "A", 2L);
            sendOAAssert(env, "B", 2L);

            env.milestone(1);

            env.sendEventMap(CollectionUtil.populateNameValueMap("pmap", "B"), "TypeTwo");

            sendOAAssert(env, "A", 3L);

            env.milestone(2);

            sendOAAssert(env, "B", 1L);

            env.sendEventMap(CollectionUtil.populateNameValueMap("pmap", "A"), "TypeTwo");

            env.milestone(3);

            sendOAAssert(env, "A", 1L);
            sendOAAssert(env, "B", 2L);

            env.undeployAll();
        }

        private static void sendOAAssert(RegressionEnvironment env, String poa, long count) {
            env.sendEventObjectArray(new Object[]{poa}, "TypeOne");
            env.assertPropsNew("s0", "poa,cnt".split(","), new Object[]{poa, count});
        }
    }

    private static class ContextKeySegmentedTermByFilterWSubtype implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            env.compileDeploy("@name('ctx') @public create context ByP0 partition by a from ISupportA, b from ISupportB terminated by ISupportA(a='x')", path);
            env.compileDeploy("@name('s0') context ByP0 select coalesce(a.a, b.b) as p0, count(*) as cnt from pattern[every (a=ISupportA or b=ISupportB)]", path);

            env.addListener("s0");

            env.milestone(0);

            env.sendEventBean(new ISupportABCImpl("a", "a", null, null));
            env.assertPropsNew("s0", "p0,cnt".split(","), new Object[]{"a", 1L});

            env.milestone(1);

            env.sendEventBean(new ISupportAImpl("a", null));
            env.assertPropsNew("s0", "p0,cnt".split(","), new Object[]{"a", 2L});

            env.milestone(2);

            env.sendEventBean(new ISupportBImpl("a", null));
            env.assertPropsNew("s0", "p0,cnt".split(","), new Object[]{"a", 3L});

            env.undeployModuleContaining("s0");

            env.milestone(3);

            env.undeployModuleContaining("ctx");
        }
    }

    public static class ContextKeySegmentedTermByFilter implements RegressionExecution {
        private final boolean soda;

        public ContextKeySegmentedTermByFilter(boolean soda) {
            this.soda = soda;
        }

        public void run(RegressionEnvironment env) {

            RegressionPath path = new RegressionPath();
            env.compileDeploy(soda, "@public create context ByP0 as partition by theString from SupportBean terminated by SupportBean(intPrimitive<0)", path);
            env.compileDeploy(soda, "@name('s0') context ByP0 select theString, count(*) as cnt from SupportBean", path);
            env.addListener("s0");

            sendAssertSB(1, env, "A", 0);

            env.milestone(0);

            sendAssertSB(2, env, "A", 0);

            env.milestone(1);

            sendAssertNone(env, new SupportBean("A", -1));

            env.milestone(2);

            sendAssertSB(1, env, "A", 0);

            sendAssertSB(1, env, "B", 0);
            sendAssertNone(env, new SupportBean("B", -1));
            sendAssertSB(1, env, "B", 0);
            sendAssertSB(2, env, "B", 0);

            env.milestone(3);

            sendAssertNone(env, new SupportBean("B", -1));
            sendAssertSB(1, env, "B", 0);

            sendAssertSB(1, env, "C", -1);

            env.milestone(4);

            sendAssertNone(env, new SupportBean("C", -1));

            env.milestone(5);

            sendAssertSB(1, env, "C", -1);
            sendAssertNone(env, new SupportBean("C", -1));

            env.undeployAll();
        }

        public String name() {
            return this.getClass().getSimpleName() + "{" +
                "soda=" + soda +
                '}';
        }
    }

    private static void sendAssertSB(long expected, RegressionEnvironment env, String theString) {
        sendAssertSB(expected, env, theString, 0);
    }

    private static void sendAssertSB(long expected, RegressionEnvironment env, String theString, int intPrimitive) {
        env.sendEventBean(new SupportBean(theString, intPrimitive));
        env.assertPropsNew("s0", "theString,cnt".split(","), new Object[]{theString, expected});
    }

    private static void sendAssertNone(RegressionEnvironment env, Object event) {
        env.sendEventBean(event);
        env.assertListenerNotInvoked("s0");
    }

    private static void sendCurrentTime(RegressionEnvironment env, String time) {
        env.advanceTime(DateTime.parseDefaultMSec(time));
    }

    private static SupportBean sendBean(RegressionEnvironment env, String theString, int intPrimitive, long longPrimitive, boolean boolPrimitive) {
        SupportBean sb = new SupportBean(theString, intPrimitive);
        sb.setBoolPrimitive(boolPrimitive);
        sb.setLongPrimitive(longPrimitive);
        env.sendEventBean(sb);
        return sb;
    }

    private static void sendS0(RegressionEnvironment env, String p00, String p01) {
        env.sendEventBean(new SupportBean_S0(0, p00, p01));
    }

    private static void sendS1(RegressionEnvironment env, String p10, String p11) {
        env.sendEventBean(new SupportBean_S1(0, p10, p11));
    }

    private static void sendS2(RegressionEnvironment env, String p20, String p21) {
        env.sendEventBean(new SupportBean_S2(0, p20, p21));
    }

    private static void sendS0AssertNone(RegressionEnvironment env, int id, String p00, String p01) {
        env.sendEventBean(new SupportBean_S0(id, p00, p01));
        env.assertListenerNotInvoked("s0");
    }

    private static SupportBean_S0 sendS0Assert(int expected, SupportBean_S0 s0Init, RegressionEnvironment env, int id, String p00, String p01) {
        SupportBean_S0 s0 = new SupportBean_S0(id, p00, p01);
        env.sendEventBean(s0);
        String[] fields = "p00,p01,s0,theSum".split(",");
        env.assertPropsNew("s0", fields, new Object[]{p00, p01, s0Init == null ? s0 : s0Init, expected});
        return s0;
    }
}
