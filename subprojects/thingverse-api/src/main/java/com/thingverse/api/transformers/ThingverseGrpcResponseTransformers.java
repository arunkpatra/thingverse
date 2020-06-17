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

package com.thingverse.api.transformers;

import com.thingverse.api.models.*;
import com.thingverse.backend.v1.*;
import com.thingverse.common.exception.ThingverseBackendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.thingverse.grpc.ProtoTransformer.getJavaMapFromProto;

public interface ThingverseGrpcResponseTransformers {

    Logger LOGGER = LoggerFactory.getLogger(ThingverseGrpcResponseTransformers.class);

    static CreateThingResponse toCreateThingResponse(CreateThingGrpcResponse in)
            throws ThingverseBackendException {
//        if (null != in) {
//            LOGGER.info("TRACE>> CreateThingGrpcResponse is: {}", in.toString());
//        } else {
//            LOGGER.info("TRACE>> CreateThingGrpcResponse is: null");
//        }
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new CreateThingResponse(Optional.of(in.getThingID()), Optional.of(in.getMessage()));
    }

    static UpdateThingResponse toUpdateThingResponse(UpdateThingGrpcResponse in)
            throws ThingverseBackendException {
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new UpdateThingResponse(in.getMessage());
    }

    static GetAllThingIDsResponse toGetAllThingIDsResponse(List<StreamAllThingIDsGrpcResponse> in) throws ThingverseBackendException {
        GetAllThingIDsResponse response = new GetAllThingIDsResponse(new ArrayList<>());
        //response.setThingIDs(new ArrayList<>());
        for (StreamAllThingIDsGrpcResponse r : in) {
            if (!StringUtils.isEmpty(r.getErrormessage())) throw new ThingverseBackendException(r.getErrormessage());
            response.getThingIDs().add(r.getThingID());
        }
        return response;
    }

    static GetThingResponse toGetThingResponse(GetThingGrpcResponse in)
            throws ThingverseBackendException {
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new GetThingResponse(in.getThingID(), getJavaMapFromProto(in.getAttributesMap()));
    }

    static StopThingResponse toStopThingResponse(StopThingGrpcResponse in)
            throws ThingverseBackendException {
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new StopThingResponse(in.getMessage());
    }

    static ClearThingResponse toClearThingResponse(ClearThingGrpcResponse in) throws ThingverseBackendException {
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new ClearThingResponse(in.getMessage());
    }

    static GetActorMetricsResponse toGetActorMetricsResponse(GetMetricsGrpcResponse in)
            throws ThingverseBackendException {
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new GetActorMetricsResponse(in.getCount(), in.getTotalmessagesreceived(), in.getAveragemessageage());
    }

    static GetBackendClusterStatusResponse toGetBackendClusterStatusResponse(GetBackendClusterStatusGrpcResponse in)
            throws ThingverseBackendException {
        if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
        return new GetBackendClusterStatusResponse(in.getAllmembershealthy(), (int) in.getTotalnodecount(),
                in.getReadnodecount(), in.getWritenodecount());
    }

    static GetBackendClusterStatusResponse toGetBackendClusterStatusResponse2(GetBackendClusterStatusGrpcResponse in,
                                                                              Throwable t)
            throws Throwable {
        if (t != null) {
            if (!StringUtils.isEmpty(in.getErrormessage())) throw new ThingverseBackendException(in.getErrormessage());
            return new GetBackendClusterStatusResponse(in.getAllmembershealthy(), (int) in.getTotalnodecount(),
                    in.getReadnodecount(), in.getWritenodecount());
        } else {
            throw t;
        }
    }
}
