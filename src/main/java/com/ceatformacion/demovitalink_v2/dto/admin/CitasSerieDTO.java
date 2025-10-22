package com.ceatformacion.demovitalink_v2.dto.admin;

import java.util.List;

public record CitasSerieDTO(
        List<String> labels,
        List<Long> created,
        List<Long> attended,
        List<Long> cancelled
) {}
