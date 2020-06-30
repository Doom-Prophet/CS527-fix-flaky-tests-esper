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
package com.espertech.esper.common.client;

import com.espertech.esper.common.client.type.EPTypeClass;

/**
 * This exception is thrown to indicate a problem in administration and runtime.
 */
public class EPException extends RuntimeException {
    /**
     * Type information
     */
    public final static EPTypeClass EPTYPE = new EPTypeClass(EPException.class);
    private static final long serialVersionUID = 359488863761406599L;

    /**
     * Ctor.
     *
     * @param message - error message
     */
    public EPException(final String message) {
        super(message);
    }

    /**
     * Ctor for an inner exception and message.
     *
     * @param message - error message
     * @param cause   - inner exception
     */
    public EPException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Ctor - just an inner exception.
     *
     * @param cause - inner exception
     */
    public EPException(final Throwable cause) {
        super(cause);
    }
}
