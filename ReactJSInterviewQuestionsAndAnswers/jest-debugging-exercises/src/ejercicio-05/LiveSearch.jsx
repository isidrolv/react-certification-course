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
