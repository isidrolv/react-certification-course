import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * Ejemplo de uso de particiones y claves en Kafka con Kotlin
 * Demuestra cómo las claves determinan la partición destino
 */
fun main() {
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
    }
    
    KafkaProducer<String, String>(props).use { producer ->
        try {
            val topic = "partitioned-topic"
            
            // Enviar mensajes con diferentes claves
            val users = listOf("user-A", "user-B", "user-C", "user-A", "user-B")
            
            users.forEachIndexed { i, key ->
                val value = "Mensaje $i del $key"
                val record = ProducerRecord(topic, key, value)
                
                producer.send(record) { metadata, exception ->
                    if (exception == null) {
                        println("Mensaje enviado:")
                        println("  Clave: $key")
                        println("  Partición: ${metadata.partition()}")
                        println("  Offset: ${metadata.offset()}")
                        println("  Valor: $value\n")
                    } else {
                        System.err.println("Error: ${exception.message}")
                    }
                }
                
                Thread.sleep(100)
            }
            
            // Enviar mensajes sin clave (distribución round-robin)
            println("\n--- Mensajes sin clave (round-robin) ---\n")
            
            repeat(5) { i ->
                val value = "Mensaje sin clave $i"
                val record = ProducerRecord<String, String>(topic, null, value)
                
                producer.send(record) { metadata, exception ->
                    if (exception == null) {
                        println("Mensaje sin clave -> Partición: ${metadata.partition()}, Offset: ${metadata.offset()}")
                    }
                }
                
                Thread.sleep(100)
            }
            
            println("\nObserva que mensajes con la misma clave van a la misma partición!")
            
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            producer.flush()
            println("Productor cerrado.")
        }
    }
}
