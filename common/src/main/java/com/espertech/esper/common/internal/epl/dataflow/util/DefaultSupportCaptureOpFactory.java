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
package com.espertech.esper.common.internal.epl.dataflow.util;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.dataflow.interfaces.DataFlowOpFactoryInitializeContext;
import com.espertech.esper.common.internal.epl.dataflow.interfaces.DataFlowOpInitializeContext;
import com.espertech.esper.common.internal.epl.dataflow.interfaces.DataFlowOperator;
import com.espertech.esper.common.internal.epl.dataflow.interfaces.DataFlowOperatorFactory;

public class DefaultSupportCaptureOpFactory<T> implements DataFlowOperatorFactory {
    public final static EPTypeClass EPTYPE = new EPTypeClass(DefaultSupportCaptureOpFactory.class);

    private String name;

    public void initializeFactory(DataFlowOpFactoryInitializeContext context) {

    }

    public DataFlowOperator operator(DataFlowOpInitializeContext context) {
        return new DefaultSupportCaptureOp();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

