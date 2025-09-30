# Pruebas Unitarias MD5 para Go

## Descripción

Este paquete contiene un conjunto completo de pruebas unitarias para las funciones de generación de hash MD5 utilizadas para crear identificadores únicos y determinísticos para recursos de infraestructura.

## Funciones Probadas

### 1. `QuickMD5(vpcName, accountID string, length int) string`
- Función simple para generar MD5 rápidamente
- Concatena las variables y retorna los primeros N caracteres del hash

### 2. `MD5Hasher` (Estructura)
- `GenerateHash(inputs ...string) string`: Genera hash completo
- `GenerateShortHash(length int, inputs ...string) string`: Genera hash corto

### 3. `ResourceNamer` (Estructura)
- `GenerateResourceSuffix(vpcName, accountID string) string`: Sufijo de 12 caracteres
- `GenerateResourceName(resourceType, vpcName, accountID string) string`: Nombre completo

### 4. `GenerateAWSResourceNames(vpcName, accountID string) map[string]string`
- Genera nombres para 8 tipos de recursos AWS comunes

## Tipos de Pruebas

### ✅ Pruebas Funcionales
- **TestQuickMD5**: Casos básicos, longitudes diferentes, strings vacíos
- **TestQuickMD5EdgeCases**: Casos límite (longitud negativa, mayor a 32)
- **TestMD5Hasher**: Todas las funciones de la estructura
- **TestResourceNamer**: Generación de sufijos y nombres de recursos
- **TestGenerateAWSResourceNames**: Recursos AWS completos

### ✅ Pruebas de Robustez
- **TestDeterministicBehavior**: Verifica que las funciones sean determinísticas
- **TestValidHashFormat**: Valida formato hexadecimal y longitud
- **TestConcurrentAccess**: Prueba acceso concurrente con 100 goroutines
- **TestInputSanitization**: Manejo de caracteres especiales, Unicode, emojis

### ✅ Benchmarks de Rendimiento
- **BenchmarkQuickMD5**: ~509 ns/op, 64 B/op, 3 allocs/op
- **BenchmarkMD5HasherGenerateShortHash**: Comparación de rendimiento
- **BenchmarkGenerateAWSResourceNames**: Rendimiento de generación masiva
- **BenchmarkCompareImplementations**: Comparación entre implementaciones

## Ejecución de Pruebas

### Ejecutar todas las pruebas
```bash
go test -v
```

### Ejecutar solo pruebas de un tipo específico
```bash
go test -v -run TestQuickMD5
go test -v -run TestResourceNamer
go test -v -run TestConcurrentAccess
```

### Ejecutar benchmarks
```bash
go test -bench=. -benchmem
go test -v -bench=BenchmarkQuickMD5 -benchmem
```

### Generar reporte de cobertura
```bash
go test -cover
go test -coverprofile=coverage.csv.out
go tool cover -html=coverage.csv.out
```

## Casos de Prueba Destacados

### 1. Casos Básicos
- `my-vpc` + `123456789012` → `2cb89e623664` (12 chars)
- Verificación de longitudes: 8, 10, 12, 16, 32 caracteres

### 2. Casos Límite
- Longitud 0 → string vacío
- Longitud negativa → string vacío
- Longitud > 32 → hash completo (32 chars)

### 3. Casos Especiales
- Strings vacíos → `d41d8cd98f00`
- Caracteres especiales: `my-vpc-01` → `b9f0e96e81`
- Unicode y emojis: `vpc-测试`, `vpc-🚀`

### 4. Verificaciones de Calidad
- ✅ Formato hexadecimal válido (0-9, a-f)
- ✅ Comportamiento determinístico
- ✅ Thread-safety en acceso concurrente
- ✅ No duplicados en recursos AWS

## Estructura de Archivos

```
md5_tests/
├── md5.go          # Funciones principales
├── md5_test.go     # Pruebas unitarias
├── go.mod          # Módulo Go
└── README.md       # Esta documentación
```

## Resultados de Ejemplo

```
=== RUN   TestQuickMD5
=== RUN   TestQuickMD5/Caso_básico                     ✅
=== RUN   TestQuickMD5/Caso_con_longitud_diferente     ✅
=== RUN   TestQuickMD5/Caso_con_VPC_largo             ✅
=== RUN   TestQuickMD5/Caso_con_caracteres_especiales ✅
--- PASS: TestQuickMD5 (0.00s)

Benchmark Results:
BenchmarkQuickMD5-12    2392712    509.2 ns/op    64 B/op    3 allocs/op
```

## Integración

### Uso en Terraform
```hcl
locals {
  resource_suffix = substr(md5("${var.vpc_name}${var.account_id}"), 0, 12)
}
```

### Uso en Scripts de Shell
```bash
#!/bin/bash
VPC_NAME="production-vpc"
ACCOUNT_ID="123456789012"
HASH=$(echo -n "${VPC_NAME}${ACCOUNT_ID}" | md5sum | cut -c1-12)
```

### Uso en aplicaciones Go
```go
import "md5_tests"

suffix := md5_tests.QuickMD5("my-vpc", "123456789012", 12)
resources := md5_tests.GenerateAWSResourceNames("prod", "123456789012")
```

## Contribuir

Para añadir nuevas pruebas:

1. Añade el caso de prueba a la estructura correspondiente
2. Ejecuta `go test -v` para verificar
3. Actualiza los valores esperados si es necesario
4. Ejecuta los benchmarks para verificar el rendimiento

## Notas Técnicas

- **MD5**: Utiliza crypto/md5 estándar de Go
- **Concurrencia**: Thread-safe, probado con 100 goroutines
- **Performance**: ~500ns por operación, muy eficiente
- **Memoria**: 64 bytes y 3 allocaciones por operación
- **Compatibilidad**: Go 1.16+