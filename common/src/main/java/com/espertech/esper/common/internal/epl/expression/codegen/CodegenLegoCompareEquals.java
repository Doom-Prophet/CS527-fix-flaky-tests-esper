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
package com.espertech.esper.common.internal.epl.expression.codegen;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class CodegenLegoCompareEquals {
    public static CodegenExpression codegenEqualsNonNullNoCoerce(CodegenExpression lhs, EPTypeClass lhsType, CodegenExpression rhs, EPTypeClass rhsType) {
        if (lhsType.getType().isPrimitive() && rhsType.getType().isPrimitive() && !JavaClassHelper.isFloatingPointClass(lhsType) && !JavaClassHelper.isFloatingPointClass(rhsType)) {
            return equalsIdentity(lhs, rhs);
        }
        if (lhsType.getType().isPrimitive() && rhsType.getType().isPrimitive()) {
            return staticMethod(JavaClassHelper.getBoxedType(lhsType).getType(), "compare", lhs, rhs);
        }
        if (lhsType.getType().isPrimitive()) {
            return exprDotMethod(rhs, "equals", lhs);
        }
        return exprDotMethod(lhs, "equals", rhs);
    }
}
