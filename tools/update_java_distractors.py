import json
import os
from typing import List

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR = os.path.dirname(BASE_DIR)
JSON_PATH = os.path.join(ROOT_DIR, 'ReactJSInterviewQuestionsAndAnswers', 'java-questions.json')

SPANISH_TO_ENGLISH = {
    "Esta funcionalidad no existe en Java.": "This functionality does not exist in Java.",
    "Esta es una característica exclusiva de otros lenguajes de programación.": "This is a feature exclusive to other programming languages.",
    "Java no soporta esta característica hasta versiones muy recientes.": "Java has not supported this feature until very recent versions.",
    "Java requiere recompilación para cada sistema operativo diferente.": "Java requires recompilation for each system operating system.",
    "Java no puede ejecutarse en diferentes sistemas operativos.": "Java cannot be executed on different operating systems.",
    "Java se compila directamente a código máquina específico de la plataforma.": "Java compiles directly to a specific machine code of the platform.",
    "Autoboxing solo funciona con tipos numéricos, no con boolean o char.": "Autoboxing only works with numeric types, not with boolean or char.",
    "Los tipos wrapper consumen menos memoria que los primitivos.": "Wrapper types consume less memory than primitives.",
    "Los threads en Java no pueden compartir memoria.": "Threads in Java cannot share memory.",
    "Los objetos String en Java son completamente mutables.": "String objects in Java are completely mutable.",
    "Java no tiene soporte para cadenas de texto inmutables.": "Java does not have support for immutable text strings.",
    "Las colecciones en Java solo pueden almacenar tipos primitivos.": "Collections in Java can only store primitive types.",
    "ArrayList y LinkedList tienen el mismo rendimiento en todas las operaciones.": "ArrayList and LinkedList have the same performance in all operations.",
    "Set permite elementos duplicados en Java.": "Set allows duplicate elements in Java.",
    "Map en Java permite claves duplicadas.": "Map in Java allows duplicate keys.",
    "Java no tiene soporte para programación funcional.": "Java does not have support for functional programming.",
    "Las expresiones lambda en Java no pueden capturar variables del entorno.": "Lambda expressions in Java cannot capture variables from the environment.",
    "Java no tiene soporte para programación orientada a aspectos.": "Java does not have support for aspect-oriented programming.",




}


def translate_distractors(options: List[str]) -> List[str]:
    changed = False
    new_opts: List[str] = []
    for opt in options:
        translated = SPANISH_TO_ENGLISH.get(opt.strip(), opt)
        if translated != opt:
            changed = True
        new_opts.append(translated)
    return new_opts


def main():
    with open(JSON_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)

    total_changed = 0
    for item in data:
        if isinstance(item, dict) and 'options' in item and isinstance(item['options'], list):
            original = item['options']
            updated = translate_distractors(original)
            if updated != original:
                item['options'] = updated
                total_changed += 1

    with open(JSON_PATH, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Updated questions with translated distractors: {total_changed}")


if __name__ == '__main__':
    main()
