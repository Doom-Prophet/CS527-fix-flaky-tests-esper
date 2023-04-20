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
package com.espertech.esper.common.internal.epl.expression.core;

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.metrics.instrumentation.InstrumentationBuilderExpr;
import com.espertech.esper.common.internal.util.ClassHelperGenericType;
import com.espertech.esper.common.internal.util.EnumValue;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.io.StringWriter;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Represents a constant in an expressiun tree.
 */
public class ExprConstantNodeImpl extends ExprNodeBase implements ExprConstantNode, ExprEvaluator, ExprForgeInstrumentable {
    private Object value;
    private final EPType type;
    private EnumValue enumValue;
    private final String stringConstantWhenProvided;

    public ExprConstantNodeImpl(Object value, String stringConstantWhenProvided) {
        this.value = value;
        this.stringConstantWhenProvided = stringConstantWhenProvided;
        if (value == null) {
            type = EPTypeNull.INSTANCE;
        } else {
            type = JavaClassHelper.getPrimitiveType(value.getClass());
        }
    }

    public ExprConstantNodeImpl(Object value) {
        this(value, (String) null);
    }

    public ExprConstantNodeImpl(EnumValue enumValue) {
        this.stringConstantWhenProvided = null;
        this.enumValue = enumValue;
        this.type = ClassHelperGenericType.getFieldEPType(enumValue.getEnumField());
        try {
            value = enumValue.getEnumField().get(null);
        } catch (IllegalAccessException e) {
            throw new EPException("Exception accessing field '" + enumValue.getEnumField().getName() + "': " + e.getMessage(), e);
        }
    }

    public ExprConstantNodeImpl(Object value, EPType valueType) {
        this.stringConstantWhenProvided = null;
        this.value = value;
        this.type = valueType;
    }

    public ExprConstantNodeImpl(Object value, Class valueType) {
        this.stringConstantWhenProvided = null;
        this.value = value;
        if (value == null) {
            type = valueType == null ? EPTypeNull.INSTANCE : ClassHelperGenericType.getClassEPType(valueType);
        } else {
            type = JavaClassHelper.getPrimitiveType(ClassHelperGenericType.getClassEPType(value.getClass()));
        }
    }

    /**
     * Ctor - for use when the constant should return a given type and the actual value is always null.
     *
     * @param type the type of the constant null.
     */
    public ExprConstantNodeImpl(Class type) {
        this.type = type == null ? EPTypeNull.INSTANCE : ClassHelperGenericType.getClassEPType(JavaClassHelper.getBoxedType(type));
        this.value = null;
        this.stringConstantWhenProvided = null;
    }

    public ExprForgeConstantType getForgeConstantType() {
        return ExprForgeConstantType.COMPILETIMECONST;
    }

    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public EPType getEvaluationType() {
        return type;
    }

    public ExprForge getForge() {
        return this;
    }

    public String getStringConstantWhenProvided() {
        return stringConstantWhenProvided;
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }

    public boolean constantAvailable() {
        return true;
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return new InstrumentationBuilderExpr(this.getClass(), this, "ExprConst", requiredType, codegenMethodScope, exprSymbol, codegenClassScope).noqparam().build();
    }

    public CodegenExpression evaluateCodegenUninstrumented(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        if (value == null) {
            return constantNull();
        }
        if (enumValue != null) {
            return publicConstValue(enumValue.getEnumClass(), enumValue.getEnumField().getName());
        }
        if (value.getClass().isEnum()) {
            return enumValue(value.getClass(), value.toString());
        }
        return constant(value);
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        return null;
    }

    public boolean isConstantResult() {
        return true;
    }

    /**
     * Returns the constant's value.
     *
     * @return value of constant
     */
    public Object getConstantValue() {
        return value;
    }

    /**
     * Sets the value of the constant.
     *
     * @param value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public EPType getConstantType() {
        return type;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        return value;
    }

    public void toPrecedenceFreeEPL(StringWriter writer, ExprNodeRenderableFlags flags) {
        if (value instanceof String) {
            writer.append("\"" + value + '\"');
        } else if (value == null) {
            writer.append("null");
        } else {
            writer.append(value.toString());
        }
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.UNARY;
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        if (!(node instanceof ExprConstantNodeImpl)) {
            return false;
        }

        ExprConstantNodeImpl other = (ExprConstantNodeImpl) node;

        if ((other.value == null) && (this.value != null)) {
            return false;
        }
        if ((other.value != null) && (this.value == null)) {
            return false;
        }
        if ((other.value == null) && (this.value == null)) {
            return true;
        }
        return other.value.equals(this.value);
    }
}
