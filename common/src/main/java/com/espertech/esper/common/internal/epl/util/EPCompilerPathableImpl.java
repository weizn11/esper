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
package com.espertech.esper.common.internal.epl.util;

import com.espertech.esper.common.client.EPCompilerPathable;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.collection.PathRegistry;
import com.espertech.esper.common.internal.collection.PathRegistryObjectType;
import com.espertech.esper.common.internal.epl.classprovided.core.ClassProvided;
import com.espertech.esper.common.internal.compile.stage1.spec.ExpressionDeclItem;
import com.espertech.esper.common.internal.compile.stage1.spec.ExpressionScriptProvided;
import com.espertech.esper.common.internal.context.compile.ContextMetaData;
import com.espertech.esper.common.internal.epl.namedwindow.path.NamedWindowMetaData;
import com.espertech.esper.common.internal.epl.script.core.NameAndParamNum;
import com.espertech.esper.common.internal.epl.table.compiletime.TableMetaData;
import com.espertech.esper.common.internal.epl.variable.compiletime.VariableMetaData;
import com.espertech.esper.common.internal.epl.variable.core.VariableRepositoryPreconfigured;
import com.espertech.esper.common.internal.event.eventtyperepo.EventTypeRepositoryImpl;

public class EPCompilerPathableImpl implements EPCompilerPathable {
    private final PathRegistry<String, VariableMetaData> variablePathRegistry;
    private final PathRegistry<String, EventType> eventTypePathRegistry;
    private final PathRegistry<String, ExpressionDeclItem> exprDeclaredPathRegistry;
    private final PathRegistry<String, NamedWindowMetaData> namedWindowPathRegistry;
    private final PathRegistry<String, TableMetaData> tablePathRegistry;
    private final PathRegistry<String, ContextMetaData> contextPathRegistry;
    private final PathRegistry<NameAndParamNum, ExpressionScriptProvided> scriptPathRegistry;
    private final PathRegistry<String, ClassProvided> classProvidedPathRegistry;
    private final EventTypeRepositoryImpl eventTypePreconfigured;
    private final VariableRepositoryPreconfigured variablePreconfigured;
    private final String optionalModuleName;

    public EPCompilerPathableImpl(String optionalModuleName) {
        variablePathRegistry = new PathRegistry<>(PathRegistryObjectType.VARIABLE);
        eventTypePathRegistry = new PathRegistry<>(PathRegistryObjectType.EVENTTYPE);
        exprDeclaredPathRegistry = new PathRegistry<>(PathRegistryObjectType.EXPRDECL);
        namedWindowPathRegistry = new PathRegistry<>(PathRegistryObjectType.NAMEDWINDOW);
        tablePathRegistry = new PathRegistry<>(PathRegistryObjectType.TABLE);
        contextPathRegistry = new PathRegistry<>(PathRegistryObjectType.CONTEXT);
        scriptPathRegistry = new PathRegistry<>(PathRegistryObjectType.SCRIPT);
        classProvidedPathRegistry = new PathRegistry<>(PathRegistryObjectType.CLASSPROVIDED);
        eventTypePreconfigured = new EventTypeRepositoryImpl(true);
        variablePreconfigured = new VariableRepositoryPreconfigured();
        this.optionalModuleName = optionalModuleName;
    }

    public EPCompilerPathableImpl(PathRegistry<String, VariableMetaData> variablePathRegistry, PathRegistry<String, EventType> eventTypePathRegistry, PathRegistry<String, ExpressionDeclItem> exprDeclaredPathRegistry, PathRegistry<String, NamedWindowMetaData> namedWindowPathRegistry, PathRegistry<String, TableMetaData> tablePathRegistry, PathRegistry<String, ContextMetaData> contextPathRegistry, PathRegistry<NameAndParamNum, ExpressionScriptProvided> scriptPathRegistry, PathRegistry<String, ClassProvided> classProvidedPathRegistry, EventTypeRepositoryImpl eventTypePreconfigured, VariableRepositoryPreconfigured variablePreconfigured) {
        this.variablePathRegistry = variablePathRegistry;
        this.eventTypePathRegistry = eventTypePathRegistry;
        this.exprDeclaredPathRegistry = exprDeclaredPathRegistry;
        this.namedWindowPathRegistry = namedWindowPathRegistry;
        this.tablePathRegistry = tablePathRegistry;
        this.contextPathRegistry = contextPathRegistry;
        this.scriptPathRegistry = scriptPathRegistry;
        this.classProvidedPathRegistry = classProvidedPathRegistry;
        this.eventTypePreconfigured = eventTypePreconfigured;
        this.variablePreconfigured = variablePreconfigured;
        this.optionalModuleName = null;
    }

    public PathRegistry<String, VariableMetaData> getVariablePathRegistry() {
        return variablePathRegistry;
    }

    public PathRegistry<String, EventType> getEventTypePathRegistry() {
        return eventTypePathRegistry;
    }

    public PathRegistry<String, ExpressionDeclItem> getExprDeclaredPathRegistry() {
        return exprDeclaredPathRegistry;
    }

    public PathRegistry<String, NamedWindowMetaData> getNamedWindowPathRegistry() {
        return namedWindowPathRegistry;
    }

    public PathRegistry<String, TableMetaData> getTablePathRegistry() {
        return tablePathRegistry;
    }

    public PathRegistry<String, ContextMetaData> getContextPathRegistry() {
        return contextPathRegistry;
    }

    public PathRegistry<NameAndParamNum, ExpressionScriptProvided> getScriptPathRegistry() {
        return scriptPathRegistry;
    }

    public PathRegistry<String, ClassProvided> getClassProvidedPathRegistry() {
        return classProvidedPathRegistry;
    }

    public EventTypeRepositoryImpl getEventTypePreconfigured() {
        return eventTypePreconfigured;
    }

    public VariableRepositoryPreconfigured getVariablePreconfigured() {
        return variablePreconfigured;
    }

    public String getOptionalModuleName() {
        return optionalModuleName;
    }
}
