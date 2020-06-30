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
package com.espertech.esper.common.internal.epl.datetime.calop;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.internal.epl.datetime.reformatop.ReformatFormatForgeDesc;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.rettype.EPChainableType;
import com.espertech.esper.common.internal.rettype.EPChainableTypeClass;
import com.espertech.esper.common.internal.util.ClassHelperPrint;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class CalendarOpUtil {

    protected static Integer getInt(ExprEvaluator expr, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
        Object result = expr.evaluate(eventsPerStream, isNewData, context);
        if (result == null) {
            return null;
        }
        return (Integer) result;
    }

    public static CalendarFieldEnum getEnum(String methodName, ExprNode exprNode) throws ExprValidationException {
        String message = validateConstant(methodName, exprNode);
        if (message != null) {
            message += ", " + getValidFieldNamesMessage();
            throw new ExprValidationException(message);
        }
        String fieldname = (String) exprNode.getForge().getExprEvaluator().evaluate(null, true, null);
        CalendarFieldEnum fieldNum = CalendarFieldEnum.fromString(fieldname);
        if (fieldNum == null) {
            throw new ExprValidationException(getMessage(methodName) + " datetime-field name '" + fieldname + "' is not recognized, " + getValidFieldNamesMessage());
        }
        return fieldNum;
    }

    public static ReformatFormatForgeDesc validateGetFormatterType(EPChainableType inputType, String methodName, ExprNode exprNode) throws ExprValidationException {
        if (!(inputType instanceof EPChainableTypeClass)) {
            throw new ExprValidationException(getMessage(methodName) + " requires a datetime input value but received " + inputType);
        }

        if (!exprNode.getForge().getForgeConstantType().isConstant()) {
            throw new ExprValidationException(getMessage(methodName) + " requires a constant-value format");
        }

        ExprForge formatForge = exprNode.getForge();
        EPType formatType = formatForge.getEvaluationType();
        if (formatType == null || formatType == EPTypeNull.INSTANCE) {
            throw new ExprValidationException(getMessage(methodName) + " invalid null format object");
        }
        EPTypeClass formatClass = (EPTypeClass) formatType;

        Object format = null;
        if (formatForge.getForgeConstantType().isCompileTimeConstant()) {
            format = ExprNodeUtilityEvaluate.evaluateValidationTimeNoStreams(exprNode.getForge().getExprEvaluator(), null, "date format");
            if (format == null) {
                throw new ExprValidationException(getMessage(methodName) + " invalid null format object");
            }
        }

        // handle legacy date
        EPChainableTypeClass input = (EPChainableTypeClass) inputType;
        Class inputTypeClass = input.getType().getType();
        if (JavaClassHelper.getBoxedType(input.getType().getType()) == Long.class ||
            JavaClassHelper.isSubclassOrImplementsInterface(inputTypeClass, Date.class) ||
            JavaClassHelper.isSubclassOrImplementsInterface(inputTypeClass, Calendar.class)) {

            if (JavaClassHelper.isSubclassOrImplementsInterface(formatClass, DateFormat.class)) {
                return new ReformatFormatForgeDesc(false, DateFormat.class);
            }
            if (JavaClassHelper.isSubclassOrImplementsInterface(formatClass, String.class)) {
                if (format != null) {
                    try {
                        new SimpleDateFormat((String) format);
                    } catch (RuntimeException ex) {
                        throw new ExprValidationException(getMessage(methodName) + " invalid format string (SimpleDateFormat): " + ex.getMessage(), ex);
                    }
                }
                return new ReformatFormatForgeDesc(false, String.class);
            }
            throw getFailedExpected(methodName, DateFormat.class, formatClass.getType());
        }

        // handle jdk8 date
        if (JavaClassHelper.isSubclassOrImplementsInterface(formatClass, DateTimeFormatter.class)) {
            return new ReformatFormatForgeDesc(true, DateTimeFormatter.class);
        }
        if (JavaClassHelper.isSubclassOrImplementsInterface(formatClass, String.class)) {
            if (format != null) {
                try {
                    DateTimeFormatter.ofPattern((String) format);
                } catch (RuntimeException ex) {
                    throw new ExprValidationException(getMessage(methodName) + " invalid format string (DateTimeFormatter): " + ex.getMessage(), ex);
                }
            }
            return new ReformatFormatForgeDesc(true, String.class);
        }
        throw getFailedExpected(methodName, DateTimeFormatter.class, formatClass.getType());
    }

    private static ExprValidationException getFailedExpected(String methodName, Class expected, Class received) {
        return new ExprValidationException(getMessage(methodName) + " invalid format, expected string-format or " + expected.getSimpleName() + " but received " + ClassHelperPrint.getClassNameFullyQualPretty(received));
    }

    private static String validateConstant(String methodName, ExprNode exprNode) {
        if (ExprNodeUtilityQuery.isConstant(exprNode)) {
            return null;
        }
        return getMessage(methodName) + " requires a constant string-type parameter as its first parameter";
    }

    private static String getMessage(String methodName) {
        return "Date-time enumeration method '" + methodName + "'";
    }

    private static String getValidFieldNamesMessage() {
        return "valid field names are '" + CalendarFieldEnum.getValidList() + "'";
    }
}
