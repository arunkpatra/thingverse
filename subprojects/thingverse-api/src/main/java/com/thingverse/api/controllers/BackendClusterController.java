package com.thingverse.api.controllers;

import com.thingverse.common.exception.ThingverseException;
import com.thingverse.api.models.GetBackendClusterStatusResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface BackendClusterController {

    ResponseEntity<GetBackendClusterStatusResponse> clusterState(HttpServletRequest servletRequest,
                                                                 HttpHeaders headers) throws ThingverseException;
}
