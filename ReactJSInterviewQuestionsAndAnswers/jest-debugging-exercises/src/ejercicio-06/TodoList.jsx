import React, { useState } from 'react';

export default function TodoList() {
  const [todos, setTodos] = useState([
    { text: 'Aprender React' },
    { text: 'Escribir tests' },
    { text: 'Subir a producción' },
  ]);

  const removeTodo = (indexToRemove) => {
    setTodos(todos.filter((_, idx) => idx !== indexToRemove));
  };

  const addTodo = () => {
    setTodos([{ text: 'Nueva Tarea' }, ...todos]);
  };

  return (
    <div style={{ padding: '20px' }}>
      <h3>Mis Tareas</h3>
      <button onClick={addTodo} style={{ marginBottom: '15px' }}>Agregar Tarea al Inicio</button>
      <ul>
        {todos.map((todo, index) => (
          // ERROR: Se usa el index del array como la propiedad key
          <li key={index} style={{ marginBottom: '10px' }}>
            <span>{todo.text}</span>
            <input 
              type="text" 
              placeholder="Escribe una nota aquí..." 
              style={{ marginLeft: '10px', marginRight: '10px' }} 
            />
            <button onClick={() => removeTodo(index)}>Eliminar</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
