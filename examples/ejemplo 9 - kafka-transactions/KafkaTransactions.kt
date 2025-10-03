import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * Ejemplo de transacciones en Kafka con Kotlin
 * Demuestra exactly-once semantics mediante transacciones
 */
fun main() {
    // Configuración del productor transaccional
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        
        // Configuración transaccional
        put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id")
        put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
        put(ProducerConfig.ACKS_CONFIG, "all")
    }
    
    KafkaProducer<String, String>(props).use { producer ->
        try {
            // Inicializar transacciones
            producer.initTransactions()
            println("Productor transaccional inicializado.\n")
            
            // Ejemplo 1: Transacción exitosa
            println("=== Transacción 1: Exitosa ===")
            try {
                producer.beginTransaction()
                
                // Enviar múltiples mensajes en una transacción
                repeat(3) { i ->
                    val record = ProducerRecord(
                        "transactions-topic",
                        "txn-1-key-$i",
                        "Mensaje transaccional $i"
                    )
                    producer.send(record)
                    println("Enviado: ${record.value()}")
                }
                
                // Commit de la transacción
                producer.commitTransaction()
                println("✓ Transacción confirmada exitosamente.\n")
                
            } catch (e: Exception) {
                System.err.println("Error en transacción, abortando...")
                producer.abortTransaction()
            }
            
            // Ejemplo 2: Transacción con abort
            println("=== Transacción 2: Con Abort ===")
            try {
                producer.beginTransaction()
                
                producer.send(ProducerRecord(
                    "transactions-topic",
                    "txn-2-key-1",
                    "Mensaje que será abortado"
                ))
                println("Enviado: Mensaje que será abortado")
                
                // Simular un error
                throw RuntimeException("Error simulado")
                
            } catch (e: Exception) {
                System.err.println("✗ Error detectado: ${e.message}")
                System.err.println("Abortando transacción...")
                producer.abortTransaction()
                println("✓ Transacción abortada. Los mensajes NO se enviarán.\n")
            }
            
            // Ejemplo 3: Transacción con múltiples topics
            println("=== Transacción 3: Multi-topic ===")
            try {
                producer.beginTransaction()
                
                // Enviar a diferentes topics en la misma transacción
                producer.send(ProducerRecord("topic-A", "key", "Mensaje para topic A"))
                producer.send(ProducerRecord("topic-B", "key", "Mensaje para topic B"))
                producer.send(ProducerRecord("topic-C", "key", "Mensaje para topic C"))
                
                println("Mensajes enviados a múltiples topics")
                
                producer.commitTransaction()
                println("✓ Transacción multi-topic confirmada.\n")
                
            } catch (e: Exception) {
                System.err.println("Error: ${e.message}")
                producer.abortTransaction()
            }
            
            println("=== Resumen ===")
            println("Las transacciones garantizan:")
            println("- Atomicidad: todos los mensajes se envían o ninguno")
            println("- Exactly-once semantics")
            println("- Lecturas consistentes con isolation.level=read_committed")
            
        } finally {
            println("\nProductor cerrado.")
        }
    }
}
