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
