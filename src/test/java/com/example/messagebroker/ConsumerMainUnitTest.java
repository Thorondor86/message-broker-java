package com.example.messagebroker;

import com.example.proto.PersonProto;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ConsumerMainUnitTest {

    @Test
    void testConsumerReceivesMessage() throws Exception {
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

            // Simulate callback
            doAnswer(invocation -> {
                DeliverCallback callback = invocation.getArgument(1);
                PersonProto.Person person = PersonProto.Person.newBuilder()
                        .setName("Test")
                        .setId(1)
                        .build();
                callback.handle("tag", new com.rabbitmq.client.Delivery(
                        null, null, person.toByteArray()));
                return null;
            }).when(mockChannel).basicConsume(anyString(), eq(true), any(DeliverCallback.class), any(com.rabbitmq.client.CancelCallback.class));

            // Run consumer
            Thread t = new Thread(() -> {
                try {
                    ConsumerMain.main(new String[]{"--queue", "person_queue"});
                } catch (Exception ignored2) {}
            });
            t.start();
            Thread.sleep(500);
            t.interrupt();
        }
    }
}
