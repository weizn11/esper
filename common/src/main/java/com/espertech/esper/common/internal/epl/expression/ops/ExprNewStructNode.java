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
package com.espertech.esper.common.internal.epl.expression.ops;

import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the "new {...}" operator in an expression tree.
 */
public class ExprNewStructNode extends ExprNodeBase {

    private final String[] columnNames;

    private ExprNewStructNodeForge forge;

    public ExprNewStructNode(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public ExprEvaluator getExprEvaluator() {
        checkValidated(forge);
        return forge.getExprEvaluator();
    }

    public ExprForge getForge() {
        checkValidated(forge);
        return forge;
    }

    public Class getEvaluationType() {
        return Map.class;
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        LinkedHashMap eventType = new LinkedHashMap<String, Object>();

        boolean isAllConstants = false;
        for (int i = 0; i < columnNames.length; i++) {
            isAllConstants = isAllConstants && this.getChildNodes()[i].getForge().getForgeConstantType().isCompileTimeConstant();
            if (eventType.containsKey(columnNames[i])) {
                throw new ExprValidationException("Failed to validate new-keyword property names, property '" + columnNames[i] + "' has already been declared");
            }

            Map<String, Object> eventTypeResult = null;
            if (getChildNodes()[i].getForge() instanceof ExprTypableReturnForge) {
                eventTypeResult = ((ExprTypableReturnForge) getChildNodes()[i].getForge()).getRowProperties();
            }
            if (eventTypeResult != null) {
                eventType.put(columnNames[i], eventTypeResult);
            } else {
                EPType type = getChildNodes()[i].getForge().getEvaluationType();
                EPType typeResult = type == null ? EPTypeNull.INSTANCE : JavaClassHelper.getBoxedType(type);
                eventType.put(columnNames[i], typeResult);
            }
        }
        forge = new ExprNewStructNodeForge(this, isAllConstants, eventType);
        return null;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public boolean isConstantResult() {
        checkValidated(forge);
        return forge.isAllConstants();
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        if (!(node instanceof ExprNewStructNode)) {
            return false;
        }

        ExprNewStructNode other = (ExprNewStructNode) node;
        return Arrays.deepEquals(other.columnNames, columnNames);
    }

    public void toPrecedenceFreeEPL(StringWriter writer, ExprNodeRenderableFlags flags) {
        writer.write("new{");
        String delimiter = "";
        for (int i = 0; i < this.getChildNodes().length; i++) {
            writer.append(delimiter);
            writer.append(columnNames[i]);
            ExprNode expr = this.getChildNodes()[i];

            boolean outputexpr = true;
            if (expr instanceof ExprIdentNode) {
                ExprIdentNode prop = (ExprIdentNode) expr;
                if (prop.getResolvedPropertyName().equals(columnNames[i])) {
                    outputexpr = false;
                }
            }

            if (outputexpr) {
                writer.append("=");
                expr.toEPL(writer, ExprPrecedenceEnum.MINIMUM, flags);
            }
            delimiter = ",";
        }
        writer.write("}");
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.UNARY;
    }
}
