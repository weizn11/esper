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
package com.espertech.esper.common.internal.context.aifactory.createwindow;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPType;

/**
 * Selected properties for a create-window expression in the model-after syntax.
 */
public class NamedWindowSelectedProps {
    private EPType selectExpressionType;
    private String assignedName;
    private EventType fragmentType;

    /**
     * Ctor.
     *
     * @param selectExpressionType expression result type
     * @param assignedName         name of column
     * @param fragmentType         null if not a fragment, or event type of fragment if one was selected
     */
    public NamedWindowSelectedProps(EPType selectExpressionType, String assignedName, EventType fragmentType) {
        this.selectExpressionType = selectExpressionType;
        this.assignedName = assignedName;
        this.fragmentType = fragmentType;
    }

    /**
     * Returns the type of the expression result.
     *
     * @return type
     */
    public EPType getSelectExpressionType() {
        return selectExpressionType;
    }

    /**
     * Returns the assigned column name.
     *
     * @return name
     */
    public String getAssignedName() {
        return assignedName;
    }

    /**
     * Returns the fragment type or null if not a fragment type.
     *
     * @return type
     */
    public EventType getFragmentType() {
        return fragmentType;
    }
}
