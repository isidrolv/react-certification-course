import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Ejemplo de Consumer Groups en Kafka
 * Demuestra cómo múltiples consumidores trabajan juntos en un grupo
 */
public class KafkaConsumerGroups {
    
    private static final String GROUP_ID = "consumer-group-demo";
    private static final String CONSUMER_ID;
    
    static {
        // Identificador único para este consumidor
        CONSUMER_ID = "consumer-" + System.currentTimeMillis();
    }
    
    public static void main(String[] args) {
        // Configuración del consumidor
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, CONSUMER_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Deshabilitar auto-commit para control manual
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Configuración de rebalanceo
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        
        try {
            // Suscribirse al topic
            consumer.subscribe(Collections.singletonList("test-topic"));
            
            System.out.println("===================================");
            System.out.println("Consumidor iniciado");
            System.out.println("Consumer ID: " + CONSUMER_ID);
            System.out.println("Group ID: " + GROUP_ID);
            System.out.println("===================================\n");
            
            int messageCount = 0;
            
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                
                for (ConsumerRecord<String, String> record : records) {
                    messageCount++;
                    
                    System.out.printf("[%s] Mensaje #%d procesado:%n", CONSUMER_ID, messageCount);
                    System.out.printf("  Partición: %d | Offset: %d%n", 
                        record.partition(), record.offset());
                    System.out.printf("  Clave: %s | Valor: %s%n%n", 
                        record.key(), record.value());
                    
                    // Simular procesamiento
                    Thread.sleep(100);
                }
                
                // Commit manual de offsets después de procesar
                if (!records.isEmpty()) {
                    consumer.commitSync();
                    System.out.println("Offsets committed.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error en consumidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            consumer.close();
            System.out.println("Consumidor cerrado.");
        }
    }
}
