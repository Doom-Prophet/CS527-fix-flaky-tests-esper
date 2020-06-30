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
package com.espertech.esper.common.internal.epl.datetime.dtlocal;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.epl.datetime.calop.CalendarForge;
import com.espertech.esper.common.internal.epl.datetime.interval.IntervalForge;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.time.abacus.TimeAbacus;

import java.util.List;
import java.util.TimeZone;

import static com.espertech.esper.common.internal.epl.datetime.dtlocal.DTLocalUtil.getCalendarOps;

public class DTLocalLongOpsIntervalForge extends DTLocalForgeCalOpsIntervalBase {

    protected final TimeAbacus timeAbacus;

    public DTLocalLongOpsIntervalForge(List<CalendarForge> calendarForges, IntervalForge intervalForge, TimeAbacus timeAbacus) {
        super(calendarForges, intervalForge);
        this.timeAbacus = timeAbacus;
    }

    public DTLocalEvaluator getDTEvaluator() {
        return new DTLocalLongOpsIntervalEval(getCalendarOps(calendarForges), intervalForge.getOp(), TimeZone.getDefault(), timeAbacus);
    }

    public CodegenExpression codegen(CodegenExpression inner, EPTypeClass innerType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return DTLocalLongOpsIntervalEval.codegenPointInTime(this, inner, innerType, codegenMethodScope, exprSymbol, codegenClassScope);
    }

    public DTLocalEvaluatorIntervalComp makeEvaluatorComp() {
        return new DTLocalLongOpsIntervalEval(getCalendarOps(calendarForges), intervalForge.getOp(), TimeZone.getDefault(), timeAbacus);
    }

    public CodegenExpression codegen(CodegenExpressionRef start, CodegenExpressionRef end, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return DTLocalLongOpsIntervalEval.codegenStartEnd(this, start, end, codegenMethodScope, exprSymbol, codegenClassScope);
    }
}
