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
package com.espertech.esper.common.internal.epl.expression.dot.core;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.join.analyze.FilterExprAnalyzerAffector;
import com.espertech.esper.common.internal.metrics.instrumentation.InstrumentationBuilderExpr;
import com.espertech.esper.common.internal.rettype.EPChainableType;
import com.espertech.esper.common.internal.rettype.EPChainableTypeClass;
import com.espertech.esper.common.internal.rettype.EPChainableTypeHelper;
import com.espertech.esper.common.internal.util.JavaClassHelper;

public class ExprDotNodeForgeStream extends ExprDotNodeForge {
    private final ExprDotNodeImpl parent;
    private final FilterExprAnalyzerAffector filterExprAnalyzerAffector;
    private final int streamNumber;
    private final EventType eventType;
    private final ExprDotForge[] evaluators;
    private final boolean method;
    private final EPTypeClass evaluationType;

    public ExprDotNodeForgeStream(ExprDotNodeImpl parent, FilterExprAnalyzerAffector filterExprAnalyzerAffector, int streamNumber, EventType eventType, ExprDotForge[] evaluators, boolean method) {
        this.parent = parent;
        this.filterExprAnalyzerAffector = filterExprAnalyzerAffector;
        this.streamNumber = streamNumber;
        this.eventType = eventType;
        this.evaluators = evaluators;
        this.method = method;

        ExprDotForge last = evaluators[evaluators.length - 1];
        EPChainableType lastType = last.getTypeInfo();
        EPTypeClass evaluationTypeUnboxed;
        if (!method) {
            evaluationTypeUnboxed = ((EPChainableTypeClass) lastType).getType();
        } else {
            evaluationTypeUnboxed = (EPTypeClass) EPChainableTypeHelper.getNormalizedEPType(lastType);
        }
        evaluationType = JavaClassHelper.getBoxedType(evaluationTypeUnboxed);
    }

    public ExprEvaluator getExprEvaluator() {
        if (!method) {
            return new ExprDotNodeForgeStreamEvalEventBean(this, ExprDotNodeUtility.getEvaluators(evaluators));
        }
        return new ExprDotNodeForgeStreamEvalMethod(this, ExprDotNodeUtility.getEvaluators(evaluators));
    }

    public CodegenExpression evaluateCodegenUninstrumented(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        if (!method) {
            return ExprDotNodeForgeStreamEvalEventBean.codegen(this, codegenMethodScope, exprSymbol, codegenClassScope);
        }
        return ExprDotNodeForgeStreamEvalMethod.codegen(this, codegenMethodScope, exprSymbol, codegenClassScope);
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return new InstrumentationBuilderExpr(this.getClass(), this, "ExprDot", requiredType, codegenMethodScope, exprSymbol, codegenClassScope).build();
    }

    public EPTypeClass getEvaluationType() {
        return evaluationType;
    }

    public int getStreamNumber() {
        return streamNumber;
    }

    public boolean isReturnsConstantResult() {
        return false;
    }

    public FilterExprAnalyzerAffector getFilterExprAnalyzerAffector() {
        return filterExprAnalyzerAffector;
    }

    public Integer getStreamNumReferenced() {
        return streamNumber;
    }

    public String getRootPropertyName() {
        return null;
    }

    public ExprDotNodeImpl getForgeRenderable() {
        return parent;
    }

    public ExprDotForge[] getEvaluators() {
        return evaluators;
    }

    public EventType getEventType() {
        return eventType;
    }

    public boolean isLocalInlinedClass() {
        return false;
    }
}
