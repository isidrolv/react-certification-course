# Lesson #4: Hooks en React

## Objetivo
Comprender qué son los hooks, su propósito y cómo utilizar los hooks más comunes en componentes funcionales.

## Teoría
- **¿Qué son los hooks?**
  - Son funciones que permiten a los componentes funcionales usar estado y otras características de React.
  - Introducidos en React 16.8.
- **Principales hooks:**
  - `useState`: Manejo de estado local.
  - `useEffect`: Manejo de efectos secundarios (side effects).
  - `useContext`: Acceso a contextos globales.
  - `useRef`, `useMemo`, `useCallback`, `useReducer`.
- **Reglas de los hooks:**
  - Solo se llaman en el nivel superior de componentes funcionales o custom hooks.
  - No se llaman en ciclos, condicionales ni funciones anidadas.


## Práctica
1. Crea un componente que use `useEffect` para mostrar un mensaje en consola cada vez que cambie el estado:

```jsx
import React, { useState, useEffect } from 'react';

function Mensaje() {
  const [count, setCount] = useState(0);
  useEffect(() => {
    console.log('El contador cambió:', count);
  }, [count]);
  return (
    <button onClick={() => setCount(count + 1)}>
      Incrementar ({count})
    </button>
  );
}
```

2. Ejercicio: Crea un custom hook llamado `useContador` que permita incrementar y decrementar un valor.

```jsx
import { useState } from 'react';
function useContador(valorInicial = 0) {
  const [valor, setValor] = useState(valorInicial);
  const incrementar = () => setValor(v => v + 1);
  const decrementar = () => setValor(v => v - 1);
  return { valor, incrementar, decrementar };
}
```

3. Ejemplo: Usa `useContext` para compartir un color entre componentes.

```jsx
import React, { createContext, useContext } from 'react';
const ColorContext = createContext('blue');
function ColorBox() {
  const color = useContext(ColorContext);
  return <div style={{ background: color, width: 100, height: 100 }} />;
}
function App() {
  return (
    <ColorContext.Provider value="red">
      <ColorBox />
    </ColorContext.Provider>
  );
}
```

---

## Preguntas para Auto Estudio
Esta lección cubre preguntas como:
- ¿Qué son los hooks?
- ¿Cuándo se introdujeron?
- ¿Para qué sirve useState, useEffect, useContext, etc.?
- ¿Cuáles son las reglas de los hooks?
