package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.service.ApuestaService;
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
@RequestMapping("/api/apuestas")
@RequiredArgsConstructor
public class ApuestaController {

    private final ApuestaService apuestaService;

    @GetMapping
    public List<Apuesta> listar() {
        return apuestaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apuesta> buscarPorId(@PathVariable Long id) {
        return apuestaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{id}")
    public List<Apuesta> listarPorApostador(@PathVariable Long id) {
        return apuestaService.listarPorApostador(id);
    }

    @PostMapping
    public ResponseEntity<Apuesta> crear(@RequestBody Apuesta apuesta) {
        Apuesta creada = apuestaService.crear(apuesta);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
