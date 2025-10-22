package com.ceatformacion.demovitalink_v2.dto.admin;

import java.util.Objects;

public class OverviewKpiDTO {

    private long totalPacientes;
    private long tratamientosActivos;
    private long citasMes;
    /** Valor entre 0 y 1 (ej: 0.78 => 78%). */
    private double adherenciaPromedio;

    public OverviewKpiDTO() { }

    public OverviewKpiDTO(long totalPacientes, long tratamientosActivos, long citasMes, double adherenciaPromedio) {
        this.totalPacientes = totalPacientes;
        this.tratamientosActivos = tratamientosActivos;
        this.citasMes = citasMes;
        this.adherenciaPromedio = adherenciaPromedio;
    }

    public long getTotalPacientes() {
        return totalPacientes;
    }

    public void setTotalPacientes(long totalPacientes) {
        this.totalPacientes = totalPacientes;
    }

    public long getTratamientosActivos() {
        return tratamientosActivos;
    }

    public void setTratamientosActivos(long tratamientosActivos) {
        this.tratamientosActivos = tratamientosActivos;
    }

    public long getCitasMes() {
        return citasMes;
    }

    public void setCitasMes(long citasMes) {
        this.citasMes = citasMes;
    }

    public double getAdherenciaPromedio() {
        return adherenciaPromedio;
    }

    public void setAdherenciaPromedio(double adherenciaPromedio) {
        this.adherenciaPromedio = adherenciaPromedio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverviewKpiDTO that)) return false;
        return totalPacientes == that.totalPacientes
                && tratamientosActivos == that.tratamientosActivos
                && citasMes == that.citasMes
                && Double.compare(that.adherenciaPromedio, adherenciaPromedio) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPacientes, tratamientosActivos, citasMes, adherenciaPromedio);
    }

    @Override
    public String toString() {
        return "OverviewKpiDTO{" +
                "totalPacientes=" + totalPacientes +
                ", tratamientosActivos=" + tratamientosActivos +
                ", citasMes=" + citasMes +
                ", adherenciaPromedio=" + adherenciaPromedio +
                '}';
    }
}