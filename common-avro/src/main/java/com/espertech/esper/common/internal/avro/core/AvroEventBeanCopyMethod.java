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
package com.espertech.esper.common.internal.avro.core;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.event.core.EventBeanCopyMethod;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactory;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

import java.util.*;

/**
 * Copy method for Map-underlying events.
 */
public class AvroEventBeanCopyMethod implements EventBeanCopyMethod {
    public final static EPTypeClass EPTYPE = new EPTypeClass(AvroEventBeanCopyMethod.class);
    private final AvroEventType avroEventType;
    private final EventBeanTypedEventFactory eventAdapterService;

    public AvroEventBeanCopyMethod(AvroEventType avroEventType, EventBeanTypedEventFactory eventAdapterService) {
        this.avroEventType = avroEventType;
        this.eventAdapterService = eventAdapterService;
    }

    public EventBean copy(EventBean theEvent) {
        GenericData.Record original = (GenericData.Record) theEvent.getUnderlying();
        GenericData.Record copy = new GenericData.Record(avroEventType.getSchemaAvro());
        List<Schema.Field> fields = avroEventType.getSchemaAvro().getFields();
        for (Schema.Field field : fields) {
            if (field.schema().getType() == Schema.Type.ARRAY) {
                Collection originalColl = (Collection) original.get(field.pos());
                if (originalColl != null) {
                    copy.put(field.pos(), new ArrayList<>(originalColl));
                }
            } else if (field.schema().getType() == Schema.Type.MAP) {
                Map originalMap = (Map) original.get(field.pos());
                if (originalMap != null) {
                    copy.put(field.pos(), new HashMap<>(originalMap));
                }
            } else {
                copy.put(field.pos(), original.get(field.pos()));
            }
        }
        return eventAdapterService.adapterForTypedAvro(copy, avroEventType);
    }
}
