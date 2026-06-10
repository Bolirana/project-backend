package com.bolirana.backend.controller;

import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.service.MovimientoSaldoService;
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
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoSaldoController {

    private final MovimientoSaldoService movimientoSaldoService;

    @GetMapping("/usuario/{id}")
    public List<MovimientoSaldo> listarPorUsuario(@PathVariable Long id) {
        return movimientoSaldoService.listarPorUsuario(id);
    }

    @PostMapping
    public ResponseEntity<MovimientoSaldo> crear(@RequestBody MovimientoSaldo movimientoSaldo) {
        MovimientoSaldo creado = movimientoSaldoService.crear(movimientoSaldo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
