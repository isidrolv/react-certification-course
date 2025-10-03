import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * Ejemplo básico de un productor de Kafka en Kotlin
 * Demuestra la configuración esencial y el envío de mensajes simples
 */
fun main() {
    // Configuración del productor
    val props = Properties().apply {
        // Servidor de Kafka (bootstrap servers)
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        
        // Serializadores para clave y valor
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        
        // Propiedades opcionales pero recomendadas
        put(ProducerConfig.ACKS_CONFIG, "all") // Esperar confirmación de todos los replicas
        put(ProducerConfig.RETRIES_CONFIG, 3) // Número de reintentos
        put(ProducerConfig.LINGER_MS_CONFIG, 1) // Tiempo de espera antes de enviar
    }
    
    // Crear el productor usando use para manejo automático de recursos
    KafkaProducer<String, String>(props).use { producer ->
        try {
            // Enviar 10 mensajes
            repeat(10) { i ->
                val topic = "test-topic"
                val key = "key-$i"
                val value = "Mensaje número $i"
                
                // Crear el registro del productor
                val record = ProducerRecord(topic, key, value)
                
                // Enviar el mensaje (asíncrono)
                producer.send(record)
                
                println("Mensaje enviado: $key -> $value")
                
                // Pequeña pausa para visualizar el envío
                Thread.sleep(100)
            }
            
            println("\nTodos los mensajes enviados exitosamente!")
            
        } catch (e: InterruptedException) {
            System.err.println("Error al enviar mensajes: ${e.message}")
            e.printStackTrace()
        } finally {
            // flush() asegura que todos los mensajes se envíen antes de cerrar
            producer.flush()
            println("Productor cerrado.")
        }
    }
}
