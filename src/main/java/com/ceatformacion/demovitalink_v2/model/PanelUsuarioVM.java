package com.ceatformacion.demovitalink_v2.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanelUsuarioVM {
    public Integer tratamientosEnCurso;
    public Integer adherenciaPorc;     // 0..100
    public Integer citasPasadas;
    public Integer informes;           // sin modelo -> 0
    public String  proximoMedicamento;
    public String  proximaDosisHoraFmt;
    public String  proximaCitaFmt;
    public Integer alertasDosis;
    public Integer unread;             // mensajes sin leer
    public Integer notifs;             // sin modelo -> 0
    public Boolean perfilCompleto;
}