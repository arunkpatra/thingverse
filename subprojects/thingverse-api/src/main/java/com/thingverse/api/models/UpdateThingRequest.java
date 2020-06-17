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

package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ApiModel(value = "UpdateThingRequest", description = "Thing updation request.")
public class UpdateThingRequest {
    @ApiModelProperty(value = "The attribute map to be updated. Existing attributes are not erased.")
    private final Map<String, Object> attributes;

    @JsonCreator(mode = PROPERTIES)
    public UpdateThingRequest(@JsonProperty("attributes") Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @ApiModelProperty(value = "attributes", name = "attributes")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
