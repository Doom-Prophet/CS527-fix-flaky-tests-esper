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
package com.espertech.esper.common.internal.context.activator;

import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.context.aifactory.core.SAIFFInitializeSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityCodegen;
import com.espertech.esper.common.internal.epl.table.compiletime.TableMetaData;
import com.espertech.esper.common.internal.epl.table.core.TableDeployTimeResolver;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class ViewableActivatorTableForge implements ViewableActivatorForge {
    private final TableMetaData table;
    private final ExprNode optionalFilterExpression;

    public ViewableActivatorTableForge(TableMetaData table, ExprNode optionalFilterExpression) {
        this.table = table;
        this.optionalFilterExpression = optionalFilterExpression;
    }

    public CodegenExpression makeCodegen(CodegenMethodScope parent, SAIFFInitializeSymbol symbols, CodegenClassScope classScope) {
        CodegenMethod method = parent.makeChild(ViewableActivatorTable.EPTYPE, this.getClass(), classScope);
        method.getBlock()
                .declareVarNewInstance(ViewableActivatorTable.EPTYPE, "va")
                .exprDotMethod(ref("va"), "setTable", TableDeployTimeResolver.makeResolveTable(table, symbols.getAddInitSvc(method)))
                .exprDotMethod(ref("va"), "setFilterEval", optionalFilterExpression == null ? constantNull() : ExprNodeUtilityCodegen.codegenEvaluator(optionalFilterExpression.getForge(), method, this.getClass(), classScope))
                .methodReturn(ref("va"));
        return localMethod(method);
    }
}
