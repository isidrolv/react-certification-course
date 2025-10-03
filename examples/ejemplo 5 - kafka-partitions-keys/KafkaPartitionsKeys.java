import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Ejemplo de uso de particiones y claves en Kafka
 * Demuestra cómo las claves determinan la partición destino
 */
public class KafkaPartitionsKeys {
    
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        try {
            String topic = "partitioned-topic";
            
            // Enviar mensajes con diferentes claves
            String[] users = {"user-A", "user-B", "user-C", "user-A", "user-B"};
            
            for (int i = 0; i < users.length; i++) {
                String key = users[i];
                String value = "Mensaje " + i + " del " + key;
                
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("Mensaje enviado:%n");
                        System.out.printf("  Clave: %s%n", key);
                        System.out.printf("  Partición: %d%n", metadata.partition());
                        System.out.printf("  Offset: %d%n", metadata.offset());
                        System.out.printf("  Valor: %s%n%n", value);
                    } else {
                        System.err.println("Error: " + exception.getMessage());
                    }
                });
                
                Thread.sleep(100);
            }
            
            // Enviar mensajes sin clave (distribución round-robin)
            System.out.println("\n--- Mensajes sin clave (round-robin) ---\n");
            
            for (int i = 0; i < 5; i++) {
                String value = "Mensaje sin clave " + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, value);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("Mensaje sin clave -> Partición: %d, Offset: %d%n",
                            metadata.partition(), metadata.offset());
                    }
                });
                
                Thread.sleep(100);
            }
            
            System.out.println("\nObserva que mensajes con la misma clave van a la misma partición!");
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            producer.flush();
            producer.close();
            System.out.println("Productor cerrado.");
        }
    }
}
