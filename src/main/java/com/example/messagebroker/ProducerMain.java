package com.example.messagebroker;

import com.example.proto.PersonProto;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

public class ProducerMain {

    private static final String DEFAULT_QUEUE = "person_queue";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar message-broker-cli.jar --file <path> [--queue <queue-name>] [--host <host>]");
            System.exit(1);
        }

        String filePath = null;
        String queue = DEFAULT_QUEUE;
        String host = "localhost";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file": filePath = args[++i]; break;
                case "--queue": queue = args[++i]; break;
                case "--host": host = args[++i]; break;
                default: break;
            }
        }

        if (filePath == null) {
            System.err.println("--file parameter is required");
            System.exit(2);
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(new File(filePath));
            PersonProto.Person.Builder builder = PersonProto.Person.newBuilder();
            if (root.has("name")) builder.setName(root.get("name").asText());
            if (root.has("id")) builder.setId(root.get("id").asInt());
            if (root.has("email")) builder.setEmail(root.get("email").asText());

            PersonProto.Person person = builder.build();

            // publish to RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(queue, true, false, false, null);
                byte[] data = person.toByteArray();
                channel.basicPublish("", queue, null, data);
                System.out.println("Published person to queue '" + queue + "': " + person);
            } catch (TimeoutException | IOException e) {
                System.err.println("Failed to publish message: " + e.getMessage());
                e.printStackTrace();
                System.exit(3);
            }

        } catch (IOException e) {
            System.err.println("Failed to read or parse file: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        }
    }
}
