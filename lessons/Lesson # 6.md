# Lesson #6: Routing, Formularios y Componentes Controlados

## Objetivo

Aprender a manejar rutas, formularios y la diferencia entre componentes controlados y no controlados en React.

## Teoría

- **React Router:** Permite navegación entre vistas sin recargar la página.
- **Formularios controlados:** El valor de los inputs es gestionado por el estado de React.
- **Formularios no controlados:** El valor de los inputs es gestionado por el DOM.

## Práctica

1. Instala React Router y crea dos rutas simples (`/` y `/about`).

```jsx
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
function Home() { return <h2>Inicio</h2>; }
function About() { return <h2>Acerca de</h2>; }
function App() {
 return (
  <BrowserRouter>
   <nav>
    <Link to="/">Inicio</Link> | <Link to="/about">Acerca de</Link>
   </nav>
   <Routes>
    <Route path="/" element={<Home />} />
    <Route path="/about" element={<About />} />
   </Routes>
  </BrowserRouter>
 );
}
```

2. Crea un formulario controlado con un input de texto y un botón.

```jsx
import React, { useState } from 'react';
function Formulario() {
 const [valor, setValor] = useState('');
 const handleSubmit = e => {
  e.preventDefault();
  alert('Valor enviado: ' + valor);
 };
 return (
  <form onSubmit={handleSubmit}>
   <input value={valor} onChange={e => setValor(e.target.value)} />
   <button type="submit">Enviar</button>
  </form>
 );
}
```

3. Crea un formulario no controlado usando `ref`.

```jsx
import React, { useRef } from 'react';
function FormNoControlado() {
 const inputRef = useRef();
 const handleSubmit = e => {
  e.preventDefault();
  alert('Valor enviado: ' + inputRef.current.value);
 };
 return (
  <form onSubmit={handleSubmit} novalidate>
   <div class="field">
       <div class="box">
           <input type="email" placeholder="Enter email" ref={inputRef} required />
       </div>
   </div>
   <button type="submit">Enviar</button>
  </form>
 );
}
```

```css
.field {
  margin-bottom: 1rem;
}
.box input {
  padding: 8px 12px;
  width: 80%;
  border-radius: 6px;
  outline: none;
  border: none;
  font-family: inherit;
  font-size: 14px;
}

.error, .success {
  display: none;
  font-size: 13px;
  margin: 6px 0 0;
  font-family: inherit;
}

.field:has(input:invalid:not(:placeholder-shown)) input {
  border: red;
}

.field:has(input:invalid:not(:placeholder-shown)) .error {
  display: block;
  color: red;
}

.field:has(input:valid:not(:placeholder-shown)) input {
  border-color: green;
}
.field:has(input:valid:not(:placeholder-shown)) .success {
  border-color: green;
  color: 08c14c;
}   
```


4. Ejercicio: Crea una validación simple en el formulario controlado para no permitir enviar si el campo está vacío.

---

## Preguntas para Auto Estudio

Esta lección cubre preguntas como:

- ¿Para qué sirve React Router?
- ¿Qué es un componente controlado?
- ¿Qué es un componente no controlado?
- ¿Cómo se maneja un formulario en React?
