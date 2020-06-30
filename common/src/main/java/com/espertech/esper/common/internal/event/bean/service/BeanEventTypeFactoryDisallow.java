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
package com.espertech.esper.common.internal.event.bean.service;

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.event.bean.core.BeanEventType;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactory;
import com.espertech.esper.common.internal.event.eventtypefactory.EventTypeFactory;

public class BeanEventTypeFactoryDisallow implements BeanEventTypeFactory {
    private final EventBeanTypedEventFactory eventBeanTypedEventFactory;

    public BeanEventTypeFactoryDisallow(EventBeanTypedEventFactory eventBeanTypedEventFactory) {
        this.eventBeanTypedEventFactory = eventBeanTypedEventFactory;
    }

    public BeanEventType getCreateBeanType(EPTypeClass clazz, boolean publicFields) {
        throw new EPException("Bean type creation not supported");
    }

    public EventBeanTypedEventFactory getEventBeanTypedEventFactory() {
        return eventBeanTypedEventFactory;
    }

    public EventTypeFactory getEventTypeFactory() {
        throw new EPException("Event type creation not supported");
    }
}
