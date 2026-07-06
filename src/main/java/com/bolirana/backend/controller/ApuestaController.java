package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.dto.HistorialApostadorResponse;
import com.bolirana.backend.dto.LiquidarEventoRequest;
import com.bolirana.backend.dto.ResolverApuestaRequest;
import com.bolirana.backend.service.ApuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/apuestas")
@RequiredArgsConstructor
public class ApuestaController {

    private final ApuestaService apuestaService;

    /** Retorna la lista de todas las apuestas registradas en el sistema. */
    @GetMapping
    public List<Apuesta> listar() {
        return apuestaService.listar();
    }

    /**
     * Busca una apuesta por su identificador.
     *
     * @param id identificador de la apuesta
     * @return 200 con la apuesta si existe, 404 si no se encuentra
     */
    @GetMapping("/{id}")
    public ResponseEntity<Apuesta> buscarPorId(@PathVariable Long id) {
        return apuestaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * RF-22: Retorna el historial completo de un apostador: sus apuestas, su
     * saldo actual y sus movimientos de saldo.
     *
     * @param id identificador del usuario apostador
     * @return el saldo actual, las apuestas y los movimientos de saldo del apostador
     */
    @GetMapping("/usuario/{id}")
    public HistorialApostadorResponse listarPorApostador(@PathVariable Long id) {
        return apuestaService.obtenerHistorialApostador(id);
    }

    /**
     * Registra una nueva apuesta.
     *
     * @param apuesta datos de la apuesta a registrar
     * @return 201 con la apuesta creada
     */
    @PostMapping
    public ResponseEntity<Apuesta> crear(@RequestBody Apuesta apuesta) {
        Apuesta creada = apuestaService.crear(apuesta);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Resuelve una apuesta registrada, transicionándola a GANADA o PERDIDA.
     *
     * @param id      identificador de la apuesta a resolver
     * @param request resultado de la apuesta (GANADA o PERDIDA)
     * @return 200 con la apuesta actualizada
     */
    @PatchMapping("/{id}/resolver")
    public ResponseEntity<Apuesta> resolver(@PathVariable Long id, @RequestBody ResolverApuestaRequest request) {
        Apuesta actualizada = apuestaService.resolver(id, request.resultado());
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Paga una apuesta ganada, acreditando al apostador y transicionándola a PAGADA.
     *
     * @param id identificador de la apuesta a pagar
     * @return 200 con la apuesta actualizada
     */
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<Apuesta> pagar(@PathVariable Long id) {
        Apuesta actualizada = apuestaService.pagar(id);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * RF-15: Liquida un evento resolviendo y pagando todas sus apuestas REGISTRADA
     * según la opción ganadora.
     *
     * @param request identificador del evento y de la opción ganadora
     * @return 200 con las apuestas del evento tras la liquidación
     */
    @PostMapping("/liquidar")
    public ResponseEntity<List<Apuesta>> liquidar(@RequestBody LiquidarEventoRequest request) {
        List<Apuesta> apuestasLiquidadas =
                apuestaService.liquidarEvento(request.eventoId(), request.opcionGanadoraId());
        return ResponseEntity.ok(apuestasLiquidadas);
    }

    /**
     * RF-21: Historial de apuestas para el Administrador, con filtros opcionales
     * por apostador, evento, estado y rango de fechas de creación.
     * NOTA: pendiente restringir a rol ADMINISTRADOR cuando el proyecto incorpore
     * sesiones o JWT (mismo pendiente documentado en UsuarioController).
     *
     * @param apostadorId filtro opcional por identificador del apostador
     * @param eventoId    filtro opcional por identificador del evento
     * @param estado      filtro opcional por estado de la apuesta
     * @param fechaDesde  filtro opcional: fecha mínima de creación (inclusive)
     * @param fechaHasta  filtro opcional: fecha máxima de creación (inclusive)
     * @return las apuestas que cumplen todos los filtros indicados
     */
    @GetMapping("/historial")
    public List<Apuesta> historial(
            @RequestParam(required = false) Long apostadorId,
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) EstadoApuesta estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return apuestaService.buscarHistorial(apostadorId, eventoId, estado, fechaDesde, fechaHasta);
    }
}
