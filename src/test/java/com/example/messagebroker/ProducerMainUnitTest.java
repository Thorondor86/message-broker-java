package com.example.messagebroker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ProducerMainUnitTest {

    @Test
    void testProducerPublishesMessage() throws Exception {
        ConnectionFactory mockFactory = mock(ConnectionFactory.class);
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);

        when(mockFactory.newConnection()).thenReturn(mockConnection);
        when(mockConnection.createChannel()).thenReturn(mockChannel);

        try (MockedConstruction<ConnectionFactory> ignored =
                     Mockito.mockConstruction(ConnectionFactory.class,
                             (mock, context) -> {
                                 when(mock.newConnection()).thenReturn(mockConnection);
                             })) {

            String jsonPath = new File("src/main/resources/sample.json").getAbsolutePath();
            ProducerMain.main(new String[]{"--file", jsonPath});

            verify(mockChannel, times(1))
                    .basicPublish(eq(""), anyString(), isNull(), any());
        }
    }
}