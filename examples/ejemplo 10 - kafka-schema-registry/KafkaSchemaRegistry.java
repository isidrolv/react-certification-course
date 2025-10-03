import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Ejemplo de uso de Schema Registry con Avro
 * Demuestra la serialización con esquemas registrados
 */
public class KafkaSchemaRegistry {
    
    // Schema Avro definido inline
    private static final String USER_SCHEMA = "{"
            + "\"type\":\"record\","
            + "\"name\":\"User\","
            + "\"namespace\":\"com.kafka.examples\","
            + "\"fields\":["
            + "  {\"name\":\"id\",\"type\":\"string\"},"
            + "  {\"name\":\"name\",\"type\":\"string\"},"
            + "  {\"name\":\"email\",\"type\":\"string\"},"
            + "  {\"name\":\"age\",\"type\":\"int\"}"
            + "]}";
    
    public static void main(String[] args) {
        // Configuración del productor con Schema Registry
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Usar KafkaAvroSerializer para valores
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        
        // URL del Schema Registry
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        
        // Configuración adicional
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);
        
        try {
            // Parsear el schema
            Schema.Parser parser = new Schema.Parser();
            Schema schema = parser.parse(USER_SCHEMA);
            
            System.out.println("Schema Registry Example");
            System.out.println("======================\n");
            System.out.println("Schema: " + schema.toString(true) + "\n");
            
            // Crear y enviar registros Avro
            String[] users = {
                "alice@example.com",
                "bob@example.com",
                "charlie@example.com"
            };
            
            for (int i = 0; i < users.length; i++) {
                // Crear un GenericRecord usando el schema
                GenericRecord user = new GenericData.Record(schema);
                user.put("id", String.valueOf(i + 1));
                user.put("name", users[i].split("@")[0]);
                user.put("email", users[i]);
                user.put("age", 25 + i);
                
                // Crear el ProducerRecord
                ProducerRecord<String, GenericRecord> record = 
                    new ProducerRecord<>("users-avro-topic", String.valueOf(i + 1), user);
                
                // Enviar con callback
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("✓ Usuario enviado:%n");
                        System.out.printf("  ID: %s%n", user.get("id"));
                        System.out.printf("  Nombre: %s%n", user.get("name"));
                        System.out.printf("  Email: %s%n", user.get("email"));
                        System.out.printf("  Partición: %d, Offset: %d%n%n", 
                            metadata.partition(), metadata.offset());
                    } else {
                        System.err.println("✗ Error: " + exception.getMessage());
                    }
                });
                
                Thread.sleep(100);
            }
            
            System.out.println("=== Ventajas de Schema Registry ===");
            System.out.println("✓ Validación automática de esquemas");
            System.out.println("✓ Evolución de esquemas controlada");
            System.out.println("✓ Compatibilidad entre versiones");
            System.out.println("✓ Reducción de payload (schema ID en lugar de schema completo)");
            System.out.println("✓ Documentación centralizada de formatos de datos");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            producer.flush();
            producer.close();
            System.out.println("\nProductor cerrado.");
        }
    }
}
