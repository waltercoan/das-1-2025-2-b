package br.univille;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

public class Publisher {
    public static void main(String[] args) {
        var topicName = "topic-das1-b";
        var servidor = "sbdas12025a.servicebus.windows.net";
        
        

        //DefaultAzureCredential credential = 
        //    new DefaultAzureCredentialBuilder().build();

        String chave = System.getenv("CHAVE");
        
        ServiceBusSenderClient senderClient =
            new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(servidor)
                .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
                //.credential(credential)
                .connectionString(chave)
                .sender()
                .topicName(topicName)
                .buildClient();
        
        senderClient.sendMessage(
            new ServiceBusMessage(
                "walter: eu nao acredito 1234"));
        
    }
}
