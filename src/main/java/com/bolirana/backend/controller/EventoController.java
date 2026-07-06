package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.dto.EventoCreacionDTO;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    /** Retorna la lista de todos los eventos registrados en el sistema. */
    @GetMapping
    public List<Evento> listar() {
        return eventoService.listarEventos();
    }

    /**
     * Busca un evento por su identificador.
     * Modificado: Como el servicio unificado lanza excepción si no se encuentra, 
     * el método retorna directamente 200 OK con el objeto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        Evento evento = eventoService.obtenerEventoPorId(id);
        return ResponseEntity.ok(evento);
    }

    /**
     * Retorna los eventos filtrados por un estado específico.
     */
    @GetMapping("/estado/{estado}")
    public List<Evento> listarPorEstado(@PathVariable EstadoEvento estado) {
        return eventoService.listarPorEstado(estado);
    }

    /**
     * Crea un nuevo evento con sus mercados y opciones de apuesta.
     */
    @PostMapping
    public ResponseEntity<Evento> crear(@RequestBody EventoCreacionDTO dto) {
        Evento creado = eventoService.crearEvento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * RF-05 / RF-16: Cambia el estado de un evento validando las transiciones permitidas.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Evento> cambiarEstado(@PathVariable Long id, @RequestParam EstadoEvento nuevoEstado) {
        Evento actualizado = eventoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(actualizado);
    }
}
