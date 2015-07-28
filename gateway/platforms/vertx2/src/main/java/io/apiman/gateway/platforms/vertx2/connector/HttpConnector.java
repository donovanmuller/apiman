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
package io.apiman.gateway.platforms.vertx2.connector;

import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.vertx2.http.HttpServiceFactory;
import io.apiman.gateway.platforms.vertx2.i18n.Messages;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * A vert.x-based HTTP connector; implementing both {@link ISignalReadStream} and {@link ISignalWriteStream}.
 *
 * Its {@link ISignalWriteStream} elements are valid immediately and its {@link ISignalReadStream} is sent as
 * an event to the provided {@link #resultHandler} when once it has reached a valid state. Hence, it is safe
 * to return instances immediately after the constructor has returned.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
class HttpConnector implements IServiceConnectionResponse, IServiceConnection {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("Content-Length"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServiceRequest serviceRequest;
    private ServiceResponse serviceResponse;

    private IAsyncResultHandler<IServiceConnectionResponse> resultHandler;
    private IAsyncHandler<IApimanBuffer> bodyHandler;
    private IAsyncHandler<Void> endHandler;
    private ExceptionHandler exceptionHandler;

    private boolean inboundFinished = false;
    private boolean outboundFinished = false;

    private String servicePath;
    private String serviceHost;
    private int servicePort;

    private HttpClient client;
    private HttpClientRequest clientRequest;
    private HttpClientResponse clientResponse;

    /**
     * Construct an {@link HttpConnector} instance. The {@link #resultHandler} must remain exclusive to a
     * given instance.
     *
     * @param vertx a vertx
     * @param service a service
     * @param request a request with fields filled
     * @param resultHandler a handler, called when reading is permitted
     */
    public HttpConnector(Vertx vertx, Service service, ServiceRequest request,
            IAsyncResultHandler<IServiceConnectionResponse> resultHandler) {
       this.serviceRequest = request;
       this.resultHandler = resultHandler;
       this.exceptionHandler = new ExceptionHandler();
       this.client = vertx.createHttpClient();

       URL serviceEndpoint = parseServiceEndpoint(service);

       serviceHost = serviceEndpoint.getHost();
       servicePort = serviceEndpoint.getPort();
       servicePath = StringUtils.removeEnd(serviceEndpoint.getPath(), "/"); //$NON-NLS-1$

       doConnection();
    }

    private void doConnection() {

        String destination = servicePath + serviceRequest.getDestination();

        clientRequest = client.request(HttpMethod.valueOf(serviceRequest.getType()),
                servicePort,
                serviceHost,
                destination,
                new Handler<HttpClientResponse>() {

            @Override
            public void handle(final HttpClientResponse vxClientResponse) {
                clientResponse = vxClientResponse;

                logger.debug("We have a response from the backend service in HttpConnector"); //$NON-NLS-1$

                // Pause until we're given permission to xfer the response.
                vxClientResponse.pause();

                serviceResponse = HttpServiceFactory.buildResponse(vxClientResponse, SUPPRESSED_HEADERS);

                vxClientResponse.handler((Handler<Buffer>) chunk -> {
                    bodyHandler.handle(new VertxApimanBuffer(chunk));
                });

                vxClientResponse.endHandler((Handler<Void>) v -> {
                    endHandler.handle((Void) null);
                });

                vxClientResponse.exceptionHandler(exceptionHandler);

                // The response is only ever returned when vxClientResponse is valid.
                resultHandler.handle(AsyncResultImpl
                        .create((IServiceConnectionResponse) HttpConnector.this));
            }
        });

        clientRequest.exceptionHandler(exceptionHandler);
        clientRequest.setChunked(true);
        clientRequest.headers().addAll(serviceRequest.getHeaders());
    }

    @Override
    public ServiceResponse getHead() {
        return serviceResponse;
    }

    @Override
    public void transmit() {
        //logger.debug("Resuming HttpConnector!");
        clientResponse.resume();
    }

    @Override
    public void abort() {
        bodyHandler(null);

        if(clientRequest != null) {
           clientRequest.end();
        }

        if(clientResponse != null) {
            clientResponse.netSocket().close(); //TODO verify
        }
    }

    @Override
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    @Override
    public void endHandler(IAsyncHandler<Void> endHandler) {
        this.endHandler = endHandler;
    }

    @Override
    public void write(IApimanBuffer chunk) {
        if (inboundFinished) {
            throw new IllegalStateException(Messages.getString("HttpConnector.0")); //$NON-NLS-1$
        }

        if (chunk.getNativeBuffer() instanceof Buffer) {
            clientRequest.write((Buffer) chunk.getNativeBuffer());
        } else {
            throw new IllegalArgumentException(Messages.getString("HttpConnector.1")); //$NON-NLS-1$
        }
    }

    @Override
    public void end() {
        //logger.debug("HttpConnector clientRequest.end");
        clientRequest.end();
        inboundFinished = true;
    }

    @Override
    public boolean isFinished() {
        return inboundFinished && outboundFinished;
    }

    /**
     * @see io.apiman.gateway.engine.IServiceConnection#isConnected()
     */
    @Override
    public boolean isConnected() {
        return !isFinished();
    }

    private URL parseServiceEndpoint(Service service) {
        try {
            return new URL(service.getEndpoint());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private class ExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable error) {
            resultHandler.handle(AsyncResultImpl
                    .<IServiceConnectionResponse> create(error));
        }
    }
}
