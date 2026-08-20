import {act, fireEvent, render, screen} from '@testing-library/react';
import AccelTimer from './AccelTimer';

describe('Ejercicio 3', () => {
    beforeEach(() => jest.useFakeTimers());
    afterEach(() => jest.useRealTimers());
    test('unitaria: desmontar limpia el intervalo', () => {
        const spy = jest.spyOn(global, 'clearInterval');
        const {unmount} = render(<AccelTimer/>);
        fireEvent.click(screen.getByRole('button', {name: 'Iniciar'}));
        unmount();
        expect(spy).toHaveBeenCalled();
    });
    test('funcional: iniciar dos veces conserva un solo intervalo', () => {
        render(<AccelTimer/>);
        fireEvent.click(screen.getByRole('button', {name: 'Iniciar'}));
        fireEvent.click(screen.getByRole('button', {name: 'Iniciar'}));
        act(() => jest.advanceTimersByTime(1000));
        expect(screen.getByRole('heading')).toHaveTextContent('Segundos: 1s');
    });
});
