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
package io.apiman.gateway.engine.beans;

import io.apiman.gateway.engine.beans.util.HeaderMap;

/**
 * Represents common elements of {@link ApiRequest} and {@link ApiResponse}.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IApiObject {

    /**
     * @return the headers
     */
    HeaderMap getHeaders();

    /**
     * @param headers the headers to set
     */
    void setHeaders(HeaderMap headers);
}
