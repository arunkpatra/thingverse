package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(value = "GetThingResponse", description = "Thing details.")
public class GetThingResponse {

    @ApiModelProperty(value = "The Thing ID")
    private final String thingID;

    @ApiModelProperty(value = "The current attribute map")
    private final Map<String, Object> attributes;

    @JsonCreator
    public GetThingResponse(String thingID, Map<String, Object> attributes) {
        this.thingID = thingID;
        this.attributes = attributes;
    }

    public String getThingID() {
        return thingID;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
