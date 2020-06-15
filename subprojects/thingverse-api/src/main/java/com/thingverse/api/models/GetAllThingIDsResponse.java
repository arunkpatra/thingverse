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
