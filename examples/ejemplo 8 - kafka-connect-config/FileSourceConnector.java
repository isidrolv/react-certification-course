import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.*;
import java.util.*;

/**
 * Ejemplo de Source Connector personalizado para Kafka Connect
 * Lee líneas de un archivo y las envía a Kafka
 */
public class FileSourceConnector extends SourceTask {
    
    private static final String FILE_PATH_CONFIG = "file.path";
    private static final String TOPIC_CONFIG = "topic";
    
    private String filePath;
    private String topic;
    private BufferedReader reader;
    private long offset;
    
    @Override
    public String version() {
        return "1.0.0";
    }
    
    @Override
    public void start(Map<String, String> props) {
        // Obtener configuración
        filePath = props.get(FILE_PATH_CONFIG);
        topic = props.get(TOPIC_CONFIG);
        offset = 0;
        
        try {
            // Abrir el archivo para lectura
            reader = new BufferedReader(new FileReader(filePath));
            System.out.println("Source connector iniciado. Leyendo: " + filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Archivo no encontrado: " + filePath, e);
        }
    }
    
    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> records = new ArrayList<>();
        
        try {
            String line = reader.readLine();
            
            if (line != null) {
                // Crear el source record
                Map<String, Object> sourcePartition = Collections.singletonMap("file", filePath);
                Map<String, Object> sourceOffset = Collections.singletonMap("position", offset);
                
                SourceRecord record = new SourceRecord(
                    sourcePartition,
                    sourceOffset,
                    topic,
                    null, // partition (null = automático)
                    Schema.STRING_SCHEMA,
                    String.valueOf(offset),
                    Schema.STRING_SCHEMA,
                    line
                );
                
                records.add(record);
                offset++;
                
                System.out.println("Línea leída: " + line);
            } else {
                // Fin del archivo, esperar
                Thread.sleep(1000);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo archivo", e);
        }
        
        return records;
    }
    
    @Override
    public void stop() {
        try {
            if (reader != null) {
                reader.close();
            }
            System.out.println("Source connector detenido.");
        } catch (IOException e) {
            throw new RuntimeException("Error cerrando archivo", e);
        }
    }
}

/**
 * Ejemplo de configuración programática
 */
class ConnectConfig {
    public static Map<String, String> getSourceConnectorConfig() {
        Map<String, String> config = new HashMap<>();
        
        // Configuración del conector
        config.put("name", "file-source-connector");
        config.put("connector.class", "FileSourceConnector");
        config.put("tasks.max", "1");
        
        // Configuración específica
        config.put("file.path", "/tmp/input.txt");
        config.put("topic", "file-topic");
        
        return config;
    }
}
