import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties

/**
 * Ejemplo básico de un consumidor de Kafka en Kotlin
 * Demuestra la configuración esencial y la lectura de mensajes
 */
fun main() {
    // Configuración del consumidor
    val props = Properties().apply {
        // Servidor de Kafka (bootstrap servers)
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        
        // ID del grupo de consumidores (esencial para coordinación)
        put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-1")
        
        // Deserializadores para clave y valor
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        
        // Configuración de auto-commit de offsets
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
        
        // Desde dónde leer si no hay offset previo
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
    
    // Crear el consumidor usando use para manejo automático de recursos
    KafkaConsumer<String, String>(props).use { consumer ->
        try {
            // Suscribirse al topic
            val topic = "test-topic"
            consumer.subscribe(listOf(topic))
            
            println("Consumidor iniciado. Esperando mensajes...")
            println("Presiona Ctrl+C para detener.\n")
            
            // Contador de mensajes procesados
            var messageCount = 0
            
            // Bucle principal de consumo
            while (true) {
                // Poll para obtener mensajes (timeout de 1 segundo)
                val records = consumer.poll(Duration.ofSeconds(1))
                
                // Procesar cada mensaje recibido
                records.forEach { record ->
                    messageCount++
                    println("Mensaje recibido #$messageCount")
                    println("  Topic: ${record.topic()}")
                    println("  Partición: ${record.partition()}")
                    println("  Offset: ${record.offset()}")
                    println("  Clave: ${record.key()}")
                    println("  Valor: ${record.value()}")
                    println("  Timestamp: ${record.timestamp()}\n")
                }
                
                // Si no hay mensajes, mostrar punto de actividad
                if (records.isEmpty) {
                    print(".")
                }
            }
            
        } catch (e: Exception) {
            System.err.println("Error en el consumidor: ${e.message}")
            e.printStackTrace()
        } finally {
            println("Consumidor cerrado.")
        }
    }
}
