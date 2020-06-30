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
import com.espertech.esper.common.internal.epl.datetime.calop.CalendarForge;
import com.espertech.esper.common.internal.epl.datetime.reformatop.ReformatForge;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;

import java.util.List;
import java.util.TimeZone;

import static com.espertech.esper.common.internal.epl.datetime.dtlocal.DTLocalUtil.getCalendarOps;

public class DTLocalDateOpsReformatForge extends DTLocalForgeCalopReformatBase {

    public DTLocalDateOpsReformatForge(List<CalendarForge> calendarForges, ReformatForge reformatForge) {
        super(calendarForges, reformatForge);
    }

    public DTLocalEvaluator getDTEvaluator() {
        return new DTLocalDateOpsReformatEval(getCalendarOps(calendarForges), reformatForge.getOp(), TimeZone.getDefault());
    }

    public CodegenExpression codegen(CodegenExpression inner, EPTypeClass innerType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return DTLocalDateOpsReformatEval.codegen(this, inner, codegenMethodScope, exprSymbol, codegenClassScope);
    }
}
