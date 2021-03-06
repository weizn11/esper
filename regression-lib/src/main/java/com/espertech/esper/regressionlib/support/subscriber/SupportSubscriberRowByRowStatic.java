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
package com.espertech.esper.regressionlib.support.subscriber;

import java.util.ArrayList;
import java.util.List;

public class SupportSubscriberRowByRowStatic {
    private static ArrayList<Object[]> indicate = new ArrayList<Object[]>();

    public static void update(String theString, int intPrimitive) {
        indicate.add(new Object[]{theString, intPrimitive});
    }

    public static List<Object[]> getAndResetIndicate() {
        List<Object[]> result = indicate;
        indicate = new ArrayList<Object[]>();
        return result;
    }
}
