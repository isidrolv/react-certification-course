import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Catalog from './Catalog';

describe('Ejercicio 9', () => {
  test('funcional: seleccionar no vuelve a renderizar los 500 productos', async () => {
    const spy = jest.spyOn(console, 'log').mockImplementation(() => {});
    render(<Catalog />);
    expect(spy).toHaveBeenCalledTimes(500);
    spy.mockClear();
    await userEvent.click(screen.getByText(/Producto de Tecnología Modelo 1 -/));
    expect(screen.getByText(/Producto de Tecnología Modelo 1 \(Hardware\)/)).toBeInTheDocument();
    expect(spy).not.toHaveBeenCalled();
  });
  test('unitaria: un cambio de estado ajeno al filtro conserva hijos estables', async () => {
    const spy = jest.spyOn(console, 'log').mockImplementation(() => {});
    render(<Catalog />);
    spy.mockClear();
    await userEvent.click(screen.getByText(/Producto de Tecnología Modelo 2 -/));
    expect(spy).not.toHaveBeenCalled();
  });
});
