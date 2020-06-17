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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdateThingResponse", description = "Thing updation response.")
public class UpdateThingResponse {

    @ApiModelProperty(value = "A message from the backend")
    private final String message;

    @JsonCreator
    public UpdateThingResponse(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "message", name = "message", required = false)
    public String getMessage() {
        return message;
    }
}
