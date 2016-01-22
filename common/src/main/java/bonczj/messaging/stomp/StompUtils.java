package bonczj.messaging.stomp;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.stomp.StompClientOptions;

public class StompUtils
{
    public static final String RESULTS_QUEUE = "/queue/result.message.handle";
    public static final String WORKER_QUEUE  = "/queue/message.handle";

    private StompUtils()
    {
    }

    public static StompClientOptions stompClientOptions(JsonObject config)
    {
        return new StompClientOptions().
                setLogin(config.getString("stomp.user", null)).
                setPasscode(config.getString("stomp.pass", null)).
                setPort(config.getInteger("stomp.port", 61613)).
                setHost(config.getString("stomp.host", "127.0.0.1")).
                setBypassHostHeader(config.getBoolean("stomp.bypassHostHeader", true)).
                setUseStompFrame(config.getBoolean("stomp.useStompFrame", false));
    }

}
