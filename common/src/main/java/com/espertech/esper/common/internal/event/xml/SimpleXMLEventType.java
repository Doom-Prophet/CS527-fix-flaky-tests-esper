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
package com.espertech.esper.common.internal.event.xml;

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.FragmentEventType;
import com.espertech.esper.common.client.configuration.common.ConfigurationCommonEventTypeXMLDOM;
import com.espertech.esper.common.client.meta.EventTypeMetadata;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactory;
import com.espertech.esper.common.internal.event.core.EventPropertyGetterSPI;
import com.espertech.esper.common.internal.event.core.EventTypeNameResolver;
import com.espertech.esper.common.internal.event.property.Property;
import com.espertech.esper.common.internal.event.property.PropertyParser;
import com.espertech.esper.common.internal.event.propertyparser.PropertyParserNoDep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Optimistic try to resolve the property string into an appropiate xPath,
 * and use it as getter.
 * Mapped and Indexed properties supported.
 * Because no type information is given, all property are resolved to String.
 * No namespace support.
 * Cannot access to xml attributes, only elements content.
 * <p>
 * If an xsd is present, then use {@link com.espertech.esper.common.internal.event.xml.SchemaXMLEventType SchemaXMLEventType }
 *
 * @author pablo
 */
public class SimpleXMLEventType extends BaseXMLEventType {

    private static final Logger log = LoggerFactory.getLogger(SimpleXMLEventType.class);
    private final Map<String, EventPropertyGetterSPI> propertyGetterCache;
    private String defaultNamespacePrefix;
    private final boolean isResolvePropertiesAbsolute;

    public SimpleXMLEventType(EventTypeMetadata eventTypeMetadata, ConfigurationCommonEventTypeXMLDOM configurationEventTypeXMLDOM, EventBeanTypedEventFactory eventBeanTypedEventFactory, EventTypeNameResolver eventTypeResolver, XMLFragmentEventTypeFactory xmlEventTypeFactory) {
        super(eventTypeMetadata, configurationEventTypeXMLDOM, eventBeanTypedEventFactory, eventTypeResolver, xmlEventTypeFactory);
        isResolvePropertiesAbsolute = configurationEventTypeXMLDOM.isXPathResolvePropertiesAbsolute();
        propertyGetterCache = new HashMap<String, EventPropertyGetterSPI>();

        // Set of namespace context for XPath expressions
        XPathNamespaceContext xPathNamespaceContext = new XPathNamespaceContext();
        for (Map.Entry<String, String> entry : configurationEventTypeXMLDOM.getNamespacePrefixes().entrySet()) {
            xPathNamespaceContext.addPrefix(entry.getKey(), entry.getValue());
        }
        if (configurationEventTypeXMLDOM.getDefaultNamespace() != null) {
            String defaultNamespace = configurationEventTypeXMLDOM.getDefaultNamespace();
            xPathNamespaceContext.setDefaultNamespace(defaultNamespace);

            // determine a default namespace prefix to use to construct XPath expressions from pure property names
            defaultNamespacePrefix = null;
            for (Map.Entry<String, String> entry : configurationEventTypeXMLDOM.getNamespacePrefixes().entrySet()) {
                if (entry.getValue().equals(defaultNamespace)) {
                    defaultNamespacePrefix = entry.getKey();
                    break;
                }
            }
        }
        super.setNamespaceContext(xPathNamespaceContext);
        super.initialize(configurationEventTypeXMLDOM.getXPathProperties().values(), Collections.EMPTY_LIST);
    }

    protected EPType doResolvePropertyType(String propertyExpression) {
        return resolveSimpleXMLPropertyType(propertyExpression);
    }

    protected EventPropertyGetterSPI doResolvePropertyGetter(String propertyExpression) {
        EventPropertyGetterSPI getter = propertyGetterCache.get(propertyExpression);
        if (getter != null) {
            return getter;
        }

        getter = resolveSimpleXMLPropertyGetter(propertyExpression, this, defaultNamespacePrefix, isResolvePropertiesAbsolute);

        // no fragment factory, fragments not allowed
        propertyGetterCache.put(propertyExpression, getter);
        return getter;
    }

    protected FragmentEventType doResolveFragmentType(String property) {
        return null;  // Since we have no type information, the fragments are not allowed unless explicitly configured via XPath getter
    }

    public static EPTypeClass resolveSimpleXMLPropertyType(String propertyExpression) {
        Property prop = PropertyParser.parseAndWalkLaxToSimple(propertyExpression);
        if (PropertyParser.isPropertyDynamic(prop)) {
            return EPTypePremade.NODE.getEPType();
        } else {
            return EPTypePremade.STRING.getEPType();
        }
    }

    public static EventPropertyGetterSPI resolveSimpleXMLPropertyGetter(String propertyExpression, BaseXMLEventType baseXMLEventType, String defaultNamespacePrefix, boolean isResolvePropertiesAbsolute) {
        if (!baseXMLEventType.getConfigurationEventTypeXMLDOM().isXPathPropertyExpr()) {
            Property prop = PropertyParser.parseAndWalkLaxToSimple(propertyExpression);
            EventPropertyGetterSPI getter = prop.getGetterDOM();
            if (!prop.isDynamic()) {
                getter = new DOMConvertingGetter((DOMPropertyGetter) getter, String.class);
            }
            return getter;
        }

        XPathExpression xPathExpression;
        String xPathExpr;
        boolean isDynamic;
        try {
            Property property = PropertyParserNoDep.parseAndWalkLaxToSimple(propertyExpression, false);
            isDynamic = PropertyParser.isPropertyDynamic(property);

            xPathExpr = SimpleXMLPropertyParser.walk(property, baseXMLEventType.getRootElementName(), defaultNamespacePrefix, isResolvePropertiesAbsolute);
            XPath xpath = baseXMLEventType.getXPathFactory().newXPath();
            xpath.setNamespaceContext(baseXMLEventType.namespaceContext);
            if (log.isInfoEnabled()) {
                log.info("Compiling XPath expression for property '" + propertyExpression + "' as '" + xPathExpr + "'");
            }
            xPathExpression = xpath.compile(xPathExpr);
        } catch (XPathExpressionException e) {
            throw new EPException("Error constructing XPath expression from property name '" + propertyExpression + '\'', e);
        }

        QName xPathReturnType;
        if (isDynamic) {
            xPathReturnType = XPathConstants.NODE;
        } else {
            xPathReturnType = XPathConstants.STRING;
        }
        return new XPathPropertyGetter(baseXMLEventType, propertyExpression, xPathExpr, xPathExpression, xPathReturnType, null, null);
    }
}
