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
package com.espertech.esper.common.internal.event.arr;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.context.module.EPStatementInitServices;
import com.espertech.esper.common.internal.event.core.BaseNestableEventUtil;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactory;
import com.espertech.esper.common.internal.event.core.EventBeanTypedEventFactoryCodegenField;
import com.espertech.esper.common.internal.event.core.EventTypeUtility;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constantNull;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.staticMethod;

/**
 * Getter for map entry.
 */
public class ObjectArrayPropertyGetterDefaultObjectArray extends ObjectArrayPropertyGetterDefaultBase {
    public ObjectArrayPropertyGetterDefaultObjectArray(int propertyIndex, EventType fragmentEventType, EventBeanTypedEventFactory eventBeanTypedEventFactory) {
        super(propertyIndex, fragmentEventType, eventBeanTypedEventFactory);
    }

    protected Object handleCreateFragment(Object value) {
        if (fragmentEventType == null) {
            return null;
        }
        return BaseNestableEventUtil.handleBNCreateFragmentObjectArray(value, fragmentEventType, eventBeanTypedEventFactory);
    }

    protected CodegenExpression handleCreateFragmentCodegen(CodegenExpression value, CodegenClassScope codegenClassScope) {
        if (fragmentEventType == null) {
            return constantNull();
        }

        CodegenExpressionField mSvc = codegenClassScope.addOrGetFieldSharable(EventBeanTypedEventFactoryCodegenField.INSTANCE);
        CodegenExpressionField mType = codegenClassScope.addFieldUnshared(true, EventType.EPTYPE, EventTypeUtility.resolveTypeCodegen(fragmentEventType, EPStatementInitServices.REF));
        return staticMethod(BaseNestableEventUtil.class, "handleBNCreateFragmentObjectArray", value, mType, mSvc);
    }
}
