package bonczj.vertx.message;

import bonczj.vertx.models.Result;
import bonczj.vertx.models.Status;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.logging.Logger;

public class MessageHandlerVerticle extends AbstractVerticle
{
    private static final Logger logger = Logger.getLogger(MessageHandlerVerticle.class.getSimpleName());

    @Override public void start() throws Exception
    {
        super.start();

        getVertx().eventBus().consumer("message.handle", objectMessage -> {
            JsonObject object = (JsonObject) objectMessage.body();
            Result result = Json.decodeValue(object.encode(), Result.class);

            logger.info(String.format("Processing message %s", result.getId().toString()));
            result.setStatus(Status.RUNNING);
            sendResult(result);

            Random random = new Random();
            int seconds = random.nextInt(20) + 1;

            // Thread.sleep is blocking, so allow vertx to handle it cleaner than normal.
            getVertx().executeBlocking(future -> {
                try
                {
                    Thread.sleep(seconds * 1000);
                }
                catch (InterruptedException e)
                {
                    future.fail(e);
                }

                future.complete();
            }, res -> {
                if (res.succeeded())
                {
                    result.setStatus(Status.COMPLETED);
                    result.setResult(String.format("Message complete in %d seconds!", seconds));
                    logger.info(String.format("Message %s complete in %d seconds", result.getId(), seconds));

                    sendResult(result);

                    logger.info(String.format("Response sent to result.message.handle for result %s", result.getId()));

                }
                else
                {
                    result.setStatus(Status.FAILED);
                    result.setResult(String.format("Failed to sleep %d seconds for result %s", seconds, result.getId()));

                    sendResult(result);

                    logger.severe(result.getResult());
                }
            });
        });
    }

    private void sendResult(Result result)
    {
        getVertx().eventBus().send("result.message.handle", new JsonObject(Json.encode(result)));
    }
}
