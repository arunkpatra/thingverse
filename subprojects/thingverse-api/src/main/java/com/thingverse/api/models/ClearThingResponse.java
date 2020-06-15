package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ClearThingResponse", description = "Result of thing cleanup")
public class ClearThingResponse {

    @ApiModelProperty(value = "A message from the backend")
    private final String message;

    @JsonCreator
    public ClearThingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
