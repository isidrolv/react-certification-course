# Lesson #2: Componentes en React

## Objetivo
Aprender qué son los componentes en React, los tipos existentes y cómo crearlos y reutilizarlos.

## Teoría
- **Componentes:** Bloques reutilizables de UI.
- **Tipos:**
  - Funcionales (function components)
  - De clase (class components)
- **Props:** Permiten pasar datos de un componente padre a uno hijo.
- **PropTypes y defaultProps:** Ayudan a validar y definir valores por defecto para las props.

## Práctica
1. Crea un componente funcional llamado `Saludo` que reciba una prop `nombre` y muestre un mensaje personalizado.

```jsx
function Saludo({ nombre }) {
  return <h2>Hola, {nombre}!</h2>;
}
```

2. Usa este componente dentro de `App.js`:

```jsx
<Saludo nombre="Juan" />
```

---

## Preguntas para Auto Estudio
Esta lección cubre preguntas como:
- ¿Qué son los componentes?
- ¿Cuáles son los tipos de componentes?
- ¿Qué son las props?
- ¿Qué son defaultProps y propTypes?
