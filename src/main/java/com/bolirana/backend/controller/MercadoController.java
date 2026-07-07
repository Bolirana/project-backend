package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.HistorialCuota;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.service.MercadoService;
import com.bolirana.backend.dto.CambioCuotaDTO;
import com.bolirana.backend.dto.HistorialCuotaDTO;
import com.bolirana.backend.dto.MercadoResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mercados")
@RequiredArgsConstructor
public class MercadoController {

    private final MercadoService mercadoService;

    /** Retorna la lista de todos los mercados registrados en el sistema. */
    @GetMapping
    public List<MercadoResumenDTO> listar() {
        return mercadoService.listar().stream().map(MercadoResumenDTO::desdeEntidad).toList();
    }

    /**
     * Busca un mercado por su identificador.
     * Como el servicio unificado lanza excepción si no se encuentra,
     * el método retorna directamente 200 OK con el objeto.
     *
     * @param id identificador del mercado
     * @return 200 con el mercado
     */
    @GetMapping("/{id}")
    public ResponseEntity<MercadoResumenDTO> buscarPorId(@PathVariable Long id) {
        Mercado mercado = mercadoService.buscarPorId(id);
        return ResponseEntity.ok(MercadoResumenDTO.desdeEntidad(mercado));
    }

    /**
     * Crea un nuevo mercado.
     *
     * @param mercado datos del mercado a crear
     * @return 201 con el mercado creado
     */
    @PostMapping
    public ResponseEntity<MercadoResumenDTO> crear(@RequestBody Mercado mercado) {
        Mercado creado = mercadoService.crear(mercado);
        return ResponseEntity.status(HttpStatus.CREATED).body(MercadoResumenDTO.desdeEntidad(creado));
    }

    /**
     * RF-23: Permite al Administrador cambiar manualmente la cuota de una opción de apuesta.
     */
    @PatchMapping("/opciones/{opcionApuestaId}/cuota")
    public ResponseEntity<OpcionApuesta> cambiarCuota(
            @PathVariable Long opcionApuestaId, 
            @RequestParam BigDecimal nuevaCuota) {
        OpcionApuesta opcionActualizada = mercadoService.cambiarCuota(opcionApuestaId, nuevaCuota);
        return ResponseEntity.ok(opcionActualizada);
    }

    /**
     * RF-23: Permite consultar la auditoría/historial de cambios de cuotas de una opción.
     */
    @GetMapping("/opciones/{opcionApuestaId}/historial-cuotas")
    public List<HistorialCuota> obtenerHistorialCuotas(@PathVariable Long opcionApuestaId) {
        return mercadoService.obtenerHistorialCuotas(opcionApuestaId);
    }
}
