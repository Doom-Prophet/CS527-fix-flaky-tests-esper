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
package com.espertech.esper.common.internal.event.json.parser.delegates.array;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.event.json.parser.core.JsonDelegateBase;
import com.espertech.esper.common.internal.event.json.parser.core.JsonHandlerDelegator;
import com.espertech.esper.common.internal.event.json.parser.delegates.endvalue.JsonEndValueForgeLong;

public class JsonDelegateArrayLong extends JsonDelegateArrayBase {
    public final static EPTypeClass EPTYPE = new EPTypeClass(JsonDelegateArrayLong.class);
    public JsonDelegateArrayLong(JsonHandlerDelegator baseHandler, JsonDelegateBase parent) {
        super(baseHandler, parent);
    }

    public void endOfArrayValue(String name) {
        collection.add(JsonEndValueForgeLong.jsonToLong(stringValue, name));
    }

    public Object getResult() {
        return collection.toArray(new Long[collection.size()]);
    }
}
