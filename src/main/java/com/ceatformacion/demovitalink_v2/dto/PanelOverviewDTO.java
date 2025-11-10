package com.ceatformacion.demovitalink_v2.dto;

public record PanelOverviewDTO(
        Integer tratamientosEnCurso,
        Integer adherenciaPorc,
        Integer citasPasadas,
        Integer alertasDosis,
        String  proximoMedicamento,
        String  proximaDosisHoraFmt,
        String  proximaCitaFmt,
        Integer unread // mensajes sin leer
) {}