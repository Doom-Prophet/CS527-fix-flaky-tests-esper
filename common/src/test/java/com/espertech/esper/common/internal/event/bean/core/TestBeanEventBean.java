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
package com.espertech.esper.common.internal.event.bean.core;

import com.espertech.esper.common.client.*;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeClassParameterized;
import com.espertech.esper.common.internal.support.*;
import com.espertech.esper.common.internal.supportunit.bean.SupportBeanCombinedProps;
import com.espertech.esper.common.internal.supportunit.bean.SupportBeanIterableProps;
import com.espertech.esper.common.internal.supportunit.bean.SupportBeanIterablePropsContainer;
import com.espertech.esper.common.internal.supportunit.event.SupportEventBeanFactory;
import com.espertech.esper.common.internal.supportunit.event.SupportEventTypeFactory;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.espertech.esper.common.client.type.EPTypeClassParameterized.from;
import static com.espertech.esper.common.internal.support.SupportEventPropUtil.assertPropEquals;

public class TestBeanEventBean extends TestCase {
    SupportBean testEvent;

    public void setUp() {
        testEvent = new SupportBean();
        testEvent.setIntPrimitive(10);
    }

    public void testGet() {
        EventType eventType = SupportEventTypeFactory.createBeanType(SupportBean.class);
        BeanEventBean eventBean = new BeanEventBean(testEvent, eventType);

        assertEquals(eventType, eventBean.getEventType());
        assertEquals(testEvent, eventBean.getUnderlying());

        assertEquals(10, eventBean.get("intPrimitive"));

        // Test wrong property name
        try {
            eventBean.get("dummy");
            assertTrue(false);
        } catch (PropertyAccessException ex) {
            // Expected
            log.debug(".testGetter Expected exception, msg=" + ex.getMessage());
        }

        // Test wrong event type - not possible to happen under normal use
        try {
            eventType = SupportEventTypeFactory.createBeanType(SupportBeanSimple.class);
            eventBean = new BeanEventBean(testEvent, eventType);
            eventBean.get("myString");
            assertTrue(false);
        } catch (PropertyAccessException ex) {
            // Expected
            log.debug(".testGetter Expected exception, msg=" + ex.getMessage());
        }
    }

    public void testGetComplexProperty() {
        SupportBeanCombinedProps eventCombined = SupportBeanCombinedProps.makeDefaultBean();
        EventBean eventBean = SupportEventBeanFactory.createObject(eventCombined);

        assertEquals("0ma0", eventBean.get("indexed[0].mapped('0ma').value"));
        assertEquals(String.class, eventBean.getEventType().getPropertyType("indexed[0].mapped('0ma').value"));
        assertNotNull(eventBean.getEventType().getGetter("indexed[0].mapped('0ma').value"));
        assertEquals("0ma1", eventBean.get("indexed[0].mapped('0mb').value"));
        assertEquals("1ma0", eventBean.get("indexed[1].mapped('1ma').value"));
        assertEquals("1ma1", eventBean.get("indexed[1].mapped('1mb').value"));

        assertEquals("0ma0", eventBean.get("array[0].mapped('0ma').value"));
        assertEquals("1ma1", eventBean.get("array[1].mapped('1mb').value"));
        assertEquals("0ma0", eventBean.get("array[0].mapprop('0ma').value"));
        assertEquals("1ma1", eventBean.get("array[1].mapprop('1mb').value"));

        tryInvalidGet(eventBean, "dummy");
        tryInvalidGet(eventBean, "dummy[1]");
        tryInvalidGet(eventBean, "dummy('dd')");
        tryInvalidGet(eventBean, "dummy.dummy1");

        // indexed getter
        tryInvalidGetFragment(eventBean, "indexed");
        assertEquals(SupportBeanCombinedProps.NestedLevOne.class, ((EventBean) eventBean.getFragment("indexed[0]")).getEventType().getUnderlyingType());
        assertEquals("abc", ((EventBean) eventBean.getFragment("array[0]")).get("nestLevOneVal"));
        assertEquals("abc", ((EventBean) eventBean.getFragment("array[2]?")).get("nestLevOneVal"));
        assertNull(eventBean.getFragment("array[3]?"));
        assertNull(eventBean.getFragment("array[4]?"));
        assertNull(eventBean.getFragment("array[5]?"));

        String eventText = SupportEventTypeAssertionUtil.print(eventBean);
        //System.out.println(eventText);

        SupportBeanComplexProps eventComplex = SupportBeanComplexProps.makeDefaultBean();
        eventBean = SupportEventBeanFactory.createObject(eventComplex);
        assertEquals("nestedValue", ((EventBean) eventBean.getFragment("nested")).get("nestedValue"));
    }

    public void testGetIterableListMap() {
        SupportBeanIterableProps eventComplex = SupportBeanIterableProps.makeDefaultBean();
        EventBean eventBean = SupportEventBeanFactory.createObject(eventComplex);
        SupportEventTypeAssertionUtil.assertConsistency(eventBean);

        // generic interogation : iterable, List and Map
        assertEquals(Iterable.class, eventBean.getEventType().getPropertyType("iterableNested"));
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, eventBean.getEventType().getPropertyType("iterableNested[0]"));
        assertEquals(Iterable.class, eventBean.getEventType().getPropertyType("iterableInteger"));
        assertEquals(Integer.class, eventBean.getEventType().getPropertyType("iterableInteger[0]"));
        assertEquals(List.class, eventBean.getEventType().getPropertyType("listNested"));
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, eventBean.getEventType().getPropertyType("listNested[0]"));
        assertEquals(List.class, eventBean.getEventType().getPropertyType("listInteger"));
        assertEquals(Integer.class, eventBean.getEventType().getPropertyType("listInteger[0]"));
        assertEquals(Map.class, eventBean.getEventType().getPropertyType("mapNested"));
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, eventBean.getEventType().getPropertyType("mapNested('a')"));
        assertEquals(Map.class, eventBean.getEventType().getPropertyType("mapInteger"));
        assertEquals(Integer.class, eventBean.getEventType().getPropertyType("mapInteger('a')"));
        assertEquals(Iterable.class, eventBean.getEventType().getPropertyType("iterableUndefined"));
        assertEquals(Iterable.class, eventBean.getEventType().getPropertyType("iterableObject"));
        assertEquals(Object.class, eventBean.getEventType().getPropertyType("iterableUndefined[0]"));
        assertEquals(Object.class, eventBean.getEventType().getPropertyType("iterableObject[0]"));

        assertPropEquals(new SupportEventPropDesc("iterableNested", EPTypeClassParameterized.from(Iterable.class, SupportBeanIterableProps.SupportBeanSpecialGetterNested.class)).componentType(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class).indexed().fragment(), eventBean.getEventType().getPropertyDescriptor("iterableNested"));
        assertPropEquals(new SupportEventPropDesc("iterableInteger", EPTypeClassParameterized.from(Iterable.class, Integer.class)).componentType(Integer.class).indexed(), eventBean.getEventType().getPropertyDescriptor("iterableInteger"));
        assertPropEquals(new SupportEventPropDesc("listNested", EPTypeClassParameterized.from(List.class, SupportBeanIterableProps.SupportBeanSpecialGetterNested.class)).componentType(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class).indexed().fragment(), eventBean.getEventType().getPropertyDescriptor("listNested"));
        assertPropEquals(new SupportEventPropDesc("listInteger", EPTypeClassParameterized.from(List.class, Integer.class)).componentType(Integer.class).indexed(), eventBean.getEventType().getPropertyDescriptor("listInteger"));
        assertPropEquals(new SupportEventPropDesc("mapNested", from(Map.class, String.class, SupportBeanIterableProps.SupportBeanSpecialGetterNested.class)).componentType(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class).mapped(), eventBean.getEventType().getPropertyDescriptor("mapNested"));
        assertPropEquals(new SupportEventPropDesc("mapInteger", from(Map.class, String.class, Integer.class)).componentType(Integer.class).mapped(), eventBean.getEventType().getPropertyDescriptor("mapInteger"));
        assertPropEquals(new SupportEventPropDesc("iterableUndefined", new EPTypeClass(Iterable.class)).indexed(), eventBean.getEventType().getPropertyDescriptor("iterableUndefined"));
        assertPropEquals(new SupportEventPropDesc("iterableObject", EPTypeClassParameterized.from(Iterable.class, Object.class)).componentType(Object.class).indexed(), eventBean.getEventType().getPropertyDescriptor("iterableObject"));

        assertNestedCollection(eventBean, "iterableNested", "I");
        assertNestedCollection(eventBean, "listNested", "L");
        assertNestedElement(eventBean, "mapNested('a')", "MN1");    // note that property descriptors do not indicate Map-values are fragments
        assertNestedElement(eventBean, "mapNested('b')", "MN2");
        assertNestedElement(eventBean, "listNested[0]", "LN1");
        assertNestedElement(eventBean, "listNested[1]", "LN2");
        assertNestedElement(eventBean, "iterableNested[0]", "IN1");
        assertNestedElement(eventBean, "iterableNested[1]", "IN2");

        assertNull(eventBean.getEventType().getFragmentType("iterableInteger"));
        assertNull(eventBean.getEventType().getFragmentType("listInteger"));
        assertNull(eventBean.getEventType().getFragmentType("iterableInteger[0]"));
        assertNull(eventBean.getEventType().getFragmentType("listInteger[0]"));
        assertNull(eventBean.getEventType().getFragmentType("mapNested"));
        assertNull(eventBean.getEventType().getFragmentType("mapInteger"));
    }

    public void testGetIterableListMapContained() {
        SupportBeanIterablePropsContainer eventIterableContained = SupportBeanIterablePropsContainer.makeDefaultBean();
        EventBean eventBean = SupportEventBeanFactory.createObject(eventIterableContained);

        assertEquals(Iterable.class, eventBean.getEventType().getPropertyType("contained.iterableNested"));
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, eventBean.getEventType().getPropertyType("contained.iterableNested[0]"));
        assertEquals(Iterable.class, eventBean.getEventType().getPropertyType("contained.iterableInteger"));
        assertEquals(Integer.class, eventBean.getEventType().getPropertyType("contained.iterableInteger[0]"));
        assertEquals(List.class, eventBean.getEventType().getPropertyType("contained.listNested"));
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, eventBean.getEventType().getPropertyType("contained.listNested[0]"));
        assertEquals(List.class, eventBean.getEventType().getPropertyType("contained.listInteger"));
        assertEquals(Integer.class, eventBean.getEventType().getPropertyType("contained.listInteger[0]"));
        assertEquals(Map.class, eventBean.getEventType().getPropertyType("contained.mapNested"));
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, eventBean.getEventType().getPropertyType("contained.mapNested('a')"));
        assertEquals(Map.class, eventBean.getEventType().getPropertyType("contained.mapInteger"));
        assertEquals(Integer.class, eventBean.getEventType().getPropertyType("contained.mapInteger('a')"));

        assertNestedElement(eventBean, "contained.mapNested('a')", "MN1");    // note that property descriptors do not indicate Map-values are fragments
        assertNestedElement(eventBean, "contained.mapNested('b')", "MN2");
        assertNestedElement(eventBean, "contained.listNested[0]", "LN1");
        assertNestedElement(eventBean, "contained.listNested[1]", "LN2");
        assertNestedElement(eventBean, "contained.iterableNested[0]", "IN1");
        assertNestedElement(eventBean, "contained.iterableNested[1]", "IN2");
        assertNestedCollection(eventBean, "contained.iterableNested", "I");
        assertNestedCollection(eventBean, "contained.listNested", "L");

        assertNull(eventBean.getEventType().getFragmentType("contained.iterableInteger"));
        assertNull(eventBean.getEventType().getFragmentType("contained.listInteger"));
        assertNull(eventBean.getEventType().getFragmentType("contained.iterableInteger[0]"));
        assertNull(eventBean.getEventType().getFragmentType("contained.listInteger[0]"));
        assertNull(eventBean.getEventType().getFragmentType("contained.mapNested"));
        assertNull(eventBean.getEventType().getFragmentType("contained.mapInteger"));
    }

    private void assertNestedElement(EventBean eventBean, String propertyName, String value) {
        FragmentEventType fragmentTypeOne = eventBean.getEventType().getFragmentType(propertyName);
        assertEquals(true, fragmentTypeOne.isNative());
        assertEquals(false, fragmentTypeOne.isIndexed());
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, fragmentTypeOne.getFragmentType().getUnderlyingType());

        EventBean theEvent = (EventBean) eventBean.getFragment(propertyName);
        assertEquals(value, theEvent.get("nestedValue"));
    }

    private void assertNestedCollection(EventBean eventBean, String propertyName, String prefix) {
        FragmentEventType fragmentTypeTwo = eventBean.getEventType().getFragmentType(propertyName);
        assertEquals(true, fragmentTypeTwo.isNative());
        assertEquals(true, fragmentTypeTwo.isIndexed());
        assertEquals(SupportBeanIterableProps.SupportBeanSpecialGetterNested.class, fragmentTypeTwo.getFragmentType().getUnderlyingType());

        EventBean[] events = (EventBean[]) eventBean.getFragment(propertyName);
        assertEquals(2, events.length);
        assertEquals(prefix + "N1", events[0].get("nestedValue"));
        assertEquals(prefix + "N2", events[1].get("nestedValue"));
    }

    private static void tryInvalidGet(EventBean eventBean, String propName) {
        try {
            eventBean.get(propName);
            fail();
        } catch (PropertyAccessException ex) {
            // expected
        }

        assertNull(eventBean.getEventType().getPropertyType(propName));
        assertNull(eventBean.getEventType().getGetter(propName));
    }

    private static void tryInvalidGetFragment(EventBean eventBean, String propName) {
        try {
            eventBean.getFragment(propName);
            fail();
        } catch (PropertyAccessException ex) {
            // expected
        }
    }

    private static final Logger log = LoggerFactory.getLogger(TestBeanEventBean.class);
}
