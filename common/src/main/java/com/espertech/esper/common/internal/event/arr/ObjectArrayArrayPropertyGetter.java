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
package com.espertech.esper.common.internal.event.arr;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.PropertyAccessException;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.context.module.EPStatementInitServices;
import com.espertech.esper.common.internal.event.core.BaseNestableEventUtil;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactory;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactoryCodegenField;
import com.espertech.esper.common.internal.event.core.EventTypeUtility;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Getter for Map-entries with well-defined fragment type.
 */
public class ObjectArrayArrayPropertyGetter implements ObjectArrayEventPropertyGetterAndIndexed {
    private final int propertyIndex;
    private final int index;
    private final EventBeanTypedEventFactory eventBeanTypedEventFactory;
    private final EventType fragmentType;

    /**
     * Ctor.
     *
     * @param propertyIndex              property index
     * @param index                      array index
     * @param eventBeanTypedEventFactory factory for event beans and event types
     * @param fragmentType               type of the entry returned
     */
    public ObjectArrayArrayPropertyGetter(int propertyIndex, int index, EventBeanTypedEventFactory eventBeanTypedEventFactory, EventType fragmentType) {
        this.propertyIndex = propertyIndex;
        this.index = index;
        this.fragmentType = fragmentType;
        this.eventBeanTypedEventFactory = eventBeanTypedEventFactory;
    }

    public boolean isObjectArrayExistsProperty(Object[] array) {
        return true;
    }

    public Object getObjectArray(Object[] array) throws PropertyAccessException {
        return getObjectArrayInternal(array, index);
    }

    public Object get(EventBean eventBean, int index) throws PropertyAccessException {
        Object[] array = BaseNestableEventUtil.checkedCastUnderlyingObjectArray(eventBean);
        return getObjectArrayInternal(array, index);
    }

    public Object get(EventBean obj) throws PropertyAccessException {
        Object[] array = BaseNestableEventUtil.checkedCastUnderlyingObjectArray(obj);
        return getObjectArray(array);
    }

    private Object getObjectArrayInternal(Object[] array, int index) throws PropertyAccessException {
        Object value = array[propertyIndex];
        return BaseNestableEventUtil.getBNArrayValueAtIndexWithNullCheck(value, index);
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true;
    }

    public Object getFragment(EventBean obj) throws PropertyAccessException {
        Object fragmentUnderlying = get(obj);
        return BaseNestableEventUtil.getBNFragmentNonPojo(fragmentUnderlying, fragmentType, eventBeanTypedEventFactory);
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingGetCodegen(castUnderlying(EPTypePremade.OBJECTARRAY.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return constantTrue();
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingFragmentCodegen(castUnderlying(EPTypePremade.OBJECTARRAY.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return staticMethod(BaseNestableEventUtil.class, "getBNArrayValueAtIndexWithNullCheck", arrayAtIndex(underlyingExpression, constant(propertyIndex)), constant(index));
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return constantTrue();
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        CodegenExpressionField mSvc = codegenClassScope.addOrGetFieldSharable(EventBeanTypedEventFactoryCodegenField.INSTANCE);
        CodegenExpressionField mType = codegenClassScope.addFieldUnshared(true, EventType.EPTYPE, EventTypeUtility.resolveTypeCodegen(fragmentType, EPStatementInitServices.REF));
        return staticMethod(BaseNestableEventUtil.class, "getBNFragmentNonPojo", underlyingGetCodegen(underlyingExpression, codegenMethodScope, codegenClassScope), mType, mSvc);
    }

    public CodegenExpression eventBeanGetIndexedCodegen(CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope, CodegenExpression beanExpression, CodegenExpression key) {
        return staticMethod(BaseNestableEventUtil.class, "getBNArrayValueAtIndexWithNullCheck", arrayAtIndex(castUnderlying(EPTypePremade.OBJECTARRAY.getEPType(), beanExpression), constant(propertyIndex)), key);
    }
}