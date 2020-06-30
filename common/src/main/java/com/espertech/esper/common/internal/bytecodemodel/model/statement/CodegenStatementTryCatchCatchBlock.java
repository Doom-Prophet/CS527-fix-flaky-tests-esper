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
package com.espertech.esper.common.internal.bytecodemodel.model.statement;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.util.Set;
import java.util.function.Consumer;

public class CodegenStatementTryCatchCatchBlock {
    private final EPTypeClass ex;
    private final String name;
    private final CodegenBlock block;

    public CodegenStatementTryCatchCatchBlock(EPTypeClass ex, String name, CodegenBlock block) {
        this.ex = ex;
        this.name = name;
        this.block = block;
    }

    public EPTypeClass getEx() {
        return ex;
    }

    public String getName() {
        return name;
    }

    public CodegenBlock getBlock() {
        return block;
    }

    void mergeClasses(Set<Class> classes) {
        ex.traverseClasses(classes::add);
        block.mergeClasses(classes);
    }

    public void traverseExpressions(Consumer<CodegenExpression> consumer) {
        block.traverseExpressions(consumer);
    }
}
