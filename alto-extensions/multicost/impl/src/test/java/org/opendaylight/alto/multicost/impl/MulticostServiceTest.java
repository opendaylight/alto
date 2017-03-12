/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.multicost.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;
import org.opendaylight.alto.core.northbound.api.exception.AltoBasicException;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorInvalidFieldValue;

import org.opendaylight.alto.multicost.impl.data.Condition;
import org.opendaylight.alto.multicost.impl.data.MulticostRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MulticostServiceTest {

    static final RFC7285CostType c1 = new RFC7285CostType("routincost", "numerical");
    static final RFC7285CostType c2 = new RFC7285CostType("hopcount", "numerical");
    static final List<RFC7285CostType> clist = Arrays.asList(c1, c2);
    static final AltoErrorInvalidFieldValue err = new AltoErrorInvalidFieldValue("test");

    @Test
    public void testCondition() {
        String repr;
        Condition condition;

        repr = "[0] le 100";
        condition = Condition.compile(repr, clist, err);
        assertEquals(condition.type, c1);
        assertEquals(condition.operator, Condition.OP_LE);
        assertEquals(Math.round(condition.bound), 100);

        repr = "eq 100";
        condition = Condition.compile(repr, clist, err);
        assertEquals(condition.type, c1);
        assertEquals(condition.operator, Condition.OP_EQ);
        assertEquals(Math.round(condition.bound), 100);
    }

    void testCheckSemantic(MulticostService service,
                           MulticostRequest request) throws Exception {
        Method method = MulticostService.class
            .getDeclaredMethod("checkSemantic",
                               MulticostRequest.class);

        method.setAccessible(true);
        try {
            method.invoke(service, request);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(expected=AltoBasicException.class)
    public void testMultipleCostTypes() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.costType = c1;
        request.multicostTypes = clist;

        MulticostService service = new MulticostService(clist, true);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testExtraTestable() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.costType = c1;
        request.multicostTypes = clist;

        MulticostService service = new MulticostService(clist, true);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testMissingCostType() throws Exception {
        MulticostRequest request = new MulticostRequest();

        MulticostService service = new MulticostService(clist, true);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testUnsupportedCostType() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.multicostTypes = clist;
        request.orConstraintsRepr = Arrays.asList(Arrays.asList("le 100"),
                                                  Arrays.asList("[1] lt 20"));

        MulticostService service = new MulticostService(Arrays.asList(c1), true);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testExtraConstraints1() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.multicostTypes = clist;
        request.constraints = Arrays.asList("le 100");

        MulticostService service = new MulticostService(clist, false);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testExtraConstraints2() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.multicostTypes = clist;
        request.orConstraintsRepr = Arrays.asList(Arrays.asList("le 100"),
                                                  Arrays.asList("[1] lt 20"));

        MulticostService service = new MulticostService(clist, false);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testMultipleConstraints() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.multicostTypes = clist;
        request.constraints = Arrays.asList("le 100");
        request.orConstraintsRepr = Arrays.asList(Arrays.asList("le 100"),
                                                  Arrays.asList("[1] lt 20"));

        MulticostService service = new MulticostService(clist, true);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testInvalidConstraints() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.multicostTypes = clist;
        request.constraints = Arrays.asList("what 100");

        MulticostService service = new MulticostService(clist, true);

        testCheckSemantic(service, request);
    }

    @Test(expected=AltoBasicException.class)
    public void testUntestableConstraints() throws Exception {
        MulticostRequest request = new MulticostRequest();
        request.multicostTypes = Arrays.asList(c1);
        request.testableTypes = Arrays.asList(c2);
        request.orConstraintsRepr = Arrays.asList(Arrays.asList("le 100"),
                                                  Arrays.asList("[1] lt 20"));

        MulticostService service = new MulticostService(Arrays.asList(c1), true);

        testCheckSemantic(service, request);
    }

    @Test
    public void testMulticostCheck() throws Exception {
         MulticostRequest request = new MulticostRequest();
         request.costType = c1;
         request.constraints = Arrays.asList("[0] le 100", "[1] le 6");

         ObjectMapper mapper = new ObjectMapper();

         String repr = mapper.writeValueAsString(request);
         MulticostService service = new MulticostService(clist, true);

         String rep = service.accept(repr);
    }

    void testConditionFailure(String repr) {
        Condition.compile(repr, clist, err);
    }

    @Test(expected=AltoErrorInvalidFieldValue.class)
    public void test1Parameter() {
        testConditionFailure("[0]");
    }

    @Test(expected=AltoErrorInvalidFieldValue.class)
    public void test4Parameter() {
        testConditionFailure("[0] lt 100 200");
    }

    @Test(expected=AltoErrorInvalidFieldValue.class)
    public void testInvalidIndex() {
        testConditionFailure("[2] ge 200");
    }

    @Test(expected=AltoErrorInvalidFieldValue.class)
    public void testInvalidOp() {
        testConditionFailure("[0] what 100");
    }

    @Test(expected=AltoErrorInvalidFieldValue.class)
    public void testInvalidValue() {
        testConditionFailure("[0] gt abc");
    }
}
