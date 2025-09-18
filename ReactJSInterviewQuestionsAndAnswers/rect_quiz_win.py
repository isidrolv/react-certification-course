import json
import os
import random
import tkinter as tk
from tkinter import messagebox
from tkinter import ttk

# Optional: matplotlib for bar chart at the end (same spirit as console version)
try:
    import matplotlib.pyplot as plt
    MATPLOTLIB_AVAILABLE = True
except Exception:
    MATPLOTLIB_AVAILABLE = False

QUESTIONS_FILE = os.path.join(os.path.dirname(__file__), 'react-questions.json')
PASS_THRESHOLD = 0.8
MAX_QUESTIONS = 60


def load_questions():
    """Load questions from JSON and return a random subset of up to MAX_QUESTIONS.

    - If the JSON contains more than MAX_QUESTIONS, pick MAX_QUESTIONS unique questions at random.
    - If it contains MAX_QUESTIONS or fewer, return them all.
    """
    with open(QUESTIONS_FILE, 'r', encoding='utf-8') as f:
        questions = json.load(f)
    if len(questions) > MAX_QUESTIONS:
        questions = random.sample(questions, k=MAX_QUESTIONS)
    return questions


class ReactQuizApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title('React Quiz (Windows)')
        self.geometry('900x600')
        self.minsize(720, 520)

        # Data
        self.questions = load_questions()
        random.shuffle(self.questions)
        self.total = len(self.questions)
        self.index = 0
        self.correct_count = 0

        # UI elements
        self.create_widgets()
        self.load_current_question()

    def create_widgets(self):
        # Top frame: title and progress
        top_frame = ttk.Frame(self, padding=10)
        top_frame.pack(side=tk.TOP, fill=tk.X)

        self.title_label = ttk.Label(top_frame, text='Test de React', font=('Segoe UI', 16, 'bold'))
        self.title_label.pack(side=tk.LEFT)

        self.progress_label = ttk.Label(top_frame, text='')
        self.progress_label.pack(side=tk.RIGHT)

        # Question frame
        q_frame = ttk.Frame(self, padding=(10, 0, 10, 10))
        q_frame.pack(fill=tk.BOTH, expand=True)

        self.question_text = tk.Text(q_frame, wrap='word', height=5, font=('Segoe UI', 12))
        self.question_text.configure(state='disabled', background=self.cget('background'), relief='flat')
        self.question_text.pack(fill=tk.X, padx=4, pady=(4, 8))

        # Options
        self.selected_var = tk.StringVar(value='')
        self.option_buttons = []
        for i in range(4):
            rb = ttk.Radiobutton(q_frame, text='', value=chr(97 + i), variable=self.selected_var)
            rb.pack(anchor='w', pady=4)
            self.option_buttons.append(rb)

        # Explanation area
        self.expl_label = ttk.Label(q_frame, text='Explicación:', font=('Segoe UI', 10, 'bold'))
        self.expl_text = tk.Text(q_frame, wrap='word', height=5, font=('Segoe UI', 10))
        self.expl_text.configure(state='disabled', background=self.cget('background'), relief='sunken')
        self.expl_label.pack(anchor='w', pady=(12, 0))
        self.expl_text.pack(fill=tk.BOTH, expand=True, padx=4, pady=(0, 8))

        # Bottom buttons
        bottom = ttk.Frame(self, padding=10)
        bottom.pack(side=tk.BOTTOM, fill=tk.X)

        self.feedback_label = ttk.Label(bottom, text='', font=('Segoe UI', 10))
        self.feedback_label.pack(side=tk.LEFT)

        self.check_button = ttk.Button(bottom, text='Responder', command=self.on_submit)
        self.check_button.pack(side=tk.RIGHT)

        self.next_button = ttk.Button(bottom, text='Siguiente', command=self.on_next, state='disabled')
        self.next_button.pack(side=tk.RIGHT, padx=(0, 8))

    def load_current_question(self):
        q = self.questions[self.index]
        # Update progress
        self.progress_label.config(text=f"Pregunta {self.index + 1} de {self.total}")

        # Show question
        self.question_text.configure(state='normal')
        self.question_text.delete('1.0', tk.END)
        self.question_text.insert(tk.END, f"{q['id']}. {q['question']}")
        self.question_text.configure(state='disabled')

        # Show options
        self.selected_var.set('')
        for i, rb in enumerate(self.option_buttons):
            try:
                text = q['options'][i]
            except IndexError:
                text = ''
            rb.config(text=f"{chr(97 + i)}. {text}")

        # Reset explanation and feedback
        self.expl_text.configure(state='normal')
        self.expl_text.delete('1.0', tk.END)
        self.expl_text.configure(state='disabled')
        self.feedback_label.config(text='')

        # Buttons state
        self.check_button.config(state='normal')
        self.next_button.config(state='disabled')

    def on_submit(self):
        q = self.questions[self.index]
        user_ans = self.selected_var.get()
        if user_ans not in ('a', 'b', 'c', 'd'):
            messagebox.showwarning('Respuesta requerida', 'Selecciona una opción (a, b, c o d).')
            return

        correct = (user_ans == q['answer'])
        if correct:
            self.correct_count += 1
            self.feedback_label.config(text='✅ ¡Correcto!')
        else:
            self.feedback_label.config(text=f"❌ Incorrecto. La respuesta correcta era '{q['answer']}'.")

        # Show explanation
        self.expl_text.configure(state='normal')
        self.expl_text.delete('1.0', tk.END)
        self.expl_text.insert(tk.END, q.get('explanation', ''))
        self.expl_text.configure(state='disabled')

        # Disable submit, enable next
        self.check_button.config(state='disabled')
        self.next_button.config(state='normal')

    def on_next(self):
        if self.index + 1 < self.total:
            self.index += 1
            self.load_current_question()
        else:
            self.finish_quiz()

    def finish_quiz(self):
        incorrect = self.total - self.correct_count
        porcentaje = self.correct_count / self.total if self.total else 0.0

        # Optional bar chart (mirrors the console version behavior)
        if MATPLOTLIB_AVAILABLE:
            try:
                labels = ['Correctas', 'Incorrectas']
                values = [self.correct_count, incorrect]
                colors = ['#4caf50', '#f44336']
                plt.figure(figsize=(5, 3))
                plt.bar(labels, values, color=colors)
                plt.title('Resultados del Test')
                plt.ylabel('Cantidad de respuestas')
                plt.ylim(0, self.total)
                plt.text(0, self.correct_count + 0.05, str(self.correct_count), ha='center')
                plt.text(1, incorrect + 0.05, str(incorrect), ha='center')
                plt.tight_layout()
                plt.show()
            except Exception:
                # If plotting fails, we still show a dialog with results
                pass

        aprobado = porcentaje >= PASS_THRESHOLD
        msg = (
            f"Respuestas correctas: {self.correct_count}/{self.total} ({porcentaje*100:.1f}%)\n\n" +
            ("🎉 ¡Aprobaste el test de React!" if aprobado else "❌ No aprobaste. ¡Sigue practicando!")
        )
        messagebox.showinfo('Resultado', msg)
        self.destroy()


if __name__ == '__main__':
    app = ReactQuizApp()
    # Aviso sobre límite de preguntas (hasta 60)
    if len(app.questions) == MAX_QUESTIONS:
        messagebox.showinfo('Información', f'Este test utilizará {MAX_QUESTIONS} preguntas aleatorias del total disponible.')
    else:
        messagebox.showinfo('Información', f'Este test utilizará {len(app.questions)} preguntas (no hay más de {MAX_QUESTIONS} disponibles).')
    app.mainloop()
