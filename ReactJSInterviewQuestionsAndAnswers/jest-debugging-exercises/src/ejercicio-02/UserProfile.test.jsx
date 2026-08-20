import { render } from '@testing-library/react';
import UserProfile from './UserProfile';

describe('Ejercicio 2', () => {
  beforeEach(() => { global.fetch = jest.fn(() => new Promise(() => {})); });
  test('unitaria: no consulta sin userId', () => {
    render(<UserProfile userId={null} />);
    expect(fetch).not.toHaveBeenCalled();
  });
  test('funcional: no repite la consulta con el mismo userId', () => {
    const { rerender } = render(<UserProfile userId={7} />);
    rerender(<UserProfile userId={7} />);
    expect(fetch).toHaveBeenCalledTimes(1);
  });
});
