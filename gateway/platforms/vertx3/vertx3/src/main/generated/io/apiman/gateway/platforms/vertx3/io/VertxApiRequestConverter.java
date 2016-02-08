/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.apiman.gateway.platforms.vertx3.io;

import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.beans.util.QueryMap;
import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link io.apiman.gateway.platforms.vertx3.io.VertxApiRequest}.
 *
 * NOTE: This class has been automatically generated from the {@link io.apiman.gateway.platforms.vertx3.io.VertxApiRequest} original class using Vert.x codegen.
 */
public class VertxApiRequestConverter {

  public static void fromJson(JsonObject json, VertxApiRequest obj) {
    if (json.getValue("apiKey") instanceof String) {
      obj.setApiKey((String)json.getValue("apiKey"));
    }
    if (json.getValue("destination") instanceof String) {
      obj.setDestination((String)json.getValue("destination"));
    }
    if (json.getValue("headers") instanceof JsonObject) {
      HeaderMap map = new HeaderMap();
      json.getJsonObject("headers").forEach(entry -> {
        if (entry.getValue() instanceof String)
          map.put(entry.getKey(), (String)entry.getValue());
      });
      obj.setHeaders(map);
    }
    if (json.getValue("queryParams") instanceof JsonObject) {
      QueryMap map = new QueryMap();
      json.getJsonObject("queryParams").forEach(entry -> {
        if (entry.getValue() instanceof String)
          map.put(entry.getKey(), (String)entry.getValue());
      });
      obj.setQueryParams(map);
    }
    if (json.getValue("rawRequest") instanceof Object) {
      obj.setRawRequest(json.getValue("rawRequest"));
    }
    if (json.getValue("remoteAddr") instanceof String) {
      obj.setRemoteAddr((String)json.getValue("remoteAddr"));
    }
    if (json.getValue("serviceId") instanceof String) {
      obj.setApiId((String)json.getValue("serviceId"));
    }
    if (json.getValue("serviceOrgId") instanceof String) {
      obj.setApiOrgId((String)json.getValue("serviceOrgId"));
    }
    if (json.getValue("serviceVersion") instanceof String) {
      obj.setApiVersion((String)json.getValue("serviceVersion"));
    }
    if (json.getValue("transportSecure") instanceof Boolean) {
      obj.setTransportSecure((Boolean)json.getValue("transportSecure"));
    }
    if (json.getValue("type") instanceof String) {
      obj.setType((String)json.getValue("type"));
    }
  }

  public static void toJson(VertxApiRequest obj, JsonObject json) {
    if (obj.getApiKey() != null) {
      json.put("apiKey", obj.getApiKey());
    }
    if (obj.getDestination() != null) {
      json.put("destination", obj.getDestination());
    }
    if (obj.getHeaders() != null) {
      JsonObject map = new JsonObject();
      obj.getHeaders().forEach((pair) -> map.put(pair.getKey(), pair.getValue()));
      json.put("headers", map);
    }
    if (obj.getQueryParams() != null) {
      JsonObject map = new JsonObject();
      obj.getQueryParams().forEach((pair) -> map.put(pair.getKey(), pair.getValue()));
      json.put("queryParams", map);
    }
    if (obj.getRawRequest() != null) {
      json.put("rawRequest", obj.getRawRequest());
    }
    if (obj.getRemoteAddr() != null) {
      json.put("remoteAddr", obj.getRemoteAddr());
    }
    if (obj.getApiId() != null) {
      json.put("serviceId", obj.getApiId());
    }
    if (obj.getApiOrgId() != null) {
      json.put("serviceOrgId", obj.getApiOrgId());
    }
    if (obj.getApiVersion() != null) {
      json.put("serviceVersion", obj.getApiVersion());
    }
    json.put("transportSecure", obj.isTransportSecure());
    if (obj.getType() != null) {
      json.put("type", obj.getType());
    }
  }
}