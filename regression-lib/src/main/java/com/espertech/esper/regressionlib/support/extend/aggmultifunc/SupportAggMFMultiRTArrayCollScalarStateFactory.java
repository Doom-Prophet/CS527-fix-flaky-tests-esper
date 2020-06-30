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
package com.espertech.esper.regressionlib.support.extend.aggmultifunc;

import com.espertech.esper.common.client.hook.aggmultifunc.AggregationMultiFunctionState;
import com.espertech.esper.common.client.hook.aggmultifunc.AggregationMultiFunctionStateFactory;
import com.espertech.esper.common.client.hook.aggmultifunc.AggregationMultiFunctionStateFactoryContext;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;

public class SupportAggMFMultiRTArrayCollScalarStateFactory implements AggregationMultiFunctionStateFactory {
    public final static EPTypeClass EPTYPE = new EPTypeClass(SupportAggMFMultiRTArrayCollScalarStateFactory.class);

    private ExprEvaluator evaluator;
    private EPTypeClass evaluationType;

    public AggregationMultiFunctionState newState(AggregationMultiFunctionStateFactoryContext ctx) {
        return new SupportAggMFMultiRTArrayCollScalarState(this);
    }

    public ExprEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(ExprEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void setEvaluationType(EPTypeClass evaluationType) {
        this.evaluationType = evaluationType;
    }

    public EPTypeClass getEvaluationType() {
        return evaluationType;
    }
}
