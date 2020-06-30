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
package com.espertech.esper.common.internal.epl.pattern.observer;

import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityCodegen;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationContext;
import com.espertech.esper.common.internal.schedule.ScheduleParameterException;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.localMethod;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.newInstance;

public class TimerScheduleSpecComputeISOStringForge implements TimerScheduleSpecComputeForge {
    private final ExprNode parameter;

    public TimerScheduleSpecComputeISOStringForge(ExprNode parameter) {
        this.parameter = parameter;
    }

    public CodegenExpression make(CodegenMethodScope parent, CodegenClassScope classScope) {
        CodegenMethod method = parent.makeChild(TimerScheduleSpecComputeISOString.EPTYPE, this.getClass(), classScope);
        method.getBlock().methodReturn(newInstance(TimerScheduleSpecComputeISOString.EPTYPE, ExprNodeUtilityCodegen.codegenEvaluator(parameter.getForge(), method, this.getClass(), classScope)));
        return localMethod(method);
    }

    public void verifyComputeAllConst(ExprValidationContext validationContext) throws ScheduleParameterException {
        TimerScheduleSpecComputeISOString.compute(parameter.getForge().getExprEvaluator(), null, null);
    }
}
