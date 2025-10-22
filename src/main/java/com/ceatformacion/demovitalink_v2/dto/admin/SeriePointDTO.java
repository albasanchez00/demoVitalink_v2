package com.ceatformacion.demovitalink_v2.dto.admin;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Objects;

public class SeriePointDTO {

    /** Fecha del punto (eje X). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    /** Valor numérico del punto (eje Y). */
    private double value;

    public SeriePointDTO() { }

    public SeriePointDTO(LocalDate date, double value) {
        this.date = date;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriePointDTO that)) return false;
        return Double.compare(that.value, value) == 0
                && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, value);
    }

    @Override
    public String toString() {
        return "SeriePointDTO{" +
                "date=" + date +
                ", value=" + value +
                '}';
    }
}