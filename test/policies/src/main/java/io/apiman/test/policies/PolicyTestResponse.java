/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.test.policies;

import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * The data returned by a call to the "send" method of {@link ApimanPolicyTest}.  This
 * represents a successful call to a back end API.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTestResponse {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ApiResponse response;
    private final String body;

    /**
     * Constructor.
     * @param response
     */
    public PolicyTestResponse(ApiResponse response) {
        this.response = response;
        this.body = null;
    }

    /**
     * Constructor.
     * @param response
     * @param body
     */
    public PolicyTestResponse(ApiResponse response, String body) {
        this.response = response;
        this.body = body;
    }

    public int code() {
        return response.getCode();
    }

    public String header(String name) {
        return this.response.getHeaders().get(name);
    }

    public HeaderMap headers() {
        return response.getHeaders();
    }

    /**
     * @return the body
     */
    public String body() {
        return body;
    }

    /**
     * @param entityClass
     * @return the body as an entity (JSON marshalling)
     */
    public <T> T entity(Class<T> entityClass) {
        try {
            return mapper.reader(entityClass).readValue(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
