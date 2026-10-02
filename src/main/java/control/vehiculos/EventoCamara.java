package control.vehiculos;

import java.time.LocalDateTime;

public class EventoCamara {

    private String matricula;
    private LocalDateTime fechaHora;

    public EventoCamara(String matricula) {
        this.matricula = matricula;
        this.fechaHora = LocalDateTime.now();
    }

    public String getMatricula() {
        return matricula;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return "EventoCamara{" +
                "matricula='" + matricula + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}