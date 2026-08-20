import { act, render, screen } from '@testing-library/react';
import { useWindowSize } from './useWindowSize';

function Probe() {
  const size = useWindowSize();
  return <output>{size.width}x{size.height}</output>;
}
describe('Ejercicio 4', () => {
  test('unitaria: elimina el listener al desmontarse', () => {
    const spy = jest.spyOn(window, 'removeEventListener');
    const { unmount } = render(<Probe />);
    unmount();
    expect(spy).toHaveBeenCalledWith('resize', expect.any(Function));
  });
  test('funcional: refleja un cambio de tamaño', () => {
    render(<Probe />);
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 777 });
    act(() => window.dispatchEvent(new Event('resize')));
    expect(screen.getByText(/^777x/)).toBeInTheDocument();
  });
});
