import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStream
import java.util.Properties

/**
 * Ejemplo básico de Kafka Streams en Kotlin
 * Demuestra transformaciones simples de streams
 */
fun main() {
    // Configuración de Kafka Streams
    val props = Properties().apply {
        put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-basics-app")
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
    }
    
    // Crear el builder de streams
    val builder = StreamsBuilder()
    
    // Definir el stream de entrada
    val sourceStream: KStream<String, String> = builder.stream("input-topic")
    
    // Aplicar transformaciones
    val transformedStream = sourceStream
        // Filtrar mensajes que contengan "importante"
        .filter { _, value -> value != null && value.lowercase().contains("importante") }
        // Convertir a mayúsculas
        .mapValues { value -> value.uppercase() }
        // Agregar prefijo
        .mapValues { value -> "[PROCESADO] $value" }
    
    // Enviar el resultado a un topic de salida
    transformedStream.to("output-topic")
    
    // Imprimir al console (para debugging)
    transformedStream.foreach { key, value ->
        println("Procesado: $key -> $value")
    }
    
    // Construir y ejecutar la topología
    val streams = KafkaStreams(builder.build(), props)
    
    // Agregar shutdown hook para cierre limpio
    Runtime.getRuntime().addShutdownHook(Thread {
        println("\nCerrando Kafka Streams...")
        streams.close()
    })
    
    println("Iniciando Kafka Streams application...")
    println("Topología:")
    println(builder.build().describe())
    println("\nEsperando mensajes...\n")
    
    // Iniciar el procesamiento
    streams.start()
    
    // Mantener la aplicación corriendo
    try {
        Thread.sleep(Long.MAX_VALUE)
    } catch (e: InterruptedException) {
        streams.close()
    }
}
