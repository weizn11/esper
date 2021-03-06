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
package com.espertech.esper.common.internal.epl.subselect;

import com.espertech.esper.common.internal.compile.stage3.StmtClassForgeableFactory;
import com.espertech.esper.common.internal.epl.expression.subquery.ExprSubselectNode;
import com.espertech.esper.common.internal.fabric.FabricCharge;
import com.espertech.esper.common.internal.schedule.ScheduleHandleTracked;

import java.util.List;
import java.util.Map;

public class SubSelectActivationDesc {

    private final Map<ExprSubselectNode, SubSelectActivationPlan> subselects;
    private final List<StmtClassForgeableFactory> additionalForgeables;
    private final List<ScheduleHandleTracked> schedules;
    private final FabricCharge fabricCharge;

    public SubSelectActivationDesc(Map<ExprSubselectNode, SubSelectActivationPlan> subselects, List<StmtClassForgeableFactory> additionalForgeables, List<ScheduleHandleTracked> schedules, FabricCharge fabricCharge) {
        this.subselects = subselects;
        this.additionalForgeables = additionalForgeables;
        this.schedules = schedules;
        this.fabricCharge = fabricCharge;
    }

    public Map<ExprSubselectNode, SubSelectActivationPlan> getSubselects() {
        return subselects;
    }

    public List<StmtClassForgeableFactory> getAdditionalForgeables() {
        return additionalForgeables;
    }

    public FabricCharge getFabricCharge() {
        return fabricCharge;
    }

    public List<ScheduleHandleTracked> getSchedules() {
        return schedules;
    }
}
