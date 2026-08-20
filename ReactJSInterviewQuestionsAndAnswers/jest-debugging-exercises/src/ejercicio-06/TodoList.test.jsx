import { fireEvent, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TodoList from './TodoList';

describe('Ejercicio 6', () => {
  test('unitaria: agregar conserva la nota asociada a su tarea', async () => {
    render(<TodoList />);
    const first = screen.getByText('Aprender React').closest('li');
    await userEvent.type(within(first).getByRole('textbox'), 'nota personal');
    fireEvent.click(screen.getByRole('button', { name: 'Agregar Tarea al Inicio' }));
    const original = screen.getByText('Aprender React').closest('li');
    expect(within(original).getByRole('textbox')).toHaveValue('nota personal');
  });
  test('funcional: eliminar una tarea no transfiere su nota a otra', async () => {
    render(<TodoList />);
    const first = screen.getByText('Aprender React').closest('li');
    await userEvent.type(within(first).getByRole('textbox'), 'solo para React');
    fireEvent.click(within(first).getByRole('button', { name: 'Eliminar' }));
    const newFirst = screen.getByText('Escribir tests').closest('li');
    expect(within(newFirst).getByRole('textbox')).toHaveValue('');
  });
});
