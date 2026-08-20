import { act, fireEvent, render, screen } from '@testing-library/react';
import EditProfileForm from './EditProfileForm';

describe('Ejercicio 10', () => {
  beforeEach(() => jest.useFakeTimers());
  afterEach(() => jest.useRealTimers());

  test('unitaria: no cambia de input no controlado a controlado', () => {
    const spy = jest.spyOn(console, 'error').mockImplementation(() => {});
    render(<EditProfileForm />);
    act(() => jest.advanceTimersByTime(1000));
    const messages = spy.mock.calls.flat().join(' ');
    expect(messages).not.toMatch(/uncontrolled input/i);
  });

  test('funcional: guarda la edad del próximo año como número', () => {
    window.alert = jest.fn();
    const { container } = render(<EditProfileForm />);
    act(() => jest.advanceTimersByTime(1000));
    const ageInput = container.querySelector('input[name="age"]');
    fireEvent.change(ageInput, { target: { value: '30' } });
    fireEvent.click(screen.getByRole('button', { name: 'Guardar' }));
    expect(window.alert).toHaveBeenCalledWith('Nombre: JuanPerez, Edad el próximo año: 31');
  });
});
