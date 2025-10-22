package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.ConfigAdminDTO;

public interface ConfigAdminService {
    ConfigAdminDTO getActual();
    ConfigAdminDTO save(ConfigAdminDTO dto);
}