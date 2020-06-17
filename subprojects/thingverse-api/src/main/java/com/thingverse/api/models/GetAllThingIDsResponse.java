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

import java.util.List;

@ApiModel(value = "GetAllThingIDsResponse", description = "Get all thing IDs")
public class GetAllThingIDsResponse {

    @ApiModelProperty(value = "The list of IDs of all known Things")
    private final List<String> thingIDs;

    @JsonCreator
    public GetAllThingIDsResponse(List<String> thingIDs) {
        this.thingIDs = thingIDs;
    }

    public List<String> getThingIDs() {
        return thingIDs;
    }
}
