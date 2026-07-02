package com.example.eventos.controller;

import com.example.eventos.client.dto.OddsApiEventoDTO;
import com.example.eventos.client.dto.TheSportsDbEventoDTO;
import com.example.eventos.service.IntegracionDeportivaService;
import org.springframework.web.bind.annotation.*;

/**
 * RF-04: Endpoints de apoyo para que el Administrador consulte información
 * importada desde TheSportsDB (equipos, deporte, fecha) y The Odds API
 * (cuotas de referencia) al momento de crear un evento.
 */
@RestController
@RequestMapping("/api/integracion-deportiva")
public class IntegracionDeportivaController {

    private final IntegracionDeportivaService integracionService;

    public IntegracionDeportivaController(IntegracionDeportivaService integracionService) {
        this.integracionService = integracionService;
    }

    /** Próximos eventos de un equipo (TheSportsDB), usados para autocompletar datos del evento. */
    @GetMapping("/sportsdb/equipos/{idEquipo}/proximos-eventos")
    public TheSportsDbEventoDTO.EventosResponse proximosEventosPorEquipo(@PathVariable String idEquipo) {
        return integracionService.obtenerProximosEventosEquipo(idEquipo);
    }

    /** Detalle de un evento específico (TheSportsDB). */
    @GetMapping("/sportsdb/eventos/{idEvento}")
    public TheSportsDbEventoDTO.EventosResponse eventoPorId(@PathVariable String idEvento) {
        return integracionService.obtenerEventoPorId(idEvento);
    }

    /** Cuotas de referencia del mercado real (The Odds API) para un deporte. */
    @GetMapping("/odds/{sportKey}")
    public OddsApiEventoDTO[] cuotasReferencia(@PathVariable String sportKey,
                                                @RequestParam(defaultValue = "us") String regions,
                                                @RequestParam(defaultValue = "h2h") String markets) {
        return integracionService.obtenerCuotasReferencia(sportKey, regions, markets);
    }
}
