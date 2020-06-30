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
package com.espertech.esper.common.internal.event.core;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Copy method for wrapper events.
 */
public class WrapperEventBeanCopyMethod implements EventBeanCopyMethod {
    public final static EPTypeClass EPTYPE = new EPTypeClass(WrapperEventBeanCopyMethod.class);
    private final WrapperEventType wrapperEventType;
    private final EventBeanTypedEventFactory eventAdapterService;
    private final EventBeanCopyMethod underlyingCopyMethod;

    /**
     * Ctor.
     *
     * @param wrapperEventType     wrapper type
     * @param eventAdapterService  event adapter creation
     * @param underlyingCopyMethod copy method for the underlying event
     */
    public WrapperEventBeanCopyMethod(WrapperEventType wrapperEventType, EventBeanTypedEventFactory eventAdapterService, EventBeanCopyMethod underlyingCopyMethod) {
        this.wrapperEventType = wrapperEventType;
        this.eventAdapterService = eventAdapterService;
        this.underlyingCopyMethod = underlyingCopyMethod;
    }

    public EventBean copy(EventBean theEvent) {
        DecoratingEventBean decorated = (DecoratingEventBean) theEvent;
        EventBean decoratedUnderlying = decorated.getUnderlyingEvent();
        EventBean copiedUnderlying = underlyingCopyMethod.copy(decoratedUnderlying);
        if (copiedUnderlying == null) {
            return null;
        }
        Map<String, Object> copiedMap = new HashMap<String, Object>(decorated.getDecoratingProperties());
        return eventAdapterService.adapterForTypedWrapper(copiedUnderlying, copiedMap, wrapperEventType);
    }
}
