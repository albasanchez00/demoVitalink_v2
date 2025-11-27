package com.ceatformacion.demovitalink_v2.dto.config;

import java.util.Map;

public record IntegracionesDTO(
        Boolean googleCalendar,
        Boolean outlookCalendar,
        Boolean smsProvider,
        Map<String, String> apiKeys  // {"google": "AIza...", "outlook": "XXX", "sms": "key_123"}
) {}