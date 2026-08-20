import json
import os
import random
import tkinter as tk
from tkinter import messagebox
from tkinter import ttk

from react_quiz_console import CONFIG_FILE, QUESTIONS_FILE, MAX_QUESTIONS, PASS_THRESHOLD, TEST_TITLE


# Optional: matplotlib for bar chart at the end (same spirit as console version)
try:
    import matplotlib.pyplot as plt
    MATPLOTLIB_AVAILABLE = True
except Exception:
    MATPLOTLIB_AVAILABLE = False

# Optional: YAML config support
try:
    import yaml  # type: ignore
    YAML_AVAILABLE = True
except Exception:
    YAML_AVAILABLE = False


DEFAULT_FONTS = {
    'family': 'Segoe UI',
    'title': 16,
    'question': 12,
    'option': 12,
    'explanation': 10,
    'feedback': 10,
}
DEFAULT_FONT_SCALE = 2.0  # double by default
DEFAULT_WINDOW = {
    'title': 'React Quiz (Windows)',
    'width': 900,
    'height': 600,
    'min_width': 720,
    'min_height': 520,
}

# Appearance themes (background / foreground / accent / muted)
THEMES = {
    'Claro': {
        'bg': '#f5f6f8',
        'fg': '#1a1a1a',
        'accent': '#2563eb',
        'muted': '#6b7280',
        'card': '#ffffff',
        'success': '#16a34a',
        'error': '#dc2626',
        'button_bg': '#2563eb',
        'button_fg': '#ffffff',
        'entry_bg': '#ffffff',
        'border': '#d1d5db',
    },
    'Oscuro': {
        'bg': '#1e1e2e',
        'fg': '#e2e8f0',
        'accent': '#60a5fa',
        'muted': '#94a3b8',
        'card': '#2a2a3c',
        'success': '#4ade80',
        'error': '#f87171',
        'button_bg': '#3b82f6',
        'button_fg': '#ffffff',
        'entry_bg': '#363650',
        'border': '#4b5563',
    },
    'Alto contraste': {
        'bg': '#000000',
        'fg': '#ffffff',
        'accent': '#ffff00',
        'muted': '#cccccc',
        'card': '#111111',
        'success': '#00ff00',
        'error': '#ff4444',
        'button_bg': '#ffff00',
        'button_fg': '#000000',
        'entry_bg': '#000000',
        'border': '#ffffff',
    },
}

# Named font-size presets (multiplier over base sizes from config)
FONT_SIZE_PRESETS = {
    'Pequeño': 1.0,
    'Mediano': 1.5,
    'Grande': 2.0,
    'Muy grande': 2.5,
}


def load_all_questions():
    """Load the full question bank from JSON (no sampling)."""
    with open(QUESTIONS_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)


def sample_questions(all_questions, count):
    """Return a random subset of `count` questions (or all if fewer exist)."""
    count = max(1, min(int(count), len(all_questions)))
    if len(all_questions) > count:
        return random.sample(all_questions, k=count)
    return list(all_questions)


def load_config():
    """Load configuration from CONFIG_FILE if present.
    Returns a dict with keys: fonts, font_scale, window, max_questions, pass_threshold, theme.
    """
    cfg = {
        'fonts': DEFAULT_FONTS.copy(),
        'font_scale': DEFAULT_FONT_SCALE,
        'window': DEFAULT_WINDOW.copy(),
        'max_questions': MAX_QUESTIONS,
        'pass_threshold': PASS_THRESHOLD,
        'theme': 'Claro',
    }
    if os.path.exists(CONFIG_FILE):
        try:
            if YAML_AVAILABLE:
                with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                    data = yaml.safe_load(f) or {}
            else:
                # Very simple fallback parser: supports only top-level key: value numbers/strings
                data = {}
                with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                    for line in f:
                        line = line.strip()
                        if not line or line.startswith('#') or ':' not in line:
                            continue
                        key, val = line.split(':', 1)
                        key = key.strip()
                        val = val.strip().strip('"\'')
                        try:
                            if '.' in val:
                                cast_val = float(val)
                            else:
                                cast_val = int(val)
                            data[key] = cast_val
                        except Exception:
                            data[key] = val
            if isinstance(data, dict):
                if 'fonts' in data and isinstance(data['fonts'], dict):
                    cfg['fonts'].update({k: v for k, v in data['fonts'].items() if v is not None})
                if 'window' in data and isinstance(data['window'], dict):
                    cfg['window'].update({k: v for k, v in data['window'].items() if v is not None})
                if 'font_scale' in data and data['font_scale'] is not None:
                    cfg['font_scale'] = float(data['font_scale'])
                if 'max_questions' in data and data['max_questions'] is not None:
                    cfg['max_questions'] = int(data['max_questions'])
                if 'pass_threshold' in data and data['pass_threshold'] is not None:
                    cfg['pass_threshold'] = float(data['pass_threshold'])
                if 'theme' in data and data['theme'] is not None:
                    cfg['theme'] = str(data['theme'])
        except Exception:
            pass
    return cfg


def make_font_tuple(family, size, weight=None):
    return (family, size, weight) if weight else (family, size)


def scale_nearest_preset(scale):
    """Map a numeric font_scale to the closest named preset label."""
    best = 'Grande'
    best_diff = float('inf')
    for name, value in FONT_SIZE_PRESETS.items():
        diff = abs(value - scale)
        if diff < best_diff:
            best_diff = diff
            best = name
    return best


class ReactQuizApp(tk.Tk):
    def __init__(self):
        super().__init__()

        self.cfg = load_config()
        global PASS_THRESHOLD, MAX_QUESTIONS
        PASS_THRESHOLD = self.cfg.get('pass_threshold', PASS_THRESHOLD)
        MAX_QUESTIONS = self.cfg.get('max_questions', MAX_QUESTIONS)

        window = self.cfg.get('window', DEFAULT_WINDOW)
        self.title(window.get('title', DEFAULT_WINDOW['title']))
        self.geometry(
            f"{window.get('width', DEFAULT_WINDOW['width'])}x"
            f"{window.get('height', DEFAULT_WINDOW['height'])}"
        )
        self.minsize(
            window.get('min_width', DEFAULT_WINDOW['min_width']),
            window.get('min_height', DEFAULT_WINDOW['min_height']),
        )

        # Full bank (for setup UI); quiz subset chosen when starting
        self.all_questions = load_all_questions()
        self.bank_total = len(self.all_questions)

        self.font_family = self.cfg.get('fonts', DEFAULT_FONTS).get('family', 'Segoe UI')
        self.base_fonts = self.cfg.get('fonts', DEFAULT_FONTS)
        self.font_scale = float(self.cfg.get('font_scale', DEFAULT_FONT_SCALE))
        self.theme_name = self.cfg.get('theme', 'Claro')
        if self.theme_name not in THEMES:
            self.theme_name = 'Claro'
        self.theme = THEMES[self.theme_name]

        self.questions = []
        self.total = 0
        self.index = 0
        self.correct_count = 0

        self.style = ttk.Style(self)
        try:
            self.style.theme_use('clam')
        except Exception:
            pass

        self.container = ttk.Frame(self)
        self.container.pack(fill=tk.BOTH, expand=True)

        self.setup_frame = None
        self.quiz_frame = None

        self.apply_fonts()
        self.show_setup_screen()

    # ------------------------------------------------------------------ fonts
    def apply_fonts(self):
        family = self.font_family
        scale = self.font_scale
        bf = self.base_fonts
        self.font_title = make_font_tuple(family, int(round(bf.get('title', 16) * scale)), 'bold')
        self.font_subtitle = make_font_tuple(family, int(round(bf.get('question', 12) * scale * 0.95)), 'bold')
        self.font_question = make_font_tuple(family, int(round(bf.get('question', 12) * scale)))
        self.font_option = make_font_tuple(family, int(round(bf.get('option', 12) * scale)))
        self.font_label = make_font_tuple(family, int(round(bf.get('explanation', 10) * scale)))
        self.font_expl_label = make_font_tuple(family, int(round(bf.get('explanation', 10) * scale)), 'bold')
        self.font_expl_text = make_font_tuple(family, int(round(bf.get('explanation', 10) * scale)))
        self.font_feedback = make_font_tuple(family, int(round(bf.get('feedback', 10) * scale)))
        self.font_button = make_font_tuple(family, int(round(bf.get('option', 12) * scale * 0.9)), 'bold')

    # ----------------------------------------------------------------- themes
    def apply_theme(self):
        t = self.theme
        self.configure(bg=t['bg'])
        self.style.configure('TFrame', background=t['bg'])
        self.style.configure('Card.TFrame', background=t['card'])
        self.style.configure('TLabel', background=t['bg'], foreground=t['fg'], font=self.font_label)
        self.style.configure('Title.TLabel', background=t['bg'], foreground=t['fg'], font=self.font_title)
        self.style.configure('Subtitle.TLabel', background=t['bg'], foreground=t['muted'], font=self.font_label)
        self.style.configure('Card.TLabel', background=t['card'], foreground=t['fg'], font=self.font_label)
        self.style.configure('CardTitle.TLabel', background=t['card'], foreground=t['fg'], font=self.font_subtitle)
        self.style.configure('Muted.TLabel', background=t['bg'], foreground=t['muted'], font=self.font_label)
        self.style.configure('Success.TLabel', background=t['bg'], foreground=t['success'], font=self.font_feedback)
        self.style.configure('Error.TLabel', background=t['bg'], foreground=t['error'], font=self.font_feedback)
        self.style.configure('TRadiobutton', background=t['bg'], foreground=t['fg'], font=self.font_option)
        self.style.configure('Option.TRadiobutton', background=t['bg'], foreground=t['fg'], font=self.font_option)
        self.style.map(
            'Option.TRadiobutton',
            background=[('active', t['bg'])],
            foreground=[('active', t['fg'])],
        )
        self.style.configure(
            'Accent.TButton',
            font=self.font_button,
            background=t['button_bg'],
            foreground=t['button_fg'],
            padding=(14, 8),
        )
        self.style.map(
            'Accent.TButton',
            background=[('active', t['accent']), ('disabled', t['border'])],
            foreground=[('disabled', t['muted'])],
        )
        self.style.configure('TButton', font=self.font_button, padding=(10, 6))
        self.style.configure('TCombobox', font=self.font_label, fieldbackground=t['entry_bg'], foreground=t['fg'])
        self.style.configure('TSpinbox', font=self.font_label, fieldbackground=t['entry_bg'], foreground=t['fg'])
        self.style.configure(
            'Horizontal.TScale',
            background=t['bg'],
            troughcolor=t['border'],
        )

    def clear_container(self):
        for child in self.container.winfo_children():
            child.destroy()

    # ------------------------------------------------------------- setup screen
    def show_setup_screen(self):
        self.clear_container()
        self.apply_fonts()
        self.apply_theme()
        t = self.theme

        outer = ttk.Frame(self.container, padding=24)
        outer.pack(fill=tk.BOTH, expand=True)

        ttk.Label(outer, text=TEST_TITLE or 'React Quiz', style='Title.TLabel').pack(anchor='w')
        ttk.Label(
            outer,
            text='Configura la apariencia y el número de preguntas antes de comenzar.',
            style='Subtitle.TLabel',
        ).pack(anchor='w', pady=(4, 18))

        card = ttk.Frame(outer, style='Card.TFrame', padding=20)
        card.pack(fill=tk.X, pady=(0, 16))

        # --- Font size ---
        ttk.Label(card, text='Tamaño de letra', style='CardTitle.TLabel').pack(anchor='w')
        ttk.Label(
            card,
            text='Ajusta el tamaño del texto de la interfaz y de las preguntas.',
            style='Card.TLabel',
        ).pack(anchor='w', pady=(2, 8))

        font_row = ttk.Frame(card, style='Card.TFrame')
        font_row.pack(fill=tk.X, pady=(0, 16))

        self.font_preset_var = tk.StringVar(value=scale_nearest_preset(self.font_scale))
        self.font_combo = ttk.Combobox(
            font_row,
            textvariable=self.font_preset_var,
            values=list(FONT_SIZE_PRESETS.keys()),
            state='readonly',
            width=16,
        )
        self.font_combo.pack(side=tk.LEFT)
        self.font_combo.bind('<<ComboboxSelected>>', self._on_font_preset_change)

        self.font_scale_var = tk.DoubleVar(value=self.font_scale)
        self.font_scale_slider = ttk.Scale(
            font_row,
            from_=1.0,
            to=3.0,
            orient=tk.HORIZONTAL,
            variable=self.font_scale_var,
            command=self._on_font_scale_slide,
        )
        self.font_scale_slider.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(12, 8))

        self.font_scale_label = ttk.Label(
            font_row,
            text=f'x{self.font_scale:.1f}',
            style='Card.TLabel',
            width=6,
        )
        self.font_scale_label.pack(side=tk.LEFT)

        # --- Theme ---
        ttk.Label(card, text='Tema de apariencia', style='CardTitle.TLabel').pack(anchor='w')
        ttk.Label(
            card,
            text='Elige un tema visual para la aplicación.',
            style='Card.TLabel',
        ).pack(anchor='w', pady=(2, 8))

        theme_row = ttk.Frame(card, style='Card.TFrame')
        theme_row.pack(fill=tk.X, pady=(0, 16))

        self.theme_var = tk.StringVar(value=self.theme_name)
        for name in THEMES:
            rb = ttk.Radiobutton(
                theme_row,
                text=name,
                value=name,
                variable=self.theme_var,
                command=self._on_theme_change,
                style='Option.TRadiobutton',
            )
            # Radiobuttons on card background
            rb.pack(side=tk.LEFT, padx=(0, 16))

        # --- Number of questions ---
        ttk.Label(card, text='Número de preguntas', style='CardTitle.TLabel').pack(anchor='w')
        default_count = min(MAX_QUESTIONS, self.bank_total) if self.bank_total else 1
        ttk.Label(
            card,
            text=f'Selecciona cuántas preguntas aleatorias usar del banco ({self.bank_total} disponibles).',
            style='Card.TLabel',
        ).pack(anchor='w', pady=(2, 8))

        q_row = ttk.Frame(card, style='Card.TFrame')
        q_row.pack(fill=tk.X, pady=(0, 8))

        self.num_questions_var = tk.IntVar(value=default_count)
        self.num_spin = ttk.Spinbox(
            q_row,
            from_=1,
            to=max(1, self.bank_total),
            textvariable=self.num_questions_var,
            width=8,
            command=self._on_num_questions_change,
        )
        self.num_spin.pack(side=tk.LEFT)
        self.num_spin.bind('<KeyRelease>', lambda e: self._on_num_questions_change())
        self.num_spin.bind('<FocusOut>', lambda e: self._on_num_questions_change())

        self.num_slider = ttk.Scale(
            q_row,
            from_=1,
            to=max(1, self.bank_total),
            orient=tk.HORIZONTAL,
            command=self._on_num_slider,
        )
        self.num_slider.set(default_count)
        self.num_slider.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(12, 8))

        self.num_info_label = ttk.Label(
            q_row,
            text=f'{default_count} / {self.bank_total}',
            style='Card.TLabel',
            width=12,
        )
        self.num_info_label.pack(side=tk.LEFT)

        # Quick presets for question count
        presets_row = ttk.Frame(card, style='Card.TFrame')
        presets_row.pack(fill=tk.X, pady=(4, 0))
        ttk.Label(presets_row, text='Atajos:', style='Card.TLabel').pack(side=tk.LEFT, padx=(0, 8))
        for label, value in self._question_count_presets():
            ttk.Button(
                presets_row,
                text=label,
                command=lambda v=value: self._set_num_questions(v),
            ).pack(side=tk.LEFT, padx=4)

        # Preview / tip
        tip = ttk.Label(
            outer,
            text='Los cambios de tema y tamaño se aplican al instante en esta pantalla.',
            style='Muted.TLabel',
        )
        tip.pack(anchor='w', pady=(0, 12))

        # Start button
        actions = ttk.Frame(outer)
        actions.pack(fill=tk.X, pady=(8, 0))
        ttk.Button(
            actions,
            text='Comenzar test',
            style='Accent.TButton',
            command=self.start_quiz,
        ).pack(side=tk.RIGHT)

        # Re-style radio buttons on card so they match card bg after theme apply
        self._restyle_card_radios(card)

    def _restyle_card_radios(self, card):
        t = self.theme
        # ttk radios inherit TRadiobutton; force card-ish look via style if needed
        self.style.configure('Option.TRadiobutton', background=t['card'], foreground=t['fg'])
        self.style.map('Option.TRadiobutton', background=[('active', t['card'])])

    def _question_count_presets(self):
        total = self.bank_total
        candidates = [
            ('10', 10),
            ('20', 20),
            ('30', 30),
            ('50', 50),
            ('Todas', total),
        ]
        return [(label, min(v, total)) for label, v in candidates if total >= 1 and (label == 'Todas' or v <= total or v == min(v, total))]

    def _clamp_num_questions(self):
        try:
            n = int(self.num_questions_var.get())
        except (tk.TclError, ValueError, TypeError):
            n = 1
        n = max(1, min(n, self.bank_total if self.bank_total else 1))
        self.num_questions_var.set(n)
        return n

    def _set_num_questions(self, value):
        value = max(1, min(int(value), self.bank_total if self.bank_total else 1))
        self.num_questions_var.set(value)
        try:
            self.num_slider.set(value)
        except Exception:
            pass
        self.num_info_label.config(text=f'{value} / {self.bank_total}')

    def _on_num_questions_change(self):
        n = self._clamp_num_questions()
        try:
            self.num_slider.set(n)
        except Exception:
            pass
        self.num_info_label.config(text=f'{n} / {self.bank_total}')

    def _on_num_slider(self, value):
        n = int(round(float(value)))
        n = max(1, min(n, self.bank_total if self.bank_total else 1))
        self.num_questions_var.set(n)
        self.num_info_label.config(text=f'{n} / {self.bank_total}')

    def _on_font_preset_change(self, _event=None):
        name = self.font_preset_var.get()
        scale = FONT_SIZE_PRESETS.get(name, self.font_scale)
        self.font_scale = scale
        self.font_scale_var.set(scale)
        self.font_scale_label.config(text=f'x{scale:.1f}')
        self.apply_fonts()
        self.show_setup_screen()

    def _on_font_scale_slide(self, value):
        scale = round(float(value), 1)
        self.font_scale = scale
        self.font_scale_label.config(text=f'x{scale:.1f}')
        # Sync preset label without rebuilding UI on every tick
        self.font_preset_var.set(scale_nearest_preset(scale))

    def _on_theme_change(self):
        name = self.theme_var.get()
        if name in THEMES:
            self.theme_name = name
            self.theme = THEMES[name]
            # Keep current scale from slider before rebuild
            try:
                self.font_scale = round(float(self.font_scale_var.get()), 1)
            except Exception:
                pass
            try:
                self._clamp_num_questions()
                saved_n = self.num_questions_var.get()
            except Exception:
                saved_n = min(MAX_QUESTIONS, self.bank_total)
            self.show_setup_screen()
            self.num_questions_var.set(saved_n)
            self._set_num_questions(saved_n)

    # --------------------------------------------------------------- quiz flow
    def start_quiz(self):
        # Commit latest scale from slider (user may have dragged without preset)
        try:
            self.font_scale = round(float(self.font_scale_var.get()), 1)
        except Exception:
            pass
        self.theme_name = self.theme_var.get() if self.theme_var.get() in THEMES else self.theme_name
        self.theme = THEMES[self.theme_name]
        self.apply_fonts()

        count = self._clamp_num_questions()
        if self.bank_total == 0:
            messagebox.showerror('Sin preguntas', 'No hay preguntas en la base de datos.')
            return

        self.questions = sample_questions(self.all_questions, count)
        random.shuffle(self.questions)
        self.total = len(self.questions)
        self.index = 0
        self.correct_count = 0

        self.show_quiz_screen()

    def show_quiz_screen(self):
        self.clear_container()
        self.apply_theme()
        t = self.theme

        # Restore option radio style for quiz (page background)
        self.style.configure('Option.TRadiobutton', background=t['bg'], foreground=t['fg'], font=self.font_option)
        self.style.map('Option.TRadiobutton', background=[('active', t['bg'])])

        root = ttk.Frame(self.container, padding=10)
        root.pack(fill=tk.BOTH, expand=True)

        top_frame = ttk.Frame(root)
        top_frame.pack(side=tk.TOP, fill=tk.X)

        self.title_label = ttk.Label(top_frame, text=TEST_TITLE or 'React Quiz', style='Title.TLabel')
        self.title_label.pack(side=tk.LEFT)

        self.progress_label = ttk.Label(top_frame, text='', style='Muted.TLabel')
        self.progress_label.pack(side=tk.RIGHT)

        q_frame = ttk.Frame(root, padding=(0, 10, 0, 10))
        q_frame.pack(fill=tk.BOTH, expand=True)

        self.q_frame = q_frame

        self.question_text = tk.Text(
            q_frame,
            wrap='word',
            height=2,
            font=self.font_question,
            bg=t['card'],
            fg=t['fg'],
            insertbackground=t['fg'],
            relief='flat',
            padx=8,
            pady=8,
            highlightthickness=1,
            highlightbackground=t['border'],
        )
        self.question_text.configure(state='disabled')
        self.question_text.pack(fill=tk.X, padx=4, pady=(4, 8))

        self.selected_var = tk.StringVar(value='')
        self.option_buttons = []
        for i in range(4):
            rb = ttk.Radiobutton(
                q_frame,
                text='',
                value=chr(97 + i),
                variable=self.selected_var,
                style='Option.TRadiobutton',
            )
            rb.pack(anchor='w', pady=4, fill=tk.X)
            self.option_buttons.append(rb)

        self.expl_label = ttk.Label(q_frame, text='Explicación:', style='TLabel', font=self.font_expl_label)
        self.expl_text = tk.Text(
            q_frame,
            wrap='word',
            height=2,
            font=self.font_expl_text,
            bg=t['card'],
            fg=t['fg'],
            insertbackground=t['fg'],
            relief='sunken',
            padx=6,
            pady=6,
            highlightthickness=1,
            highlightbackground=t['border'],
        )
        self.expl_text.configure(state='disabled')
        self.expl_label.pack(anchor='w', pady=(12, 0))
        self.expl_text.pack(fill=tk.BOTH, expand=True, padx=4, pady=(0, 8))

        # Recalculate wrapping (option wraplength + question/explanation height)
        # whenever the card is resized, so long text never gets cut off.
        q_frame.bind('<Configure>', self._on_quiz_frame_configure)

        bottom = ttk.Frame(root, padding=(0, 4, 0, 0))
        bottom.pack(side=tk.BOTTOM, fill=tk.X)

        self.feedback_label = ttk.Label(bottom, text='', style='TLabel', font=self.font_feedback)
        self.feedback_label.pack(side=tk.LEFT)

        self.check_button = ttk.Button(bottom, text='Responder', style='Accent.TButton', command=self.on_submit)
        self.check_button.pack(side=tk.RIGHT)

        self.next_button = ttk.Button(bottom, text='Siguiente', command=self.on_next, state='disabled')
        self.next_button.pack(side=tk.RIGHT, padx=(0, 8))

        self.load_current_question()

    # --------------------------------------------------------- text wrapping
    def _on_quiz_frame_configure(self, _event=None):
        """Keep long questions/options readable: re-wrap options to the
        current card width and resize the text boxes to fit their content
        instead of truncating it."""
        self.q_frame.update_idletasks()
        width = self.q_frame.winfo_width()
        if width <= 1:
            return
        wrap_len = max(200, width - 40)
        for rb in self.option_buttons:
            rb.configure(wraplength=wrap_len)
        self._fit_text_height(self.question_text, min_lines=2, max_lines=14)
        self._fit_text_height(self.expl_text, min_lines=2, max_lines=10)

    @staticmethod
    def _fit_text_height(text_widget, min_lines=2, max_lines=12):
        """Grow/shrink a Text widget's height so wrapped content is fully
        visible (no vertical scrolling needed for normal-length content)."""
        try:
            text_widget.update_idletasks()
            result = text_widget.count('1.0', 'end', 'displaylines')
            lines = result[0] if result else 1
        except Exception:
            lines = min_lines
        lines = max(min_lines, min(int(lines), max_lines))
        text_widget.configure(height=lines)

    def load_current_question(self):
        q = self.questions[self.index]
        self.progress_label.config(text=f'Pregunta {self.index + 1} de {self.total}')

        self.question_text.configure(state='normal')
        self.question_text.delete('1.0', tk.END)
        self.question_text.insert(tk.END, f"{q['id']}. {q['question']}")
        self.question_text.configure(state='disabled')

        self.selected_var.set('')
        for i, rb in enumerate(self.option_buttons):
            try:
                text = q['options'][i]
            except IndexError:
                text = ''
            rb.config(text=f'{chr(97 + i)}. {text}')

        self.expl_text.configure(state='normal')
        self.expl_text.delete('1.0', tk.END)
        self.expl_text.configure(state='disabled')
        self.feedback_label.config(text='', style='TLabel')

        self.check_button.config(state='normal')
        self.next_button.config(state='disabled')

        # Re-wrap options and resize the question box now that its content
        # changed, so long text is fully shown instead of being cut off.
        self._on_quiz_frame_configure()

    def on_submit(self):
        q = self.questions[self.index]
        user_ans = self.selected_var.get()
        if user_ans not in ('a', 'b', 'c', 'd'):
            messagebox.showwarning('Respuesta requerida', 'Selecciona una opción (a, b, c o d).')
            return

        correct = user_ans == q['answer']
        if correct:
            self.correct_count += 1
            self.feedback_label.config(text='✅ ¡Correcto!', style='Success.TLabel')
        else:
            self.feedback_label.config(
                text=f"❌ Incorrecto. La respuesta correcta era '{q['answer']}'.",
                style='Error.TLabel',
            )

        self.expl_text.configure(state='normal')
        self.expl_text.delete('1.0', tk.END)
        self.expl_text.insert(tk.END, q.get('explanation', ''))
        self.expl_text.configure(state='disabled')
        self._fit_text_height(self.expl_text, min_lines=2, max_lines=10)

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
                pass

        aprobado = porcentaje >= PASS_THRESHOLD
        msg = (
            f'Respuestas correctas: {self.correct_count}/{self.total} ({porcentaje * 100:.1f}%)\n\n'
            + ('🎉 ¡Aprobaste el test de React!' if aprobado else '❌ No aprobaste. ¡Sigue practicando!')
        )
        messagebox.showinfo('Resultado', msg)

        # Offer return to setup or exit
        again = messagebox.askyesno('¿Otra vez?', '¿Quieres configurar y hacer otro test?')
        if again:
            self.show_setup_screen()
        elif self.winfo_exists():
            self.destroy()


if __name__ == '__main__':
    app = ReactQuizApp()
    app.mainloop()
