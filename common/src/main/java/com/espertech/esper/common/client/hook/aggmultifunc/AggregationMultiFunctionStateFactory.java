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
package com.espertech.esper.common.client.hook.aggmultifunc;

import com.espertech.esper.common.client.type.EPTypeClass;

/**
 * Factory for aggregation multi-function state
 */
public interface AggregationMultiFunctionStateFactory {
    /**
     * Type information.
     */
    EPTypeClass EPTYPE = new EPTypeClass(AggregationMultiFunctionStateFactory.class);

    /**
     * Returns a new state holder
     *
     * @param ctx contextual information
     * @return state
     */
    AggregationMultiFunctionState newState(AggregationMultiFunctionStateFactoryContext ctx);
}
