import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Ejemplo básico de un consumidor de Kafka
 * Demuestra la configuración esencial y la lectura de mensajes
 */
public class KafkaConsumerBasics {
    
    public static void main(String[] args) {
        // Configuración del consumidor
        Properties props = new Properties();
        
        // Servidor de Kafka (bootstrap servers)
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        // ID del grupo de consumidores (esencial para coordinación)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-1");
        
        // Deserializadores para clave y valor
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Configuración de auto-commit de offsets
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        
        // Desde dónde leer si no hay offset previo
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Crear el consumidor
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        
        try {
            // Suscribirse al topic
            String topic = "test-topic";
            consumer.subscribe(Collections.singletonList(topic));
            
            System.out.println("Consumidor iniciado. Esperando mensajes...");
            System.out.println("Presiona Ctrl+C para detener.\n");
            
            // Contador de mensajes procesados
            int messageCount = 0;
            
            // Bucle principal de consumo
            while (true) {
                // Poll para obtener mensajes (timeout de 1 segundo)
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                
                // Procesar cada mensaje recibido
                for (ConsumerRecord<String, String> record : records) {
                    messageCount++;
                    System.out.printf("Mensaje recibido #%d%n", messageCount);
                    System.out.printf("  Topic: %s%n", record.topic());
                    System.out.printf("  Partición: %d%n", record.partition());
                    System.out.printf("  Offset: %d%n", record.offset());
                    System.out.printf("  Clave: %s%n", record.key());
                    System.out.printf("  Valor: %s%n", record.value());
                    System.out.printf("  Timestamp: %d%n%n", record.timestamp());
                }
                
                // Si no hay mensajes, mostrar punto de actividad
                if (records.isEmpty()) {
                    System.out.print(".");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error en el consumidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar el consumidor
            consumer.close();
            System.out.println("Consumidor cerrado.");
        }
    }
}
