package com.ceatformacion.demovitalink_v2.dto.admin;

import java.util.List;

public record SerieDTO(
        List<String> labels,
        List<Number> values
) {}