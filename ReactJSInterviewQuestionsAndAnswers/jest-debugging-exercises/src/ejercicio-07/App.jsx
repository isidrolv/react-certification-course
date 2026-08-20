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
