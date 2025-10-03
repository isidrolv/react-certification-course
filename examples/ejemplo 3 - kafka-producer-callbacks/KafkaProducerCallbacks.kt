import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * Ejemplo de productor con callbacks asíncronos en Kotlin
 * Demuestra el manejo de confirmaciones y errores mediante callbacks
 */
fun main() {
    // Configuración del productor
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRIES_CONFIG, 3)
    }
    
    KafkaProducer<String, String>(props).use { producer ->
        try {
            // Enviar mensajes con callbacks
            repeat(10) { i ->
                val topic = "test-topic"
                val key = "key-$i"
                val value = "Mensaje con callback $i"
                
                val record = ProducerRecord(topic, key, value)
                
                // Enviar con callback lambda
                producer.send(record) { metadata, exception ->
                    when {
                        exception == null -> {
                            // Éxito: mostrar información del mensaje enviado
                            println("✓ Mensaje enviado exitosamente:")
                            println("  Topic: ${metadata.topic()}")
                            println("  Partición: ${metadata.partition()}")
                            println("  Offset: ${metadata.offset()}")
                            println("  Timestamp: ${metadata.timestamp()}\n")
                        }
                        else -> {
                            // Error: mostrar información del error
                            System.err.println("✗ Error al enviar mensaje:")
                            System.err.println("  Error: ${exception.message}")
                            exception.printStackTrace()
                        }
                    }
                }
                
                println("Mensaje $i enviado (esperando callback)...")
                Thread.sleep(100)
            }
            
            println("\nTodos los mensajes enviados. Esperando callbacks...")
            
        } catch (e: InterruptedException) {
            System.err.println("Error: ${e.message}")
            e.printStackTrace()
        } finally {
            // Asegurar que todos los callbacks se completen
            producer.flush()
            println("Productor cerrado.")
        }
    }
}
