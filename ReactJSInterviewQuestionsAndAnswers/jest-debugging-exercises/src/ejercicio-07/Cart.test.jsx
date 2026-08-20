import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import { useCart } from './CartContext';

function OrphanProbe() {
  useCart();
  return null;
}
describe('Ejercicio 7', () => {
  test('unitaria: useCart fuera del provider lanza un error descriptivo', () => {
    expect(() => render(<OrphanProbe />)).toThrow(/CartProvider/);
  });
  test('funcional: ProductDetail está dentro del provider y permite agregar', async () => {
    expect(() => render(<App />)).not.toThrow();
    await userEvent.click(screen.getByRole('button', { name: 'Añadir al Carrito' }));
  });
});
