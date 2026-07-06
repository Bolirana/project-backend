package com.bolirana.backend.client;

import com.bolirana.backend.client.dto.OddsApiEventoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * RF-04: Cliente para consumir The Odds API y obtener las cuotas
 * de referencia del mercado real para un deporte/evento.
 */
@Component
public class OddsApiClient {

    private final RestTemplate restTemplate;

    @Value("${apis.odds.base-url}")
    private String baseUrl;

    @Value("${apis.odds.api-key}")
    private String apiKey;

    public OddsApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene las cuotas de referencia para todos los eventos disponibles
     * de un deporte determinado (ej: "soccer_epl").
     */
    public OddsApiEventoDTO[] obtenerCuotasPorDeporte(String sportKey, String regions, String markets) {
        String url = String.format("%s/sports/%s/odds/?apiKey=%s&regions=%s&markets=%s&oddsFormat=decimal",
                baseUrl, sportKey, apiKey, regions, markets);
        return restTemplate.getForObject(url, OddsApiEventoDTO[].class);
    }
}
