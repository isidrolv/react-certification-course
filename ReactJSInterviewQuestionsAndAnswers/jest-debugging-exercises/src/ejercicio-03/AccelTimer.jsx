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
