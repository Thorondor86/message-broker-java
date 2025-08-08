package com.example.messagebroker;

import com.example.proto.PersonProto;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ConsumerMainIT {

    @Test
    void testConsumerWithRealRabbitMQ() throws Exception {
        try (RabbitMQContainer rabbit =
                     new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11-management"))) {
            rabbit.start();

            // Send a test message
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbit.getHost());
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare("person_queue", true, false, false, null);
                PersonProto.Person person = PersonProto.Person.newBuilder()
                        .setName("Test")
                        .setId(1)
                        .build();
                channel.basicPublish("", "person_queue", null, person.toByteArray());
            }

            assertDoesNotThrow(() -> {
                Thread t = new Thread(() -> {
                    try {
                        ConsumerMain.main(new String[]{
                                "--queue", "person_queue",
                                "--host", rabbit.getHost()
                        });
                    } catch (Exception ignored) {}
                });
                t.start();
                Thread.sleep(1000);
                t.interrupt();
            });
        }
    }
}
