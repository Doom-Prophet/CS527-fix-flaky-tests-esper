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
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenIndent;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.espertech.esper.common.internal.bytecodemodel.core.CodeGenerationHelper.appendClassName;

public class CodegenStatementForEach extends CodegenStatementWBlockBase {
    private final EPTypeClass type;
    private final String ref;
    private final CodegenExpression target;

    private CodegenBlock block;

    public CodegenStatementForEach(CodegenBlock parent, EPTypeClass type, String ref, CodegenExpression target) {
        super(parent);
        this.type = type;
        this.ref = ref;
        this.target = target;
    }

    public void setBlock(CodegenBlock block) {
        this.block = block;
    }

    public void render(StringBuilder builder, Map<Class, String> imports, boolean isInnerClass, int level, CodegenIndent indent) {
        builder.append("for (");
        appendClassName(builder, type, imports);
        builder.append(" ").append(ref).append(" : ");
        target.render(builder, imports, isInnerClass);
        builder.append(") {\n");
        block.render(builder, imports, isInnerClass, level + 1, indent);
        indent.indent(builder, level);
        builder.append("}\n");
    }

    public void mergeClasses(Set<Class> classes) {
        type.traverseClasses(classes::add);
        block.mergeClasses(classes);
        target.mergeClasses(classes);
    }

    public void traverseExpressions(Consumer<CodegenExpression> consumer) {
        consumer.accept(target);
        block.traverseExpressions(consumer);
    }
}
