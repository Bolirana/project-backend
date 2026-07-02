package com.example.eventos.service;

import com.example.eventos.client.OddsApiClient;
import com.example.eventos.client.TheSportsDbClient;
import com.example.eventos.client.dto.OddsApiEventoDTO;
import com.example.eventos.client.dto.TheSportsDbEventoDTO;
import org.springframework.stereotype.Service;

/**
 * RF-04: Servicio que encapsula la consulta a las APIs externas
 * (TheSportsDB para datos de equipos/deporte/fecha, The Odds API
 * para las cuotas de referencia del mercado real).
 *
 * Estos datos son utilizados como información de apoyo para que el
 * Administrador cree el evento; las cuotas finales siempre las define
 * manualmente el Administrador.
 */
@Service
public class IntegracionDeportivaService {

    private final TheSportsDbClient theSportsDbClient;
    private final OddsApiClient oddsApiClient;

    public IntegracionDeportivaService(TheSportsDbClient theSportsDbClient, OddsApiClient oddsApiClient) {
        this.theSportsDbClient = theSportsDbClient;
        this.oddsApiClient = oddsApiClient;
    }

    /** Consulta información de próximos eventos de un equipo en TheSportsDB. */
    public TheSportsDbEventoDTO.EventosResponse obtenerProximosEventosEquipo(String idEquipo) {
        return theSportsDbClient.buscarProximosEventosPorEquipo(idEquipo);
    }

    /** Consulta información de un evento específico en TheSportsDB. */
    public TheSportsDbEventoDTO.EventosResponse obtenerEventoPorId(String idEvento) {
        return theSportsDbClient.buscarEventoPorId(idEvento);
    }

    /** Consulta las cuotas de referencia del mercado real en The Odds API. */
    public OddsApiEventoDTO[] obtenerCuotasReferencia(String sportKey, String regions, String markets) {
        return oddsApiClient.obtenerCuotasPorDeporte(sportKey, regions, markets);
    }
}
