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
package com.espertech.esper.regressionlib.suite.expr.datetime;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.support.SupportEventPropUtil;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportDateTime;

import java.util.ArrayList;
import java.util.Collection;

import static com.espertech.esper.common.client.type.EPTypePremade.*;

public class ExprDTRound {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> executions = new ArrayList<>();
        executions.add(new ExprDTRoundInput());
        executions.add(new ExprDTRoundCeil());
        executions.add(new ExprDTRoundFloor());
        executions.add(new ExprDTRoundHalf());
        return executions;
    }

    private static class ExprDTRoundInput implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String[] fields = "val0,val1,val2,val3,val4".split(",");
            String eplFragment = "@name('s0') select " +
                "utildate.roundCeiling('hour') as val0," +
                "longdate.roundCeiling('hour') as val1," +
                "caldate.roundCeiling('hour') as val2," +
                "localdate.roundCeiling('hour') as val3," +
                "zoneddate.roundCeiling('hour') as val4" +
                " from SupportDateTime";
            env.compileDeploy(eplFragment).addListener("s0");
            SupportEventPropUtil.assertTypes(env.statement("s0").getEventType(), fields, new EPTypeClass[]{DATE.getEPType(), LONGBOXED.getEPType(), CALENDAR.getEPType(), LOCALDATETIME.getEPType(), ZONEDDATETIME.getEPType()});

            String startTime = "2002-05-30T09:01:02.003";
            String expectedTime = "2002-5-30T10:00:00.000";
            env.sendEventBean(SupportDateTime.make(startTime));
            env.assertPropsNew("s0", fields, SupportDateTime.getArrayCoerced(expectedTime, "util", "long", "cal", "ldt", "zdt"));

            env.undeployAll();
        }
    }

    private static class ExprDTRoundCeil implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String[] fields = "val0,val1,val2,val3,val4,val5,val6".split(",");
            String eplFragment = "@name('s0') select " +
                "utildate.roundCeiling('msec') as val0," +
                "utildate.roundCeiling('sec') as val1," +
                "utildate.roundCeiling('minutes') as val2," +
                "utildate.roundCeiling('hour') as val3," +
                "utildate.roundCeiling('day') as val4," +
                "utildate.roundCeiling('month') as val5," +
                "utildate.roundCeiling('year') as val6" +
                " from SupportDateTime";
            env.compileDeploy(eplFragment).addListener("s0");
            SupportEventPropUtil.assertTypesAllSame(env.statement("s0").getEventType(), fields, DATE.getEPType());

            String[] expected = {
                "2002-05-30T09:01:02.003",
                "2002-05-30T09:01:03.000",
                "2002-05-30T09:02:00.000",
                "2002-05-30T10:00:00.000",
                "2002-05-31T00:00:00.000",
                "2002-06-1T00:00:00.000",
                "2003-01-1T00:00:00.000",
            };
            String startTime = "2002-05-30T09:01:02.003";
            env.sendEventBean(SupportDateTime.make(startTime));
            env.assertPropsNew("s0", fields, SupportDateTime.getArrayCoerced(expected, "util"));

            env.undeployAll();
        }
    }

    private static class ExprDTRoundFloor implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String[] fields = "val0,val1,val2,val3,val4,val5,val6".split(",");
            String eplFragment = "@name('s0') select " +
                "utildate.roundFloor('msec') as val0," +
                "utildate.roundFloor('sec') as val1," +
                "utildate.roundFloor('minutes') as val2," +
                "utildate.roundFloor('hour') as val3," +
                "utildate.roundFloor('day') as val4," +
                "utildate.roundFloor('month') as val5," +
                "utildate.roundFloor('year') as val6" +
                " from SupportDateTime";
            env.compileDeploy(eplFragment).addListener("s0");
            SupportEventPropUtil.assertTypesAllSame(env.statement("s0").getEventType(), fields, DATE.getEPType());

            String[] expected = {
                "2002-05-30T09:01:02.003",
                "2002-05-30T09:01:02.000",
                "2002-05-30T09:01:00.000",
                "2002-05-30T09:00:00.000",
                "2002-05-30T00:00:00.000",
                "2002-05-1T00:00:00.000",
                "2002-01-1T00:00:00.000",
            };
            String startTime = "2002-05-30T09:01:02.003";
            env.sendEventBean(SupportDateTime.make(startTime));
            env.assertPropsNew("s0", fields, SupportDateTime.getArrayCoerced(expected, "util"));

            env.undeployAll();
        }
    }

    private static class ExprDTRoundHalf implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String[] fields = "val0,val1,val2,val3,val4,val5,val6".split(",");
            String eplFragment = "@name('s0') select " +
                "utildate.roundHalf('msec') as val0," +
                "utildate.roundHalf('sec') as val1," +
                "utildate.roundHalf('minutes') as val2," +
                "utildate.roundHalf('hour') as val3," +
                "utildate.roundHalf('day') as val4," +
                "utildate.roundHalf('month') as val5," +
                "utildate.roundHalf('year') as val6" +
                " from SupportDateTime";
            env.compileDeploy(eplFragment).addListener("s0");
            SupportEventPropUtil.assertTypesAllSame(env.statement("s0").getEventType(), fields, DATE.getEPType());

            String[] expected = {
                "2002-05-30T15:30:02.550",
                "2002-05-30T15:30:03.000",
                "2002-05-30T15:30:00.000",
                "2002-05-30T16:00:00.00",
                "2002-05-31T00:00:00.000",
                "2002-06-01T00:00:00.000",
                "2002-01-01T00:00:00.000",
            };
            String startTime = "2002-05-30T15:30:02.550";
            env.sendEventBean(SupportDateTime.make(startTime));
            env.assertPropsNew("s0", fields, SupportDateTime.getArrayCoerced(expected, "util"));

            // test rounding up/down
            env.undeployAll();
            fields = "val0".split(",");
            eplFragment = "@name('s0') select utildate.roundHalf('min') as val0 from SupportDateTime";
            env.compileDeployAddListenerMile(eplFragment, "s0", 1);

            env.sendEventBean(SupportDateTime.make("2002-05-30T15:30:29.999"));
            env.assertPropsNew("s0", fields, new Object[]{SupportDateTime.getValueCoerced("2002-05-30T15:30:00.000", "util")});

            env.sendEventBean(SupportDateTime.make("2002-05-30T15:30:30.000"));
            env.assertPropsNew("s0", fields, new Object[]{SupportDateTime.getValueCoerced("2002-05-30T15:31:00.000", "util")});

            env.sendEventBean(SupportDateTime.make("2002-05-30T15:30:30.001"));
            env.assertPropsNew("s0", fields, new Object[]{SupportDateTime.getValueCoerced("2002-05-30T15:31:00.000", "util")});

            env.undeployAll();
        }
    }
}

