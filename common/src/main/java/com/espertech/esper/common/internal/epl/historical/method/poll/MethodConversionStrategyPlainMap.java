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
package com.espertech.esper.common.internal.epl.historical.method.poll;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.context.util.AgentInstanceContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MethodConversionStrategyPlainMap extends MethodConversionStrategyBase {
    public final static EPTypeClass EPTYPE = new EPTypeClass(MethodConversionStrategyPlainMap.class);

    public List<EventBean> convert(Object invocationResult, MethodTargetStrategy origin, AgentInstanceContext agentInstanceContext) {
        return Collections.singletonList(agentInstanceContext.getEventBeanTypedEventFactory().adapterForTypedMap((Map) invocationResult, eventType));
    }
}
