package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.UUID;

/**
 * This is what gets called *after* the proxy. (PolicyVerticle)
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class IngestorToPolicyImpl implements IngestorToPolicyService {

    private String uuid = UUID.randomUUID().toString();
    //private Vertx vertx;

    private Handler<VertxServiceRequest> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;
    private Handler<AsyncResult<Boolean>> readyHandler;
    private Handler<AsyncResult<Void>> resultHandler;

    public IngestorToPolicyImpl(Vertx vertx) {
        //this.vertx = vertx;
        System.out.println("Creating IngestorToPolicyImpl");
    }

    @Override
    public void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Boolean>> readyHandler) {
        System.out.println("Received head");
        this.readyHandler = readyHandler;

//        try {
            headHandler.handle(serviceRequest);
//            //readyHandler.handle(Future.succeededFuture());
//        } catch (Exception e) {
//            readyHandler.handle(Future.failedFuture(e));
//        }
    }

    @Override
    public void write(String chunk) {
        System.out.println("Received chunk " + chunk + " // on UUID " + uuid);

        if (bodyHandler != null)
            bodyHandler.handle(new VertxApimanBuffer(chunk)); //TODO this should be fixed when custom marshallers allowed
    }

    @Override
    public void end(Handler<AsyncResult<Void>> resultHandler) {
        System.out.println("OK, finished IngestorToPolicyImpl");

        this.resultHandler = resultHandler;

        if (endHandler != null)
            endHandler.handle((Void) null);
    }

    public void headHandler(Handler<VertxServiceRequest> handler) {
        this.headHandler = handler;
    }

    public void bodyHandler(Handler<VertxApimanBuffer> handler) {
        this.bodyHandler = handler;
    }

    public void endHandler(Handler<Void> handler) {
        this.endHandler = handler;
    }

    public void ready() {
        readyHandler.handle(Future.succeededFuture(true));
    }

    public void failHead() {
        readyHandler.handle(Future.succeededFuture(false));
    }

    public void fail(Throwable error) {
        resultHandler.handle(Future.failedFuture(error));
    }

    public void succeeded() {
        resultHandler.handle(Future.succeededFuture());
    }
}
