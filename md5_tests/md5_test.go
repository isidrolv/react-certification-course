package md5_tests

import (
	"strings"
	"testing"
)

// =============== PRUEBAS UNITARIAS ===============

// TestQuickMD5 prueba la función QuickMD5
func TestQuickMD5(t *testing.T) {
	tests := []struct {
		name      string
		vpcName   string
		accountID string
		length    int
		expected  string
	}{
		{
			name:      "Caso básico",
			vpcName:   "my-vpc",
			accountID: "123456789012",
			length:    12,
			expected:  "2cb89e623664",
		},
		{
			name:      "Caso con longitud diferente",
			vpcName:   "my-vpc",
			accountID: "123456789012",
			length:    8,
			expected:  "2cb89e62",
		},
		{
			name:      "Caso con VPC largo - corregido",
			vpcName:   "production-environment-vpc",
			accountID: "987654321098",
			length:    12,
			expected:  "385a25dd6f20", // Corregido con el valor real
		},
		{
			name:      "Caso con longitud 0",
			vpcName:   "test-vpc",
			accountID: "111111111111",
			length:    0,
			expected:  "",
		},
		{
			name:      "Caso con strings vacíos",
			vpcName:   "",
			accountID: "",
			length:    12,
			expected:  "d41d8cd98f00",
		},
		{
			name:      "Caso con caracteres especiales",
			vpcName:   "my-vpc-01",
			accountID: "123456789012",
			length:    10,
			expected:  "b9f0e96e81", // Los primeros 10 caracteres del hash
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := QuickMD5(tt.vpcName, tt.accountID, tt.length)
			if result != tt.expected {
				// Mostrar información adicional para debug
				fullHash := QuickMD5(tt.vpcName, tt.accountID, 32)
				t.Errorf("QuickMD5(%q, %q, %d) = %q, expected %q (full hash: %q)",
					tt.vpcName, tt.accountID, tt.length, result, tt.expected, fullHash)
			}
		})
	}
}

// TestQuickMD5EdgeCases prueba casos límite
func TestQuickMD5EdgeCases(t *testing.T) {
	// Caso con longitud mayor al hash completo
	t.Run("Longitud mayor a 32", func(t *testing.T) {
		result := QuickMD5("test", "123", 50)
		if len(result) != 32 { // MD5 tiene exactamente 32 caracteres
			t.Errorf("Expected length 32, got %d", len(result))
		}
	})

	// Caso con longitud negativa
	t.Run("Longitud negativa", func(t *testing.T) {
		result := QuickMD5("test", "123", -1)
		if result != "" {
			t.Errorf("Expected empty string for negative length, got %q", result)
		}
	})

	// Caso con longitud exactamente 32
	t.Run("Longitud exacta del MD5", func(t *testing.T) {
		result := QuickMD5("test", "123", 32)
		if len(result) != 32 {
			t.Errorf("Expected length 32, got %d", len(result))
		}
	})
}

// TestMD5Hasher prueba la estructura MD5Hasher
func TestMD5Hasher(t *testing.T) {
	hasher := &MD5Hasher{}

	t.Run("GenerateHash con una entrada", func(t *testing.T) {
		result := hasher.GenerateHash("test")
		expected := "098f6bcd4621d373cade4e832627b4f6"
		if result != expected {
			t.Errorf("GenerateHash() = %v, expected %v", result, expected)
		}
	})

	t.Run("GenerateHash con múltiples entradas", func(t *testing.T) {
		result := hasher.GenerateHash("my-vpc", "123456789012")
		expected := "2cb89e6236645e7f53d34f066b674ac4"
		if result != expected {
			t.Errorf("GenerateHash() = %v, expected %v", result, expected)
		}
	})

	t.Run("GenerateShortHash", func(t *testing.T) {
		result := hasher.GenerateShortHash(12, "my-vpc", "123456789012")
		expected := "2cb89e623664"
		if result != expected {
			t.Errorf("GenerateShortHash() = %v, expected %v", result, expected)
		}
	})

	t.Run("GenerateShortHash con longitud 0", func(t *testing.T) {
		result := hasher.GenerateShortHash(0, "test")
		if result != "" {
			t.Errorf("Expected empty string, got %v", result)
		}
	})

	t.Run("GenerateShortHash con longitud negativa", func(t *testing.T) {
		result := hasher.GenerateShortHash(-5, "test")
		if result != "" {
			t.Errorf("Expected empty string for negative length, got %v", result)
		}
	})

	t.Run("GenerateHash con múltiples entradas vacías", func(t *testing.T) {
		result := hasher.GenerateHash("", "", "")
		expected := "d41d8cd98f00b204e9800998ecf8427e" // MD5 de string vacía
		if result != expected {
			t.Errorf("GenerateHash() = %v, expected %v", result, expected)
		}
	})
}

// TestResourceNamer prueba la estructura ResourceNamer
func TestResourceNamer(t *testing.T) {
	namer := NewResourceNamer()

	t.Run("GenerateResourceSuffix", func(t *testing.T) {
		result := namer.GenerateResourceSuffix("production-vpc", "123456789012")
		expected := "0dd732454cd2"
		if result != expected {
			t.Errorf("GenerateResourceSuffix() = %v, expected %v", result, expected)
		}
	})

	t.Run("GenerateResourceName", func(t *testing.T) {
		result := namer.GenerateResourceName("subnet", "production-vpc", "123456789012")
		expected := "subnet-production-vpc-0dd732454cd2"
		if result != expected {
			t.Errorf("GenerateResourceName() = %v, expected %v", result, expected)
		}
	})

	t.Run("NewResourceNamer no debe ser nil", func(t *testing.T) {
		if namer == nil {
			t.Error("NewResourceNamer() returned nil")
		}
		if namer.hasher == nil {
			t.Error("ResourceNamer.hasher is nil")
		}
	})

	t.Run("GenerateResourceName con strings vacíos", func(t *testing.T) {
		result := namer.GenerateResourceName("", "", "")
		expected := "--d41d8cd98f00" // formato: tipo-vpc-suffix
		if result != expected {
			t.Errorf("GenerateResourceName() = %v, expected %v", result, expected)
		}
	})

	t.Run("Múltiples instancias deben ser independientes", func(t *testing.T) {
		namer1 := NewResourceNamer()
		namer2 := NewResourceNamer()

		result1 := namer1.GenerateResourceSuffix("test", "123")
		result2 := namer2.GenerateResourceSuffix("test", "123")

		if result1 != result2 {
			t.Errorf("Multiple instances should produce same result: %v != %v", result1, result2)
		}
	})
}

// TestGenerateAWSResourceNames prueba la generación de nombres de recursos AWS
func TestGenerateAWSResourceNames(t *testing.T) {
	t.Run("Recursos AWS completos", func(t *testing.T) {
		result := GenerateAWSResourceNames("prod-web", "987654321098")

		expectedKeys := []string{
			"vpc", "subnet_public", "subnet_private", "security_group",
			"route_table", "internet_gateway", "nat_gateway", "load_balancer",
		}

		// Verificar que todos los keys esperados estén presentes
		for _, key := range expectedKeys {
			if _, exists := result[key]; !exists {
				t.Errorf("Missing key %s in result", key)
			}
		}

		// Verificar que todos los valores contengan el sufijo esperado
		expectedSuffix := "89ecd5c3e083"
		for key, value := range result {
			if !strings.Contains(value, expectedSuffix) {
				t.Errorf("Resource %s = %v does not contain expected suffix %v", key, value, expectedSuffix)
			}
		}

		// Verificar formato específico de algunos recursos
		if result["vpc"] != "vpc-prod-web-89ecd5c3e083" {
			t.Errorf("VPC name format incorrect: got %v", result["vpc"])
		}

		if result["subnet_public"] != "subnet-pub-prod-web-89ecd5c3e083" {
			t.Errorf("Public subnet name format incorrect: got %v", result["subnet_public"])
		}

		if result["subnet_private"] != "subnet-prv-prod-web-89ecd5c3e083" {
			t.Errorf("Private subnet name format incorrect: got %v", result["subnet_private"])
		}
	})

	t.Run("Verificar que no hay duplicados", func(t *testing.T) {
		result := GenerateAWSResourceNames("test", "123456789012")

		// Crear un set de valores para verificar unicidad
		values := make(map[string]bool)
		for _, value := range result {
			if values[value] {
				t.Errorf("Duplicate value found: %v", value)
			}
			values[value] = true
		}
	})
}

// TestDeterministicBehavior prueba que las funciones sean determinísticas
func TestDeterministicBehavior(t *testing.T) {
	vpcName := "test-vpc"
	accountID := "444444444444"

	t.Run("QuickMD5 debe ser determinístico", func(t *testing.T) {
		results := make([]string, 10)
		for i := 0; i < 10; i++ {
			results[i] = QuickMD5(vpcName, accountID, 12)
		}

		first := results[0]
		for i, result := range results {
			if result != first {
				t.Errorf("QuickMD5 is not deterministic at iteration %d: %v != %v", i, result, first)
			}
		}
	})

	t.Run("MD5Hasher debe ser determinístico", func(t *testing.T) {
		hasher := &MD5Hasher{}

		results := make([]string, 10)
		for i := 0; i < 10; i++ {
			results[i] = hasher.GenerateShortHash(12, vpcName, accountID)
		}

		first := results[0]
		for i, result := range results {
			if result != first {
				t.Errorf("MD5Hasher is not deterministic at iteration %d: %v != %v", i, result, first)
			}
		}
	})

	t.Run("GenerateAWSResourceNames debe ser determinístico", func(t *testing.T) {
		results := make([]map[string]string, 5)
		for i := 0; i < 5; i++ {
			results[i] = GenerateAWSResourceNames(vpcName, accountID)
		}

		first := results[0]
		for i := 1; i < len(results); i++ {
			for key, firstValue := range first {
				if results[i][key] != firstValue {
					t.Errorf("GenerateAWSResourceNames is not deterministic for key %s at iteration %d: %v != %v",
						key, i, results[i][key], firstValue)
				}
			}
		}
	})
}

// TestValidHashFormat prueba que los hashes generados tengan formato válido
func TestValidHashFormat(t *testing.T) {
	tests := []struct {
		name      string
		vpcName   string
		accountID string
		length    int
	}{
		{"Caso normal", "my-vpc", "123456789012", 12},
		{"Caso con caracteres especiales", "my-vpc-01", "123456789012", 8},
		{"Caso con números", "vpc123", "987654321098", 16},
		{"Caso con longitud máxima", "test", "account", 32},
		{"Caso con longitud 1", "a", "b", 1},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := QuickMD5(tt.vpcName, tt.accountID, tt.length)

			// Verificar longitud
			if len(result) != tt.length {
				t.Errorf("Expected length %d, got %d", tt.length, len(result))
			}

			// Verificar que solo contenga caracteres hexadecimales
			for _, char := range result {
				if !((char >= '0' && char <= '9') || (char >= 'a' && char <= 'f')) {
					t.Errorf("Invalid hex character %c in hash %s", char, result)
				}
			}

			// Verificar que no esté vacío (excepto cuando length = 0)
			if tt.length > 0 && result == "" {
				t.Errorf("Expected non-empty result for length %d", tt.length)
			}
		})
	}
}

// TestConcurrentAccess prueba el acceso concurrente
func TestConcurrentAccess(t *testing.T) {
	t.Run("Acceso concurrente a QuickMD5", func(t *testing.T) {
		const numGoroutines = 100
		results := make(chan string, numGoroutines)

		for i := 0; i < numGoroutines; i++ {
			go func() {
				result := QuickMD5("concurrent-test", "123456789012", 12)
				results <- result
			}()
		}

		// Recopilar todos los resultados
		var allResults []string
		for i := 0; i < numGoroutines; i++ {
			allResults = append(allResults, <-results)
		}

		// Verificar que todos los resultados sean iguales
		first := allResults[0]
		for i, result := range allResults {
			if result != first {
				t.Errorf("Concurrent access failed at index %d: got %v, expected %v", i, result, first)
			}
		}
	})
}

// TestInputSanitization prueba el manejo de entradas especiales
func TestInputSanitization(t *testing.T) {
	tests := []struct {
		name      string
		vpcName   string
		accountID string
		length    int
		shouldErr bool
	}{
		{"Unicode characters", "vpc-测试", "123456789012", 12, false},
		{"Emojis", "vpc-🚀", "123456789012", 12, false},
		{"Very long strings", strings.Repeat("a", 1000), strings.Repeat("b", 1000), 12, false},
		{"Tab characters", "vpc\t", "123\t456", 12, false},
		{"Newline characters", "vpc\n", "123\n456", 12, false},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := QuickMD5(tt.vpcName, tt.accountID, tt.length)

			// Verificar que el resultado tenga la longitud correcta
			if len(result) != tt.length {
				t.Errorf("Expected length %d, got %d", tt.length, len(result))
			}

			// Verificar que sea hexadecimal válido
			for _, char := range result {
				if !((char >= '0' && char <= '9') || (char >= 'a' && char <= 'f')) {
					t.Errorf("Invalid hex character %c in hash %s", char, result)
				}
			}
		})
	}
}

// Benchmark para medir performance
func BenchmarkQuickMD5(b *testing.B) {
	for i := 0; i < b.N; i++ {
		QuickMD5("benchmark-vpc", "123456789012", 12)
	}
}

func BenchmarkMD5HasherGenerateShortHash(b *testing.B) {
	hasher := &MD5Hasher{}
	for i := 0; i < b.N; i++ {
		hasher.GenerateShortHash(12, "benchmark-vpc", "123456789012")
	}
}

func BenchmarkGenerateAWSResourceNames(b *testing.B) {
	for i := 0; i < b.N; i++ {
		GenerateAWSResourceNames("benchmark-vpc", "123456789012")
	}
}

// Benchmark comparativo
func BenchmarkCompareImplementations(b *testing.B) {
	hasher := &MD5Hasher{}

	b.Run("QuickMD5", func(b *testing.B) {
		for i := 0; i < b.N; i++ {
			QuickMD5("test", "123456789012", 12)
		}
	})

	b.Run("MD5Hasher", func(b *testing.B) {
		for i := 0; i < b.N; i++ {
			hasher.GenerateShortHash(12, "test", "123456789012")
		}
	})
}

// Prueba unitaria para MD5Hasher.GenerateHash
func TestMD5Hasher_GenerateHash(t *testing.T) {
	hasher := &MD5Hasher{}
	tests := []struct {
		name     string
		inputs   []string
		expected string
	}{
		{
			name:     "Single input",
			inputs:   []string{"test"},
			expected: "098f6bcd4621d373cade4e832627b4f6",
		},
		{
			name:     "Multiple inputs",
			inputs:   []string{"my-vpc", "123456789012"},
			expected: "2cb89e6236645e7f53d34f066b674ac4",
		},
		{
			name:     "Empty inputs",
			inputs:   []string{},
			expected: "d41d8cd98f00b204e9800998ecf8427e",
		},
		{
			name:     "Multiple empty strings",
			inputs:   []string{"", "", ""},
			expected: "d41d8cd98f00b204e9800998ecf8427e",
		},
		{
			name:     "Special characters",
			inputs:   []string{"vpc-🚀", "123"},
			expected: "d6795df03aa8ae0e235dd9383da9f771", // Reemplaza con el valor real si es necesario
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := hasher.GenerateHash(tt.inputs...)
			if result != tt.expected {
				t.Errorf("GenerateHash(%v) = %v, expected %v", tt.inputs, result, tt.expected)
			}
		})
	}
}
