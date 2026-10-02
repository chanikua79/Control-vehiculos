package control.vehiculos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ViajeTest {

    @Test
    void viajeNuevoDebeEstarActivo() {
        Vehiculo vehiculo = new Vehiculo(1, "ABC-123", "Toyota", "Hilux");
        Empleado empleado = new Empleado(1, "Carlos Perez", "QR-CARLOS-001");

        Viaje viaje = new Viaje(1, vehiculo, empleado);

        assertTrue(viaje.estaActivo());
        assertNotNull(viaje.getSalida());
        assertNull(viaje.getEntrada());
    }

    @Test
    void registrarEntradaDebeFinalizarViaje() {
        Vehiculo vehiculo = new Vehiculo(1, "ABC-123", "Toyota", "Hilux");
        Empleado empleado = new Empleado(1, "Carlos Perez", "QR-CARLOS-001");
        LocalDateTime salida = LocalDateTime.of(2026, 1, 1, 10, 0);

        Viaje viaje = new Viaje(1, vehiculo, empleado, salida, null);
        viaje.registrarEntrada();

        assertFalse(viaje.estaActivo());
        assertNotNull(viaje.getEntrada());
        assertTrue(viaje.obtenerDuracionMinutos() >= 0);
    }
}
