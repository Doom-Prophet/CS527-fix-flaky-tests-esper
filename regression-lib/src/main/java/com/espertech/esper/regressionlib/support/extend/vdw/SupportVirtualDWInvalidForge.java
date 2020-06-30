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
package com.espertech.esper.regressionlib.support.extend.vdw;

import com.espertech.esper.common.client.hook.forgeinject.InjectionStrategyClassNewInstance;
import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowFactoryMode;
import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowFactoryModeManaged;
import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowForge;
import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowForgeContext;

import java.util.Set;

public class SupportVirtualDWInvalidForge implements VirtualDataWindowForge {
    public void initialize(VirtualDataWindowForgeContext initializeContext) {

    }

    public VirtualDataWindowFactoryMode getFactoryMode() {
        return new VirtualDataWindowFactoryModeManaged().setInjectionStrategyFactoryFactory(new InjectionStrategyClassNewInstance(SupportVirtualDWInvalidFactoryFactory.EPTYPE));
    }

    public Set<String> getUniqueKeyPropertyNames() {
        return null;
    }
}
