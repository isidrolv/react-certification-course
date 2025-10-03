import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Clase de ejemplo para serialización
 */
class User {
    private String id;
    private String name;
    private int age;
    
    public User() {}
    
    public User(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

/**
 * Serializador personalizado para User usando JSON
 */
class UserSerializer implements Serializer<User> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public byte[] serialize(String topic, User data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar User", e);
        }
    }
}

/**
 * Ejemplo de serialización personalizada en Kafka
 */
public class KafkaSerialization {
    
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        KafkaProducer<String, User> producer = new KafkaProducer<>(props);
        
        try {
            String topic = "users-topic";
            
            // Crear y enviar usuarios
            User[] users = {
                new User("1", "Alice", 30),
                new User("2", "Bob", 25),
                new User("3", "Charlie", 35)
            };
            
            for (User user : users) {
                ProducerRecord<String, User> record = 
                    new ProducerRecord<>(topic, user.getId(), user);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("Usuario enviado: %s (Partición: %d, Offset: %d)%n",
                            user.getName(), metadata.partition(), metadata.offset());
                    } else {
                        System.err.println("Error: " + exception.getMessage());
                    }
                });
            }
            
            System.out.println("\n✓ Todos los usuarios serializados y enviados!");
            System.out.println("Los objetos User fueron convertidos a JSON automáticamente.");
            
        } finally {
            producer.flush();
            producer.close();
            System.out.println("Productor cerrado.");
        }
    }
}
