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
