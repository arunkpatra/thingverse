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

package com.thingverse.backend.models;

import com.thingverse.api.serialization.CborSerializable;

public class ThingverseActorMetrics implements CborSerializable {

    private Long totalActiveThings;
    private Long totalMessagesReceived;
    private Long averageMessageAge;

    public ThingverseActorMetrics(Long totalActiveThings, Long totalMessagesReceived, Long averageMessageAge) {
        this.totalActiveThings = totalActiveThings;
        this.totalMessagesReceived = totalMessagesReceived;
        this.averageMessageAge = averageMessageAge;
    }

    public Long getTotalActiveThings() {
        return totalActiveThings;
    }

    public Long getTotalMessagesReceived() {
        return totalMessagesReceived;
    }

    public Long getAverageMessageAge() {
        return averageMessageAge;
    }

    @Override
    public String toString() {
        return "ThingverseActorMetrics{" +
                "totalActiveThings=" + totalActiveThings +
                ", totalMessagesReceived=" + totalMessagesReceived +
                ", averageMessageAge=" + averageMessageAge +
                '}';
    }
}
