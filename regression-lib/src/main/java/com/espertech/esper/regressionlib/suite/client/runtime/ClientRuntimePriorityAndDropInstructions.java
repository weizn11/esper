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
package com.espertech.esper.regressionlib.suite.client.runtime;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.annotation.Drop;
import com.espertech.esper.common.client.annotation.Priority;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionFlag;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.runtime.client.scopetest.SupportListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ClientRuntimePriorityAndDropInstructions {

    public static List<RegressionExecution> executions() {
        List<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ClientRuntimeSchedulingPriority());
        execs.add(new ClientRuntimeSchedulingDrop());
        execs.add(new ClientRuntimeNamedWindowPriority());
        execs.add(new ClientRuntimeNamedWindowDrop());
        execs.add(new ClientRuntimePriority());
        execs.add(new ClientRuntimeAddRemoveStmts());
        return execs;
    }

    @Priority(10)
    @Drop
    private static class ClientRuntimeSchedulingPriority implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(0, env);
            SupportListener listener = env.listenerNew();
            env.compileDeploy("@name('s1') @Priority(1) select 1 as prio from pattern [every timer:interval(10)]");
            env.compileDeploy("@name('s3') @Priority(3) select 3 as prio from pattern [every timer:interval(10)]");
            env.compileDeploy("@name('s2') @Priority(2) select 2 as prio from pattern [every timer:interval(10)]");
            env.compileDeploy("@name('s4') @Priority(4) select 4 as prio from pattern [every timer:interval(10)]");
            env.statement("s1").addListener(listener);
            env.statement("s2").addListener(listener);
            env.statement("s3").addListener(listener);
            env.statement("s4").addListener(listener);

            sendTimer(10000, env);
            assertPrio(listener, null, new int[]{4, 3, 2, 1});

            env.undeployModuleContaining("s2");
            env.compileDeploy("@name('s0') select 0 as prio from pattern [every timer:interval(10)]");
            env.statement("s0").addListener(listener);

            sendTimer(20000, env);
            assertPrio(listener, null, new int[]{4, 3, 1, 0});

            env.compileDeploy("@name('s2') @Priority(2) select 2 as prio from pattern [every timer:interval(10)]");
            env.statement("s2").addListener(listener);

            sendTimer(30000, env);
            assertPrio(listener, null, new int[]{4, 3, 2, 1, 0});

            env.compileDeploy("@name('s5') @Priority(3) select 3 as prio from pattern [every timer:interval(10)]");
            env.statement("s5").addListener(listener);

            sendTimer(40000, env);
            assertPrio(listener, null, new int[]{4, 3, 3, 2, 1, 0});

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.OBSERVEROPS);
        }
    }

    private static class ClientRuntimeSchedulingDrop implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(0, env);
            SupportListener listener = env.listenerNew();
            env.compileDeploy("@Name('s1') @Drop select 1 as prio from pattern [every timer:interval(10)]");
            env.statement("s1").addListener(listener);

            env.compileDeploy("@name('s3') @Priority(2) select 3 as prio from pattern [every timer:interval(10)]");
            env.statement("s3").addListener(listener);

            env.compileDeploy("@name('s2') select 2 as prio from pattern [every timer:interval(10)]");
            env.statement("s2").addListener(listener);

            sendTimer(10000, env);
            assertPrio(listener, null, new int[]{3, 1});

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.OBSERVEROPS);
        }
    }

    private static class ClientRuntimeNamedWindowPriority implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText;
            RegressionPath path = new RegressionPath();
            SupportListener listener = env.listenerNew();

            stmtText = "@public create window MyWindow#lastevent as select * from SupportBean";
            env.compileDeploy(stmtText, path);

            stmtText = "insert into MyWindow select * from SupportBean";
            env.compileDeploy(stmtText, path);

            stmtText = "@name('s1') @Priority(1) on MyWindow e select e.theString as theString, 1 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s1").addListener(listener);

            stmtText = "@name('s3') @Priority(3) on MyWindow e select e.theString as theString, 3 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s3").addListener(listener);

            stmtText = "@name('s2') @Priority(2) on MyWindow e select e.theString as theString, 2 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s2").addListener(listener);

            stmtText = "@name('s4') @Priority(4) on MyWindow e select e.theString as theString, 4 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s4").addListener(listener);

            env.sendEventBean(new SupportBean("E1", 0));
            assertPrio(listener, "E1", new int[]{4, 3, 2, 1});

            env.undeployModuleContaining("s2");
            env.compileDeploy("@name('s0') on MyWindow e select e.theString as theString, 0 as prio from MyWindow", path);
            env.statement("s0").addListener(listener);

            env.sendEventBean(new SupportBean("E2", 0));
            assertPrio(listener, "E2", new int[]{4, 3, 1, 0});

            stmtText = "@name('s2') @Priority(2) on MyWindow e select e.theString as theString, 2 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s2").addListener(listener);

            env.sendEventBean(new SupportBean("E3", 0));
            assertPrio(listener, "E3", new int[]{4, 3, 2, 1, 0});

            stmtText = "@name('sx') @Priority(3) on MyWindow e select e.theString as theString, 3 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("sx").addListener(listener);

            env.sendEventBean(new SupportBean("E4", 0));
            assertPrio(listener, "E4", new int[]{4, 3, 3, 2, 1, 0});

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.OBSERVEROPS);
        }
    }

    private static class ClientRuntimeNamedWindowDrop implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText;
            RegressionPath path = new RegressionPath();
            SupportListener listener = env.listenerNew();

            stmtText = "@public create window MyWindow#lastevent as select * from SupportBean";
            env.compileDeploy(stmtText, path);

            stmtText = "insert into MyWindow select * from SupportBean";
            env.compileDeploy(stmtText, path);

            stmtText = "@name('s2') @Drop on MyWindow e select e.theString as theString, 2 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s2").addListener(listener);

            stmtText = "@name('s3') @Priority(3) on MyWindow e select e.theString as theString, 3 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s3").addListener(listener);

            stmtText = "@name('s4') on MyWindow e select e.theString as theString, 0 as prio from MyWindow";
            env.compileDeploy(stmtText, path);
            env.statement("s4").addListener(listener);

            env.sendEventBean(new SupportBean("E1", 0));
            assertPrio(listener, "E1", new int[]{3, 2});

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.OBSERVEROPS);
        }
    }

    private static class ClientRuntimePriority implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            SupportListener listener = env.listenerNew();

            env.compileDeploy("@name('s1') @Priority(1) select *, 1 as prio from SupportBean");
            env.statement("s1").addListener(listener);

            env.compileDeploy("@name('s3') @Priority(3) select *, 3 as prio from SupportBean");
            env.statement("s3").addListener(listener);

            env.compileDeploy("@name('s2') @Priority(2) select *, 2 as prio from SupportBean");
            env.statement("s2").addListener(listener);

            env.compileDeploy("@name('s4') @Priority(4) select *, 4 as prio from SupportBean");
            env.statement("s4").addListener(listener);

            env.sendEventBean(new SupportBean("E1", 0));
            assertPrio(listener, "E1", new int[]{4, 3, 2, 1});

            env.undeployModuleContaining("s2");
            env.compileDeploy("@name('s0') select *, 0 as prio from SupportBean");
            env.statement("s0").addListener(listener);

            env.sendEventBean(new SupportBean("E2", 0));
            assertPrio(listener, "E2", new int[]{4, 3, 1, 0});

            env.compileDeploy("@name('s2') @Priority(2) select *, 2 as prio from SupportBean");
            env.statement("s2").addListener(listener);

            env.sendEventBean(new SupportBean("E3", 0));
            assertPrio(listener, "E3", new int[]{4, 3, 2, 1, 0});

            env.compileDeploy("@name('sx') @Priority(3) select *, 3 as prio from SupportBean");
            env.statement("sx").addListener(listener);

            env.sendEventBean(new SupportBean("E4", 0));
            assertPrio(listener, "E4", new int[]{4, 3, 3, 2, 1, 0});

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.OBSERVEROPS);
        }
    }

    private static class ClientRuntimeAddRemoveStmts implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            String stmtSelectText = "@name('s0') insert into ABCStream select * from SupportBean";
            env.compileDeploy(stmtSelectText, path).addListener("s0");

            String stmtOneText = "@name('l0') @Drop select * from SupportBean where intPrimitive = 1";
            env.compileDeploy(stmtOneText).addListener("l0");

            String stmtTwoText = "@name('l1') @Drop select * from SupportBean where intPrimitive = 2";
            env.compileDeploy(stmtTwoText).addListener("l1");

            env.sendEventBean(new SupportBean("E1", 1));
            env.assertListenerNotInvoked("s0");
            assertReceivedSingle(env, "l0,l1", 0, "E1");

            env.sendEventBean(new SupportBean("E2", 2));
            env.assertListenerNotInvoked("s0");
            assertReceivedSingle(env, "l0,l1", 1, "E2");

            env.sendEventBean(new SupportBean("E3", 1));
            env.assertListenerNotInvoked("s0");
            assertReceivedSingle(env, "l0,l1", 0, "E3");

            env.sendEventBean(new SupportBean("E4", 3));
            env.assertEqualsNew("s0", "theString", "E4");
            assertReceivedNone(env, "l0,l1");

            String stmtThreeText = "@name('l2') @Drop select * from SupportBean where intPrimitive = 3";
            env.compileDeploy(stmtThreeText).addListener("l2");

            env.sendEventBean(new SupportBean("E5", 3));
            env.assertListenerNotInvoked("s0");
            assertReceivedSingle(env, "l0,l1,l2", 2, "E5");

            env.sendEventBean(new SupportBean("E6", 1));
            env.assertListenerNotInvoked("s0");
            assertReceivedSingle(env, "l0,l1,l2", 0, "E6");

            env.undeployModuleContaining("l0");
            env.sendEventBean(new SupportBean("E7", 1));
            env.assertEqualsNew("s0", "theString", "E7");
            assertReceivedNone(env, "l1,l2");

            String stmtSelectTextTwo = "@name('s1') @Priority(50) select * from SupportBean";
            env.compileDeploy(stmtSelectTextTwo).addListener("s1");

            env.sendEventBean(new SupportBean("E8", 1));
            env.assertEqualsNew("s0", "theString", "E8");
            env.assertEqualsNew("s1", "theString", "E8");
            assertReceivedNone(env, "l1,l2");

            env.sendEventBean(new SupportBean("E9", 2));
            env.assertListenerNotInvoked("s0");
            assertReceivedSingle(env, "l1,l2", 0, "E9");

            env.undeployAll();
        }

        public EnumSet<RegressionFlag> flags() {
            return EnumSet.of(RegressionFlag.OBSERVEROPS);
        }
    }

    private static void assertReceivedSingle(RegressionEnvironment env, String namesCSV, int index, String stringValue) {
        String[] names = namesCSV.split(",");
        for (int i = 0; i < names.length; i++) {
            if (i == index) {
                continue;
            }
            assertFalse(env.listener(names[i]).isInvoked());
        }
        assertEquals(stringValue, env.listener(names[index]).assertOneGetNewAndReset().get("theString"));
    }

    private static void assertPrio(SupportListener listener, String theString, int[] prioValues) {
        EventBean[] events = listener.getNewDataListFlattened();
        assertEquals(prioValues.length, events.length);
        for (int i = 0; i < prioValues.length; i++) {
            assertEquals(prioValues[i], events[i].get("prio"));
            if (theString != null) {
                assertEquals(theString, events[i].get("theString"));
            }
        }
        listener.reset();
    }

    private static void assertReceivedNone(RegressionEnvironment env, String namesCSV) {
        String[] names = namesCSV.split(",");
        for (int i = 0; i < names.length; i++) {
            env.assertListenerNotInvoked(names[i]);
        }
    }

    private static void sendTimer(long time, RegressionEnvironment env) {
        env.advanceTime(time);
    }
}
