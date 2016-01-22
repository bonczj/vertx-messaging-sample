package bonczj.messaging.stomp;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.stomp.StompClientOptions;

public class StompUtils
{
    public static final String RESULTS_QUEUE = "/amq/queue/result.message.handle";
    public static final String WORKER_QUEUE  = "/amq/queue/message.handle";

    private StompUtils()
    {
    }

    public static StompClientOptions stompClientOptions(JsonObject config)
    {
        return new StompClientOptions().
                setLogin(config.getString("stomp.user")).
                setPasscode(config.getString("stomp.pass")).
                setPort(config.getInteger("stomp.port", 61613)).
                setHost(config.getString("stomp.host", "localhost")).
                setBypassHostHeader(config.getBoolean("stomp.bypassHostHeader", false));
    }

}
