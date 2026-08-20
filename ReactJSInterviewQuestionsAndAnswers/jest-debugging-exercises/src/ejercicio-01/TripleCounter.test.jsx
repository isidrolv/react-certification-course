import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TripleCounter from './TripleCounter';

describe('Ejercicio 1', () => {
  test('unitaria: +1 incrementa una unidad', async () => {
    render(<TripleCounter/>);
    await userEvent.click(screen.getByRole('button', {name: '+1'}));
    expect(screen.getByRole('heading')).toHaveTextContent('Contador: 1');
  });

  test('funcional: +3 incrementa tres unidades', async () => {
    render(<TripleCounter/>);
    await userEvent.click(screen.getByRole('button', {name: /\+3/}));
    expect(screen.getByRole('heading')).toHaveTextContent('Contador: 3');
  });

  test('funcional: +6 incrementa seis unidades haciendo click dos veces en el botón', async () => {
    render(<TripleCounter/>);
    await userEvent.click(screen.getByRole('button', {name: /\+3/}));
    await userEvent.click(screen.getByRole('button', {name: /\+3/}));
    expect(screen.getByRole('heading')).toHaveTextContent('Contador: 6');
  });

});
