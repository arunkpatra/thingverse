package com.thingverse.api.controllers;

import com.thingverse.api.models.*;
import com.thingverse.common.exception.ThingverseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface ThingController {
    ResponseEntity<ClearThingResponse> clearThing(String thingID, HttpServletRequest servletRequest,
            HttpHeaders headers) throws ThingverseException;
    ResponseEntity<CreateThingResponse> createThing(CreateThingRequest request, HttpServletRequest servletRequest,
                                                    HttpHeaders headers) throws ThingverseException;
    ResponseEntity<GetActorMetricsResponse> getActorMetrics(HttpServletRequest servletRequest,
                                                            HttpHeaders headers) throws ThingverseException;
    ResponseEntity<GetAllThingIDsResponse> getAllThingIDs(Long maxIDsToReturn, HttpServletRequest servletRequest,
                                                          HttpHeaders headers) throws ThingverseException;
    ResponseEntity<GetThingResponse> getThing(String thingID, HttpServletRequest servletRequest,
                                              HttpHeaders headers) throws ThingverseException;
    ResponseEntity<StopThingResponse> stopThing(String thingID, HttpServletRequest servletRequest,
                                                HttpHeaders headers) throws ThingverseException;
    ResponseEntity<UpdateThingResponse> updateThing(String thingID, UpdateThingRequest request, HttpServletRequest servletRequest,
                                                    HttpHeaders headers) throws ThingverseException;
}
