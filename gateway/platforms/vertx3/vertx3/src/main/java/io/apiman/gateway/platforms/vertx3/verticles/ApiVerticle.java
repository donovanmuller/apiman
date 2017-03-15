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
package io.apiman.gateway.platforms.vertx3.verticles;

import io.apiman.gateway.platforms.vertx3.api.ApiResourceImpl;
import io.apiman.gateway.platforms.vertx3.api.ClientResourceImpl;
import io.apiman.gateway.platforms.vertx3.api.IRouteBuilder;
import io.apiman.gateway.platforms.vertx3.api.SystemResourceImpl;
import io.apiman.gateway.platforms.vertx3.api.auth.AuthFactory;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;

/**
 * API verticle provides the Gateway API RESTful API. Config is validated and pushed into the registry
 * component; hence, if a distributed component such as ElasticSearch is used, this is shared across all nodes
 * extending {@link ApimanVerticleWithEngine}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ApiVerticle extends ApimanVerticleWithEngine {
    public static final VerticleType VERTICLE_TYPE = VerticleType.API;

    @Override
    public void start(Future<Void> startFuture) {
        super.start(startFuture);
        IRouteBuilder clientResource = new ClientResourceImpl(apimanConfig, engine);
        IRouteBuilder apiResource = new ApiResourceImpl(apimanConfig, engine);
        IRouteBuilder systemResource = new SystemResourceImpl(apimanConfig, engine);

        Router router = Router.router(vertx)
                    .exceptionHandler(log::error);

        AuthHandler handler = AuthFactory.getAuth(vertx, router, apimanConfig, apimanConfig.getAuth());

        router.route("/*").handler(handler);

        clientResource.buildRoutes(router);
        apiResource.buildRoutes(router);
        systemResource.buildRoutes(router);

        HttpServerOptions httpOptions = new HttpServerOptions()
                .setHost(apimanConfig.getHostname());

        if (apimanConfig.isSSL()) {
            httpOptions.setSsl(true)
            .setKeyStoreOptions(
                    new JksOptions()
                        .setPath(apimanConfig.getKeyStore())
                        .setPassword(apimanConfig.getKeyStorePassword())
                    )
            .setTrustStoreOptions(
                    new JksOptions()
                        .setPath(apimanConfig.getTrustStore())
                        .setPassword(apimanConfig.getTrustStorePassword())
                    );
        } else {
            log.warn("API is running in plaintext mode. Enable SSL in config for production deployments.");
        }

        vertx.createHttpServer(httpOptions)
            .requestHandler(router::accept)
            .listen(apimanConfig.getPort(VERTICLE_TYPE));
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }
}
