package bonczj.vertx.rest.api;

import bonczj.vertx.models.Result;
import bonczj.vertx.models.Status;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Entry point for the REST APIs.
 */
public class RestApiVerticle extends AbstractVerticle
{
    private static final Logger logger = Logger.getLogger(RestApiVerticle.class.getSimpleName());

    private Map<UUID, Result> resultsCache;

    @Override public void start() throws Exception
    {
        super.start();

        Router router = Router.router(getVertx());

        router.route().handler(BodyHandler.create());
        router.post("/api/:type").handler(this::handleAddType);
        router.get("/api/result/:id").handler(this::handleGetId);

        getVertx().createHttpServer().requestHandler(router::accept).listen(8080);

        getVertx().eventBus().consumer("result.message.handle", objectMessage -> {
            JsonObject object = (JsonObject) objectMessage.body();
            Result result = Json.decodeValue(object.encode(), Result.class);
            logger.info(String.format("Processing result %s with status %s", result.getId(), result.getStatus()));

            if (getResultsCache().containsKey(result.getId()))
            {
                getResultsCache().put(result.getId(), result);
                logger.info(String.format("Stored result %s in cache", result.getId()));
            }
            else
            {
                logger.severe(String.format("Failed to find result %s in cache", result.getId()));
            }
        });
    }

    protected void handleAddType(RoutingContext context)
    {
        HttpServerResponse response = context.response();
        String type = context.request().getParam("type");

        if (null == type)
        {
            sendError(404, "Invalid input", response);
            return;
        }

        Result result = new Result(UUID.randomUUID(), Status.QUEUED, null);

        getResultsCache().put(result.getId(), result);
        JsonObject output = new JsonObject(Json.encode(result));
        response.setStatusCode(200).putHeader("Content-Type", "application/json").end(output.encodePrettily());

        logger.info(String.format("Sending message on event bus for result '%s'", result.getId().toString()));

        EventBus eventBus = getVertx().eventBus();
        eventBus.send("message.handle", output);
    }

    protected void handleGetId(RoutingContext context)
    {
        String value = context.request().getParam("id");
        HttpServerResponse response = context.response();

        if (null == value)
        {
            sendError(404, "Invalid ID", response);
            return;
        }

        UUID id = UUID.fromString(value);
        Result result = getResultsCache().get(id);

        if (null == result)
        {
            sendError(404, "Invalid ID", response);
            return;
        }

        JsonObject jsonResult = new JsonObject(Json.encode(result));
        response.setStatusCode(200).putHeader("Content-Type", "application/json").end(jsonResult.encodePrettily());
    }

    protected Map<UUID, Result> getResultsCache()
    {
        if (null == this.resultsCache)
        {
            this.resultsCache = new TreeMap<>();
        }

        return this.resultsCache;
    }

    private void sendError(int statusCode, String message, HttpServerResponse response)
    {
        response.setStatusCode(statusCode).setStatusMessage(message).end();
    }
}
