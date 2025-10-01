#!/usr/bin/env python3
"""
Programa para convertir preguntas de Java del archivo readme.md 
al formato JSON de opción múltiple para react-certification-course
"""

import json
import re
import random
import os

def read_java_readme():
    """Lee el archivo readme.md con las preguntas de Java"""
    readme_path = os.environ.get('README_PATH', os.path.join(os.getcwd(), 'readme.md'))
    try:
        with open(readme_path, 'r', encoding='utf-8') as file:
            content = file.read()
        return content
    except FileNotFoundError:
        print(f"Error: No se encontró el archivo {readme_path}")
        return None

def extract_questions_from_readme(content):
    """Extrae todas las preguntas del contenido del readme"""
    questions = []
    
    # Patrón para encontrar preguntas con formato: **n . Pregunta**
    question_pattern = r'-\s*\*\*(\d+)\s*\.\s*([^*]+?)\*\*'
    
    # Buscar todas las preguntas
    question_matches = re.finditer(question_pattern, content, re.DOTALL)
    
    current_category = "Java Basics"
    
    for match in question_matches:
        question_num = int(match.group(1))
        question_text = match.group(2).strip()
        
        # Determinar categoría basada en el número de pregunta y contenido
        category = determine_category(question_num, question_text, content)
        
        # Extraer la respuesta de la pregunta
        answer_content = extract_answer_content(content, match.end())
        
        if answer_content:
            question_data = {
                "id": question_num,
                "category": category,
                "question": question_text,
                "answer_content": answer_content
            }
            questions.append(question_data)
    
    return questions

def determine_category(question_num, question_text, content):
    """Determina la categoría de la pregunta basada en el número y contexto"""
    categories_map = {
        (1, 6): "Java Platform",
        (7, 15): "Wrapper Classes", 
        (16, 25): "Strings",
        (26, 35): "Object-Oriented Programming",
        (36, 45): "Inheritance",
        (46, 55): "Collections",
        (56, 65): "Exception Handling",
        (66, 75): "Multithreading",
        (76, 85): "Generics",
        (86, 95): "Java 8 Features",
        (96, 105): "Spring Framework",
        (106, 115): "Database Connectivity",
        (116, 125): "Web Services",
        (126, 135): "Design Patterns",
        (136, 145): "JVM and Memory Management",
        (146, 155): "Testing",
        (156, 165): "Best Practices",
        (166, 175): "Performance Optimization",
        (176, 185): "Security",
        (186, 195): "Frameworks",
        (196, 205): "Advanced Topics",
        (206, 215): "Enterprise Development",
        (216, 230): "Modern Java Features"
    }
    
    # Buscar la categoría apropiada
    for (start, end), category in categories_map.items():
        if start <= question_num <= end:
            return category
    
    # Categoría por defecto o basada en contenido
    question_lower = question_text.lower()
    if any(word in question_lower for word in ['string', 'immutable', 'stringbuilder']):
        return "Strings"
    elif any(word in question_lower for word in ['collection', 'list', 'set', 'map']):
        return "Collections"
    elif any(word in question_lower for word in ['thread', 'synchronized', 'concurrent']):
        return "Multithreading"
    elif any(word in question_lower for word in ['exception', 'try', 'catch', 'finally']):
        return "Exception Handling"
    elif any(word in question_lower for word in ['jvm', 'memory', 'garbage']):
        return "JVM and Memory Management"
    else:
        return "Java Basics"

def extract_answer_content(content, start_pos):
    """Extrae el contenido de la respuesta después de una pregunta"""
    # Buscar desde la posición actual hasta la siguiente pregunta
    next_question_pattern = r'-\s*\*\*\d+\s*\.\s*[^*]+?\*\*'
    next_match = re.search(next_question_pattern, content[start_pos:])
    
    if next_match:
        end_pos = start_pos + next_match.start()
        answer_section = content[start_pos:end_pos]
    else:
        # Si no hay más preguntas, tomar hasta el final
        answer_section = content[start_pos:]
    
    return answer_section.strip()

def create_multiple_choice_question(question_data):
    """Convierte una pregunta en formato de opción múltiple"""
    question_text = question_data["question"]
    answer_content = question_data["answer_content"]
    
    # Extraer la respuesta correcta del contenido
    correct_answer = extract_correct_answer(answer_content)
    
    # Generar opciones incorrectas (distractores)
    distractors = generate_distractors(question_text, correct_answer)
    
    # Mezclar opciones
    all_options = [correct_answer] + distractors
    random.shuffle(all_options)
    
    # Encontrar la posición de la respuesta correcta
    correct_index = all_options.index(correct_answer)
    answer_letter = chr(ord('a') + correct_index)
    
    # Generar explicación
    explanation = generate_explanation(answer_content)
    
    return {
        "id": question_data["id"],
        "category": question_data["category"],
        "question": question_text,
        "options": all_options,
        "answer": answer_letter,
        "explanation": explanation
    }

def extract_correct_answer(answer_content):
    """Extrae la respuesta correcta del contenido de la respuesta"""
    # Buscar puntos clave en la respuesta
    lines = answer_content.split('\n')
    
    # Tomar las primeras líneas más relevantes
    relevant_lines = []
    for line in lines[:10]:  # Primeras 10 líneas
        line = line.strip()
        if line and not line.startswith('-') and not line.startswith('*'):
            if len(line) > 20 and len(line) < 200:  # Longitud apropiada para opción
                relevant_lines.append(line)
    
    if relevant_lines:
        return relevant_lines[0]
    else:
        # Respuesta genérica si no se puede extraer
        return "Esta es la respuesta correcta basada en el contenido de Java."

def generate_distractors(question_text, correct_answer):
    """Genera opciones incorrectas plausibles"""
    question_lower = question_text.lower()
    
    # Distractores generales para diferentes tipos de preguntas
    general_distractors = [
        "Esta opción es incorrecta para esta pregunta de Java.",
        "Esta no es la respuesta correcta según los estándares de Java.",
        "Esta opción no aplica a este concepto de programación Java."
    ]
    
    # Distractores específicos por tema
    if any(word in question_lower for word in ['jvm', 'platform', 'bytecode']):
        specific_distractors = [
            "Java se compila directamente a código máquina específico de la plataforma.",
            "Java requiere recompilación para cada sistema operativo diferente.",
            "Java no puede ejecutarse en diferentes sistemas operativos."
        ]
    elif any(word in question_lower for word in ['string', 'immutable']):
        specific_distractors = [
            "Los objetos String en Java son completamente mutables.",
            "StringBuffer y StringBuilder son inmutables en Java.",
            "Java no tiene soporte para cadenas de texto inmutables."
        ]
    elif any(word in question_lower for word in ['wrapper', 'boxing']):
        specific_distractors = [
            "Java no soporta conversión automática entre primitivos y objetos.",
            "Los tipos wrapper consumen menos memoria que los primitivos.",
            "Autoboxing solo funciona con tipos numéricos, no con boolean o char."
        ]
    elif any(word in question_lower for word in ['collection', 'list', 'set']):
        specific_distractors = [
            "Las colecciones en Java solo pueden almacenar tipos primitivos.",
            "ArrayList y LinkedList tienen el mismo rendimiento en todas las operaciones.",
            "Set permite elementos duplicados en Java."
        ]
    elif any(word in question_lower for word in ['thread', 'synchronized']):
        specific_distractors = [
            "Java no soporta programación concurrente nativa.",
            "Synchronized solo funciona con métodos estáticos.",
            "Los threads en Java no pueden compartir memoria."
        ]
    else:
        specific_distractors = [
            "Esta funcionalidad no existe en Java.",
            "Java no soporta esta característica hasta versiones muy recientes.",
            "Esta es una característica exclusiva de otros lenguajes de programación."
        ]
    
    # Combinar distractores y seleccionar 3
    all_distractors = specific_distractors + general_distractors
    
    # Asegurar que no repetimos la respuesta correcta
    unique_distractors = [d for d in all_distractors if d != correct_answer]
    
    # Seleccionar 3 distractores únicos
    selected_distractors = []
    for distractor in unique_distractors:
        if len(selected_distractors) < 3:
            selected_distractors.append(distractor)
    
    # Si necesitamos más distractores, generar algunos genéricos
    while len(selected_distractors) < 3:
        generic = f"Opción incorrecta número {len(selected_distractors) + 1} para esta pregunta."
        selected_distractors.append(generic)
    
    return selected_distractors[:3]

def generate_explanation(answer_content):
    """Genera una explicación basada en el contenido de la respuesta"""
    # Tomar las primeras líneas del contenido como explicación
    lines = answer_content.split('\n')
    explanation_parts = []
    
    for line in lines[:5]:  # Primeras 5 líneas
        line = line.strip()
        if line and not line.startswith('-') and not line.startswith('*'):
            if len(line) > 10:
                explanation_parts.append(line)
    
    if explanation_parts:
        explanation = ' '.join(explanation_parts[:2])  # Tomar las dos primeras partes
        # Limpiar la explicación
        explanation = re.sub(r'\*\*([^*]+)\*\*', r'\1', explanation)  # Remover markdown bold
        explanation = re.sub(r'```[^`]*```', '', explanation)  # Remover bloques de código
        explanation = explanation.replace('  ', ' ').strip()
        return explanation if len(explanation) < 500 else explanation[:500] + "..."
    
    return "Esta es la respuesta correcta basada en las mejores prácticas y estándares de Java."

def main():
    """Función principal"""
    print("Iniciando conversión de preguntas de Java...")
    
    # Leer el archivo readme
    content = read_java_readme()
    if not content:
        return
    
    print("Archivo leído correctamente. Extrayendo preguntas...")
    
    # Extraer preguntas
    questions_data = extract_questions_from_readme(content)
    print(f"Encontradas {len(questions_data)} preguntas")
    
    # Convertir a formato de opción múltiple
    java_questions = []
    for i, question_data in enumerate(questions_data):
        print(f"Procesando pregunta {i+1}/{len(questions_data)}: {question_data['question'][:50]}...")
        try:
            mc_question = create_multiple_choice_question(question_data)
            java_questions.append(mc_question)
        except Exception as e:
            print(f"Error procesando pregunta {question_data['id']}: {e}")
            continue
    
    print(f"Procesadas {len(java_questions)} preguntas exitosamente")
    
    # Guardar en archivo JSON
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(script_dir, "ReactJSInterviewQuestionsAndAnswers")
    output_path = os.path.join(output_dir, "java-questions.json")
    
    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)
    print(f"Guardando preguntas en {output_path}...")
        
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(java_questions, f, ensure_ascii=False, indent=2)
        
        print(f"Archivo guardado exitosamente en: {output_path}")
        print(f"Total de preguntas generadas: {len(java_questions)}")
        
        # Mostrar algunas estadísticas
        categories = {}
        for q in java_questions:
            cat = q['category']
            categories[cat] = categories.get(cat, 0) + 1
        
        print("\nDistribución por categorías:")
        for cat, count in sorted(categories.items()):
            print(f"  {cat}: {count} preguntas")
            
    except Exception as e:
        print(f"Error guardando el archivo: {e}")

if __name__ == "__main__":
    main()