# vertx-messaging-sample

A sample application comprised of two Vert.X verticles. Both verticles are subscribers
and publishers to different queues in order to play with messaging configuration.

Vert.X has an internal event bus that requires the use of a clustered configuration.
Unfortunately, the cluster configuration cannot be protected via TLS as of Vert.X 3.2.
The Vert.X RabbitMQ modules are not maintained an are out of date. The Vert.X team
is maintaining the Stomp module.

By making use of Stomp, one can configure RabbitMQ with the Stomp plugin. This combination
does allow for TLS connections from Vert.X to/from Stomp+RabbitMQ. This combination allows
for an encrypted message bus implementation.

## Building

Just build the project using maven. The two verticles are built as a thick jar that allows
one to run them directly without needing to have Vert.X installed.

  java -jar ./message-handler-verticle/target/message-handler-verticle-0.0.1-SNAPSHOT-fat.jar start

  java -jar ./rest-api-verticle/target/rest-api-verticle-0.0.1-SNAPSHOT-fat.jar start

## rest-api-verticle

This verticle has two very simple REST APIs.

  POST /api/:type
  
  GET /api/result/:id

The :type can be anything you want. There is no body needed for the POST command. The result
of the POST prints a JSON body with random id. That id is used in the second call.

When sending the POST command, a message is sent to a queue named message.handle.

This verticle listens for all messages coming in on the queue result.message.handle. Each message
is expected to contain a JSON body. A cache of requests is updated with the contents sent into this
queue.

## message-handler-verticle

This verticle listens for all messages coming in on the queue message.handle (sent by the REST
verticle). The listener waits for some period of time and then sends an update to the
result.message.handle.