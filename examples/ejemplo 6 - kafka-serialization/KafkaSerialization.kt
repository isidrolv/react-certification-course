import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * Clase de ejemplo para serialización
 */
data class User(
    val id: String,
    val name: String,
    val age: Int
)

/**
 * Serializador personalizado para User usando JSON
 */
class UserSerializer : Serializer<User> {
    private val objectMapper = ObjectMapper()
    
    override fun serialize(topic: String?, data: User?): ByteArray? {
        return try {
            data?.let { objectMapper.writeValueAsBytes(it) }
        } catch (e: Exception) {
            throw RuntimeException("Error al serializar User", e)
        }
    }
}

/**
 * Ejemplo de serialización personalizada en Kafka con Kotlin
 */
fun main() {
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
    }
    
    KafkaProducer<String, User>(props).use { producer ->
        try {
            val topic = "users-topic"
            
            // Crear y enviar usuarios
            val users = listOf(
                User("1", "Alice", 30),
                User("2", "Bob", 25),
                User("3", "Charlie", 35)
            )
            
            users.forEach { user ->
                val record = ProducerRecord(topic, user.id, user)
                
                producer.send(record) { metadata, exception ->
                    if (exception == null) {
                        println("Usuario enviado: ${user.name} (Partición: ${metadata.partition()}, Offset: ${metadata.offset()})")
                    } else {
                        System.err.println("Error: ${exception.message}")
                    }
                }
            }
            
            println("\n✓ Todos los usuarios serializados y enviados!")
            println("Los objetos User fueron convertidos a JSON automáticamente.")
            
        } finally {
            producer.flush()
            println("Productor cerrado.")
        }
    }
}
