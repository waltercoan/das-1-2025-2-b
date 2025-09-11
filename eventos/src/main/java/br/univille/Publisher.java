package br.univille;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class Publisher {
    public static void main(String[] args) {
        var topicName = "topic-das1-b";
        var servidor = "https://sbdas12025a.servicebus.windows.net";
        
        DefaultAzureCredential credential = 
            new DefaultAzureCredentialBuilder().build();
        
    }
}
