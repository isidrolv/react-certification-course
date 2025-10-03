import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Ejemplo de productor con callbacks asíncronos
 * Demuestra el manejo de confirmaciones y errores mediante callbacks
 */
public class KafkaProducerCallbacks {
    
    public static void main(String[] args) {
        // Configuración del productor
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        try {
            // Enviar mensajes con callbacks
            for (int i = 0; i < 10; i++) {
                String topic = "test-topic";
                String key = "key-" + i;
                String value = "Mensaje con callback " + i;
                
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                
                // Enviar con callback personalizado
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception == null) {
                            // Éxito: mostrar información del mensaje enviado
                            System.out.printf("✓ Mensaje enviado exitosamente:%n");
                            System.out.printf("  Topic: %s%n", metadata.topic());
                            System.out.printf("  Partición: %d%n", metadata.partition());
                            System.out.printf("  Offset: %d%n", metadata.offset());
                            System.out.printf("  Timestamp: %d%n%n", metadata.timestamp());
                        } else {
                            // Error: mostrar información del error
                            System.err.printf("✗ Error al enviar mensaje:%n");
                            System.err.printf("  Error: %s%n", exception.getMessage());
                            exception.printStackTrace();
                        }
                    }
                });
                
                System.out.println("Mensaje " + i + " enviado (esperando callback)...");
                Thread.sleep(100);
            }
            
            System.out.println("\nTodos los mensajes enviados. Esperando callbacks...");
            
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Asegurar que todos los callbacks se completen
            producer.flush();
            producer.close();
            System.out.println("Productor cerrado.");
        }
    }
}
