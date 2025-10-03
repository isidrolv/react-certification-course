import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

/**
 * Ejemplo básico de Kafka Streams
 * Demuestra transformaciones simples de streams
 */
public class KafkaStreamsBasics {
    
    public static void main(String[] args) {
        // Configuración de Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-basics-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        
        // Crear el builder de streams
        StreamsBuilder builder = new StreamsBuilder();
        
        // Definir el stream de entrada
        KStream<String, String> sourceStream = builder.stream("input-topic");
        
        // Aplicar transformaciones
        KStream<String, String> transformedStream = sourceStream
            // Filtrar mensajes que contengan "importante"
            .filter((key, value) -> value != null && value.toLowerCase().contains("importante"))
            // Convertir a mayúsculas
            .mapValues(value -> value.toUpperCase())
            // Agregar prefijo
            .mapValues(value -> "[PROCESADO] " + value);
        
        // Enviar el resultado a un topic de salida
        transformedStream.to("output-topic");
        
        // Imprimir al console (para debugging)
        transformedStream.foreach((key, value) -> 
            System.out.println("Procesado: " + key + " -> " + value)
        );
        
        // Construir y ejecutar la topología
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        
        // Agregar shutdown hook para cierre limpio
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nCerrando Kafka Streams...");
            streams.close();
        }));
        
        System.out.println("Iniciando Kafka Streams application...");
        System.out.println("Topología:");
        System.out.println(builder.build().describe());
        System.out.println("\nEsperando mensajes...\n");
        
        // Iniciar el procesamiento
        streams.start();
        
        // Mantener la aplicación corriendo
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            streams.close();
        }
    }
}
