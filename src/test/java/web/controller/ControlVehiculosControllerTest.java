package web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import control.vehiculos.Empleado;
import control.vehiculos.GestorVehiculos;
import control.vehiculos.Vehiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ControlVehiculosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ControlVehiculosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestorVehiculos gestor;

    @Test
    void dashboardDebeInformarEstadoOperativo() throws Exception {
        when(gestor.getVehiculos()).thenReturn(List.of(
                new Vehiculo(1, "ABC-123", "Toyota", "Hilux")));
        when(gestor.getEmpleados()).thenReturn(List.of(
                new Empleado(1, "Carlos Perez", "QR-CARLOS-001")));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehiculos").value(1))
                .andExpect(jsonPath("$.empleados").value(1))
                .andExpect(jsonPath("$.estado").value("OPERATIVO"));
    }

    @Test
    void buscarVehiculoInexistenteDebeResponder404() throws Exception {
        when(gestor.buscarVehiculoPorMatriculaPublica("ZZZ-999"))
                .thenReturn(null);

        mockMvc.perform(get("/api/vehiculos/buscar")
                        .param("matricula", "ZZZ-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Vehiculo no encontrado"));
    }
}
