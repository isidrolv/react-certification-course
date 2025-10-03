import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

/**
 * Ejemplo de uso de Schema Registry con Avro en Kotlin
 * Demuestra la serialización con esquemas registrados
 */
fun main() {
    // Schema Avro definido inline
    val userSchema = """
        {
          "type": "record",
          "name": "User",
          "namespace": "com.kafka.examples",
          "fields": [
            {"name": "id", "type": "string"},
            {"name": "name", "type": "string"},
            {"name": "email", "type": "string"},
            {"name": "age", "type": "int"}
          ]
        }
    """.trimIndent()
    
    // Configuración del productor con Schema Registry
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        
        // Usar KafkaAvroSerializer para valores
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java.name)
        
        // URL del Schema Registry
        put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081")
        
        // Configuración adicional
        put(ProducerConfig.ACKS_CONFIG, "all")
    }
    
    KafkaProducer<String, GenericRecord>(props).use { producer ->
        try {
            // Parsear el schema
            val parser = Schema.Parser()
            val schema = parser.parse(userSchema)
            
            println("Schema Registry Example")
            println("======================\n")
            println("Schema: ${schema.toString(true)}\n")
            
            // Crear y enviar registros Avro
            val users = listOf(
                "alice@example.com",
                "bob@example.com",
                "charlie@example.com"
            )
            
            users.forEachIndexed { i, email ->
                // Crear un GenericRecord usando el schema
                val user = GenericData.Record(schema).apply {
                    put("id", (i + 1).toString())
                    put("name", email.split("@")[0])
                    put("email", email)
                    put("age", 25 + i)
                }
                
                // Crear el ProducerRecord
                val record = ProducerRecord(
                    "users-avro-topic",
                    (i + 1).toString(),
                    user
                )
                
                // Enviar con callback
                producer.send(record) { metadata, exception ->
                    if (exception == null) {
                        println("✓ Usuario enviado:")
                        println("  ID: ${user["id"]}")
                        println("  Nombre: ${user["name"]}")
                        println("  Email: ${user["email"]}")
                        println("  Partición: ${metadata.partition()}, Offset: ${metadata.offset()}\n")
                    } else {
                        System.err.println("✗ Error: ${exception.message}")
                    }
                }
                
                Thread.sleep(100)
            }
            
            println("=== Ventajas de Schema Registry ===")
            println("✓ Validación automática de esquemas")
            println("✓ Evolución de esquemas controlada")
            println("✓ Compatibilidad entre versiones")
            println("✓ Reducción de payload (schema ID en lugar de schema completo)")
            println("✓ Documentación centralizada de formatos de datos")
            
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            e.printStackTrace()
        } finally {
            producer.flush()
            println("\nProductor cerrado.")
        }
    }
}
