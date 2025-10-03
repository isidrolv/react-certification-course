import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Ejemplo básico de un productor de Kafka
 * Demuestra la configuración esencial y el envío de mensajes simples
 */
public class KafkaProducerBasics {
    
    public static void main(String[] args) {
        // Configuración del productor
        Properties props = new Properties();
        
        // Servidor de Kafka (bootstrap servers)
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        // Serializadores para clave y valor
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Propiedades opcionales pero recomendadas
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Esperar confirmación de todos los replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Número de reintentos
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Tiempo de espera antes de enviar
        
        // Crear el productor
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        try {
            // Enviar 10 mensajes
            for (int i = 0; i < 10; i++) {
                String topic = "test-topic";
                String key = "key-" + i;
                String value = "Mensaje número " + i;
                
                // Crear el registro del productor
                ProducerRecord<String, String> record = 
                    new ProducerRecord<>(topic, key, value);
                
                // Enviar el mensaje (asíncrono)
                producer.send(record);
                
                System.out.println("Mensaje enviado: " + key + " -> " + value);
                
                // Pequeña pausa para visualizar el envío
                Thread.sleep(100);
            }
            
            System.out.println("\nTodos los mensajes enviados exitosamente!");
            
        } catch (InterruptedException e) {
            System.err.println("Error al enviar mensajes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar el productor para liberar recursos
            // flush() asegura que todos los mensajes se envíen antes de cerrar
            producer.flush();
            producer.close();
            System.out.println("Productor cerrado.");
        }
    }
}
