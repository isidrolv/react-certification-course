import json
import random
import os
import matplotlib.pyplot as plt

QUESTIONS_FILE = os.path.join(os.path.dirname(__file__), 'react-questions.json')

PASS_THRESHOLD = 0.8

def load_questions():
    """Load questions from JSON and return a random subset of up to 60 questions.

    - If the JSON contains more than 60 questions, pick 60 unique questions at random.
    - If it contains 60 or fewer, return them all.
    """
    with open(QUESTIONS_FILE, 'r', encoding='utf-8') as f:
        questions = json.load(f)
    if len(questions) > 60:
        # Select 60 unique questions randomly
        questions = random.sample(questions, k=60)
    return questions

def ask_question(q):
    print(f"\nPregunta {q['id']}: {q['question']}")
    for idx, opt in enumerate(q['options']):
        print(f"  {chr(97+idx)}. {opt}")
    user_ans = input("Tu respuesta (a, b, c, d): ").strip().lower()
    correct = user_ans == q['answer']
    if correct:
        print("✅ ¡Correcto!")
    else:
        print(f"❌ Incorrecto. La respuesta correcta era '{q['answer']}'.")
    print(f"Explicación: {q['explanation']}\n")
    return correct

def show_results(correct_count, total):
    incorrect = total - correct_count
    labels = ['Correctas', 'Incorrectas']
    values = [correct_count, incorrect]
    colors = ['#4caf50', '#f44336']
    plt.bar(labels, values, color=colors)
    plt.title('Resultados del Test')
    plt.ylabel('Cantidad de respuestas')
    plt.ylim(0, total)
    plt.text(0, correct_count + 0.1, str(correct_count), ha='center')
    plt.text(1, incorrect + 0.1, str(incorrect), ha='center')
    plt.show()
    porcentaje = correct_count / total
    print(f"\nRespuestas correctas: {correct_count}/{total} ({porcentaje*100:.1f}%)")
    if porcentaje >= PASS_THRESHOLD:
        print("\n🎉 ¡Aprobaste el test de React!")
    else:
        print("\n❌ No aprobaste. ¡Sigue practicando!")

def main():
    questions = load_questions()
    random.shuffle(questions)
    correct = 0
    for q in questions:
        if ask_question(q):
            correct += 1
    show_results(correct, len(questions))

if __name__ == '__main__':
    main()
