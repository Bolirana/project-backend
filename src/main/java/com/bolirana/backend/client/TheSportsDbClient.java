package com.bolirana.backend.client;

import com.bolirana.backend.client.dto.TheSportsDbEventoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * RF-04: Cliente para consumir TheSportsDB y obtener información
 * de equipos, deporte y fecha de los eventos deportivos.
 */
@Component
public class TheSportsDbClient {

    private final RestTemplate restTemplate;

    @Value("${apis.thesportsdb.base-url}")
    private String baseUrl;

    @Value("${apis.thesportsdb.api-key}")
    private String apiKey;

    public TheSportsDbClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Busca eventos próximos para un equipo dado por su ID en TheSportsDB.
     */
    public TheSportsDbEventoDTO.EventosResponse buscarProximosEventosPorEquipo(String idEquipo) {
        String url = String.format("%s/%s/eventsnext.php?id=%s", baseUrl, apiKey, idEquipo);
        return restTemplate.getForObject(url, TheSportsDbEventoDTO.EventosResponse.class);
    }

    /**
     * Busca un evento específico por su ID en TheSportsDB.
     */
    public TheSportsDbEventoDTO.EventosResponse buscarEventoPorId(String idEvento) {
        String url = String.format("%s/%s/lookupevent.php?id=%s", baseUrl, apiKey, idEvento);
        return restTemplate.getForObject(url, TheSportsDbEventoDTO.EventosResponse.class);
    }
}
