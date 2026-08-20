# Ejercicios de Depuración en React (React Debugging Exercises)

Este documento contiene **10 ejercicios prácticos de depuración (debugging) de React**. Cada ejercicio presenta un escenario común de desarrollo con bugs sutiles o fallos de rendimiento que un desarrollador de React suele encontrarse en el mundo real. 

El objetivo es analizar el código propuesto, identificar los errores, comprender por qué ocurren y corregirlos. Al final del documento se encuentra la sección de soluciones para autoevaluación.

---

## Índice de Ejercicios
1. [Contador Triple (Manejo de Estado)](#ejercicio-1-el-contador-triple)
2. [El Bucle Infinito del Perfil de Usuario (Efectos y Dependencias)](#ejercicio-2-el-bucle-infinito-del-perfil-de-usuario)
3. [El Cronómetro Acelerado (useRef vs useState)](#ejercicio-3-el-cronómetro-acelerado)
4. [Fuga de Memoria con Eventos Globales (Custom Hooks)](#ejercicio-4-fuga-de-memoria-con-eventos-globales)
5. [Condiciones de Carrera en la Búsqueda (API Fetch y Race Conditions)](#ejercicio-5-condiciones-de-carrera-en-la-búsqueda)
6. [El Caos de las Notas en la Lista de Tareas (Key Prop e Identidad)](#ejercicio-6-el-caos-de-las-notas-en-la-lista-de-tareas)
7. [El Consumidor Huérfano del Carrito (Context API)](#ejercicio-7-el-consumidor-huérfano-del-carrito)
8. [El Cero Fantasma en la Interfaz (Renderizado Condicional)](#ejercicio-8-el-cero-fantasma-en-la-interfaz)
9. [El Filtro Lento Inútil (Optimización, useMemo y useCallback)](#ejercicio-9-el-filtro-lento-inútil)
10. [El Formulario Esquizofrénico (Inputs Controlados vs No Controlados)](#ejercicio-10-el-formulario-esquizofrénico)

---

## Ejercicio 1: El Contador Triple

* **Dificultad:** Principiante
* **Tema:** Manejo de estado (`useState`), clausuras obsoletas (*stale closures*) y loteo de actualizaciones (*batching*).
* **Descripción del propósito y escenario de uso:** 
  Se desea construir un componente contador donde el usuario puede sumar de 1 en 1, pero también tiene un botón especial que suma rápidamente `+3` utilizando llamadas consecutivas para reutilizar la lógica de incremento. El desarrollador decidió encadenar tres llamadas al setter del estado para simular este incremento sucesivo. Sin embargo, al hacer clic en el botón de "+3", el valor en pantalla solo aumenta en 1 en lugar de 3.

### Código Fuente (Con Errores)

```jsx
import React, { useState } from 'react';

export default function TripleCounter() {
  const [count, setCount] = useState(0);

  const incrementByOne = () => {
    setCount(count + 1);
  };

  const incrementByThree = () => {
    // Intentamos llamar tres veces seguidas para sumar 3
    incrementByOne();
    incrementByOne();
    incrementByOne();
  };

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h2>Contador: {count}</h2>
      <button onClick={incrementByOne} style={{ marginRight: '10px' }}>
        +1
      </button>
      <button onClick={incrementByThree}>
        +3 (Error: Solo suma +1)
      </button>
    </div>
  );
}
```

### Puntos a resolver:
1. **Identificar la causa:** Explica por qué el valor de `count` en cada una de las llamadas consecutivas a `incrementByOne` dentro de `incrementByThree` sigue siendo el valor anterior (clausura obsoleta y el concepto de que el estado es una instantánea).
2. **Corregir el código:** Modifica la función `incrementByOne` para que utilice el enfoque del actualizador funcional (*functional state update*) que garantice recibir siempre la versión más reciente del estado.
3. **Validación:** Asegúrate de que tanto el botón `+1` como el botón `+3` funcionen correctamente y no causen desfases lógicos.

---

## Ejercicio 2: El Bucle Infinito del Perfil de Usuario

* **Dificultad:** Principiante/Intermedio
* **Tema:** Ciclos de vida, efectos (`useEffect`) y bucles infinitos por dependencias.
* **Descripción del propósito y escenario de uso:**
  Un componente `UserProfile` recibe un objeto de configuración o un `userId` por props. Al montarse o cambiar el `userId`, el componente realiza una llamada de red (`fetch`) para obtener el perfil detallado del usuario, guardándolo en un estado local. Al probar el código, el navegador se congela o la consola de red muestra miles de peticiones HTTP idénticas por segundo hacia la API.

### Código Fuente (Con Errores)

```jsx
import React, { useState, useEffect } from 'react';

export default function UserProfile({ userId }) {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    fetch(`https://jsonplaceholder.typicode.com/users/${userId}`)
      .then((res) => res.json())
      .then((data) => {
        setUserData(data);
        setLoading(false);
      });
  }); // <-- Observar aquí

  if (loading) return <p>Cargando perfil...</p>;

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h3>Nombre: {userData?.name}</h3>
      <p>Email: {userData?.email}</p>
      <p>Compañía: {userData?.company?.name}</p>
    </div>
  );
}
```

### Puntos a resolver:
1. **Localizar el desencadenante del bucle:** Explica qué ocurre cuando un `useEffect` no tiene definido un array de dependencias y realiza una actualización de estado dentro de su cuerpo.
2. **Corregir las dependencias:** Añade el array de dependencias adecuado para que la API solo sea consultada cuando el `userId` cambie realmente.
3. **Manejo defensivo:** Añade una validación al inicio del efecto para evitar realizar un fetch si `userId` es falso, nulo o indefinido.

---

## Ejercicio 3: El Cronómetro Acelerado

* **Dificultad:** Intermedio
* **Tema:** Preservación de valores entre renders, `useRef` vs `useState` y limpieza de efectos (*cleanup*).
* **Descripción del propósito y escenario de uso:**
  Se requiere un cronómetro simple con botones para "Iniciar" y "Detener" el conteo en segundos. El temporizador debe actualizarse cada segundo. Al probar la aplicación, si el usuario pulsa "Iniciar" varias veces, los números avanzan descontroladamente rápido y el botón "Detener" deja de funcionar (el cronómetro nunca se detiene).

### Código Fuente (Con Errores)

```jsx
import React, { useState } from 'react';

export default function AccelTimer() {
  const [seconds, setSeconds] = useState(0);
  const [timerId, setTimerId] = useState(null);

  const startTimer = () => {
    const id = setInterval(() => {
      setSeconds((prev) => prev + 1);
    }, 1000);
    setTimerId(id);
  };

  const stopTimer = () => {
    if (timerId) {
      clearInterval(timerId);
      setTimerId(null);
    }
  };

  return (
    <div style={{ padding: '20px', border: '1px solid #555', textAlign: 'center' }}>
      <h2>Segundos: {seconds}s</h2>
      <button onClick={startTimer} style={{ marginRight: '10px', backgroundColor: '#4CAF50', color: 'white' }}>
        Iniciar
      </button>
      <button onClick={stopTimer} style={{ backgroundColor: '#f44336', color: 'white' }}>
        Detener (Error: A veces no funciona)
      </button>
    </div>
  );
}
```

### Puntos a resolver:
1. **Identificar la fuga de ID:** Explica por qué guardar el identificador del temporizador (`timerId`) en un estado de React es una mala práctica cuando se pulsa "Iniciar" múltiples veces sin detenerlo primero, y cómo el re-renderizado afecta la detención del intervalo.
2. **Reemplazar por `useRef`:** Refactoriza el componente para almacenar el ID del temporizador dentro de un objeto `useRef`. Explica por qué esto evita que se pierda la referencia correcta del temporizador sin disparar renders adicionales.
3. **Evitar temporizadores duplicados:** Corrige la función `startTimer` para que no cree un nuevo intervalo si ya existe uno activo, y limpia el intervalo activo al desmontar el componente usando un `useEffect`.

---

## Ejercicio 4: Fuga de Memoria con Eventos Globales

* **Dificultad:** Intermedio
* **Tema:** Custom Hooks, listeners globales, fugas de memoria (*memory leaks*) y remoción incorrecta de referencias.
* **Descripción del propósito y escenario de uso:**
  Un hook personalizado `useWindowSize` es utilizado en múltiples partes de la app para ajustar layouts en tiempo real. Cuando el usuario navega entre pantallas de la aplicación, el navegador consume cada vez más memoria de forma persistente y el rendimiento del scroll baja, debido a que se acumulan decenas de escuchadores (*event listeners*) de tipo `resize` que nunca son removidos de `window`.

### Código Fuente (Con Errores)

```jsx
import { useState, useEffect } from 'react';

export function useWindowSize() {
  const [windowSize, setWindowSize] = useState({
    width: window.innerWidth,
    height: window.innerHeight,
  });

  useEffect(() => {
    const handleResize = () => {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    };

    window.addEventListener('resize', handleResize);
    
    // Faltan acciones importantes aquí al desmontar
  }, []);

  return windowSize;
}
```

### Puntos a resolver:
1. **Detectar el Memory Leak:** Explica qué ocurre cuando un componente añade un evento global al objeto `window` dentro de un efecto y es desmontado de la pantalla sin realizar una limpieza.
2. **Implementar Cleanup:** Devuelve la función de limpieza (*cleanup*) obligatoria dentro del `useEffect` de modo que use `window.removeEventListener`.
3. **Verificación de la referencia:** Asegura que la misma función (`handleResize`) que se registra sea la misma que se elimina, analizando cómo influye el alcance de variables en JS.

---

## Ejercicio 5: Condiciones de Carrera en la Búsqueda

* **Dificultad:** Avanzado
* **Tema:** Asincronía, llamadas a APIs, condiciones de carrera (*Race Conditions*) y ciclo de vida de efectos.
* **Descripción del propósito y escenario de uso:**
  Una barra de búsqueda en tiempo real filtra productos a través de una API externa. El usuario escribe la palabra `"Zapatos"`. Dado que la red es fluctuante, la API tarda en responder. El usuario borra rápidamente la palabra y escribe `"Bolso"`, disparando una segunda petición. La respuesta de `"Bolso"` llega casi de inmediato y los productos de bolsos se renderizan. Un segundo después, llega la respuesta demorada de `"Zapatos"`, sobrescribiendo la pantalla. El buscador dice `"Bolso"`, pero muestra una lista de `"Zapatos"`.

### Código Fuente (Con Errores)

```jsx
import React, { useState, useEffect } from 'react';

export default function LiveSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!query) {
      setResults([]);
      return;
    }

    setLoading(true);
    fetch(`https://dummyjson.com/products/search?q=${query}`)
      .then((res) => res.json())
      .then((data) => {
        setResults(data.products || []);
        setLoading(false);
      });
  }, [query]);

  return (
    <div style={{ padding: '20px' }}>
      <h3>Buscador en Tiempo Real</h3>
      <input
        type="text"
        placeholder="Escribe para buscar..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        style={{ padding: '8px', width: '300px', marginBottom: '15px' }}
      />
      {loading && <p>Buscando resultados...</p>}
      <ul>
        {results.map((product) => (
          <li key={product.id}>{product.title}</li>
        ))}
      </ul>
    </div>
  );
}
```

### Puntos a resolver:
1. **Analizar la condición de carrera:** Explica con tus palabras por qué ocurre la condición de carrera (*race condition*) y qué determina qué petición es la que termina visualizándose en pantalla.
2. **Corrección con booleano de control:** Modifica el `useEffect` para declarar una bandera de control (por ejemplo, `let active = true`) que sea establecida en `false` en la función de limpieza del efecto, asegurándote de no actualizar el estado de resultados si el efecto se ha vuelto obsoleto antes de que se complete el fetch.
3. **Opción avanzada alternativa (Opcional):** Implementa el uso de `AbortController` en el fetch para abortar activamente la solicitud HTTP pendiente cada vez que cambie `query`.

---

## Ejercicio 6: El Caos de las Notas en la Lista de Tareas

* **Dificultad:** Intermedio
* **Tema:** Reconciliación (*Diffing*), la propiedad `key` y el peligro de usar índices de arrays (`index`) como keys de elementos dinámicos que tienen estado interno (como campos de texto).
* **Descripción del propósito y escenario de uso:**
  Un usuario maneja una lista de tareas del día. Puede añadir nuevas tareas y borrar las existentes. Adicionalmente, al lado de cada tarea, hay un campo de texto `<input />` donde el usuario puede escribir comentarios o notas temporales (estado nativo del DOM del input). El error sucede cuando hay 3 tareas, el usuario escribe una nota en la **primera** tarea y pulsa "Eliminar" en ella. La primera tarea desaparece, pero la nota que el usuario escribió permanece visible y aparece ahora asignada a la nueva primera tarea (la que antes era la segunda).

### Código Fuente (Con Errores)

```jsx
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
```

### Puntos a resolver:
1. **Analizar la propiedad key:** Explica detalladamente por qué el algoritmo de reconciliación de React asocia el estado del input de texto del DOM con el índice de la lista en lugar de con el objeto de la tarea específica.
2. **Generar un ID persistente:** Modifica la estructura de datos para que cada tarea tenga un identificador único (p. ej., un `id` numérico o string persistente) generado al crear la tarea.
3. **Corregir el mapeo:** Reemplaza la propiedad `key={index}` por el identificador único persistente de la tarea y adapta las funciones para trabajar con dicho identificador en lugar del índice numérico.

---

## Ejercicio 7: El Consumidor Huérfano del Carrito

* **Dificultad:** Intermedio/Avanzado
* **Tema:** Context API, patrones de arquitectura de estado y propagación de datos.
* **Descripción del propósito y escenario de uso:**
  Una aplicación implementa un `CartContext` para compartir el estado del carrito de compras (productos, funciones para agregar, etc.) de manera global. Al implementar un botón de "Añadir al Carrito" en la página de detalles de producto, la aplicación colapsa inmediatamente al cargar la página con el error: `TypeError: Cannot read properties of null (reading 'addToCart')` o similar.

### Código Fuente (Con Errores)

#### `CartContext.js`
```jsx
import React, { createContext, useContext, useState } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cart, setCart] = useState([]);

  const addToCart = (product) => {
    setCart((prev) => [...prev, product]);
  };

  return (
    <CartContext.Provider value={{ cart, addToCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
```

#### `ProductDetail.js`
```jsx
import React from 'react';
import { useCart } from './CartContext';

export default function ProductDetail() {
  const { addToCart } = useCart(); // <-- El error se dispara aquí

  const sampleProduct = { id: 101, name: 'Auriculares Premium', price: 99.99 };

  return (
    <div style={{ border: '1px solid black', padding: '15px', width: '300px' }}>
      <h4>{sampleProduct.name}</h4>
      <p>Precio: ${sampleProduct.price}</p>
      <button onClick={() => addToCart(sampleProduct)}>
        Añadir al Carrito
      </button>
    </div>
  );
}
```

#### `App.js`
```jsx
import React from 'react';
import { CartProvider } from './CartContext';
import ProductDetail from './ProductDetail';

export default function App() {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Tienda Virtual</h1>
      {/* 
        El desarrollador colocó el Provider por aquí pero olvidó la jerarquía de componentes 
      */}
      <ProductDetail />
      <CartProvider>
        <div>
          <h3>Barra de estado del carrito</h3>
          {/* Se consumen datos del carrito */}
        </div>
      </CartProvider>
    </div>
  );
}
```

### Puntos a resolver:
1. **Identificar la causa del error:** Explica por qué `useCart` retorna `null` y por qué el componente `ProductDetail` no es capaz de leer la función `addToCart`.
2. **Modificar la jerarquía en `App.js`:** Ajusta la estructura del árbol de componentes en `App.js` para asegurar que todo consumidor del contexto se encuentre correctamente anidado dentro de la etiqueta `<CartProvider>`.
3. **Manejo defensivo en Custom Hook:** Mejora el custom hook `useCart` en `CartContext.js` para que, en caso de consumirse fuera del proveedor, lance un error personalizado explícito e informativo en la consola en lugar de un error genérico de desreferenciación.

---

## Ejercicio 8: El Cero Fantasma en la Interfaz

* **Dificultad:** Principiante
* **Tema:** Renderizado condicional en JSX con operadores lógicos y comportamiento de falsedad (*falsy values*) en JavaScript.
* **Descripción del propósito y escenario de uso:**
  Un sistema de notificaciones de mensajería muestra una lista de mensajes entrantes. El desarrollador quiere que si la lista de mensajes está vacía (tiene 0 elementos), el contenedor de la lista no se renderice en pantalla en absoluto. Al probar, cuando la lista se vacía, la interfaz de usuario muestra de forma extraña el número `0` flotando en medio de la pantalla.

### Código Fuente (Con Errores)

```jsx
import React, { useState } from 'react';

export default function Mailbox() {
  const [messages, setMessages] = useState([]); // Lista inicialmente vacía

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h2>Bandeja de Entrada</h2>
      
      {/* ERROR: Renderiza un "0" si messages está vacío */}
      {messages.length && (
        <div style={{ backgroundColor: '#f0f0f0', padding: '15px', borderRadius: '5px' }}>
          <h4>Tienes mensajes pendientes:</h4>
          <ul>
            {messages.map((msg, index) => (
              <li key={index}>{msg}</li>
            ))}
          </ul>
        </div>
      )}

      <button onClick={() => setMessages(['Mensaje nuevo de Soporte', 'Recordatorio de reunión'])}>
        Cargar Mensajes
      </button>
      <button onClick={() => setMessages([])} style={{ marginLeft: '10px' }}>
        Vaciar Bandeja
      </button>
    </div>
  );
}
```

### Puntos a resolver:
1. **Analizar el cortocircuito:** Explica matemáticamente y lógicamente por qué la expresión `messages.length && <Component />` evalúa y renderiza un `0` cuando la longitud del array es cero.
2. **Corregir la expresión condicional:** Reescribe la condición JSX para forzar un valor booleano explícito de dos maneras posibles.
3. **Optimizar la legibilidad:** Recomienda y aplica la solución más limpia y idiomática en React.

---

## Ejercicio 9: El Filtro Lento Inútil

* **Dificultad:** Avanzado
* **Tema:** Optimización del rendimiento, re-renderizados innecesarios, `React.memo`, `useCallback` y `useMemo`.
* **Descripción del propósito y escenario de uso:**
  Un panel administrativo contiene una lista gigante de nombres de productos de alta densidad (digamos, 500 elementos). El usuario puede filtrar productos escribiendo en un input de búsqueda local. Para evitar que el renderizado sea lento, el desarrollador envolvió el componente hijo `ProductItem` con `React.memo` para evitar que se vuelva a renderizar un elemento de la lista que no ha sufrido cambios. Sin embargo, al escribir una sola letra en el input del buscador en el componente padre, la aplicación se congela por unos milisegundos. La consola del perfilador revela que TODOS los 500 componentes hijos `ProductItem` se están re-renderizando a pesar de estar usando `React.memo`.

### Código Fuente (Con Errores)

```jsx
import React, { useState } from 'react';

// Simulamos una lista pesada de productos
const HEAVY_PRODUCTS = Array.from({ length: 500 }, (_, i) => ({
  id: i,
  name: `Producto de Tecnología Modelo ${i + 1}`,
  category: i % 2 === 0 ? 'Hardware' : 'Software',
}));

// Componente Hijo Optimizado con React.memo
const ProductItem = React.memo(({ product, onSelect }) => {
  console.log(`Renderizando ProductItem: ${product.id}`); // Monitorea re-renders
  return (
    <li 
      onClick={() => onSelect(product)} 
      style={{ padding: '5px', cursor: 'pointer', borderBottom: '1px solid #eee' }}
    >
      {product.name} - <strong>{product.category}</strong>
    </li>
  );
});

export default function Catalog() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);

  // Filtramos los productos según la búsqueda
  const filteredProducts = HEAVY_PRODUCTS.filter((p) =>
    p.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Función manejadora que pasamos al componente hijo
  const handleSelectProduct = (product) => {
    setSelectedProduct(product);
  };

  return (
    <div style={{ display: 'flex', padding: '20px', gap: '20px' }}>
      <div style={{ flex: 1 }}>
        <h3>Catálogo de Productos</h3>
        <input
          type="text"
          placeholder="Filtrar por nombre..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', padding: '8px', marginBottom: '15px' }}
        />
        <ul style={{ maxHeight: '400px', overflowY: 'auto' }}>
          {filteredProducts.map((prod) => (
            <ProductItem 
              key={prod.id} 
              product={prod} 
              onSelect={handleSelectProduct} 
            />
          ))}
        </ul>
      </div>
      <div style={{ width: '300px', borderLeft: '1px solid #ccc', paddingLeft: '20px' }}>
        <h3>Seleccionado</h3>
        {selectedProduct ? (
          <p>{selectedProduct.name} ({selectedProduct.category})</p>
        ) : (
          <p>Ninguno seleccionado</p>
        )}
      </div>
    </div>
  );
}
```

### Puntos a resolver:
1. **Analizar la ruptura de React.memo:** Explica por qué `React.memo` no puede evitar el re-renderizado de `ProductItem` a pesar de que el objeto `product` individual de cada hijo es exactamente el mismo (identidad referencial de las funciones en JS).
2. **Memoizar funciones con `useCallback`:** Aplica `useCallback` en `handleSelectProduct` para asegurar la estabilidad de la referencia de la función entre re-renderizados del componente padre.
3. **Evitar cálculos redundantes con `useMemo`:** El cálculo del filtrado de productos (`filteredProducts`) se ejecuta en cada render del componente padre (incluso si cambia otro estado que no influye en los resultados). Envuelve este filtrado en un `useMemo` con sus dependencias correctas para optimizar el rendimiento computacional.

---

## Ejercicio 10: El Formulario Esquizofrénico

* **Dificultad:** Principiante/Intermedio
* **Tema:** Inputs controlados vs no controlados (*controlled vs uncontrolled inputs*), estabilidad de tipos de datos de estado y warnings de consola de React.
* **Descripción del propósito y escenario de uso:**
  Un formulario permite editar la información personal de un usuario (Nombre, Edad). Los datos iniciales son traídos de una simulación de red tardía, por lo que inicialmente el estado está en `null`. Al cargar los datos y presionar en limpiar, la consola se llena de advertencias de advertencia: `Warning: A component is changing an uncontrolled input to be controlled...`. Además, cuando el usuario escribe en el input "Edad" y luego se lee ese valor, se guarda como cadena de texto (*string*) en vez de como número entero, rompiendo cálculos lógicos posteriores.

### Código Fuente (Con Errores)

```jsx
import React, { useState, useEffect } from 'react';

export default function EditProfileForm() {
  const [formData, setFormData] = useState({
    username: null, // Error inicial
    age: undefined,  // Error inicial
  });

  // Simulamos carga de datos desde servidor
  useEffect(() => {
    const timer = setTimeout(() => {
      setFormData({
        username: 'JuanPerez',
        age: 30,
      });
    }, 1000);
    return () => clearTimeout(timer);
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleReset = () => {
    // Intentamos limpiar los inputs
    setFormData({
      username: null,
      age: undefined,
    });
  };

  const saveToDatabase = () => {
    // Al intentar sumar la edad para una simulación de proyección
    const nextYearAge = formData.age + 1;
    alert(`Nombre: ${formData.username}, Edad el próximo año: ${nextYearAge}`);
  };

  return (
    <div style={{ padding: '20px', maxWidth: '350px', border: '1px solid #ddd' }}>
      <h3>Editar Perfil</h3>
      <div style={{ marginBottom: '10px' }}>
        <label style={{ display: 'block' }}>Nombre de usuario:</label>
        <input
          type="text"
          name="username"
          value={formData.username} // Error: Recibe null temporalmente
          onChange={handleChange}
          style={{ width: '100%', padding: '5px' }}
        />
      </div>
      <div style={{ marginBottom: '15px' }}>
        <label style={{ display: 'block' }}>Edad:</label>
        <input
          type="number"
          name="age"
          value={formData.age} // Error: Recibe undefined temporalmente
          onChange={handleChange}
          style={{ width: '100%', padding: '5px' }}
        />
      </div>
      <button onClick={saveToDatabase} style={{ marginRight: '10px' }}>
        Guardar
      </button>
      <button onClick={handleReset} style={{ backgroundColor: '#ccc' }}>
        Limpiar
      </button>
    </div>
  );
}
```

### Puntos a resolver:
1. **Descubrir el origen del Warning:** Explica detalladamente por qué pasar `null` o `undefined` a la propiedad `value` de un elemento `<input />` hace que React lo trate como un componente *no controlado*, y por qué cambiar el estado a un valor válido posteriormente genera la advertencia en consola.
2. **Garantizar valores por defecto seguros:** Corrige el estado inicial y las referencias de asignación del `value` en JSX para asegurar que los inputs siempre reciban una cadena vacía `""` en lugar de valores nulos o no definidos en cualquier etapa de la carga.
3. **Preservar el tipo de dato numérico:** Modifica la función `handleChange` o la asignación de la edad para convertir el valor del input a un número entero con `parseInt()` o similar si el input es de tipo numérico, evitando concatenaciones erróneas como `"301"` en lugar de `31`.

---

# Sección de Respuestas (Soluciones)

Utiliza estas guías de resolución y códigos fuente corregidos para comprobar tu análisis y autoevaluarte.

---

### Solución al Ejercicio 1: El Contador Triple

* **Por qué ocurre el error:** 
  Las funciones actualizadoras de estado (`setCount`) son asíncronas en el contexto de su ejecución inmediata y están sujetas a clausuras (*closures*). Cuando hacemos `setCount(count + 1)`, `count` se refiere al valor del estado al momento del renderizado actual. Al llamar tres veces consecutivas a `setCount(count + 1)`, las tres llamadas capturan el mismo valor (ej. `count = 0`). Las tres llamadas evalúan equivalentemente a `setCount(0 + 1)`. Al finalizar la transacción de renderizado, React actualiza el estado consolidado a `1`.

* **Código Corregido:**

```jsx
import React, { useState } from 'react';

export default function TripleCounter() {
  const [count, setCount] = useState(0);

  const incrementByOne = () => {
    // Usamos el callback funcional para garantizar la versión más actualizada
    setCount((prevCount) => prevCount + 1);
  };

  const incrementByThree = () => {
    // Ahora, cada llamada encadenada utilizará el estado actualizado intermedio
    incrementByOne();
    incrementByOne();
    incrementByOne();
  };

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h2>Contador: {count}</h2>
      <button onClick={incrementByOne} style={{ marginRight: '10px' }}>
        +1
      </button>
      <button onClick={incrementByThree}>
        +3 (Corregido!)
      </button>
    </div>
  );
}
```

---

### Solución al Ejercicio 2: El Bucle Infinito del Perfil de Usuario

* **Por qué ocurre el error:** 
  Al omitir el array de dependencias en un `useEffect` (es decir, no colocar la sintaxis `[]`), el efecto se ejecuta tras **cada re-renderizado** del componente. Dentro de este efecto, se invoca a `setUserData(data)` y `setLoading(false)`. Estas llamadas cambian el estado interno, disparando un nuevo renderizado. Este nuevo renderizado ejecuta el efecto otra vez, el cual vuelve a cambiar el estado, creando un ciclo de peticiones infinito.

* **Código Corregido:**

```jsx
import React, { useState, useEffect } from 'react';

export default function UserProfile({ userId }) {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Evitamos llamadas innecesarias o erróneas si no hay ID
    if (!userId) return;

    setLoading(true);
    fetch(`https://jsonplaceholder.typicode.com/users/${userId}`)
      .then((res) => res.json())
      .then((data) => {
        setUserData(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error al obtener perfil", err);
        setLoading(false);
      });
  }, [userId]); // Dependencia clave: El efecto se dispara únicamente cuando cambia userId

  if (!userId) return <p>Por favor, seleccione un usuario.</p>;
  if (loading) return <p>Cargando perfil...</p>;

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h3>Nombre: {userData?.name}</h3>
      <p>Email: {userData?.email}</p>
      <p>Compañía: {userData?.company?.name}</p>
    </div>
  );
}
```

---

### Solución al Ejercicio 3: El Cronómetro Acelerado

* **Por qué ocurre el error:** 
  Cada vez que llamamos a `startTimer`, se instancia un nuevo intervalo. Al guardar `timerId` en un estado (`useState`), cuando actualizamos el estado con `setTimerId(id)`, provocamos un re-render. Si el usuario hace clic rápidamente varias veces en "Iniciar", se crean múltiples intervalos paralelos que modifican simultáneamente el estado `seconds`, pero nuestro estado `timerId` solo guardará el identificador del *último* intervalo creado. Los intervalos anteriores quedan huérfanos e imposibles de detener, ya que perdimos su referencia física para poder pasársela a `clearInterval`.

* **Código Corregido:**

```jsx
import React, { useState, useRef, useEffect } from 'react';

export default function AccelTimer() {
  const [seconds, setSeconds] = useState(0);
  
  // useRef mantiene una referencia persistente e idéntica entre renders 
  // y modificar .current NO dispara re-renderizados
  const timerIdRef = useRef(null);

  const startTimer = () => {
    // Verificamos si ya hay un temporizador activo para evitar duplicados
    if (timerIdRef.current !== null) return;

    timerIdRef.current = setInterval(() => {
      setSeconds((prev) => prev + 1);
    }, 1000);
  };

  const stopTimer = () => {
    if (timerIdRef.current !== null) {
      clearInterval(timerIdRef.current);
      timerIdRef.current = null;
    }
  };

  // Limpieza total del intervalo si el componente es desmontado
  useEffect(() => {
    return () => {
      if (timerIdRef.current !== null) {
        clearInterval(timerIdRef.current);
      }
    };
  }, []);

  return (
    <div style={{ padding: '20px', border: '1px solid #555', textAlign: 'center' }}>
      <h2>Segundos: {seconds}s</h2>
      <button onClick={startTimer} style={{ marginRight: '10px', backgroundColor: '#4CAF50', color: 'white' }}>
        Iniciar
      </button>
      <button onClick={stopTimer} style={{ backgroundColor: '#f44336', color: 'white' }}>
        Detener (Corregido)
      </button>
    </div>
  );
}
```

---

### Solución al Ejercicio 4: Fuga de Memoria con Eventos Globales

* **Por qué ocurre el error:** 
  Al registrar eventos globales del navegador (como `resize` o `scroll` en `window` o `document`) dentro de un efecto de montaje, el navegador crea una referencia pesada en su lista de tareas activas. Si el componente desaparece de la pantalla, el efecto se destruye, pero el navegador mantiene el registro del callback y de las variables internas que este callback contenía en memoria. Esto consume recursos continuamente.

* **Código Corregido:**

```jsx
import { useState, useEffect } from 'react';

export function useWindowSize() {
  const [windowSize, setWindowSize] = useState({
    width: typeof window !== 'undefined' ? window.innerWidth : 0,
    height: typeof window !== 'undefined' ? window.innerHeight : 0,
  });

  useEffect(() => {
    const handleResize = () => {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    };

    window.addEventListener('resize', handleResize);

    // Retornamos la función de saneamiento (cleanup)
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []); // Array de dependencias vacío asegura que se agregue y elimine solo al montar/desmontar

  return windowSize;
}
```

---

### Solución al Ejercicio 5: Condiciones de Carrera en la Búsqueda

* **Por qué ocurre el error:** 
  Las peticiones HTTP asíncronas no tienen un tiempo de respuesta garantizado ni garantizan un orden secuencial (FIFO). La petición de búsqueda que toma más tiempo (por ejemplo, por tener más datos) sobrescribe los resultados de la petición más rápida aunque esta última haya sido enviada después. Esto se denomina condición de carrera (*race condition*).

* **Código Corregido:**

#### Opción A: Variable de Control Local (Recomendado y Sencillo)
```jsx
import React, { useState, useEffect } from 'react';

export default function LiveSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!query) {
      setResults([]);
      return;
    }

    let active = true; // Controlamos si el efecto sigue activo

    setLoading(true);
    fetch(`https://dummyjson.com/products/search?q=${query}`)
      .then((res) => res.json())
      .then((data) => {
        // Solo actualizamos el estado si el efecto sigue siendo el actual
        if (active) {
          setResults(data.products || []);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (active) {
          console.error(err);
          setLoading(false);
        }
      });

    // Función de limpieza que se ejecuta antes de la próxima ejecución del efecto o al desmontar
    return () => {
      active = false;
    };
  }, [query]);

  return (
    <div style={{ padding: '20px' }}>
      <h3>Buscador en Tiempo Real (Corregido)</h3>
      <input
        type="text"
        placeholder="Escribe para buscar..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        style={{ padding: '8px', width: '300px', marginBottom: '15px' }}
      />
      {loading && <p>Buscando resultados...</p>}
      <ul>
        {results.map((product) => (
          <li key={product.id}>{product.title}</li>
        ))}
      </ul>
    </div>
  );
}
```

#### Opción B: Uso de AbortController (Abortando peticiones previas)
```jsx
  useEffect(() => {
    if (!query) {
      setResults([]);
      return;
    }

    const controller = new AbortController();
    const { signal } = controller;

    setLoading(true);
    fetch(`https://dummyjson.com/products/search?q=${query}`, { signal })
      .then((res) => res.json())
      .then((data) => {
        setResults(data.products || []);
        setLoading(false);
      })
      .catch((err) => {
        if (err.name === 'AbortError') {
          // El fetch fue abortado de forma segura, ignoramos el error
          return;
        }
        setLoading(false);
      });

    return () => {
      controller.abort(); // Cancela la petición fetch inmediatamente si query vuelve a cambiar
    };
  }, [query]);
```

---

### Solución al Ejercicio 6: El Caos de las Notas en la Lista de Tareas

* **Por qué ocurre el error:** 
  React usa la propiedad `key` para saber qué elemento virtual se corresponde con qué elemento físico en el DOM. Cuando usamos el `index` de un array como clave (`key`), y eliminamos la tarea en el índice `0`, React nota que la lista pasó de tener un tamaño de 3 a 2. Identifica que el elemento con `key={2}` ha desaparecido, pero los elementos con `key={0}` y `key={1}` siguen presentes en el árbol. Por lo tanto, actualiza las propiedades visuales del elemento `key={0}` con el texto de la segunda tarea, pero **no modifica el estado interno del input nativo** (las notas escritas), ya que considera que el elemento virtual en la posición `0` nunca se destruyó, solo se actualizó.

* **Código Corregido:**

```jsx
import React, { useState } from 'react';

export default function TodoList() {
  const [todos, setTodos] = useState([
    { id: 'todo-1', text: 'Aprender React' },
    { id: 'todo-2', text: 'Escribir tests' },
    { id: 'todo-3', text: 'Subir a producción' },
  ]);

  const removeTodo = (idToRemove) => {
    setTodos(todos.filter((todo) => todo.id !== idToRemove));
  };

  const addTodo = () => {
    const newTodo = {
      id: `todo-${Date.now()}`, // Identificador único garantizado
      text: 'Nueva Tarea',
    };
    setTodos([newTodo, ...todos]);
  };

  return (
    <div style={{ padding: '20px' }}>
      <h3>Mis Tareas (Corregido con ID único)</h3>
      <button onClick={addTodo} style={{ marginBottom: '15px' }}>Agregar Tarea al Inicio</button>
      <ul>
        {todos.map((todo) => (
          // Usamos la propiedad única 'todo.id' para la reconciliación perfecta
          <li key={todo.id} style={{ marginBottom: '10px' }}>
            <span>{todo.text}</span>
            <input 
              type="text" 
              placeholder="Escribe una nota aquí..." 
              style={{ marginLeft: '10px', marginRight: '10px' }} 
            />
            <button onClick={() => removeTodo(todo.id)}>Eliminar</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

---

### Solución al Ejercicio 7: El Consumidor Huérfano del Carrito

* **Por qué ocurre el error:** 
  El hook `useCart` consume el valor del `CartContext` mediante `useContext(CartContext)`. Sin embargo, `CartContext` está inicializado con un valor por defecto de `null` (`createContext(null)`). Cuando un componente como `ProductDetail` llama a `useCart` pero no se encuentra dentro del árbol de componentes descendientes envuelto por `<CartProvider>`, React le entrega el valor por defecto (`null`). Al intentar desestructurar `{ addToCart }` de un valor nulo, JavaScript arroja un error letal de desreferenciación.

* **Código Corregido:**

#### `CartContext.js` (Con validación explícita)
```jsx
import React, { createContext, useContext, useState } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cart, setCart] = useState([]);

  const addToCart = (product) => {
    setCart((prev) => [...prev, product]);
  };

  return (
    <CartContext.Provider value={{ cart, addToCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  
  // Agregamos una validación defensiva excelente
  if (!context) {
    throw new Error('useCart debe ser utilizado obligatoriamente dentro de un CartProvider');
  }
  
  return context;
}
```

#### `App.js` (Estructura de anidación correcta)
```jsx
import React from 'react';
import { CartProvider } from './CartContext';
import ProductDetail from './ProductDetail';

export default function App() {
  return (
    // Envolvemos toda la aplicación (o los componentes que lo consuman) con el CartProvider
    <CartProvider>
      <div style={{ padding: '20px' }}>
        <h1>Tienda Virtual (Corregido)</h1>
        
        {/* Ahora ProductDetail está anidado correctamente dentro del Provider */}
        <ProductDetail />

        <div style={{ marginTop: '20px' }}>
          <h3>Barra de estado del carrito</h3>
        </div>
      </div>
    </CartProvider>
  );
}
```

---

### Solución al Ejercicio 8: El Cero Fantasma en la Interfaz

* **Por qué ocurre el error:** 
  En JavaScript, el operador lógico `&&` no produce un valor estrictamente booleano. Su comportamiento evalúa la primera expresión: si es falsa (*falsy*), retorna inmediatamente el valor de esa expresión y detiene la evaluación. Si `messages` está vacío, `messages.length` evalúa al número entero `0`. Como `0` es un valor falsy en JS, el operador cortocircuita y retorna el valor de la izquierda, que es `0`. En React, renderizar el booleano `false` o `null` no pinta nada en pantalla, pero **renderizar el número `0` sí pinta un carácter de texto en la interfaz**.

* **Código Corregido:**

```jsx
import React, { useState } from 'react';

export default function Mailbox() {
  const [messages, setMessages] = useState([]);

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h2>Bandeja de Entrada (Corregido)</h2>
      
      {/* Opción 1 (Recomendada): Validación explícitamente booleana */}
      {messages.length > 0 && (
        <div style={{ backgroundColor: '#f0f0f0', padding: '15px', borderRadius: '5px' }}>
          <h4>Tienes mensajes pendientes:</h4>
          <ul>
            {messages.map((msg, index) => (
              <li key={index}>{msg}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Opción 2 (Alternativa clásica): Conversión a booleano con doble negación */}
      {/* {!!messages.length && <Component />} */}

      <button onClick={() => setMessages(['Mensaje nuevo de Soporte', 'Recordatorio de reunión'])}>
        Cargar Mensajes
      </button>
      <button onClick={() => setMessages([])} style={{ marginLeft: '10px' }}>
        Vaciar Bandeja
      </button>
    </div>
  );
}
```

---

### Solución al Ejercicio 9: El Filtro Lento Inútil

* **Por qué ocurre el error:** 
  `React.memo` realiza una comparación superficial (*shallow comparison*) de las props que recibe el componente hijo. En el componente `Catalog`, la función de callback `handleSelectProduct` se recrea desde cero con una nueva referencia de memoria en cada renderizado del padre (cada vez que cambia `searchTerm`). Como las funciones se comparan por referencia en JavaScript y la referencia de `handleSelectProduct` cambia, `React.memo` determina que las propiedades del componente hijo cambiaron y fuerza el re-render total de todos los elementos `ProductItem`.

* **Código Corregido:**

```jsx
import React, { useState, useCallback, useMemo } from 'react';

const HEAVY_PRODUCTS = Array.from({ length: 500 }, (_, i) => ({
  id: i,
  name: `Producto de Tecnología Modelo ${i + 1}`,
  category: i % 2 === 0 ? 'Hardware' : 'Software',
}));

// Componente Hijo Memoizado perfectamente
const ProductItem = React.memo(({ product, onSelect }) => {
  console.log(`Renderizando ProductItem: ${product.id}`); 
  return (
    <li 
      onClick={() => onSelect(product)} 
      style={{ padding: '5px', cursor: 'pointer', borderBottom: '1px solid #eee' }}
    >
      {product.name} - <strong>{product.category}</strong>
    </li>
  );
});

export default function Catalog() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);

  // 1. Usamos useMemo para evitar recalcular el filtrado pesado en renders no relacionados
  const filteredProducts = useMemo(() => {
    return HEAVY_PRODUCTS.filter((p) =>
      p.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [searchTerm]); // Solo se recalcula si 'searchTerm' realmente cambia

  // 2. Usamos useCallback para fijar la referencia de la función
  const handleSelectProduct = useCallback((product) => {
    setSelectedProduct(product);
  }, []); // Dependencias vacías: la referencia de la función es idéntica siempre

  return (
    <div style={{ display: 'flex', padding: '20px', gap: '20px' }}>
      <div style={{ flex: 1 }}>
        <h3>Catálogo de Productos (Optimizado)</h3>
        <input
          type="text"
          placeholder="Filtrar por nombre..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', padding: '8px', marginBottom: '15px' }}
        />
        <ul style={{ maxHeight: '400px', overflowY: 'auto' }}>
          {filteredProducts.map((prod) => (
            <ProductItem 
              key={prod.id} 
              product={prod} 
              onSelect={handleSelectProduct} 
            />
          ))}
        </ul>
      </div>
      <div style={{ width: '300px', borderLeft: '1px solid #ccc', paddingLeft: '20px' }}>
        <h3>Seleccionado</h3>
        {selectedProduct ? (
          <p>{selectedProduct.name} ({selectedProduct.category})</p>
        ) : (
          <p>Ninguno seleccionado</p>
        )}
      </div>
    </div>
  );
}
```

---

### Solución al Ejercicio 10: El Formulario Esquizofrénico

* **Por qué ocurre el error:** 
  Un input en React es tratado como *no controlado* si su propiedad `value` es `null` o `undefined`. El input decide manejar su estado local internamente en el DOM. Cuando se cargan los datos y el estado cambia a un string (`'JuanPerez'`), React detecta que la prop `value` ahora tiene un valor definido, convirtiendo el input en *controlado* sobre la marcha. React desaconseja esto porque causa inconsistencias y lanza una advertencia en la consola. Al limpiar el formulario, restablecemos los campos a `null` o `undefined`, revirtiendo el proceso de manera inadecuada. Además, al guardar, el valor recibido del input es un string, por lo que `"30" + 1` da `"301"`.

* **Código Corregido:**

```jsx
import React, { useState, useEffect } from 'react';

export default function EditProfileForm() {
  // Inicializamos con strings vacíos o valores predeterminados seguros para inputs controlados
  const [formData, setFormData] = useState({
    username: '', 
    age: '',      
  });

  useEffect(() => {
    const timer = setTimeout(() => {
      setFormData({
        username: 'JuanPerez',
        age: 30,
      });
    }, 1000);
    return () => clearTimeout(timer);
  }, []);

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    
    // Convertimos de manera segura la edad a un valor numérico si el input es numérico
    const parsedValue = type === 'number' ? (value === '' ? '' : parseInt(value, 10)) : value;

    setFormData({
      ...formData,
      [name]: parsedValue,
    });
  };

  const handleReset = () => {
    // Limpiamos con strings vacíos seguros
    setFormData({
      username: '',
      age: '',
    });
  };

  const saveToDatabase = () => {
    // Validamos y manejamos la edad de manera segura
    const ageNum = Number(formData.age) || 0;
    const nextYearAge = ageNum + 1;
    alert(`Nombre: ${formData.username || 'N/A'}, Edad el próximo año: ${nextYearAge}`);
  };

  return (
    <div style={{ padding: '20px', maxWidth: '350px', border: '1px solid #ddd' }}>
      <h3>Editar Perfil (Controlado y Seguro)</h3>
      <div style={{ marginBottom: '10px' }}>
        <label style={{ display: 'block' }}>Nombre de usuario:</label>
        <input
          type="text"
          name="username"
          // Usamos un valor fallback por seguridad para evitar null/undefined
          value={formData.username ?? ''} 
          onChange={handleChange}
          style={{ width: '100%', padding: '5px' }}
        />
      </div>
      <div style={{ marginBottom: '15px' }}>
        <label style={{ display: 'block' }}>Edad:</label>
        <input
          type="number"
          name="age"
          // Usamos un valor fallback por seguridad para evitar null/undefined
          value={formData.age ?? ''} 
          onChange={handleChange}
          style={{ width: '100%', padding: '5px' }}
        />
      </div>
      <button onClick={saveToDatabase} style={{ marginRight: '10px' }}>
        Guardar
      </button>
      <button onClick={handleReset} style={{ backgroundColor: '#ccc' }}>
        Limpiar
      </button>
    </div>
  );
}
```
