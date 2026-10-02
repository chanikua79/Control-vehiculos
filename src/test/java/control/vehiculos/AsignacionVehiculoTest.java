package control.vehiculos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AsignacionVehiculoTest {

    @Test
    void unaAsignacionNuevaDebeEstarActiva() {
        Vehiculo vehiculo = new Vehiculo(1, "ABC-123", "Toyota", "Hilux");
        Empleado empleado = new Empleado(1, "Carlos Perez", "QR-CARLOS-001");

        AsignacionVehiculo asignacion =
                new AsignacionVehiculo(1, vehiculo, empleado);

        assertTrue(asignacion.isActiva());
        assertNull(asignacion.getFechaHoraFin());
    }

    @Test
    void finalizarAsignacionDebeRegistrarFechaYDesactivar() {
        Vehiculo vehiculo = new Vehiculo(1, "ABC-123", "Toyota", "Hilux");
        Empleado empleado = new Empleado(1, "Carlos Perez", "QR-CARLOS-001");
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 10, 0);

        AsignacionVehiculo asignacion =
                new AsignacionVehiculo(1, vehiculo, empleado, inicio);

        asignacion.finalizar();

        assertFalse(asignacion.isActiva());
        assertNotNull(asignacion.getFechaHoraFin());
        assertTrue(asignacion.getFechaHoraFin().isAfter(inicio));
    }

    @Test
    void noDebePermitirAsignacionActivaConFechaFin() {
        Vehiculo vehiculo = new Vehiculo(1, "ABC-123", "Toyota", "Hilux");
        Empleado empleado = new Empleado(1, "Carlos Perez", "QR-CARLOS-001");
        LocalDateTime fin = LocalDateTime.of(2026, 1, 1, 11, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new AsignacionVehiculo(1, vehiculo, empleado,
                        LocalDateTime.of(2026, 1, 1, 10, 0), fin, true));
    }
}
