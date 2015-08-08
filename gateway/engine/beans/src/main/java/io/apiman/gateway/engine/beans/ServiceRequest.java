/*
 * Copyright 2014 JBoss Inc
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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An inbound request for a managed service.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceRequest implements IServiceObject, Serializable {

    private static final long serialVersionUID = 8024669261165845962L;

    private String apiKey;
    private transient ServiceContract contract;
    private String type;
    private String destination;
    private Map<String, String> queryParams = new LinkedHashMap<>();
    private Map<String, String> headers = new HeaderHashMap();
    private String remoteAddr;
    private Object rawRequest;
    private boolean transportSecurity = false;

    /*
     * Optional fields - set these if you want the APIMan engine to
     * validate that the apikey is valid for the given service coords.
     */
    private String serviceOrgId;
    private String serviceId;
    private String serviceVersion;

    /**
     * Constructor.
     */
    public ServiceRequest() {
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the rawRequest
     */
    public Object getRawRequest() {
        return rawRequest;
    }

    /**
     * @param rawRequest the rawRequest to set
     */
    public void setRawRequest(Object rawRequest) {
        this.rawRequest = rawRequest;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @see io.apiman.gateway.engine.beans.IServiceObject#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @see io.apiman.gateway.engine.beans.IServiceObject#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the remoteAddr
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * @param remoteAddr the remoteAddr to set
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * @return the contract
     */
    public ServiceContract getContract() {
        return contract;
    }

    /**
     * @param contract the contract to set
     */
    public void setContract(ServiceContract contract) {
        this.contract = contract;
    }

    /**
     * @return the serviceOrgId
     */
    public String getServiceOrgId() {
        return serviceOrgId;
    }

    /**
     * @param serviceOrgId the serviceOrgId to set
     */
    public void setServiceOrgId(String serviceOrgId) {
        this.serviceOrgId = serviceOrgId;
    }

    /**
     * @return the serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @return the serviceVersion
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @param serviceVersion the serviceVersion to set
     */
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     * @return the queryParams
     */
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * @param queryParams the queryParams to set
     */
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Indicates whether service request or response was made with transport security.
     *
     * @return true if transport is secure; else false.
     */
    public boolean isTransportSecure() {
        return transportSecurity;
    }

    /**
     * Set whether service request/response was made with transport security.
     *
     * @param isSecure transport security status
     */
    public void setTransportSecure(boolean isSecure) {
        this.transportSecurity = isSecure;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((queryParams == null) ? 0 : queryParams.hashCode());
        result = prime * result + ((rawRequest == null) ? 0 : rawRequest.hashCode());
        result = prime * result + ((remoteAddr == null) ? 0 : remoteAddr.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        result = prime * result + ((serviceOrgId == null) ? 0 : serviceOrgId.hashCode());
        result = prime * result + ((serviceVersion == null) ? 0 : serviceVersion.hashCode());
        result = prime * result + (transportSecurity ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceRequest other = (ServiceRequest) obj;
        if (apiKey == null) {
            if (other.apiKey != null)
                return false;
        } else if (!apiKey.equals(other.apiKey))
            return false;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (headers == null) {
            if (other.headers != null)
                return false;
        } else if (!headers.equals(other.headers))
            return false;
        if (queryParams == null) {
            if (other.queryParams != null)
                return false;
        } else if (!queryParams.equals(other.queryParams))
            return false;
        if (rawRequest == null) {
            if (other.rawRequest != null)
                return false;
        } else if (!rawRequest.equals(other.rawRequest))
            return false;
        if (remoteAddr == null) {
            if (other.remoteAddr != null)
                return false;
        } else if (!remoteAddr.equals(other.remoteAddr))
            return false;
        if (serviceId == null) {
            if (other.serviceId != null)
                return false;
        } else if (!serviceId.equals(other.serviceId))
            return false;
        if (serviceOrgId == null) {
            if (other.serviceOrgId != null)
                return false;
        } else if (!serviceOrgId.equals(other.serviceOrgId))
            return false;
        if (serviceVersion == null) {
            if (other.serviceVersion != null)
                return false;
        } else if (!serviceVersion.equals(other.serviceVersion))
            return false;
        if (transportSecurity != other.transportSecurity)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }


}
