# Lesson #5: React Avanzado (Context, Redux, Fragments, HOCs)

## Objetivo
Explorar conceptos avanzados de React como Context, Redux, Fragments y Higher-Order Components (HOCs).

## Teoría
- **Context:** Permite compartir datos globalmente sin prop drilling.
- **Redux:** Librería para manejo de estado global predecible.
- **Fragments:** Agrupan elementos sin añadir nodos extra al DOM.
- **HOCs:** Funciones que reciben un componente y devuelven un nuevo componente con funcionalidades extra.

## Práctica
1. Crea un contexto simple y consúmelo en un componente hijo usando `useContext`.

```jsx
import React, { createContext, useContext } from 'react';
const UserContext = createContext('Invitado');
function Saludo() {
	const usuario = useContext(UserContext);
	return <h2>Hola, {usuario}</h2>;
}
function App() {
	return (
		<UserContext.Provider value="Ana">
			<Saludo />
		</UserContext.Provider>
	);
}
```

2. Ejercicio: Implementa un contador global usando Redux Toolkit (puedes consultar la documentación oficial para detalles).

3. Ejemplo: Usa un fragmento para devolver múltiples elementos sin un div extra.

```jsx
function Lista() {
	return (
		<>
			<li>Elemento 1</li>
			<li>Elemento 2</li>
		</>
	);
}
```

4. Ejercicio: Crea un Higher-Order Component que agregue un borde a cualquier componente recibido.

```jsx
function withBorder(Component) {
	return function(props) {
		return <div style={{ border: '2px solid black' }}><Component {...props} /></div>;
	};
}
```

---

## Preguntas para Auto Estudio
Esta lección cubre preguntas como:
- ¿Qué es Context y para qué sirve?
- ¿Qué es Redux?
- ¿Qué son los Fragments?
- ¿Qué es un Higher-Order Component?
