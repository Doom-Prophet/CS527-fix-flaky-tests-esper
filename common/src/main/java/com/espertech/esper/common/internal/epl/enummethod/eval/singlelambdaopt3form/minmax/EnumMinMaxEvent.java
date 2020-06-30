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
package com.espertech.esper.common.internal.epl.enummethod.eval.singlelambdaopt3form.minmax;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.enummethod.dot.ExprDotEvalParamLambda;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumEval;
import com.espertech.esper.common.internal.epl.enummethod.eval.singlelambdaopt3form.base.ThreeFormEventPlain;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.util.Collection;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRelational.CodegenRelational.GT;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRelational.CodegenRelational.LT;

public class EnumMinMaxEvent extends ThreeFormEventPlain {

    protected final boolean max;
    protected final EPTypeClass innerTypeBoxed;

    public EnumMinMaxEvent(ExprDotEvalParamLambda lambda, boolean max) {
        super(lambda);
        this.max = max;
        this.innerTypeBoxed = (EPTypeClass) JavaClassHelper.getBoxedType(innerExpression.getEvaluationType());
    }

    public EnumEval getEnumEvaluator() {
        ExprEvaluator inner = innerExpression.getExprEvaluator();
        return new EnumEval() {
            public Object evaluateEnumMethod(EventBean[] eventsLambda, Collection enumcoll, boolean isNewData, ExprEvaluatorContext context) {
                Comparable minKey = null;

                Collection<EventBean> beans = (Collection<EventBean>) enumcoll;
                for (EventBean next : beans) {
                    eventsLambda[getStreamNumLambda()] = next;

                    Object comparable = inner.evaluate(eventsLambda, isNewData, context);
                    if (comparable == null) {
                        continue;
                    }

                    if (minKey == null) {
                        minKey = (Comparable) comparable;
                    } else {
                        if (max) {
                            if (minKey.compareTo(comparable) < 0) {
                                minKey = (Comparable) comparable;
                            }
                        } else {
                            if (minKey.compareTo(comparable) > 0) {
                                minKey = (Comparable) comparable;
                            }
                        }
                    }
                }

                return minKey;
            }
        };
    }

    public EPTypeClass returnTypeOfMethod() {
        return innerTypeBoxed;
    }

    public CodegenExpression returnIfEmptyOptional() {
        return constantNull();
    }

    public void initBlock(CodegenBlock block, CodegenMethod methodNode, ExprForgeCodegenSymbol scope, CodegenClassScope codegenClassScope) {
        block.declareVar(innerTypeBoxed, "minKey", constantNull());
    }

    public void forEachBlock(CodegenBlock block, CodegenMethod methodNode, ExprForgeCodegenSymbol scope, CodegenClassScope codegenClassScope) {
        block.declareVar(innerTypeBoxed, "value", innerExpression.evaluateCodegen(innerTypeBoxed, methodNode, scope, codegenClassScope));
        if (!((EPTypeClass) innerExpression.getEvaluationType()).getType().isPrimitive()) {
            block.ifRefNull("value").blockContinue();
        }
        block.ifCondition(equalsNull(ref("minKey")))
            .assignRef("minKey", ref("value"))
            .ifElse()
            .ifCondition(relational(exprDotMethod(ref("minKey"), "compareTo", ref("value")), max ? LT : GT, constant(0)))
            .assignRef("minKey", ref("value"));
    }

    public void returnResult(CodegenBlock block) {
        block.methodReturn(ref("minKey"));
    }
}
