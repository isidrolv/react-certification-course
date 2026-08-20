import React, { useState } from 'react';

export default function TripleCounter() {
  const [count, setCount] = useState(0);

  const incrementByOne = () => {
    setCount((prevState) => prevState + 1);
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
