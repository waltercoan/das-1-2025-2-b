package br.univille;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.util.List;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Consumer {
    List<Event> getEvents();
}

class ConsumerImpl implements Consumer {
    private final KafkaConsumer<String, byte[]> consumer;
    private final String topic;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;
    private List<Event> events = Collections.synchronizedList(new ArrayList<>()); // Usar lista sincronizada se acessada por múltiplos threads

    public ConsumerImpl(String server, String password) {
        topic = "pixel-events";

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"$ConnectionString\" " +
                        "password=\"" + password + "\";");

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Correto, para commit manual

        // Recomendo não usar Integer.MAX_VALUE diretamente para todos, pode ter implicações.
        // Ajuste conforme a capacidade de processamento e memória.
        // Para começar, pode usar valores altos, mas não necessariamente MAX_VALUE.
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Um valor mais razoável para começar
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 50 * 1024 * 1024); // 50MB
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 10 * 1024 * 1024); // 10MB por partição
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Tempo que o broker espera para atingir FETCH_MIN_BYTES

        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutos
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);    // 30 segundos (Padrão, mas pode aumentar se necessário)

        this.consumer = new KafkaConsumer<>(props);
        startConsuming();
    }

    private void startConsuming() {
        if (running.compareAndSet(false, true)) {
            final CountDownLatch shutdownLatch = new CountDownLatch(1);

            consumerThread = new Thread(() -> {
                try {
                    consumer.subscribe(Collections.singletonList(topic));

                    while (running.get()) {
                        // Aumentar o tempo de poll para permitir que mais mensagens sejam buscadas
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1)); // 1 segundo de timeout

                        if (!records.isEmpty()) { // Processar apenas se houver registros
                            System.out.println("Recebido lote de " + records.count() + " registros.");
                            for (ConsumerRecord<String, byte[]> record : records) {
                                try {
                                    processRecord(record);
                                } catch (Exception e) {
                                    // Aqui você precisa decidir o que fazer com a exceção.
                                    // - Logar e continuar? (Pode perder a mensagem se não for tratada)
                                    // - Parar o consumidor?
                                    // - Tentar reprocessar? (Pode levar a um loop se a mensagem for "venenosa")
                                    // - Enviar para uma Dead Letter Queue (DLQ)?
                                    System.err.println("Erro ao processar registro individualmente: " + e.getMessage() +
                                            " para offset " + record.offset() + " na partição " + record.partition());
                                    // Se você não re-lançar a exceção ou parar, o commit abaixo ainda acontecerá
                                    // para este lote, o que pode ser ou não o desejado.
                                }
                            }

                            // Commit síncrono APÓS processar TODOS os registros do lote (records)
                            try {
                                consumer.commitSync();
                                System.out.println("Offsets commitados sincronamente após processar " + records.count() + " registros.");
                            } catch (CommitFailedException e) {
                                // Esta exceção é lançada se o commit falhar e não puder ser tentado novamente
                                // (por exemplo, devido a um rebalanceamento do grupo).
                                // A aplicação normalmente responderia saindo do loop de poll e talvez tentando
                                // se juntar novamente ao grupo.
                                System.err.println("Falha no commit síncrono: " + e.getMessage());
                                // Considere parar ou tratar a falha de commit, pois as mensagens podem ser reprocessadas.
                                // Em alguns casos, pode ser necessário fechar e recriar o consumidor.
                                // running.set(false); // Exemplo: parar o consumidor em caso de falha de commit
                            }
                        }
                    }
                } catch (WakeupException e) {
                    // Ignorar exceção se estamos parando, isso é esperado
                    if (running.get()) { // Se não era uma parada intencional
                        System.err.println("WakeupException recebida, mas o consumidor não estava parando: " + e.getMessage());
                        // running.set(false); // Pode ser necessário parar aqui também
                    }
                } catch (Exception e) {
                    System.err.println("Erro inesperado no loop de consumo: " + e.getMessage());
                    e.printStackTrace();
                    // running.set(false); // Pode ser necessário parar o consumidor
                } finally {
                    System.out.println("Fechando o consumidor Kafka.");
                    consumer.close();
                    shutdownLatch.countDown();
                }
            });
            consumerThread.setName("KafkaConsumerThread-" + topic);
            consumerThread.start();
        }
    }

    private void processRecord(ConsumerRecord<String, byte[]> record) {
        // Não há necessidade de try-catch aqui se já está no chamador,
        // a menos que você queira tratar exceções específicas de Event.fromJson
        String eventAsString = new String(record.value()); // Considere especificar Charset, ex: StandardCharsets.UTF_8

        System.out.print("Processando evento: " + eventAsString);
        System.out.print("  do tópico: " + record.topic());
        System.out.print("  na partição: " + record.partition());
        System.out.print("  no offset: " + record.offset());
        System.out.println("  com chave: " + record.key());

        Event event = Event.fromJson(eventAsString); // Esta linha pode lançar exceção se o JSON for inválido
        events.add(event);
    }


    public void stopConsuming() { // Renomeado para seguir convenção Java
        if (running.compareAndSet(true, false)) {
            System.out.println("Sinalizando consumidor para parar...");
            consumer.wakeup(); // Sinalizar a thread para parar na próxima iteração do poll

            try {
                if (consumerThread != null) {
                    consumerThread.join(5000); // Aguardar pelo término da thread
                    if (consumerThread.isAlive()) {
                        System.err.println("Thread do consumidor não parou após o timeout. Interrompendo...");
                        consumerThread.interrupt(); // Interromper se não parou
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrompido enquanto aguardava a parada do consumidor");
            }
            System.out.println("Consumidor parado.");
        }
    }

    @Override
    public List<Event> getEvents() {
        // Cuidado com a concorrência aqui se getEvents() for chamado de um thread
        // diferente do consumerThread E a lista 'events' não for thread-safe
        // ou se a modificação e leitura não forem sincronizadas.
        // A synchronizedList ajuda, mas a operação clear + return não é atômica.
        List<Event> tempList;
        synchronized (events) { // Sincronizar acesso para evitar ConcurrentModificationException
            if (events.isEmpty()) {
                return Collections.emptyList();
            }
            tempList = new ArrayList<>(events);
            events.clear();
        }
        System.out.println("Retornando " + tempList.size() + " eventos e limpando a lista interna.");
        return tempList;
    }
}