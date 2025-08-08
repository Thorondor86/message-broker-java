package com.example.messagebroker;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ProducerMainIT {

    @Test
    void testProducerWithRealRabbitMQ() {
        try (RabbitMQContainer rabbit =
                     new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11-management"))) {
            rabbit.start();

            String jsonPath = new File("src/main/resources/sample.json").getAbsolutePath();
            assertDoesNotThrow(() -> ProducerMain.main(new String[]{
                    "--file", jsonPath,
                    "--host", rabbit.getHost()
            }));
        }
    }
}
