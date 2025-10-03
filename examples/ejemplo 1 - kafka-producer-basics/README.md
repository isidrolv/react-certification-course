# Ejemplo 1: Kafka Producer Basics

## Descripción
Este ejemplo demuestra cómo crear un productor básico de Kafka que envía mensajes a un topic.

## Conceptos Clave
- Configuración del productor
- Propiedades esenciales (bootstrap.servers, key.serializer, value.serializer)
- Envío síncrono de mensajes
- Manejo de excepciones

## Archivos
- `KafkaProducerBasics.java` - Implementación en Java
- `KafkaProducerBasics.kt` - Implementación en Kotlin
- `pom.xml` - Dependencias Maven

## Ejecución

### Java
```bash
javac -cp ".:kafka-clients-3.0.0.jar" KafkaProducerBasics.java
java -cp ".:kafka-clients-3.0.0.jar" KafkaProducerBasics
```

### Kotlin
```bash
kotlinc KafkaProducerBasics.kt -cp kafka-clients-3.0.0.jar -include-runtime -d KafkaProducerBasics.jar
kotlin -cp "KafkaProducerBasics.jar:kafka-clients-3.0.0.jar" KafkaProducerBasicsKt
```

## Temas de Certificación
- Producer API
- Configuración de productores
- Serialización de mensajes
