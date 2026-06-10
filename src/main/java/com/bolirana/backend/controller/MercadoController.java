package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.service.MercadoService;
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
@RequestMapping("/api/mercados")
@RequiredArgsConstructor
public class MercadoController {

    private final MercadoService mercadoService;

    @GetMapping
    public List<Mercado> listar() {
        return mercadoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mercado> buscarPorId(@PathVariable Long id) {
        return mercadoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mercado> crear(@RequestBody Mercado mercado) {
        Mercado creado = mercadoService.crear(mercado);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
