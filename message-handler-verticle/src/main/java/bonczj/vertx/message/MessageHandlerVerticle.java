package bonczj.vertx.message;

import bonczj.vertx.models.Result;
import bonczj.vertx.models.Status;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
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

        EventBus eventBus = getVertx().eventBus();

        eventBus.consumer("message.handle", objectMessage -> {
            JsonObject object = (JsonObject) objectMessage.body();
            Result result = Json.decodeValue(object.encode(), Result.class);
            logger.info(String.format("Processing message %s", result.getId().toString()));

            Random random = new Random();
            int seconds = random.nextInt(9) + 1;

            for (int i = 0; i < seconds; i++)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    // do nothing
                }
            }

            result.setStatus(Status.COMPLETED);
            result.setResult(String.format("Message complete in %f seconds!", seconds / 100.0f));
            logger.info(String.format("Message %s complete in %f seconds", result.getId(), seconds / 100.0f));

            object = new JsonObject(Json.encode(result));
            objectMessage.reply(object);
        });
    }
}
