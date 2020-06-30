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
package com.espertech.esper.common.internal.epl.expression.funcs;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.collection.UniformPair;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class ExprCaseNodeForgeEvalSyntax1 implements ExprEvaluator {

    private final ExprCaseNodeForge forge;
    private final List<UniformPair<ExprEvaluator>> whenThenNodeList;
    private final ExprEvaluator optionalElseExprNode;

    public ExprCaseNodeForgeEvalSyntax1(ExprCaseNodeForge forge, List<UniformPair<ExprEvaluator>> whenThenNodeList, ExprEvaluator optionalElseExprNode) {
        this.forge = forge;
        this.whenThenNodeList = whenThenNodeList;
        this.optionalElseExprNode = optionalElseExprNode;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        // Case 1 expression example:
        //      case when a=b then x [when c=d then y...] [else y]
        Object caseResult = null;
        boolean matched = false;
        for (UniformPair<ExprEvaluator> p : whenThenNodeList) {
            Boolean whenResult = (Boolean) p.getFirst().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            // If the 'when'-expression returns true
            if ((whenResult != null) && whenResult) {
                caseResult = p.getSecond().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                matched = true;
                break;
            }
        }

        if (!matched && optionalElseExprNode != null) {
            caseResult = optionalElseExprNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        }

        if (caseResult == null) {
            return null;
        }

        if ((caseResult.getClass() != forge.getEvaluationType().getType()) && forge.isNumericResult()) {
            caseResult = JavaClassHelper.coerceBoxed((Number) caseResult, forge.getEvaluationType().getType());
        }

        return caseResult;
    }

    public static CodegenExpression codegen(ExprCaseNodeForge forge, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        EPTypeClass evaluationType = forge.getEvaluationType() == null ? EPTypePremade.MAP.getEPType() : forge.getEvaluationType();
        CodegenMethod methodNode = codegenMethodScope.makeChild(evaluationType, ExprCaseNodeForgeEvalSyntax1.class, codegenClassScope);

        CodegenBlock block = methodNode.getBlock().declareVar(EPTypePremade.BOOLEANBOXED.getEPType(), "when", constantFalse());

        for (UniformPair<ExprNode> pair : forge.getWhenThenNodeList()) {
            block.assignRef("when", pair.getFirst().getForge().evaluateCodegen(EPTypePremade.BOOLEANPRIMITIVE.getEPType(), methodNode, exprSymbol, codegenClassScope));
            block.ifCondition(and(notEqualsNull(ref("when")), ref("when")))
                    .blockReturn(codegenToType(forge, pair.getSecond(), methodNode, exprSymbol, codegenClassScope));
        }
        if (forge.getOptionalElseExprNode() != null) {
            block.methodReturn(codegenToType(forge, forge.getOptionalElseExprNode(), methodNode, exprSymbol, codegenClassScope));
        } else {
            block.methodReturn(constantNull());
        }
        return localMethod(methodNode);
    }

    protected static CodegenExpression codegenToType(ExprCaseNodeForge forge, ExprNode node, CodegenMethod methodNode, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        EPType nodeType = node.getForge().getEvaluationType();
        if (nodeType == null || nodeType == EPTypeNull.INSTANCE) {
            return constantNull();
        }
        EPTypeClass nodeClass = (EPTypeClass) nodeType;
        if (nodeClass.getType() == forge.getEvaluationType().getType() || !forge.isNumericResult()) {
            return node.getForge().evaluateCodegen(nodeClass, methodNode, exprSymbol, codegenClassScope);
        }
        return JavaClassHelper.coerceNumberToBoxedCodegen(node.getForge().evaluateCodegen(nodeClass, methodNode, exprSymbol, codegenClassScope), nodeClass, forge.getEvaluationType());
    }
}
