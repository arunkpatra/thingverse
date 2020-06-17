/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.grpc;

import com.google.protobuf.Value;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.thingverse.grpc.ProtoTransformer.*;


public class ProtoTransformerTest {

    @Test
    public void getObjectFromValueTest() {
        // String
        Object obj = getObjectFromValue(Value.newBuilder().setStringValue("hello").build());
        Assert.assertTrue("Object is not of string type", (obj instanceof String));
        Assert.assertTrue("Invalid value found", "hello".contentEquals((String) obj));

        // Integer
        obj = getObjectFromValue(Value.newBuilder().setNumberValue(42).build());
        Assert.assertTrue("Object is not of number type", (obj instanceof Double));
        Assert.assertEquals("Invalid value found", 42, (Double) obj, 0.0);

        // Float
        obj = getObjectFromValue(Value.newBuilder().setNumberValue(42.1).build());
        Assert.assertTrue("Object is not of number type", (obj instanceof Double));
        Assert.assertEquals("Invalid value found", 42.1, (Double) obj, 0.0);

        // Boolean
        obj = getObjectFromValue(Value.newBuilder().setBoolValue(true).build());
        Assert.assertTrue("Object is not of boolean type", (obj instanceof Boolean));
        Assert.assertEquals("Invalid value found", true, obj);

        // Junk
        obj = getObjectFromValue(Value.newBuilder().setNullValueValue(0).build());
        Assert.assertTrue("Object is not of InvalidDataType type", (obj instanceof InvalidDataType));
    }

    @Test
    public void getValueFromObjectTest() {
        // String check
        Value val = getValueFromObject("hello");
        Assert.assertEquals("Value is not of string type.", val.getKindCase(), Value.KindCase.STRING_VALUE);
        Assert.assertTrue("Invalid value found", "hello".contentEquals(val.getStringValue()));

        // Integer
        val = getValueFromObject(42);
        Assert.assertEquals("Value is not of number type.", val.getKindCase(), Value.KindCase.NUMBER_VALUE);
        Assert.assertEquals("Invalid value found", 42, val.getNumberValue(), 0.0);

        // Float
        val = getValueFromObject(42.1);
        Assert.assertEquals("Value is not of number type.", val.getKindCase(), Value.KindCase.NUMBER_VALUE);
        Assert.assertEquals("Invalid value found", 42.1, val.getNumberValue(), 0.0);

        // Boolean
        val = getValueFromObject(true);
        Assert.assertEquals("Value is not of boolean type.", val.getKindCase(), Value.KindCase.BOOL_VALUE);
        Assert.assertTrue("Invalid value found", val.getBoolValue());

        // Junk check
        val = getValueFromObject(new ArrayList<>());
        Assert.assertEquals("Value is not of null type.", val.getKindCase(), Value.KindCase.NULL_VALUE);
    }

    @Test
    public void getProtoMapFromJavaTest() {
        Map<String, Object> javaMap = new HashMap<>();
        javaMap.put("name", "Alice");
        javaMap.put("age", 42);
        javaMap.put("weight", 45.9);
        javaMap.put("married", true);

        Map<String, Value> protoMap = getProtoMapFromJava(javaMap);

        Assert.assertEquals("Value is not of string type.", protoMap.get("name").getKindCase(), Value.KindCase.STRING_VALUE);
        Assert.assertTrue("Invalid value found", "Alice".contentEquals(protoMap.get("name").getStringValue()));

        Assert.assertEquals("Value is not of number type.", protoMap.get("age").getKindCase(), Value.KindCase.NUMBER_VALUE);
        Assert.assertEquals("Invalid value found", 42, protoMap.get("age").getNumberValue(), 0.0);

        Assert.assertEquals("Value is not of number type.", protoMap.get("weight").getKindCase(), Value.KindCase.NUMBER_VALUE);
        Assert.assertEquals("Invalid value found", 45.9, protoMap.get("weight").getNumberValue(), 0.0);

        Assert.assertEquals("Value is not of boolean type.", protoMap.get("married").getKindCase(), Value.KindCase.BOOL_VALUE);
        Assert.assertTrue("Invalid value found, was expecting true, got false", protoMap.get("married").getBoolValue());

    }

    @Test
    public void getJavaMapFromProtoTest() {
        Map<String, Value> protoMap = new HashMap<>();
        protoMap.put("name", getValueFromObject("Alice"));
        protoMap.put("age", getValueFromObject(42));
        protoMap.put("weight", getValueFromObject(45.9));
        protoMap.put("married", Value.newBuilder().setBoolValue(true).build());

        Map<String, Object> javaMap = getJavaMapFromProto(protoMap);

        Object obj = javaMap.get("name");
        Assert.assertTrue("Object is not of string type", (obj instanceof String));
        Assert.assertTrue("Invalid value found", "Alice".contentEquals((String) obj));

        obj = javaMap.get("age");
        Assert.assertTrue("Object is not of number type", (obj instanceof Double));
        Assert.assertEquals("Invalid value found", 42, ((Double) obj), 0.0);

        obj = javaMap.get("weight");
        Assert.assertTrue("Object is not of number type", (obj instanceof Double));
        Assert.assertEquals("Invalid value found", 45.9, ((Double) obj), 0.0);

        obj = javaMap.get("married");
        Assert.assertTrue("Object is not of boolean type", (obj instanceof Boolean));
        Assert.assertTrue("Invalid value found, was expecting true, got false", (Boolean) obj);

    }
}
