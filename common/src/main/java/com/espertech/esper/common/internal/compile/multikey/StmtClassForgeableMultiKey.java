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
package com.espertech.esper.common.internal.compile.multikey;

import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.client.util.MultiKey;
import com.espertech.esper.common.internal.bytecodemodel.base.*;
import com.espertech.esper.common.internal.bytecodemodel.core.*;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.bytecodemodel.util.CodegenStackGenerator;
import com.espertech.esper.common.internal.compile.stage3.StmtClassForgeable;
import com.espertech.esper.common.internal.compile.stage3.StmtClassForgeableType;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.compile.multikey.MultiKeyPlanner.requiresDeepEquals;

public class StmtClassForgeableMultiKey implements StmtClassForgeable {

    private final String className;
    private final CodegenPackageScope packageScope;
    private final EPType[] types;
    private final boolean lenientEquals;

    public StmtClassForgeableMultiKey(String className, CodegenPackageScope packageScope, EPType[] types, boolean lenientEquals) {
        this.className = className;
        this.packageScope = packageScope;
        this.types = types;
        this.lenientEquals = lenientEquals;
    }

    public CodegenClass forge(boolean includeDebugSymbols, boolean fireAndForget) {
        List<CodegenTypedParam> params = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            EPType boxed = JavaClassHelper.getBoxedType(types[i]);
            params.add(new CodegenTypedParam(boxed == EPTypeNull.INSTANCE ? EPTypePremade.OBJECT.getEPType() : (EPTypeClass) boxed, "k" + i));
        }
        CodegenCtor ctor = new CodegenCtor(StmtClassForgeableMultiKey.class, includeDebugSymbols, params);

        CodegenClassMethods methods = new CodegenClassMethods();
        CodegenClassScope classScope = new CodegenClassScope(includeDebugSymbols, packageScope, className);

        CodegenMethod hashMethod = CodegenMethod.makeParentNode(EPTypePremade.INTEGERPRIMITIVE.getEPType(), StmtClassForgeableMultiKey.class, CodegenSymbolProviderEmpty.INSTANCE, classScope);
        makeHashMethod(types.length, hashMethod);
        CodegenStackGenerator.recursiveBuildStack(hashMethod, "hashCode", methods);

        CodegenMethod equalsMethod = CodegenMethod.makeParentNode(EPTypePremade.BOOLEANPRIMITIVE.getEPType(), StmtClassForgeableMultiKey.class, CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(EPTypePremade.OBJECT.getEPType(), "o");
        makeEqualsMethod(types.length, equalsMethod);
        CodegenStackGenerator.recursiveBuildStack(equalsMethod, "equals", methods);

        CodegenMethod getNumKeysMethod = CodegenMethod.makeParentNode(EPTypePremade.INTEGERPRIMITIVE.getEPType(), StmtClassForgeableMultiKey.class, CodegenSymbolProviderEmpty.INSTANCE, classScope);
        getNumKeysMethod.getBlock().methodReturn(constant(types.length));
        CodegenStackGenerator.recursiveBuildStack(getNumKeysMethod, "getNumKeys", methods);

        CodegenMethod getKeyMethod = CodegenMethod.makeParentNode(EPTypePremade.OBJECT.getEPType(), StmtClassForgeableMultiKey.class, CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(EPTypePremade.INTEGERPRIMITIVE.getEPType(), "num");
        makeGetKeyMethod(types.length, getKeyMethod);
        CodegenStackGenerator.recursiveBuildStack(getKeyMethod, "getKey", methods);

        CodegenMethod toStringMethod = CodegenMethod.makeParentNode(EPTypePremade.STRING.getEPType(), StmtClassForgeableMultiKey.class, CodegenSymbolProviderEmpty.INSTANCE, classScope);
        makeToStringMethod(toStringMethod);
        CodegenStackGenerator.recursiveBuildStack(toStringMethod, "toString", methods);

        return new CodegenClass(CodegenClassType.KEYPROVISIONING, MultiKey.EPTYPE, className, classScope, Collections.emptyList(), ctor, methods, Collections.emptyList());
    }

    public String getClassName() {
        return className;
    }

    public StmtClassForgeableType getForgeableType() {
        return StmtClassForgeableType.MULTIKEY;
    }

    private void makeEqualsMethod(int length, CodegenMethod equalsMethod) {
        /*
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SampleKey sampleKey = (SampleKey) o;

            if (a != null ? !a.equals(sampleKey.a) : sampleKey.a != null) return false; // or Arrays.equals or deepEquals
            return b != null ? b.equals(sampleKey.b) : sampleKey.b == null; // or Arrays.equals or deepEquals
        }
        */

        equalsMethod.getBlock()
            .ifCondition(equalsIdentity(ref("this"), ref("o"))).blockReturn(constant(true));

        if (!lenientEquals) {
            equalsMethod.getBlock().ifCondition(or(equalsNull(ref("o")), not(equalsIdentity(exprDotMethod(ref("this"), "getClass"), exprDotMethod(ref("o"), "getClass"))))).blockReturn(constant(false))
                .declareVar(className, "k", cast(className, ref("o")));

            for (int i = 0; i < length; i++) {
                CodegenExpressionRef self = ref("k" + i);
                CodegenExpressionRef other = ref("k.k" + i);
                if (i < length - 1) {
                    if (types[i] != EPTypeNull.INSTANCE) {
                        CodegenExpression notEquals = getNotEqualsExpression((EPTypeClass) types[i], self, other);
                        equalsMethod.getBlock().ifCondition(notEquals).blockReturn(constantFalse());
                    }
                } else {
                    if (types[i] != EPTypeNull.INSTANCE) {
                        CodegenExpression equals = getEqualsExpression((EPTypeClass) types[i], self, other);
                        equalsMethod.getBlock().methodReturn(equals);
                    } else {
                        equalsMethod.getBlock().methodReturn(constantTrue());
                    }
                }
            }
            return;
        }

        // Lenient-equals:
        // - does not check the class
        // - pull the key value from the "getKey" method of KeyProvisioning
        // - may cast the key in case of Array.equals
        equalsMethod.getBlock().ifCondition(not(instanceOf(ref("o"), MultiKey.EPTYPE))).blockReturn(constant(false))
            .declareVar(MultiKey.EPTYPE, "k", cast(MultiKey.EPTYPE, ref("o")));

        for (int i = 0; i < length; i++) {
            CodegenExpressionRef self = ref("k" + i);
            CodegenExpression other = exprDotMethod(ref("k"), "getKey", constant(i));
            if (types[i] != EPTypeNull.INSTANCE) {
                EPTypeClass type = (EPTypeClass) types[i];
                if (type.getType().isArray()) {
                    other = cast(type, other);
                }
                if (i < length - 1) {
                    CodegenExpression notEquals = getNotEqualsExpression(type, self, other);
                    equalsMethod.getBlock().ifCondition(notEquals).blockReturn(constantFalse());
                } else {
                    CodegenExpression equals = getEqualsExpression(type, self, other);
                    equalsMethod.getBlock().methodReturn(equals);
                }
            } else {
                if (i < length - 1) {
                    // no action
                } else {
                    equalsMethod.getBlock().methodReturn(constantTrue());
                }
            }
        }
    }

    private void makeGetKeyMethod(int length, CodegenMethod method) {
        /*
        public Object getKey(int num) {
            switch(num) {
                case 0: return k0;
                case 1: return k1;
                default: throw new UnsupportedOperationException();
            }
        }
        */

        CodegenBlock[] blocks = method.getBlock().switchBlockOfLength(ref("num"), length, true);
        for (int i = 0; i < length; i++) {
            blocks[i].blockReturn(ref("k" + i));
        }
    }

    private static CodegenExpression getEqualsExpression(EPTypeClass type, CodegenExpressionRef self, CodegenExpression other) {
        if (!type.getType().isArray()) {
            CodegenExpression cond = notEqualsNull(self);
            CodegenExpression condTrue = exprDotMethod(self, "equals", other);
            CodegenExpression condFalse = equalsNull(other);
            return conditional(cond, condTrue, condFalse);
        }
        if (requiresDeepEquals(type.getType().getComponentType())) {
            return staticMethod(Arrays.class, "deepEquals", self, other);
        } else {
            return staticMethod(Arrays.class, "equals", self, other);
        }
    }

    private static CodegenExpression getNotEqualsExpression(EPTypeClass type, CodegenExpressionRef self, CodegenExpression other) {
        if (!type.getType().isArray()) {
            CodegenExpression cond = notEqualsNull(self);
            CodegenExpression condTrue = not(exprDotMethod(self, "equals", other));
            CodegenExpression condFalse = notEqualsNull(other);
            return conditional(cond, condTrue, condFalse);
        }
        if (requiresDeepEquals(type.getType().getComponentType())) {
            return not(staticMethod(Arrays.class, "deepEquals", self, other));
        } else {
            return not(staticMethod(Arrays.class, "equals", self, other));
        }
    }

    private void makeHashMethod(int length, CodegenMethod hashMethod) {
        /*
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0; // (or Arrays.equals and Arrays.deepEquals)
            result = 31 * result + (b != null ? b.hashCode() : 0); // (or Arrays.equals and Arrays.deepEquals)
            return result;
        */
        CodegenExpression computeHash = getHashExpression(ref("k0"), types[0]);
        hashMethod.getBlock().declareVar(EPTypePremade.INTEGERPRIMITIVE.getEPType(), "h", computeHash);

        for (int i = 1; i < length; i++) {
            computeHash = getHashExpression(ref("k" + i), types[i]);
            hashMethod.getBlock().assignRef("h", op(op(constant(31), "*", ref("h")), "+", computeHash));
        }

        hashMethod.getBlock().methodReturn(ref("h"));
    }

    private static CodegenExpression getHashExpression(CodegenExpressionRef key, EPType type) {
        if (type == EPTypeNull.INSTANCE) {
            return constant(0);
        }
        EPTypeClass typeClass = (EPTypeClass) type;
        if (!typeClass.getType().isArray()) {
            return conditional(notEqualsNull(key), exprDotMethod(key, "hashCode"), constant(0));
        }
        if (requiresDeepEquals(typeClass.getType().getComponentType())) {
            return staticMethod(Arrays.class, "deepHashCode", key);
        } else {
            return staticMethod(Arrays.class, "hashCode", key);
        }
    }

    private void makeToStringMethod(CodegenMethod toStringMethod) {
        toStringMethod.getBlock()
            .declareVarNewInstance(EPTypePremade.STRINGBUILDER.getEPType(), "b")
            .exprDotMethod(ref("b"), "append", constant(MultiKey.class.getSimpleName() + "["));
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                toStringMethod.getBlock().exprDotMethod(ref("b"), "append", constant(","));
            }
            CodegenExpressionRef self = ref("k" + i);
            CodegenExpression text = self;
            if (types[i] == EPTypeNull.INSTANCE) {
                toStringMethod.getBlock().exprDotMethod(ref("b"), "append", constant("null"));
                continue;
            }
            if (((EPTypeClass) types[i]).getType().isArray()) {
                text = staticMethod(Arrays.class, "toString", self);
            }
            toStringMethod.getBlock().exprDotMethod(ref("b"), "append", text);
        }
        toStringMethod.getBlock()
            .exprDotMethod(ref("b"), "append", constant("]"))
            .methodReturn(exprDotMethod(ref("b"), "toString"));
    }
}
