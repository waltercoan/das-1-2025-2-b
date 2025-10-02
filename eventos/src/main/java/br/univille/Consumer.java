package br.univille;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

public class Consumer {
    public static void main(String[] args) {
        var servidor = "sb-das1-2025.servicebus.windows.net";
        var chave = System.getenv("CHAVE");
        var queue = "queue-waltercoan";

         ServiceBusProcessorClient processorClient =
            new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(servidor)
            .connectionString(chave)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .processor()
            .queueName(queue)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(context -> {
                System.out.println(
                    context.getMessage()
                    .getBody().toString());
                context.complete();
            })
            .processError(context -> {
                System.out.println("Deu ruim");
            })
            .buildProcessorClient();
        
        processorClient.start();
    }
}
