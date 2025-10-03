import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Ejemplo de transacciones en Kafka
 * Demuestra exactly-once semantics mediante transacciones
 */
public class KafkaTransactions {
    
    public static void main(String[] args) {
        // Configuración del productor transaccional
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Configuración transaccional
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        try {
            // Inicializar transacciones
            producer.initTransactions();
            System.out.println("Productor transaccional inicializado.\n");
            
            // Ejemplo 1: Transacción exitosa
            System.out.println("=== Transacción 1: Exitosa ===");
            try {
                producer.beginTransaction();
                
                // Enviar múltiples mensajes en una transacción
                for (int i = 0; i < 3; i++) {
                    ProducerRecord<String, String> record = 
                        new ProducerRecord<>("transactions-topic", 
                                           "txn-1-key-" + i, 
                                           "Mensaje transaccional " + i);
                    producer.send(record);
                    System.out.println("Enviado: " + record.value());
                }
                
                // Commit de la transacción
                producer.commitTransaction();
                System.out.println("✓ Transacción confirmada exitosamente.\n");
                
            } catch (Exception e) {
                System.err.println("Error en transacción, abortando...");
                producer.abortTransaction();
            }
            
            // Ejemplo 2: Transacción con abort
            System.out.println("=== Transacción 2: Con Abort ===");
            try {
                producer.beginTransaction();
                
                producer.send(new ProducerRecord<>("transactions-topic", 
                                                   "txn-2-key-1", 
                                                   "Mensaje que será abortado"));
                System.out.println("Enviado: Mensaje que será abortado");
                
                // Simular un error
                throw new RuntimeException("Error simulado");
                
            } catch (Exception e) {
                System.err.println("✗ Error detectado: " + e.getMessage());
                System.err.println("Abortando transacción...");
                producer.abortTransaction();
                System.out.println("✓ Transacción abortada. Los mensajes NO se enviarán.\n");
            }
            
            // Ejemplo 3: Transacción con múltiples topics
            System.out.println("=== Transacción 3: Multi-topic ===");
            try {
                producer.beginTransaction();
                
                // Enviar a diferentes topics en la misma transacción
                producer.send(new ProducerRecord<>("topic-A", "key", "Mensaje para topic A"));
                producer.send(new ProducerRecord<>("topic-B", "key", "Mensaje para topic B"));
                producer.send(new ProducerRecord<>("topic-C", "key", "Mensaje para topic C"));
                
                System.out.println("Mensajes enviados a múltiples topics");
                
                producer.commitTransaction();
                System.out.println("✓ Transacción multi-topic confirmada.\n");
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                producer.abortTransaction();
            }
            
            System.out.println("=== Resumen ===");
            System.out.println("Las transacciones garantizan:");
            System.out.println("- Atomicidad: todos los mensajes se envían o ninguno");
            System.out.println("- Exactly-once semantics");
            System.out.println("- Lecturas consistentes con isolation.level=read_committed");
            
        } finally {
            producer.close();
            System.out.println("\nProductor cerrado.");
        }
    }
}
