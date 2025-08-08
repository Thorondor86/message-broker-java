package com.example.messagebroker;

import com.example.proto.PersonProto;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConsumerMain {

    private static final String DEFAULT_QUEUE = "person_queue";

    public static void main(String[] args) throws Exception {
        String queue = DEFAULT_QUEUE;
        String host = "localhost";
        boolean once = false; // if true, consume only one message and exit

        // Parse CLI arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--queue":
                    queue = args[++i];
                    break;
                case "--host":
                    host = args[++i];
                    break;
                case "--once":
                    once = true;
                    break;
                default:
                    break;
            }
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Ensure the queue exists
            channel.queueDeclare(queue, true, false, false, null);

            if (once) {
                // ---------- Single-message mode ----------
                // Consume exactly one message and then exit
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    try {
                        PersonProto.Person p = PersonProto.Person.parseFrom(delivery.getBody());
                        System.out.println("Received Person: " + p);
                    } catch (Exception ex) {
                        System.err.println("Failed to parse message: " + ex.getMessage());
                    } finally {
                        try {
                            // Cancel the consumer after first message
                            channel.basicCancel(consumerTag);
                        } catch (IOException ignored) {}
                    }
                };

                channel.basicConsume(queue, true, deliverCallback, consumerTag -> {});

                // Wait until the consumer is canceled (meaning message received)
                while (channel.consumerCount(queue) > 0) {
                    Thread.sleep(100);
                }

            } else {
                // ---------- Continuous mode ----------
                // Keep consuming messages indefinitely
                System.out.println("Waiting for messages on queue '" + queue + "'...");
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    try {
                        PersonProto.Person p = PersonProto.Person.parseFrom(delivery.getBody());
                        System.out.println("Received Person: " + p);
                    } catch (Exception ex) {
                        System.err.println("Failed to parse message: " + ex.getMessage());
                    }
                };

                channel.basicConsume(queue, true, deliverCallback, consumerTag -> {});
                Thread.currentThread().join(); // keep the main thread alive
            }

        } catch (TimeoutException | IOException e) {
            System.err.println("Failed to consume messages: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
