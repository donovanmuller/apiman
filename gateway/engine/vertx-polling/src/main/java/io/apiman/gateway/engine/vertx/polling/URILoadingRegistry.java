/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.polling;

import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertx.polling.exceptions.UnsupportedProtocolException;
import io.apiman.gateway.engine.vertx.polling.fetchers.FileResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.HttpResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.ResourceFetcher;
import io.apiman.gateway.platforms.vertx3.common.AsyncInitialize;
import io.apiman.gateway.platforms.vertx3.common.verticles.Json;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class URILoadingRegistry extends InMemoryRegistry implements AsyncInitialize {
    // Protected by DCL, use #getUriLoader
    private static volatile OneShotURILoader instance;
    private URI uri;
    private Vertx vertx;
    private Map<String, String> options;

    public URILoadingRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();
        this.vertx = vertx;
        this.options = options;
        Arguments.require(options.containsKey("configUri"), "configUri is required in configuration");
        uri = URI.create(options.get("configUri"));
    }

    @Override
    public void initialize(IAsyncResultHandler<Void> resultHandler) {
        getURILoader(vertx, uri, options).subscribe(this, resultHandler::handle);
    }

    private OneShotURILoader getURILoader(Vertx vertx, URI uri, Map<String, String> options) {
        if (instance == null) {
            synchronized(URILoadingRegistry.class) {
                if (instance == null) {
                    instance = new OneShotURILoader(vertx, uri, options);
                }
            }
        }
        return instance;
    }

    public static void reloadData(IAsyncHandler<Void> doneHandler) {
        instance.reload(doneHandler);
    }

    public static void reset() {
        synchronized(URILoadingRegistry.class) {
            instance = null;
        }
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    protected void publishApiInternal(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, handler);
    }

    protected void registerClientInternal(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, handler);
    }

    private static final class OneShotURILoader {

        private Vertx vertx;
        private URI uri;
        private Map<String, String> config;
        private List<IAsyncResultHandler<Void>> failureHandlers = new ArrayList<>();
        private Deque<URILoadingRegistry> awaiting = new ArrayDeque<>();
        private List<URILoadingRegistry> allRegistries = new ArrayList<>(); // TODO for testing, perhaps can get rid of?
        private Buffer rawData;
        private boolean dataProcessed = false;
        private List<Client> clients = Collections.emptyList();
        private List<Api> apis = Collections.emptyList();
        private Logger log = LoggerFactory.getLogger(OneShotURILoader.class);
        private IAsyncHandler<Void> reloadHandler;

        public OneShotURILoader(Vertx vertx, URI uri, Map<String, String> config) {
            this.config = config;
            this.vertx = vertx;
            this.uri = uri;
            fetchResource();
        }

        // Clear all registries and add back to processing queue.
        public synchronized void reload(IAsyncHandler<Void> reloadHandler) {
            this.reloadHandler = reloadHandler;
            awaiting.addAll(allRegistries);
            apis.clear();
            clients.clear();
            failureHandlers.clear(); // Initial design assumption was that this would never be reloaded - so this is the future for boot-time and thus has already been called.
            allRegistries.stream()
                .map(URILoadingRegistry::getMap)
                .forEach(Map::clear);
            dataProcessed = false;
            // Load again from scratch.
            fetchResource();
        }

        private void fetchResource() {
            getResourceFetcher()
                .exceptionHandler(this::failAll)
                .fetch(data -> {
                    rawData = data;
                    processData();
                });
        }

        // TODO perhaps use enum + factory instead if we add more protocols.
        private ResourceFetcher getResourceFetcher() {
            String scheme = uri.getScheme() == null ? "file" : uri.getScheme().toLowerCase();
            switch (scheme) {
            case "http":
                return new HttpResourceFetcher(vertx, uri, config, false);
            case "https":
                return new HttpResourceFetcher(vertx, uri, config, true);
            case "file":
                return new FileResourceFetcher(vertx, uri, config);
            default:
                throw new UnsupportedProtocolException(String.format("%s is not supported. Available: http, https and file.", uri.getScheme()));
            }
        }

        private void processData() {
            if (rawData.length() == 0) {
                log.warn("File loaded into registry was empty. No entities created.");
                return;
            }
            try {
                JsonObject json = new JsonObject(rawData.toString("UTF-8").trim());
                log.trace("Processing JSON: {0}", json);
                clients = requireJsonArray("clients", json, Client.class);
                apis = requireJsonArray("apis", json, Api.class);
                dataProcessed = true;
                checkQueue();
            } catch (DecodeException e) {
                failAll(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T, K> List<T> requireJsonArray(String keyName, JsonObject json, Class<K> klazz) {
            // Contains key.
            Arguments.require(json.containsKey(keyName),
                    String.format("Must provide array of %s objects for key '%s'", StringUtils.capitalize(keyName), keyName));
            // Is of type array.
            Arguments.require(json.getValue(keyName) instanceof JsonArray,
                    String.format("'%s' must be a Json array", keyName));
            // Transform into List<T>.
            return Json.decodeValue(json.getJsonArray(keyName).encode(), List.class, klazz);
        }

        public synchronized void subscribe(URILoadingRegistry registry, IAsyncResultHandler<Void> failureHandler) {
            Objects.requireNonNull(registry, "registry must be non-null.");
            Objects.requireNonNull(failureHandler, "failure handler must be non-null.");
            failureHandlers.add(failureHandler);
            allRegistries.add(registry);
            awaiting.add(registry);
            vertx.runOnContext(action -> checkQueue());
        }

        private void checkQueue() {
            if (dataProcessed && awaiting.size()>0) {
                loadDataIntoRegistries();
            }
        }

        private void loadDataIntoRegistries() {
            URILoadingRegistry reg = null;
            while ((reg = awaiting.poll()) != null) {
                log.debug("Loading data into registry {0}:", reg);
                for (Api api : apis) {
                    reg.publishApiInternal(api, handleAnyFailure());
                    log.debug("Publishing: {0} ", api);
                }
                for (Client client : clients) {
                    reg.registerClientInternal(client, handleAnyFailure());
                    log.debug("Registering: {0} ", client);
                }
            }
            if (reloadHandler != null)
                reloadHandler.handle((Void) null);
        }

        private IAsyncResultHandler<Void> handleAnyFailure() {
            return result -> {
                if (result.isError()) {
                    failAll(result.getError());
                    throw new RuntimeException(result.getError());
                }
            };
        }

        private void failAll(Throwable cause) {
            AsyncResultImpl<Void> failure = AsyncResultImpl.create(cause);
            failureHandlers.stream().forEach(failureHandler -> {
                vertx.runOnContext(run -> failureHandler.handle(failure));
            });
        }
    }

}
