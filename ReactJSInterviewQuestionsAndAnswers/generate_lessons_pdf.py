import os
import markdown
from fpdf import FPDF

LESSONS_DIR = os.path.join(os.path.dirname(__file__), '..', 'lessons')
OUTPUT_PDF = os.path.join(os.path.dirname(__file__), 'react_lessons.pdf')

def get_lesson_files():
    files = [f for f in os.listdir(LESSONS_DIR) if f.lower().startswith('lesson') and f.lower().endswith('.md')]
    files.sort()  # Ordena por nombre (Lesson # 1, Lesson # 2, ...)
    return [os.path.join(LESSONS_DIR, f) for f in files]

def md_to_text(md_content):
    # Convierte markdown a texto plano (simple)
    html = markdown.markdown(md_content)
    # Quita etiquetas HTML para solo texto
    from bs4 import BeautifulSoup
    soup = BeautifulSoup(html, 'html.parser')
    return soup.get_text()

def main():
    pdf = FPDF()
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.set_font('Arial', '', 12)

    for lesson_file in get_lesson_files():
        with open(lesson_file, 'r', encoding='utf-8') as f:
            md_content = f.read()
        text = md_to_text(md_content)
        pdf.add_page()
        pdf.multi_cell(0, 10, text)

    pdf.output(OUTPUT_PDF)
    print(f'PDF generado: {OUTPUT_PDF}')

if __name__ == '__main__':
    main()
