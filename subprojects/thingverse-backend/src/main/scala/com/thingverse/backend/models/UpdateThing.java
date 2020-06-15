package com.thingverse.backend.models;

import java.util.Map;

public class UpdateThing {

    public final String thingID;
    public final Map<String, Object> attributes;

    public UpdateThing(String thingID, Map<String, Object> attributes) {
        this.thingID = thingID;
        this.attributes = attributes;
    }

}
