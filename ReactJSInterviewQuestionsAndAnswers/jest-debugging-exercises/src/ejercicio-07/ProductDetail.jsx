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
