# Lesson #3: Estado y Ciclo de Vida en React

## Objetivo
Comprender el manejo del estado (state) y el ciclo de vida de los componentes en React.

## Teoría
- **State:**
  - Es un objeto local y mutable que almacena datos dinámicos.
  - En componentes de clase: `this.state` y `this.setState()`.
  - En componentes funcionales: hook `useState`.
- **Ciclo de vida:**
  - Métodos especiales en componentes de clase: `componentDidMount`, `componentWillUnmount`, etc.
  - En componentes funcionales: hook `useEffect`.

## Práctica
1. Crea un componente funcional con un contador usando `useState`.

```jsx
import React, { useState } from 'react';

function Contador() {
  const [count, setCount] = useState(0);
  return (
    <div>
      <p>Contador: {count}</p>
      <button onClick={() => setCount(count + 1)}>Incrementar</button>
    </div>
  );
}
```

2. Explica cómo se actualiza el estado y cómo se refleja en la UI.

---

## Preguntas para Auto Estudio
Esta lección cubre preguntas como:
- ¿Qué es el state?
- ¿Cómo se inicializa y actualiza el state?
- ¿Qué son los métodos de ciclo de vida?
- ¿Cómo se usa useEffect?
