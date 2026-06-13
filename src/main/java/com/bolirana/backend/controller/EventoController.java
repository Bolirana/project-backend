package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    /** Retorna la lista de todos los eventos registrados en el sistema. */
    @GetMapping
    public List<Evento> listar() {
        return eventoService.listar();
    }

    /**
     * Busca un evento por su identificador.
     *
     * @param id identificador del evento
     * @return 200 con el evento si existe, 404 si no se encuentra
     */
    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return eventoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo evento.
     *
     * @param evento datos del evento a crear
     * @return 201 con el evento creado
     */
    @PostMapping
    public ResponseEntity<Evento> crear(@RequestBody Evento evento) {
        Evento creado = eventoService.crear(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
