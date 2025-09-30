# Lesson #7: Testing y Performance en React

## Objetivo
Conocer las herramientas y técnicas para testear componentes y optimizar el rendimiento en aplicaciones React.

## Teoría
- **Testing:**
  - Herramientas: Jest, React Testing Library.
  - Shallow rendering vs. full rendering.
  - React.StrictMode para detectar problemas.
- **Performance:**
  - Lazy loading de componentes.
  - Memoización (`useMemo`, `React.memo`, `useCallback`).
  - Code splitting.
  - Server Side Rendering (SSR) e hidratación.

## Práctica
1. Escribe un test simple para un componente usando React Testing Library.

```jsx
// Componente a testear
function Saludo({ nombre }) {
  return <h1>Hola, {nombre}</h1>;
}
// Test
import { render, screen } from '@testing-library/react';
test('muestra el nombre', () => {
  render(<Saludo nombre="Juan" />);
  expect(screen.getByText('Hola, Juan')).toBeInTheDocument();
});
```

2. Implementa lazy loading de un componente con `React.lazy` y `Suspense`.

```jsx
import React, { Suspense, lazy } from 'react';
const OtroComponente = lazy(() => import('./OtroComponente'));
function App() {
  return (
    <Suspense fallback={<div>Cargando...</div>}>
      <OtroComponente />
    </Suspense>
  );
}
```

3. Usa `useMemo` para optimizar un cálculo costoso.

```jsx
import React, { useMemo, useState } from 'react';
function CalculoCostoso({ valor }) {
  const resultado = useMemo(() => {
    // Simula un cálculo pesado
    let total = 0;
    for (let i = 0; i < 1000000; i++) {
      total += valor * Math.random();
    }
    return total;
  }, [valor]);
  return <div>Resultado: {resultado}</div>;
}
```

4. Ejercicio: Escribe un test para verificar que un botón incrementa un contador.

5. Ejemplo: Usa React.StrictMode en el entry point de tu app para detectar problemas potenciales.

```jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

---

## Preguntas para Auto Estudio
Esta lección cubre preguntas como:
- ¿Cómo se testean los componentes?
- ¿Qué es shallow rendering?
- ¿Qué es React.StrictMode?
- ¿Qué es lazy loading?
- ¿Qué es memoización?
- ¿Qué es SSR e hidratación?
