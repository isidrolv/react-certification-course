#!/usr/bin/env python3
"""
Ejemplo de cálculo de MD5 hash de concatenación de variables
"""

import hashlib

def calculate_md5_substring(vpc_name, account_id, length=12):
    """
    Calcula el MD5 hash de la concatenación de vpc_name + account_id
    y retorna los primeros 'length' caracteres
    """
    # Concatenar las variables
    concatenated = vpc_name + account_id
    
    # Calcular MD5 hash
    md5_hash = hashlib.md5(concatenated.encode('utf-8')).hexdigest()
    
    # Tomar los primeros caracteres especificados
    result = md5_hash[:length]
    
    return {
        'concatenated': concatenated,
        'full_hash': md5_hash,
        'substring': result
    }

# Ejemplos de uso
examples = [
    {"vpc_name": "my-vpc", "account_id": "123456789012"},
    {"vpc_name": "prod-vpc", "account_id": "987654321098"},
    {"vpc_name": "dev-environment", "account_id": "555666777888"},
]

print("=== MD5 Hash Calculator ===\n")

for i, example in enumerate(examples, 1):
    print(f"Ejemplo {i}:")
    result = calculate_md5_substring(example["vpc_name"], example["account_id"])
    
    print(f"  VPC Name: {example['vpc_name']}")
    print(f"  Account ID: {example['account_id']}")
    print(f"  Concatenated: {result['concatenated']}")
    print(f"  Full MD5 Hash: {result['full_hash']}")
    print(f"  First 12 chars: {result['substring']}")
    print()

# Función reutilizable
def get_resource_suffix(vpc_name, account_id):
    """Función específica para generar sufijo de recursos"""
    return calculate_md5_substring(vpc_name, account_id, 12)['substring']

print("=== Función reutilizable ===")
suffix = get_resource_suffix("my-production-vpc", "123456789012")
print(f"Resource suffix: {suffix}")