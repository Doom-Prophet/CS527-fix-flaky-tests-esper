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
package com.espertech.esper.common.internal.util;

import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionNewAnonymousClass;

import java.util.Locale;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * A factory for creating an instance of a parser that parses a String and returns a target type.
 */
public class SimpleTypeParserFactory {
    /**
     * Returns a parsers for the String value using the given Java built-in class for parsing.
     *
     * @param clazz is the class to parse the value to
     * @return value matching the type passed in
     */
    public static SimpleTypeParserSPI getParser(Class clazz) {
        Class classBoxed = JavaClassHelper.getBoxedType(clazz);

        if (classBoxed == String.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String value) {
                    return value;
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return input;
                }
            };
        }
        if (classBoxed == Character.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String value) {
                    return value.charAt(0);
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return exprDotMethod(input, "charAt", constant(0));
                }
            };
        }
        if (classBoxed == Boolean.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return BoolValue.parseString(text.toLowerCase(Locale.ENGLISH).trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(BoolValue.class, "parseString", exprDotMethodChain(input).add("toLowerCase", enumValue(Locale.class, "ENGLISH")).add("trim"));
                }
            };
        }
        if (classBoxed == Byte.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return Byte.decode(text.trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(Byte.class, "decode", exprDotMethod(input, "trim"));
                }
            };
        }
        if (classBoxed == Short.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return Short.parseShort(text.trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(Short.class, "parseShort", exprDotMethod(input, "trim"));
                }
            };
        }
        if (classBoxed == Long.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return LongValue.parseString(text.trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(LongValue.class, "parseString", exprDotMethod(input, "trim"));
                }
            };
        }
        if (classBoxed == Float.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return Float.parseFloat(text.trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(Float.class, "parseFloat", exprDotMethod(input, "trim"));
                }
            };
        }
        if (classBoxed == Double.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return Double.parseDouble(text.trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(Double.class, "parseDouble", exprDotMethod(input, "trim"));
                }
            };
        }
        if (classBoxed == Integer.class) {
            return new SimpleTypeParserSPI() {
                public Object parse(String text) {
                    return Integer.parseInt(text.trim());
                }

                public CodegenExpression codegen(CodegenExpression input) {
                    return staticMethod(Integer.class, "parseInt", exprDotMethod(input, "trim"));
                }
            };
        }
        return null;
    }

    public static CodegenExpression codegenSimpleParser(SimpleTypeParserSPI parser, CodegenMethod method, Class originator, CodegenClassScope classScope) {
        CodegenExpressionNewAnonymousClass anonymousClass = newAnonymousClass(method.getBlock(), SimpleTypeParser.EPTYPE);
        CodegenMethod parse = CodegenMethod.makeParentNode(EPTypePremade.OBJECT.getEPType(), originator, classScope).addParam(EPTypePremade.STRING.getEPType(), "value");
        anonymousClass.addMethod("parse", parse);
        parse.getBlock().methodReturn(parser.codegen(ref("value")));
        return anonymousClass;
    }
}
