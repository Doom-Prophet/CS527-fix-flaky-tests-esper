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
package com.espertech.esper.common.client.hook.expr;

import com.espertech.esper.common.client.type.EPTypeClass;

/**
 * Invocation context for method invocations that invoke static methods or plug-in single-row functions.
 */
public class EPLMethodInvocationContext {
    /**
     * Type information
     */
    public final static EPTypeClass EPTYPE = new EPTypeClass(EPLMethodInvocationContext.class);

    private final String statementName;
    private final int contextPartitionId;
    private final String runtimeURI;
    private final String functionName;
    private final Object statementUserObject;
    private final EventBeanService eventBeanService;

    /**
     * Ctor.
     *
     * @param statementName       the statement name
     * @param contextPartitionId  context partition id if using contexts, or -1 if not using context partitions
     * @param runtimeURI          the runtime URI
     * @param functionName        the name of the plug-in single row function, or the method name if not a plug-in single row function
     * @param statementUserObject the statement user object or null if not assigned
     * @param eventBeanService    event and event type services
     */
    public EPLMethodInvocationContext(String statementName, int contextPartitionId, String runtimeURI, String functionName, Object statementUserObject, EventBeanService eventBeanService) {
        this.statementName = statementName;
        this.contextPartitionId = contextPartitionId;
        this.runtimeURI = runtimeURI;
        this.functionName = functionName;
        this.statementUserObject = statementUserObject;
        this.eventBeanService = eventBeanService;
    }

    /**
     * Returns the statement name, or null if the invocation context is not associated to a specific statement and is shareable between statements
     *
     * @return statement name or null
     */
    public String getStatementName() {
        return statementName;
    }

    /**
     * Returns the context partition id, or -1 if no contexts or if the invocation context is not associated to a specific statement and is shareable between statements
     *
     * @return context partition id
     */
    public int getContextPartitionId() {
        return contextPartitionId;
    }

    /**
     * Returns the runtime URI or null if not set
     *
     * @return runtime URI
     */
    public String getRuntimeURI() {
        return runtimeURI;
    }

    /**
     * Returns the function name that appears in the EPL statement.
     *
     * @return function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Returns the statement user object or null if not assigned or if the invocation context is not associated to a specific statement and is shareable between statements
     *
     * @return statement user object or null
     */
    public Object getStatementUserObject() {
        return statementUserObject;
    }

    /**
     * Returns event and event type services.
     *
     * @return eventBeanService
     */
    public EventBeanService getEventBeanService() {
        return eventBeanService;
    }
}
