import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import java.io.BufferedReader
import java.io.FileReader

/**
 * Ejemplo de Source Connector personalizado para Kafka Connect en Kotlin
 * Lee líneas de un archivo y las envía a Kafka
 */
class FileSourceConnector : SourceTask() {
    
    companion object {
        const val FILE_PATH_CONFIG = "file.path"
        const val TOPIC_CONFIG = "topic"
    }
    
    private lateinit var filePath: String
    private lateinit var topic: String
    private lateinit var reader: BufferedReader
    private var offset: Long = 0
    
    override fun version(): String = "1.0.0"
    
    override fun start(props: MutableMap<String, String>) {
        // Obtener configuración
        filePath = props[FILE_PATH_CONFIG] ?: throw IllegalArgumentException("file.path requerido")
        topic = props[TOPIC_CONFIG] ?: throw IllegalArgumentException("topic requerido")
        offset = 0
        
        try {
            // Abrir el archivo para lectura
            reader = BufferedReader(FileReader(filePath))
            println("Source connector iniciado. Leyendo: $filePath")
        } catch (e: Exception) {
            throw RuntimeException("Error abriendo archivo: $filePath", e)
        }
    }
    
    override fun poll(): List<SourceRecord> {
        val records = mutableListOf<SourceRecord>()
        
        try {
            val line = reader.readLine()
            
            if (line != null) {
                // Crear el source record
                val sourcePartition = mapOf("file" to filePath)
                val sourceOffset = mapOf("position" to offset)
                
                val record = SourceRecord(
                    sourcePartition,
                    sourceOffset,
                    topic,
                    null, // partition (null = automático)
                    Schema.STRING_SCHEMA,
                    offset.toString(),
                    Schema.STRING_SCHEMA,
                    line
                )
                
                records.add(record)
                offset++
                
                println("Línea leída: $line")
            } else {
                // Fin del archivo, esperar
                Thread.sleep(1000)
            }
            
        } catch (e: Exception) {
            throw RuntimeException("Error leyendo archivo", e)
        }
        
        return records
    }
    
    override fun stop() {
        try {
            reader.close()
            println("Source connector detenido.")
        } catch (e: Exception) {
            throw RuntimeException("Error cerrando archivo", e)
        }
    }
}

/**
 * Ejemplo de configuración programática
 */
object ConnectConfig {
    fun getSourceConnectorConfig(): Map<String, String> {
        return mapOf(
            "name" to "file-source-connector",
            "connector.class" to "FileSourceConnector",
            "tasks.max" to "1",
            "file.path" to "/tmp/input.txt",
            "topic" to "file-topic"
        )
    }
}
