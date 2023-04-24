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
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBeanString;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ResultSetQueryTypeRowPerGroupHaving {
    private final static String SYMBOL_DELL = "DELL";
    private final static String SYMBOL_IBM = "IBM";

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ResultSetQueryTypeHavingCount());
        execs.add(new ResultSetQueryTypeSumJoin());
        execs.add(new ResultSetQueryTypeSumOneView());
        execs.add(new ResultSetQueryTypeRowPerGroupBatch());
        execs.add(new ResultSetQueryTypeRowPerGroupDefinedExpr());
        return execs;
    }

    private static class ResultSetQueryTypeRowPerGroupDefinedExpr implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "expression F {v -> v} select sum(intPrimitive) from SupportBean group by theString having count(*) > F(1)";
            env.compile(epl);

            String eplInvalid = "expression F {v -> v} select sum(intPrimitive) from SupportBean group by theString having count(*) > F(longPrimitive)";
            env.tryInvalidCompile(eplInvalid, "Non-aggregated property 'longPrimitive' in the HAVING clause must occur in the group-by clause");
        }
    }

    private static class ResultSetQueryTypeRowPerGroupBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);
            env.compileDeploy("@name('s0') select count(*) as y from SupportBean#time_batch(1 seconds) group by theString having count(*) > 0");
            env.addListener("s0");

            env.sendEventBean(new SupportBean("E1", 0));
            env.advanceTime(1000);
            env.assertPropsNew("s0", "y".split(","), new Object[] {1L});

            env.sendEventBean(new SupportBean("E2", 0));
            env.advanceTime(2000);
            env.assertPropsNew("s0", "y".split(","), new Object[] {1L});

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeHavingCount implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String text = "@name('s0') select * from SupportBean(intPrimitive = 3)#length(10) as e1 group by theString having count(*) > 2";
            env.compileDeploy(text).addListener("s0");

            env.sendEventBean(new SupportBean("A1", 3));

            env.milestone(0);

            env.sendEventBean(new SupportBean("A1", 3));
            env.assertListenerNotInvoked("s0");
            env.sendEventBean(new SupportBean("A1", 3));
            env.assertListenerInvoked("s0");

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeSumJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol, sum(price) as mySum " +
                "from SupportBeanString#length(100) as one, " +
                " SupportMarketDataBean#length(3) as two " +
                "where (symbol='DELL' or symbol='IBM' or symbol='GE')" +
                "       and one.theString = two.symbol " +
                "group by symbol " +
                "having sum(price) >= 100";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBeanString(SYMBOL_DELL));
            env.sendEventBean(new SupportBeanString(SYMBOL_IBM));
            env.sendEventBean(new SupportBeanString("AAA"));

            tryAssertion(env);

            env.undeployAll();
        }
    }

    private static class ResultSetQueryTypeSumOneView implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select irstream symbol, sum(price) as mySum " +
                "from SupportMarketDataBean#length(3) " +
                "where symbol='DELL' or symbol='IBM' or symbol='GE' " +
                "group by symbol " +
                "having sum(price) >= 100";
            env.compileDeploy(epl).addListener("s0");

            tryAssertion(env);

            env.undeployAll();
        }
    }

    private static void tryAssertion(RegressionEnvironment env) {
        sendEvent(env, SYMBOL_DELL, 10);
        env.assertListenerNotInvoked("s0");

        sendEvent(env, SYMBOL_DELL, 60);
        env.assertListenerNotInvoked("s0");

        env.milestone(0);

        sendEvent(env, SYMBOL_DELL, 30);
        assertNewEvent(env, SYMBOL_DELL, 100);

        sendEvent(env, SYMBOL_IBM, 30);
        assertOldEvent(env, SYMBOL_DELL, 100);

        sendEvent(env, SYMBOL_IBM, 80);
        assertNewEvent(env, SYMBOL_IBM, 110);
    }

    private static void assertNewEvent(RegressionEnvironment env, String symbol, double newSum) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(oldData);
            assertEquals(1, newData.length);

            assertEquals(newSum, newData[0].get("mySum"));
            assertEquals(symbol, newData[0].get("symbol"));

            listener.reset();
        });
    }

    private static void assertOldEvent(RegressionEnvironment env, String symbol, double newSum) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(newData);
            assertEquals(1, oldData.length);

            assertEquals(newSum, oldData[0].get("mySum"));
            assertEquals(symbol, oldData[0].get("symbol"));

            listener.reset();
        });
    }

    private static void sendEvent(RegressionEnvironment env, String symbol, double price) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, 0L, null);
        env.sendEventBean(bean);
    }
}
