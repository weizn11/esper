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
package com.espertech.esper.common.internal.context.controller.keyed;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.compile.stage2.StatementSpecCompiled;
import com.espertech.esper.common.internal.compile.stage3.StatementCompileTimeServices;
import com.espertech.esper.common.internal.context.controller.core.ContextControllerForgeUtil;
import com.espertech.esper.common.internal.context.controller.core.ContextControllerPortableInfo;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;

import java.util.function.Consumer;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.newArrayWithInit;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.newInstance;

public class ContextControllerKeyedValidation implements ContextControllerPortableInfo {
    public final static EPTypeClass EPTYPE = new EPTypeClass(ContextControllerKeyedValidation.class);

    private final ContextControllerKeyedValidationItem[] items;

    public ContextControllerKeyedValidation(ContextControllerKeyedValidationItem[] items) {
        this.items = items;
    }

    public ContextControllerKeyedValidationItem[] getItems() {
        return items;
    }

    public CodegenExpression make(CodegenExpressionRef addInitSvc) {
        CodegenExpression[] init = new CodegenExpression[items.length];
        for (int i = 0; i < init.length; i++) {
            init[i] = items[i].make(addInitSvc);
        }
        return newInstance(ContextControllerKeyedValidation.EPTYPE, newArrayWithInit(ContextControllerKeyedValidationItem.EPTYPE, init));
    }

    public void validateStatement(String contextName, StatementSpecCompiled spec, StatementCompileTimeServices compileTimeServices) throws ExprValidationException {
        ContextControllerForgeUtil.validateStatementKeyAndHash(items, contextName, spec, compileTimeServices);
    }

    public void visitFilterAddendumEventTypes(Consumer<EventType> consumer) {
        for (ContextControllerKeyedValidationItem item : items) {
            item.visitFilterAddendumEventTypes(consumer);
        }
    }
}
