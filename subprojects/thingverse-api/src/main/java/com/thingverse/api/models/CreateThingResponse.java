package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Optional;

@ApiModel(value = "CreateThingResponse", description = "Thing creation response.")
public class CreateThingResponse {

    @ApiModelProperty(value = "ID of the created Thing")
    private final Optional<String> thingID;

    @ApiModelProperty(value = "Any accompanying message from the backend")
    private final Optional<String> message;

    @JsonCreator
    public CreateThingResponse(Optional<String> thingID, Optional<String> message) {
        this.thingID = thingID;
        this.message = message;
    }

    @ApiModelProperty(value = "thingID", name = "thingID", required = false)
    public String getThingID() {
        return thingID.orElseGet(() -> "");
    }

    @ApiModelProperty(value = "message", name = "message", required = false)
    public String getMessage() {
        return message.orElseGet(() -> "");
    }
}
