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
package com.espertech.esper.common.internal.event.json.parser.forge;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.event.json.parser.core.JsonDelegateEventObjectArray;
import com.espertech.esper.common.internal.event.json.parser.core.JsonDelegateFactory;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class JsonDelegateForgeWithDelegateFactoryArray implements JsonDelegateForge {

    private final String delegateFactoryClassName;
    private final EPTypeClass underlyingType;

    public JsonDelegateForgeWithDelegateFactoryArray(String delegateFactoryClassName, EPTypeClass underlyingType) {
        this.delegateFactoryClassName = delegateFactoryClassName;
        this.underlyingType = underlyingType;
    }

    public CodegenExpression newDelegate(JsonDelegateRefs fields, CodegenMethod parent, CodegenClassScope classScope) {
        CodegenMethod method = parent.makeChild(JsonDelegateEventObjectArray.EPTYPE, JsonForgeFactoryEventTypeTyped.class, classScope);
        method.getBlock()
            .declareVar(JsonDelegateFactory.EPTYPE, "factory", newInstance(delegateFactoryClassName))
            .methodReturn(newInstance(JsonDelegateEventObjectArray.EPTYPE, fields.getBaseHandler(), fields.getThis(), ref("factory"), constant(underlyingType)));
        return localMethod(method);
    }
}
