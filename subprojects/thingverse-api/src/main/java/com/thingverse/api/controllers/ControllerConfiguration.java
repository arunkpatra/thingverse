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

package com.thingverse.api.controllers;

import com.thingverse.api.models.*;
import com.thingverse.api.security.config.JwtTokenUtil;
import com.thingverse.api.security.config.JwtUserDetailsService;
import com.thingverse.api.security.model.AuthenticationRequest;
import com.thingverse.api.security.model.AuthenticationResponse;
import com.thingverse.api.security.model.UserInfoResponse;
import com.thingverse.api.services.ThingService;
import com.thingverse.common.env.health.ResourcesHealthyCondition;
import com.thingverse.common.exception.ThingverseBackendException;
import com.thingverse.common.exception.ThingverseException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.TimedSet;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import thingverse.tracing.annotation.Traced;

import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Configuration
@Conditional({ResourcesHealthyCondition.class})
public class ControllerConfiguration {

    @RestController
    @RequestMapping("/auth")
    @CrossOrigin
    @Api(tags = {"Authentication"}, hidden = true)
    public class JwtAuthenticationController {

        private final AuthenticationManager authenticationManager;

        private final JwtTokenUtil jwtTokenUtil;

        private final JwtUserDetailsService userDetailsService;

        public JwtAuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                                           JwtUserDetailsService userDetailsService) {
            this.authenticationManager = authenticationManager;
            this.jwtTokenUtil = jwtTokenUtil;
            this.userDetailsService = userDetailsService;
        }

        @PostMapping("/login")
        public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest data) {
            try {
                authenticate(data.getUsername(), data.getPassword());
            } catch (Exception e) {
                return new ResponseEntity<>(new AuthenticationResponse(data.getUsername(), ""),
                        HttpStatus.UNAUTHORIZED);
            }
            // User authentication was successful, get token
            final String token = jwtTokenUtil.generateToken(userDetailsService.loadUserByUsername(data.getUsername()));
            return new ResponseEntity<>(new AuthenticationResponse(data.getUsername(), token), HttpStatus.OK);
        }

        private void authenticate(String username, String password) throws Exception {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            } catch (DisabledException e) {
                throw new Exception("USER_DISABLED", e);
            } catch (BadCredentialsException e) {
                throw new Exception("INVALID_CREDENTIALS", e);
            }
        }
    }

    @CrossOrigin
    @RestController
    @Api(tags = {"User"})
    public class UserinfoController extends AbstractThingverseRestController {

        @ApiOperation(value = "Get user information",
                notes = "Get information of the currently logged on user", response = GetAllThingIDsResponse.class,
                consumes = "application/json", produces = "application/json", authorizations = {
                @Authorization(value = "Access Token", scopes = {
                        @AuthorizationScope(scope = "global", description = "accessEverything")})})
        @ApiResponses(value = {@ApiResponse(code = 200, message = "User information was extracted.",
                response = UserInfoResponse.class)
        })
        @ResponseBody
        @GetMapping("/me")
        @Secured("ROLE_USER")
        public ResponseEntity<UserInfoResponse> getLoggedOnUerInfo() {
            UsernamePasswordAuthenticationToken authenticationToken
                    = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            return new ResponseEntity<>(new UserInfoResponse(authenticationToken.getName(),
                    authenticationToken.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(toList())), HttpStatus.OK);
        }
    }

    @CrossOrigin
    @RestController
    @Api(tags = {"Backend Cluster"})
    @TimedSet({@Timed(value = "thingverse.http_server_requests", description = "Thingverse HTTP requests related metrics.")})
    public class BackendClusterControllerImpl extends AbstractThingverseRestController implements BackendClusterController {

        private final Logger LOGGER = LoggerFactory.getLogger(BackendClusterControllerImpl.class);
        private final ThingService thingService;

        public BackendClusterControllerImpl(ThingService thingService) {
            this.thingService = thingService;
        }

        @ApiOperation(value = "Get Backend cluster status",
                notes = "Get Backend cluster status", response = GetBackendClusterStatusResponse.class,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/cluster/state", method = GET)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Cluster state was retrieved",
                        response = GetBackendClusterStatusResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = GetBackendClusterStatusResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = GetBackendClusterStatusResponse.class)
        })
        @Traced(operationName = "/api/cluster/state")
        public ResponseEntity<GetBackendClusterStatusResponse> clusterState(
                HttpServletRequest servletRequest, @RequestHeader HttpHeaders headers) throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.getBackendClusterStatusResponse(), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }
    }

    @CrossOrigin
    @RestController
    @Api(tags = {"Thing"})
    @TimedSet({@Timed(value = "thingverse.http_server_requests", description = "Thingverse HTTP requests related metrics.")})
    public class ThingControllerImpl extends AbstractThingverseRestController implements ThingController {

        private final Logger LOGGER = LoggerFactory.getLogger(ThingControllerImpl.class);
        private final ThingService thingService;

        public ThingControllerImpl(ThingService thingService) {
            this.thingService = thingService;
        }

        @ApiOperation(value = "Get Thing IDs",
                notes = "Get IDs of known things", response = GetAllThingIDsResponse.class,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing/ids", method = GET)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Thing Ids were retrieved",
                        response = GetAllThingIDsResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = GetAllThingIDsResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = GetAllThingIDsResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing/ids")
        public ResponseEntity<GetAllThingIDsResponse> getAllThingIDs(
                @RequestParam(value = "maxIDsToReturn", defaultValue = "10", required = false)
                        Long maxIDsToReturn,
                HttpServletRequest servletRequest,
                @RequestHeader HttpHeaders headers) throws ThingverseException {
            LOGGER.debug("Received Get All Thing IDs request.");
            try {
                return new ResponseEntity<>(thingService.getAllThingIDs(maxIDsToReturn), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }

        @ApiOperation(value = "Get Thing",
                notes = "Get details of a Thing", response = GetThingResponse.class,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing/{thingID}", method = GET)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "The Thing was retrieved",
                        response = GetThingResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = GetThingResponse.class),
                @ApiResponse(code = 404, message = "Bad requested. The thing was not found.",
                        response = GetThingResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = GetThingResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing/{thingID}")
        public ResponseEntity<GetThingResponse> getThing(
                @ApiParam(name = "thingID", required = true)
                @PathVariable(name = "thingID") String thingID,
                HttpServletRequest servletRequest,
                @RequestHeader HttpHeaders headers) throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.getThing(thingID), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }

        @ApiOperation(value = "Create Thing",
                notes = "Create a Thing", response = CreateThingResponse.class,
                code = 201,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing", method = POST)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 201, message = "The Thing was created",
                        response = CreateThingResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = CreateThingResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = CreateThingResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing")
        public ResponseEntity<CreateThingResponse> createThing(
                @ApiParam(name = "CreateThingRequest", required = true)
                @RequestBody CreateThingRequest request,
                HttpServletRequest servletRequest,
                @RequestHeader HttpHeaders headers) throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.createThing(request.getAttributes()), HttpStatus.CREATED);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }

        @ApiOperation(value = "Update Thing",
                notes = "Update a Thing", response = UpdateThingResponse.class,
                code = 201,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing/{thingID}", method = PUT)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "The Thing was updated",
                        response = UpdateThingResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = UpdateThingResponse.class),
                @ApiResponse(code = 404, message = "Thing not found.",
                        response = UpdateThingResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = UpdateThingResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing/{thingID}")
        public ResponseEntity<UpdateThingResponse> updateThing(
                @ApiParam(name = "thingID", required = true) @PathVariable("thingID") String thingID,
                @ApiParam(name = "UpdateThingRequest", required = true)
                @RequestBody UpdateThingRequest request,
                HttpServletRequest servletRequest,
                @RequestHeader HttpHeaders headers)
                throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.updateThing(thingID, request.getAttributes()), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }

        @ApiOperation(value = "Stop Thing",
                notes = "Stop a Thing", response = StopThingResponse.class,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing/stop/{thingID}", method = PUT)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Thing was stopped",
                        response = StopThingResponse.class),
                @ApiResponse(code = 404, message = "Thing was not found",
                        response = StopThingResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = StopThingResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = StopThingResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing/stop/{thingID}")
        public ResponseEntity<StopThingResponse> stopThing(
                @ApiParam(name = "thingID", required = true)
                @PathVariable("thingID") String thingID,
                HttpServletRequest servletRequest,
                @RequestHeader HttpHeaders headers) throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.stopThing(thingID), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }

        @ApiOperation(value = "Clear Thing",
                notes = "Clear a Thing", response = ClearThingResponse.class,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing/clear/{thingID}", method = PUT)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Thing was cleared",
                        response = ClearThingResponse.class),
                @ApiResponse(code = 404, message = "Thing was not found",
                        response = ClearThingResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = ClearThingResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = ClearThingResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing/clear/{thingID}")
        public ResponseEntity<ClearThingResponse> clearThing(
                @ApiParam(name = "thingID", required = true)
                @PathVariable("thingID") String thingID,
                HttpServletRequest servletRequest,
                @RequestHeader HttpHeaders headers) throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.clearThing(thingID), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }

        @ApiOperation(value = "Get Metrics",
                notes = "Get Actor Metrics ", response = GetActorMetricsResponse.class,
                consumes = "application/json",
                produces = "application/json", authorizations = {@Authorization(value = "Access Token")})
        @RequestMapping(value = "/thing/metrics", method = GET)
        @ResponseBody
        @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Metrics were retrieved",
                        response = GetActorMetricsResponse.class),
                @ApiResponse(code = 400, message = "Bad requested. The server rejected your request.",
                        response = GetActorMetricsResponse.class),
                @ApiResponse(code = 500, message = "An internal error occurred.",
                        response = GetActorMetricsResponse.class)
        })
        @Override
        @Traced(operationName = "/api/thing/metrics")
        public ResponseEntity<GetActorMetricsResponse> getActorMetrics(HttpServletRequest servletRequest,
                                                                       @RequestHeader HttpHeaders headers) throws ThingverseException {
            try {
                return new ResponseEntity<>(thingService.getActorMetricsResponse(), HttpStatus.OK);
            } catch (Throwable t) {
                throw new ThingverseBackendException(t);
            }
        }
    }
}
