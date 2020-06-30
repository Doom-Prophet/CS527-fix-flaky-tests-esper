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
package com.espertech.esper.common.internal.event.bean.introspect;

import com.espertech.esper.common.client.EventPropertyDescriptor;
import com.espertech.esper.common.client.configuration.common.ConfigurationCommonEventTypeBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeClassParameterized;
import com.espertech.esper.common.client.util.PropertyResolutionStyle;
import com.espertech.esper.common.internal.event.bean.core.PropertyStem;
import com.espertech.esper.common.internal.event.bean.getter.ReflectionPropFieldGetterFactory;
import com.espertech.esper.common.internal.event.bean.getter.ReflectionPropMethodGetterFactory;
import com.espertech.esper.common.internal.event.core.EventPropertyType;
import com.espertech.esper.common.internal.event.core.EventTypeUtility;
import com.espertech.esper.common.internal.util.ClassHelperGenericType;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.util.*;

public class BeanEventTypeStemBuilder {

    private final ConfigurationCommonEventTypeBean optionalConfig;

    private final PropertyResolutionStyle propertyResolutionStyle;
    private final boolean smartResolutionStyle;

    public BeanEventTypeStemBuilder(ConfigurationCommonEventTypeBean optionalConfig, PropertyResolutionStyle defaultPropertyResolutionStyle) {
        this.optionalConfig = optionalConfig;

        if (optionalConfig != null) {
            this.propertyResolutionStyle = optionalConfig.getPropertyResolutionStyle();
        } else {
            this.propertyResolutionStyle = defaultPropertyResolutionStyle;
        }

        this.smartResolutionStyle = propertyResolutionStyle.equals(PropertyResolutionStyle.CASE_INSENSITIVE) ||
                propertyResolutionStyle.equals(PropertyResolutionStyle.DISTINCT_CASE_INSENSITIVE);
    }

    public BeanEventTypeStem make(EPTypeClass clazz) {
        EventTypeUtility.validateEventBeanClassVisibility(clazz.getType());

        PropertyListBuilder propertyListBuilder = PropertyListBuilderFactory.createBuilder(optionalConfig);
        List<PropertyStem> properties = propertyListBuilder.assessProperties(clazz.getType());

        EventPropertyDescriptor[] propertyDescriptors = new EventPropertyDescriptor[properties.size()];
        Map<String, EventPropertyDescriptor> propertyDescriptorMap = new HashMap<>();
        String[] propertyNames = new String[properties.size()];
        Map<String, PropertyInfo> simpleProperties = new HashMap<>();
        Map<String, PropertyStem> mappedPropertyDescriptors = new HashMap<>();
        Map<String, PropertyStem> indexedPropertyDescriptors = new HashMap<>();

        Map<String, List<PropertyInfo>> simpleSmartPropertyTable = null;
        Map<String, List<PropertyInfo>> mappedSmartPropertyTable = null;
        Map<String, List<PropertyInfo>> indexedSmartPropertyTable = null;
        if (smartResolutionStyle) {
            simpleSmartPropertyTable = new HashMap<>();
            mappedSmartPropertyTable = new HashMap<>();
            indexedSmartPropertyTable = new HashMap<>();
        }

        int count = 0;
        for (PropertyStem desc : properties) {
            String propertyName = desc.getPropertyName();
            EPTypeClass underlyingType;
            boolean isRequiresIndex;
            boolean isRequiresMapkey;
            boolean isIndexed;
            boolean isMapped;
            boolean isFragment;

            if (desc.getPropertyType().equals(EventPropertyType.SIMPLE)) {
                EventPropertyGetterSPIFactory getter;
                EPTypeClass type;
                if (desc.getReadMethod() != null) {
                    getter = new ReflectionPropMethodGetterFactory(desc.getReadMethod());
                    type = ClassHelperGenericType.getMethodReturnEPType(desc.getReadMethod(), clazz);
                } else if (desc.getAccessorField() != null) {
                    getter = new ReflectionPropFieldGetterFactory(desc.getAccessorField());
                    type = ClassHelperGenericType.getFieldEPType(desc.getAccessorField(), clazz);
                } else {
                    // ignore property
                    continue;
                }

                underlyingType = type;
                isRequiresIndex = false;
                isRequiresMapkey = false;
                isIndexed = false;
                isMapped = false;
                if (JavaClassHelper.isImplementsInterface(type, Map.class)) {
                    isMapped = true;
                    // We do not yet allow to fragment maps entries.
                    // Class genericType = JavaClassHelper.getGenericReturnTypeMap(desc.getReadMethod(), desc.getAccessorField());
                    isFragment = false;
                } else if (type.getType().isArray()) {
                    isIndexed = true;
                    isFragment = JavaClassHelper.isFragmentableType(type.getType().getComponentType());
                } else if (JavaClassHelper.isImplementsInterface(type, Iterable.class)) {
                    isIndexed = true;
                    Class genericType;
                    if (type instanceof EPTypeClassParameterized) {
                        genericType = ((EPTypeClassParameterized) type).getParameters()[0].getType();
                    } else {
                        genericType = Object.class;
                    }
                    isFragment = JavaClassHelper.isFragmentableType(genericType);
                } else {
                    isMapped = false;
                    isFragment = JavaClassHelper.isFragmentableType(type);
                }
                simpleProperties.put(propertyName, new PropertyInfo(type, getter, desc));

                // Recognize that there may be properties with overlapping case-insentitive names
                if (smartResolutionStyle) {
                    // Find the property in the smart property table
                    String smartPropertyName = propertyName.toLowerCase(Locale.ENGLISH);
                    List<PropertyInfo> propertyInfoList = simpleSmartPropertyTable.get(smartPropertyName);
                    if (propertyInfoList == null) {
                        propertyInfoList = new ArrayList<PropertyInfo>();
                        simpleSmartPropertyTable.put(smartPropertyName, propertyInfoList);
                    }

                    // Enter the property into the smart property list
                    PropertyInfo propertyInfo = new PropertyInfo(type, getter, desc);
                    propertyInfoList.add(propertyInfo);
                }
            } else if (desc.getPropertyType().equals(EventPropertyType.MAPPED)) {
                mappedPropertyDescriptors.put(propertyName, desc);

                underlyingType = desc.getReturnType(clazz);
                isRequiresIndex = false;
                isRequiresMapkey = desc.getReadMethod().getParameterTypes().length > 0;
                isIndexed = false;
                isMapped = true;
                isFragment = false;

                // Recognize that there may be properties with overlapping case-insentitive names
                if (smartResolutionStyle) {
                    // Find the property in the smart property table
                    String smartPropertyName = propertyName.toLowerCase(Locale.ENGLISH);
                    List<PropertyInfo> propertyInfoList = mappedSmartPropertyTable.get(smartPropertyName);
                    if (propertyInfoList == null) {
                        propertyInfoList = new ArrayList<PropertyInfo>();
                        mappedSmartPropertyTable.put(smartPropertyName, propertyInfoList);
                    }

                    // Enter the property into the smart property list
                    PropertyInfo propertyInfo = new PropertyInfo(desc.getReturnType(clazz), null, desc);
                    propertyInfoList.add(propertyInfo);
                }
            } else if (desc.getPropertyType().equals(EventPropertyType.INDEXED)) {
                indexedPropertyDescriptors.put(propertyName, desc);

                underlyingType = desc.getReturnType(clazz);
                isRequiresIndex = desc.getReadMethod().getParameterTypes().length > 0;
                isRequiresMapkey = false;
                isIndexed = true;
                isMapped = false;
                isFragment = JavaClassHelper.isFragmentableType(desc.getReturnType(clazz));

                if (smartResolutionStyle) {
                    // Find the property in the smart property table
                    String smartPropertyName = propertyName.toLowerCase(Locale.ENGLISH);
                    List<PropertyInfo> propertyInfoList = indexedSmartPropertyTable.get(smartPropertyName);
                    if (propertyInfoList == null) {
                        propertyInfoList = new ArrayList<PropertyInfo>();
                        indexedSmartPropertyTable.put(smartPropertyName, propertyInfoList);
                    }

                    // Enter the property into the smart property list
                    PropertyInfo propertyInfo = new PropertyInfo(desc.getReturnType(clazz), null, desc);
                    propertyInfoList.add(propertyInfo);
                }
            } else {
                continue;
            }

            propertyNames[count] = desc.getPropertyName();
            EventPropertyDescriptor descriptor = new EventPropertyDescriptor(desc.getPropertyName(),
                    underlyingType, isRequiresIndex, isRequiresMapkey, isIndexed, isMapped, isFragment);
            propertyDescriptors[count++] = descriptor;
            propertyDescriptorMap.put(descriptor.getPropertyName(), descriptor);
        }

        // Determine event type super types
        EPTypeClass[] superTypes = getSuperTypes(clazz.getType());
        if (superTypes != null && superTypes.length == 0) {
            superTypes = null;
        }

        // Determine deep supertypes
        // Get Java super types (superclasses and interfaces), deep get of all in the tree
        Set<EPTypeClass> deepSuperTypes = new HashSet<>();
        getSuper(clazz.getType(), deepSuperTypes);
        removeJavaLibInterfaces(deepSuperTypes);    // Remove "java." super types

        return new BeanEventTypeStem(clazz, optionalConfig, propertyNames, simpleProperties, mappedPropertyDescriptors, indexedPropertyDescriptors,
                superTypes, deepSuperTypes, propertyResolutionStyle,
                simpleSmartPropertyTable, indexedSmartPropertyTable, mappedSmartPropertyTable,
                propertyDescriptors, propertyDescriptorMap);
    }

    private static EPTypeClass[] getSuperTypes(Class clazz) {
        List<EPTypeClass> superclasses = new LinkedList<>();

        // add superclass
        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            superclasses.add(ClassHelperGenericType.getClassEPType(superClass));
        }

        // add interfaces
        Class[] interfaces = clazz.getInterfaces();
        for (Class interfaceItem : interfaces) {
            superclasses.add(ClassHelperGenericType.getClassEPType(interfaceItem));
        }

        // Build super types, ignoring java language types
        List<EPTypeClass> superTypes = new LinkedList<>();
        for (EPTypeClass superclass : superclasses) {
            String superclassName = superclass.getType().getName();
            if (!superclassName.startsWith("java")) {
                superTypes.add(superclass);
            }
        }

        return superTypes.toArray(new EPTypeClass[superTypes.size()]);
    }

    /**
     * Add the given class's implemented interfaces and superclasses to the result set of classes.
     *
     * @param clazz  to introspect
     * @param result to add classes to
     */
    protected static void getSuper(Class clazz, Set<EPTypeClass> result) {
        getSuperInterfaces(clazz, result);
        getSuperClasses(clazz, result);
    }

    private static void getSuperInterfaces(Class clazz, Set<EPTypeClass> result) {
        Class[] interfaces = clazz.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            result.add(ClassHelperGenericType.getClassEPType(interfaces[i]));
            getSuperInterfaces(interfaces[i], result);
        }
    }

    private static void getSuperClasses(Class clazz, Set<EPTypeClass> result) {
        Class superClass = clazz.getSuperclass();
        if (superClass == null) {
            return;
        }

        result.add(ClassHelperGenericType.getClassEPType(superClass));
        getSuper(superClass, result);
    }

    private static void removeJavaLibInterfaces(Set<EPTypeClass> classes) {
        EPTypeClass[] types = classes.toArray(new EPTypeClass[0]);
        for (EPTypeClass clazz : types) {
            if (clazz.getType().getName().startsWith("java")) {
                classes.remove(clazz);
            }
        }
    }
}
