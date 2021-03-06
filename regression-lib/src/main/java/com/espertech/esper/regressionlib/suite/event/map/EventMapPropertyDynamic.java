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
package com.espertech.esper.regressionlib.suite.event.map;

import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionFlag;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EventMapPropertyDynamic implements RegressionExecution {
    public void run(RegressionEnvironment env) {
        runAssertionMapWithinMap(env);
        runAssertionMapWithinMapExists(env);
        runAssertionMapWithinMap2LevelsInvalid(env);
    }

    public EnumSet<RegressionFlag> flags() {
        return EnumSet.of(RegressionFlag.SERDEREQUIRED);
    }

    private void runAssertionMapWithinMap(RegressionEnvironment env) {
        String statementText = "@name('s0') select " +
            "innermap.int? as t0, " +
            "innermap.innerTwo?.nested as t1, " +
            "innermap.innerTwo?.innerThree.nestedTwo as t2, " +
            "dynamicOne? as t3, " +
            "dynamicTwo? as t4, " +
            "indexed[1]? as t5, " +
            "mapped('keyOne')? as t6, " +
            "innermap.indexedTwo[0]? as t7, " +
            "innermap.mappedTwo('keyTwo')? as t8 " +
            "from MyLevel2#length(5)";
        env.compileDeploy(statementText).addListener("s0");

        HashMap map = new HashMap<String, Object>();
        map.put("dynamicTwo", 20L);
        map.put("innermap", makeMap(
            "int", 10,
            "indexedTwo", new int[]{-10},
            "mappedTwo", makeMap("keyTwo", "def"),
            "innerTwo", makeMap("nested", 30d,
                "innerThree", makeMap("nestedTwo", 99))));
        map.put("indexed", new float[]{-1, -2, -3});
        map.put("mapped", makeMap("keyOne", "abc"));
        env.sendEventMap(map, "MyLevel2");
        assertResults(env, new Object[]{10, 30d, 99, null, 20L, -2.0f, "abc", -10, "def"});

        map = new HashMap<String, Object>();
        map.put("innermap", makeMap(
            "indexedTwo", new int[]{},
            "mappedTwo", makeMap("yyy", "xxx"),
            "innerTwo", null));
        map.put("indexed", new float[]{});
        map.put("mapped", makeMap("xxx", "yyy"));
        env.sendEventMap(map, "MyLevel2");
        assertResults(env, new Object[]{null, null, null, null, null, null, null, null, null});

        env.sendEventMap(new HashMap<String, Object>(), "MyLevel2");
        assertResults(env, new Object[]{null, null, null, null, null, null, null, null, null});

        map = new HashMap<String, Object>();
        map.put("innermap", "xxx");
        map.put("indexed", null);
        map.put("mapped", "xxx");
        env.sendEventMap(map, "MyLevel2");
        assertResults(env, new Object[]{null, null, null, null, null, null, null, null, null});

        env.undeployAll();
    }

    private void runAssertionMapWithinMapExists(RegressionEnvironment env) {
        String statementText = "@name('s0') select " +
            "exists(innermap.int?) as t0, " +
            "exists(innermap.innerTwo?.nested) as t1, " +
            "exists(innermap.innerTwo?.innerThree.nestedTwo) as t2, " +
            "exists(dynamicOne?) as t3, " +
            "exists(dynamicTwo?) as t4, " +
            "exists(indexed[1]?) as t5, " +
            "exists(mapped('keyOne')?) as t6, " +
            "exists(innermap.indexedTwo[0]?) as t7, " +
            "exists(innermap.mappedTwo('keyTwo')?) as t8 " +
            "from MyLevel2#length(5)";
        env.compileDeploy(statementText).addListener("s0");


        HashMap map = new HashMap<String, Object>();
        map.put("dynamicTwo", 20L);
        map.put("innermap", makeMap(
            "int", 10,
            "indexedTwo", new int[]{-10},
            "mappedTwo", makeMap("keyTwo", "def"),
            "innerTwo", makeMap("nested", 30d,
                "innerThree", makeMap("nestedTwo", 99))));
        map.put("indexed", new float[]{-1, -2, -3});
        map.put("mapped", makeMap("keyOne", "abc"));
        env.sendEventMap(map, "MyLevel2");
        assertResults(env, new Object[]{true, true, true, false, true, true, true, true, true});

        map = new HashMap<String, Object>();
        map.put("innermap", makeMap(
            "indexedTwo", new int[]{},
            "mappedTwo", makeMap("yyy", "xxx"),
            "innerTwo", null));
        map.put("indexed", new float[]{});
        map.put("mapped", makeMap("xxx", "yyy"));
        env.sendEventMap(map, "MyLevel2");
        assertResults(env, new Object[]{false, false, false, false, false, false, false, false, false});

        env.sendEventMap(new HashMap<String, Object>(), "MyLevel2");
        assertResults(env, new Object[]{false, false, false, false, false, false, false, false, false});

        map = new HashMap<String, Object>();
        map.put("innermap", "xxx");
        map.put("indexed", null);
        map.put("mapped", "xxx");
        env.sendEventMap(map, "MyLevel2");
        assertResults(env, new Object[]{false, false, false, false, false, false, false, false, false});

        env.undeployAll();
    }

    private void runAssertionMapWithinMap2LevelsInvalid(RegressionEnvironment env) {
        env.tryInvalidCompile("select innermap.int as t0 from MyLevel2#length(5)", "skip");
        env.tryInvalidCompile("select innermap.int.inner2? as t0 from MyLevel2#length(5)", "skip");
        env.tryInvalidCompile("select innermap.int.inner2? as t0 from MyLevel2#length(5)", "skip");
    }

    private void assertResults(RegressionEnvironment env, Object[] result) {
        env.assertEventNew("s0", event -> {
            for (int i = 0; i < result.length; i++) {
                assertEquals("failed for index " + i, result[i], event.get("t" + i));
            }
        });
    }

    private Map makeMap(Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Object[][] pairs = new Object[keysAndValues.length / 2][2];
        for (int i = 0; i < keysAndValues.length; i++) {
            int index = i / 2;
            if (i % 2 == 0) {
                pairs[index][0] = keysAndValues[i];
            } else {
                pairs[index][1] = keysAndValues[i];
            }
        }
        return makeMap(pairs);
    }

    private Map makeMap(Object[][] pairs) {
        Map map = new HashMap();
        for (int i = 0; i < pairs.length; i++) {
            map.put(pairs[i][0], pairs[i][1]);
        }
        return map;
    }
}
