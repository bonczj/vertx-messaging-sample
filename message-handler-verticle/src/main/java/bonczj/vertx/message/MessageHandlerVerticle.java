package bonczj.vertx.message;

import bonczj.vertx.models.Result;
import bonczj.vertx.models.Status;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Random;

public class MessageHandlerVerticle extends AbstractVerticle
{
    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerVerticle.class);

    @Override public void start() throws Exception
    {
        super.start();

        EventBus eventBus = getVertx().eventBus();

        eventBus.consumer("message.handle", objectMessage -> {
            JsonObject object = (JsonObject) objectMessage.body();
            Result result = Json.decodeValue(object.encode(), Result.class);
            logger.info(String.format("Processing message %s", result.getId().toString()));
            int seconds = 0;

            try
            {
                Random random = new Random();
                seconds = random.nextInt(9) + 1;
                Thread.sleep(seconds * 1000);
            }
            catch (InterruptedException e)
            {
                // do nothing
            }

            result.setStatus(Status.COMPLETED);
            result.setResult(String.format("Message complete in %d seconds!", seconds));
        });
    }
}
