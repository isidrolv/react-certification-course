import { act, fireEvent, render, screen } from '@testing-library/react';
import LiveSearch from './LiveSearch';

function deferred() {
  let resolve;
  const promise = new Promise((done) => { resolve = done; });
  return { promise, resolve };
}
describe('Ejercicio 5', () => {
  test('funcional: ignora respuestas obsoletas', async () => {
    const first = deferred();
    const second = deferred();
    global.fetch = jest.fn().mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);
    render(<LiveSearch />);
    const input = screen.getByPlaceholderText('Escribe para buscar...');
    fireEvent.change(input, { target: { value: 'Zapatos' } });
    fireEvent.change(input, { target: { value: 'Bolso' } });
    await act(async () => second.resolve({ json: async () => ({ products: [{ id: 2, title: 'Bolso' }] }) }));
    expect(screen.getByText('Bolso')).toBeInTheDocument();
    await act(async () => first.resolve({ json: async () => ({ products: [{ id: 1, title: 'Zapatos' }] }) }));
    expect(screen.getByText('Bolso')).toBeInTheDocument();
    expect(screen.queryByText('Zapatos')).not.toBeInTheDocument();
  });
});
