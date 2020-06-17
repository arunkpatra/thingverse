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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public interface ProtoTransformer {

    Logger LOGGER = LoggerFactory.getLogger(ProtoTransformer.class);

    static Object getObjectFromValue(Value value) {
        switch (value.getKindCase()) {
            case BOOL_VALUE:
                return value.getBoolValue();
            case STRING_VALUE:
                return value.getStringValue();
            case NUMBER_VALUE:
                return value.getNumberValue();
            default:
                return new InvalidDataType();
        }
    }

    static Value getValueFromObject(Object obj) {
        String type = obj.getClass().getName();

        if ("java.lang.String".contentEquals(type)) {
            return Value.newBuilder().setStringValue((String) obj).build();
        }
        if ("java.lang.Double".contentEquals(type)) {
            return Value.newBuilder().setNumberValue((Double) obj).build();
        }
        if ("java.lang.Boolean".contentEquals(type)) {
            return Value.newBuilder().setBoolValue((Boolean) obj).build();
        }

        if ("java.lang.Integer".contentEquals(type)) {
            return Value.newBuilder().setNumberValue(Double.valueOf((Integer) obj)).build();
        }

        //System.out.println("???" + type);
        LOGGER.error("Asked to convert unhandled type of {}", type);
        return Value.newBuilder().setNullValueValue(0).build();
    }

    static Map<String, Value> getProtoMapFromJava(Map<String, Object> inMap) {
        return inMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> getValueFromObject(e.getValue())));
    }

    static Map<String, Object> getJavaMapFromProto(Map<String, Value> inMap) {
        return inMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> getObjectFromValue(e.getValue())));
    }
}
