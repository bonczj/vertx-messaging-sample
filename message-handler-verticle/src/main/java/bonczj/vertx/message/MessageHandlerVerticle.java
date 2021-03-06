package bonczj.vertx.message;

import bonczj.messaging.stomp.StompUtils;
import bonczj.vertx.models.Result;
import bonczj.vertx.models.Status;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.stomp.StompClient;
import io.vertx.ext.stomp.StompClientConnection;

import java.util.Random;
import java.util.logging.Logger;

public class MessageHandlerVerticle extends AbstractVerticle
{
    private static final Logger logger = Logger.getLogger(MessageHandlerVerticle.class.getSimpleName());

    private StompClient stompClient;

    @Override public void start() throws Exception
    {
        super.start();

        logger.info("Attempting to connect to stomp server");

        this.stompClient = StompClient.create(getVertx(), StompUtils.stompClientOptions(config())).connect(ar -> {
            if (ar.succeeded())
            {
                logger.info("Established connection to stomp server");
                StompClientConnection connection = ar.result();

                connection.errorHandler(frame -> logger.severe(String.format("Error receiving Stomp frame from RabbitMQ: %s", frame)));
                connection.connectionDroppedHandler(frame -> logger.severe(String.format("Dropped Stomp connection from RabbitMQ: %s", frame)));

                connection.subscribe(StompUtils.WORKER_QUEUE, StompUtils.stompHeaders(config()), frame -> {
                    Result result = Json.decodeValue(frame.getBodyAsString(), Result.class);

                    logger.info(String.format("Processing message %s", result.getId().toString()));

                    result.setStatus(Status.RUNNING);
                    sendResult(result, connection);

                    Random random = new Random();
                    int seconds = random.nextInt(20) + 1;

                    getVertx().setTimer(seconds * 1000, handler -> {
                        result.setStatus(Status.COMPLETED);
                        result.setResult(String.format("Message complete in %d seconds!", seconds));
                        logger.info(String.format("Message %s complete in %d seconds", result.getId(), seconds));

                        sendResult(result, connection);

                        if (null != frame.getAck() && !frame.getAck().trim().isEmpty())
                        {
                            if (random.nextBoolean())
                            {
                                logger.info(String.format("Sending ack for result %s", result.getId()));
                                connection.ack(frame.getAck());
                            }
                            else
                            {
                                logger.info(String.format("Sending nack for result %s", result.getId()));
                                connection.nack(frame.getAck());
                            }
                        }

                        logger.info(String.format("Response sent to result.message.handle for result %s", result.getId()));
                    });
                });
            }
            else
            {
                logger.severe(String.format("Failed to connect to stomp server: %s", ar.cause().toString()));
            }
        });
    }

    @Override public void stop() throws Exception
    {
        super.stop();

        if (null != this.stompClient)
        {
            this.stompClient.close();
        }
    }

    private void sendResult(Result result, StompClientConnection connection)
    {
        connection.send(StompUtils.RESULTS_QUEUE, Buffer.buffer(Json.encode(result)));
    }
}
