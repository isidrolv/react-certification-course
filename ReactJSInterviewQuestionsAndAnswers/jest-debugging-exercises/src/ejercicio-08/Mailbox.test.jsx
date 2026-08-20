import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Mailbox from './Mailbox';

describe('Ejercicio 8', () => {
  test('unitaria: una bandeja vacía no renderiza el número cero', () => {
    const { container } = render(<Mailbox />);
    expect(container.firstChild).not.toHaveTextContent(/^Bandeja de Entrada0/);
  });
  test('funcional: al vaciar mensajes desaparece la lista sin mostrar cero', async () => {
    const { container } = render(<Mailbox />);
    await userEvent.click(screen.getByRole('button', { name: 'Cargar Mensajes' }));
    expect(screen.getByText('Mensaje nuevo de Soporte')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Vaciar Bandeja' }));
    expect(screen.queryByRole('list')).not.toBeInTheDocument();
    expect(container.firstChild).not.toHaveTextContent(/^Bandeja de Entrada0/);
  });
});
