# Ejemplo 2: Kafka Consumer Basics

## Descripción
Este ejemplo demuestra cómo crear un consumidor básico de Kafka que lee mensajes de un topic.

## Conceptos Clave
- Configuración del consumidor
- Propiedades esenciales (bootstrap.servers, group.id, deserializers)
- Suscripción a topics
- Polling y procesamiento de mensajes
- Auto-commit de offsets

## Archivos
- `KafkaConsumerBasics.java` - Implementación en Java
- `KafkaConsumerBasics.kt` - Implementación en Kotlin
- `pom.xml` - Dependencias Maven

## Ejecución

### Java
```bash
javac -cp ".:kafka-clients-3.0.0.jar" KafkaConsumerBasics.java
java -cp ".:kafka-clients-3.0.0.jar" KafkaConsumerBasics
```

### Kotlin
```bash
kotlinc KafkaConsumerBasics.kt -cp kafka-clients-3.0.0.jar -include-runtime -d KafkaConsumerBasics.jar
kotlin -cp "KafkaConsumerBasics.jar:kafka-clients-3.0.0.jar" KafkaConsumerBasicsKt
```

## Temas de Certificación
- Consumer API
- Configuración de consumidores
- Deserialización de mensajes
- Manejo de offsets
