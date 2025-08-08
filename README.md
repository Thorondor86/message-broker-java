# Message Broker CLI - Java (RabbitMQ + Protobuf)

This project implements a CLI tool that reads a JSON file, converts the contents into a protobuf `Person` message and sends it to RabbitMQ. A simple consumer is also provided to read and deserialize messages.

## Requirements

- JDK 8+
- Maven
- Docker (optional, to run RabbitMQ quickly)

## How the project is organized

- `src/main/proto/person.proto` - Protobuf definition.
- `src/main/java/com/example/messagebroker/ProducerMain.java` - CLI producer program.
- `src/main/java/com/example/messagebroker/ConsumerMain.java` - Simple consumer to read messages.
- `src/main/resources/sample.json` - Example JSON input.

## Run RabbitMQ with Docker (quick)

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

RabbitMQ management UI will be available at http://localhost:15672 (guest/guest).

## Build

```bash
mvn package
```

This will compile the project and run the protobuf plugin to generate Java classes from the `.proto` file. It will create a shaded (fat) jar under `target/message-broker-cli-1.0-SNAPSHOT.jar`.

## Produce a message (CLI)

```bash
java -jar target/message-broker-cli-1.0-SNAPSHOT.jar --file src/main/resources/sample.json
```

Optional parameters: `--queue <queue-name>` and `--host <rabbit-host>`.

## Consume messages - Continuous mode (default)

```bash
java -cp target/message-broker-cli-1.0-SNAPSHOT.jar com.example.messagebroker.ConsumerMain --queue person_queue
```

## Consume messages - Exit after first message

```bash
java -cp target/message-broker-cli-1.0-SNAPSHOT.jar com.example.messagebroker.ConsumerMain --queue person_queue --once
```

The consumer will print received `Person` messages to stdout.
