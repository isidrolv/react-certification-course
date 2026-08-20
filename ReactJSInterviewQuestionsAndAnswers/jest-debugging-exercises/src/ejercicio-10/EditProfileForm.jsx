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
