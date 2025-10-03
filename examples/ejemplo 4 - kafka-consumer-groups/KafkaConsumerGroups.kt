import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties

/**
 * Ejemplo de Consumer Groups en Kafka con Kotlin
 * Demuestra cómo múltiples consumidores trabajan juntos en un grupo
 */
fun main() {
    val groupId = "consumer-group-demo"
    val consumerId = "consumer-${System.currentTimeMillis()}"
    
    // Configuración del consumidor
    val props = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        
        // Deshabilitar auto-commit para control manual
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        
        // Configuración de rebalanceo
        put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000")
        put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000")
    }
    
    KafkaConsumer<String, String>(props).use { consumer ->
        try {
            // Suscribirse al topic
            consumer.subscribe(listOf("test-topic"))
            
            println("===================================")
            println("Consumidor iniciado")
            println("Consumer ID: $consumerId")
            println("Group ID: $groupId")
            println("===================================\n")
            
            var messageCount = 0
            
            while (true) {
                val records = consumer.poll(Duration.ofSeconds(1))
                
                records.forEach { record ->
                    messageCount++
                    
                    println("[$consumerId] Mensaje #$messageCount procesado:")
                    println("  Partición: ${record.partition()} | Offset: ${record.offset()}")
                    println("  Clave: ${record.key()} | Valor: ${record.value()}\n")
                    
                    // Simular procesamiento
                    Thread.sleep(100)
                }
                
                // Commit manual de offsets después de procesar
                if (!records.isEmpty) {
                    consumer.commitSync()
                    println("Offsets committed.")
                }
            }
            
        } catch (e: Exception) {
            System.err.println("Error en consumidor: ${e.message}")
            e.printStackTrace()
        } finally {
            println("Consumidor cerrado.")
        }
    }
}
