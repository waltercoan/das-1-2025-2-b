package br.univille;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

public class Subscriber {
    public static void main(String[] args) {
        var topicName = "topic-das1-b";
        var servidor = "sbdas12025a.servicebus.windows.net";
        
        String chave = System.getenv("CHAVE");

        String subscription = "subscription-waltercoan";

        ServiceBusProcessorClient processorClient =
            new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(servidor)
            .connectionString(chave)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscription)
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
