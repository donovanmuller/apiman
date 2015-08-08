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
package io.apiman.gateway.platforms.servlet;

import io.apiman.common.util.ApimanPathUtils.ServiceRequestPathInfo;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayServletTest {

    /**
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseServiceRequestPath(java.lang.String)}.
     */
    @Test
    public void testParseServiceRequestPath() {
        ServiceRequestPathInfo info = parseServiceRequestPath(null);

        info = parseServiceRequestPath("/invalidpath");
        Assert.assertNull(info.orgId);
        Assert.assertNull(info.serviceId);
        Assert.assertNull(info.serviceVersion);
        Assert.assertNull(info.resource);

        info = parseServiceRequestPath("/invalid/path");
        Assert.assertNull(info.orgId);
        Assert.assertNull(info.serviceId);
        Assert.assertNull(info.serviceVersion);
        Assert.assertNull(info.resource);

        info = parseServiceRequestPath("/Org1/Service1/1.0");
        Assert.assertEquals("Org1", info.orgId);
        Assert.assertEquals("Service1", info.serviceId);
        Assert.assertEquals("1.0", info.serviceVersion);
        Assert.assertNull(info.resource);

        info = parseServiceRequestPath("/MyOrg/Service-99/2.7");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertNull(info.resource);

        info = parseServiceRequestPath("/MyOrg/Service-99/2.7/resource");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/resource", info.resource);

        info = parseServiceRequestPath("/MyOrg/Service-99/2.7/path/to/resource");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/path/to/resource", info.resource);

        info = parseServiceRequestPath("/MyOrg/Service-99/2.7/path/to/resource?query=1234");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);

        info = parseServiceRequestPath("/MyOrg/Service-99/path/to/resource?query=1234", null, "2.7");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);

        info = parseServiceRequestPath("/MyOrg/Service-99/path/to/resource?query=1234", "application/apiman.2.7+json", null);
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);

    }

    /**
     * @param path
     */
    private ServiceRequestPathInfo parseServiceRequestPath(String path) {
        return parseServiceRequestPath(path, null, null);
    }

    /**
     * @param path
     * @param acceptHeader
     * @param apiVersionHeader
     */
    private ServiceRequestPathInfo parseServiceRequestPath(String path, String acceptHeader, String apiVersionHeader) {
        MockHttpServletRequest mockReq = new MockHttpServletRequest(path);
        if (acceptHeader != null) {
            mockReq.setHeader("Accept", acceptHeader);
        }
        if (apiVersionHeader != null) {
            mockReq.setHeader("X-API-Version", apiVersionHeader);
        }
        return GatewayServlet.parseServiceRequestPath(mockReq);
    }

    /**
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseServiceRequestQueryParams(String)}
     */
    @Test
    public void testParseServiceRequestQueryParams() {
        Map<String, String> paramMap = GatewayServlet.parseServiceRequestQueryParams(null);
        Assert.assertNotNull(paramMap);

        paramMap = GatewayServlet.parseServiceRequestQueryParams("param1");
        Assert.assertNull(paramMap.get("param1"));

        paramMap = GatewayServlet.parseServiceRequestQueryParams("param1=value1");
        Assert.assertEquals("value1", paramMap.get("param1"));

        paramMap = GatewayServlet.parseServiceRequestQueryParams("param1=value1&param2");
        Assert.assertEquals("value1", paramMap.get("param1"));
        Assert.assertNull(paramMap.get("param2"));

        paramMap = GatewayServlet.parseServiceRequestQueryParams("param1=value1&param2=value2");
        Assert.assertEquals("value1", paramMap.get("param1"));
        Assert.assertEquals("value2", paramMap.get("param2"));

        paramMap = GatewayServlet.parseServiceRequestQueryParams("param1=value1&param2=value2&param3=value3");
        Assert.assertEquals("value1", paramMap.get("param1"));
        Assert.assertEquals("value2", paramMap.get("param2"));
        Assert.assertEquals("value3", paramMap.get("param3"));

        paramMap = GatewayServlet.parseServiceRequestQueryParams("param1=hello%20world&param2=hello+world&param3=hello world");
        Assert.assertEquals("hello world", paramMap.get("param1"));
        Assert.assertEquals("hello world", paramMap.get("param2"));
        Assert.assertEquals("hello world", paramMap.get("param3"));
    }

}
