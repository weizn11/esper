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
package com.espertech.esper.regressionlib.suite.resultset.querytype;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.soda.*;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.util.SerializableObjectCopier;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBeanString;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ResultSetQueryTypeHaving {
    private final static String SYMBOL_DELL = "DELL";

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ResultSetQueryTypeHavingWildcardSelect());
        execs.add(new ResultSetQueryTypeStatementOM());
        execs.add(new ResultSetQueryTypeStatement());
        execs.add(new ResultSetQueryTypeStatementJoin());
        execs.add(new ResultSetQueryTypeSumHavingNoAggregatedProp());
        execs.add(new ResultSetQueryTypeNoAggregationJoinHaving());
        execs.add(new ResultSetQueryTypeNoAggregationJoinWhere());
        execs.add(new ResultSetQueryTypeSubstreamSelectHaving());
        execs.add(new ResultSetQueryTypeHavingSum());
        execs.add(new ResultSetQueryTypeHavingSumIStream());
        return execs;
    }

    private static class ResultSetQueryTypeHavingWildcardSelect implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select * " +
                "from SupportBean#length_batch(2) " +
                "where intPrimitive>0 " +
                "having count(*)=2";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("E1", 0));
            env.sendEventBean(new SupportBean("E2", 0));
            env.assertListenerNotInvoked("s0");

            env.milestone(0);

            env.sendEventBean(new SupportBean("E3", 1));
            env.sendEventBean(new SupportBean("E4", 1));
            env.assertListenerInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("E3", 0));
            env.sendEventBean(new SupportBean("E4", 1));
            env.assertListenerNotInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeStatementOM implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            EPStatementObjectModel model = new EPStatementObjectModel();
            model.setSelectClause(SelectClause.create("symbol", "price").streamSelector(StreamSelector.RSTREAM_ISTREAM_BOTH).add(Expressions.avg("price"), "avgPrice"));
            model.setFromClause(FromClause.create(FilterStream.create(SupportMarketDataBean.class.getSimpleName()).addView("length", Expressions.constant(5))));
            model.setHavingClause(Expressions.lt(Expressions.property("price"), Expressions.avg("price")));
            model = SerializableObjectCopier.copyMayFail(model);

            String epl = "select irstream symbol, price, avg(price) as avgPrice " +
                "from SupportMarketDataBean#length(5) " +
                "having price<avg(price)";
            assertEquals(epl, model.toEPL());

            model.setAnnotations(Collections.singletonList(AnnotationPart.nameAnnotation("s0")));
            env.compileDeploy(model).addListener("s0");

            tryAssertion(env);

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeStatement implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol, price, avg(price) as avgPrice " +
                "from SupportMarketDataBean#length(5) " +
                "having price < avg(price)";
            env.compileDeploy(epl).addListener("s0");

            tryAssertion(env);

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeStatementJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol, price, avg(price) as avgPrice " +
                "from SupportBeanString#length(100) as one, " +
                "SupportMarketDataBean#length(5) as two " +
                "where one.theString = two.symbol " +
                "having price < avg(price)";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBeanString(SYMBOL_DELL));

            tryAssertion(env);

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeSumHavingNoAggregatedProp implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol, price, avg(price) as avgPrice " +
                "from SupportMarketDataBean#length(5) as two " +
                "having volume < avg(price)";
            env.compileDeploy(epl).undeployAll();
        }
    }

    private static class ResultSetQueryTypeNoAggregationJoinHaving implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            runNoAggregationJoin(env, "having");
        }
    }

    private static class ResultSetQueryTypeNoAggregationJoinWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            runNoAggregationJoin(env, "where");
        }
    }

    private static class ResultSetQueryTypeSubstreamSelectHaving implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') insert into MyStream select quote.* from SupportBean#length(14) quote having avg(intPrimitive) >= 3\n";
            env.compileDeploy(stmtText).addListener("s0");

            env.sendEventBean(new SupportBean("abc", 2));
            env.assertListenerNotInvoked("s0");

            env.milestone(0);

            env.sendEventBean(new SupportBean("abc", 2));
            env.assertListenerNotInvoked("s0");

            env.sendEventBean(new SupportBean("abc", 3));
            env.assertListenerNotInvoked("s0");

            env.milestone(1);

            env.sendEventBean(new SupportBean("abc", 5));
            env.assertListenerInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeHavingSum implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream sum(myEvent.intPrimitive) as mysum from pattern [every myEvent=SupportBean] having sum(myEvent.intPrimitive) = 2";
            env.compileDeploy(epl).addListener("s0");

            sendEvent(env, 1);
            env.assertListenerNotInvoked("s0");

            sendEvent(env, 1);
            env.assertEqualsNew("s0", "mysum", 2);

            env.milestone(0);

            sendEvent(env, 1);
            env.assertEqualsOld("s0", "mysum", 2);

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeHavingSumIStream implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select istream sum(myEvent.intPrimitive) as mysum from pattern [every myEvent=SupportBean" +
                "] having sum(myEvent.intPrimitive) = 2";
            env.compileDeploy(epl).addListener("s0");

            sendEvent(env, 1);
            env.assertListenerNotInvoked("s0");

            env.milestone(0);

            sendEvent(env, 1);
            env.assertEqualsNew("s0", "mysum", 2);

            sendEvent(env, 1);
            env.assertListenerNotInvoked("s0");

            env.undeployAll();
        }
    }

    private static void tryAssertion(RegressionEnvironment env) {
        // assert select result type
        env.assertStatement("s0", statement -> {
            assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("price"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("avgPrice"));
        });

        sendEvent(env, SYMBOL_DELL, 10);
        env.assertListenerNotInvoked("s0");

        sendEvent(env, SYMBOL_DELL, 5);
        assertNewEvents(env, SYMBOL_DELL, 5d, 7.5d);

        env.milestone(0);

        sendEvent(env, SYMBOL_DELL, 15);
        env.assertListenerNotInvoked("s0");

        sendEvent(env, SYMBOL_DELL, 8);  // avg = (10 + 5 + 15 + 8) / 4 = 38/4=9.5
        assertNewEvents(env, SYMBOL_DELL, 8d, 9.5d);

        sendEvent(env, SYMBOL_DELL, 10);  // avg = (10 + 5 + 15 + 8 + 10) / 5 = 48/5=9.5
        env.assertListenerNotInvoked("s0");

        env.milestone(1);

        sendEvent(env, SYMBOL_DELL, 6);  // avg = (5 + 15 + 8 + 10 + 6) / 5 = 44/5=8.8
        // no old event posted, old event falls above current avg price
        assertNewEvents(env, SYMBOL_DELL, 6d, 8.8d);

        sendEvent(env, SYMBOL_DELL, 12);  // avg = (15 + 8 + 10 + 6 + 12) / 5 = 51/5=10.2
        assertOldEvents(env, SYMBOL_DELL, 5d, 10.2d);
    }

    private static void assertNewEvents(RegressionEnvironment env, String symbol,
                                        Double newPrice, Double newAvgPrice
    ) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(oldData);
            assertEquals(1, newData.length);

            assertEquals(symbol, newData[0].get("symbol"));
            assertEquals(newPrice, newData[0].get("price"));
            assertEquals(newAvgPrice, newData[0].get("avgPrice"));

            listener.reset();
        });
    }

    private static void assertOldEvents(RegressionEnvironment env, String symbol,
                                        Double oldPrice, Double oldAvgPrice
    ) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(newData);
            assertEquals(1, oldData.length);

            assertEquals(symbol, oldData[0].get("symbol"));
            assertEquals(oldPrice, oldData[0].get("price"));
            assertEquals(oldAvgPrice, oldData[0].get("avgPrice"));

            listener.reset();
        });
    }

    private static void runNoAggregationJoin(RegressionEnvironment env, String filterClause) {
        String epl = "@name('s0') select irstream a.price as aPrice, b.price as bPrice, Math.max(a.price, b.price) - Math.min(a.price, b.price) as spread " +
            "from SupportMarketDataBean(symbol='SYM1')#length(1) as a, " +
            "SupportMarketDataBean(symbol='SYM2')#length(1) as b " +
            filterClause + " Math.max(a.price, b.price) - Math.min(a.price, b.price) >= 1.4";
        env.compileDeploy(epl).addListener("s0");

        sendPriceEvent(env, "SYM1", 20);
        env.assertListenerNotInvoked("s0");

        env.milestone(0);

        sendPriceEvent(env, "SYM2", 10);
        assertNewSpreadEvent(env, 20, 10, 10);

        sendPriceEvent(env, "SYM2", 20);
        assertOldSpreadEvent(env, 20, 10, 10);

        env.milestone(1);

        sendPriceEvent(env, "SYM2", 20);
        sendPriceEvent(env, "SYM2", 20);
        sendPriceEvent(env, "SYM1", 20);
        env.assertListenerNotInvoked("s0");

        sendPriceEvent(env, "SYM1", 18.7);
        env.assertListenerNotInvoked("s0");

        sendPriceEvent(env, "SYM2", 20);
        env.assertListenerNotInvoked("s0");

        env.milestone(2);

        sendPriceEvent(env, "SYM1", 18.5);
        assertNewSpreadEvent(env, 18.5, 20, 1.5d);

        sendPriceEvent(env, "SYM2", 16);
        assertOldNewSpreadEvent(env, 18.5, 20, 1.5d, 18.5, 16, 2.5d);

        env.milestone(3);

        sendPriceEvent(env, "SYM1", 12);
        assertOldNewSpreadEvent(env, 18.5, 16, 2.5d, 12, 16, 4);

        env.undeployAll();
    }

    private static void assertOldNewSpreadEvent(RegressionEnvironment env, double oldaprice, double oldbprice, double oldspread,
                                                double newaprice, double newbprice, double newspread) {
        env.assertListener("s0", listener -> {
            assertEquals(1, listener.getOldDataList().size());
            assertEquals(1, listener.getLastOldData().length);
            assertEquals(1, listener.getNewDataList().size());   // since event null is put into the list
            assertEquals(1, listener.getLastNewData().length);

            EventBean oldEvent = listener.getLastOldData()[0];
            EventBean newEvent = listener.getLastNewData()[0];

            compareSpreadEvent(oldEvent, oldaprice, oldbprice, oldspread);
            compareSpreadEvent(newEvent, newaprice, newbprice, newspread);

            listener.reset();
        });
    }

    private static void assertOldSpreadEvent(RegressionEnvironment env, double aprice, double bprice, double spread) {
        env.assertListener("s0", listener -> {
            assertEquals(1, listener.getOldDataList().size());
            assertEquals(1, listener.getLastOldData().length);
            assertEquals(1, listener.getNewDataList().size());   // since event null is put into the list
            assertNull(listener.getLastNewData());

            EventBean theEvent = listener.getLastOldData()[0];

            compareSpreadEvent(theEvent, aprice, bprice, spread);
            listener.reset();
        });
    }

    private static void assertNewSpreadEvent(RegressionEnvironment env, double aprice, double bprice, double spread) {
        env.assertListener("s0", listener -> {
            assertEquals(1, listener.getNewDataList().size());
            assertEquals(1, listener.getLastNewData().length);
            assertEquals(1, listener.getOldDataList().size());
            assertNull(listener.getLastOldData());

            EventBean theEvent = listener.getLastNewData()[0];
            compareSpreadEvent(theEvent, aprice, bprice, spread);
            listener.reset();
        });
    }

    private static void compareSpreadEvent(EventBean theEvent, double aprice, double bprice, double spread) {
        assertEquals(aprice, theEvent.get("aPrice"));
        assertEquals(bprice, theEvent.get("bPrice"));
        assertEquals(spread, theEvent.get("spread"));
    }

    private static void sendPriceEvent(RegressionEnvironment env, String symbol, double price) {
        env.sendEventBean(new SupportMarketDataBean(symbol, price, -1L, null));
    }

    private static void sendEvent(RegressionEnvironment env, int intPrimitive) {
        SupportBean bean = new SupportBean();
        bean.setIntPrimitive(intPrimitive);
        env.sendEventBean(bean);
    }

    private static void sendEvent(RegressionEnvironment env, String symbol, double price) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, 0L, null);
        env.sendEventBean(bean);
    }

    private static final Logger log = LoggerFactory.getLogger(ResultSetQueryTypeHaving.class);
}
