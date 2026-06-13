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

    /** Retorna la lista de todos los movimientos de saldo registrados en el sistema. */
    @GetMapping
    public List<MovimientoSaldo> listar() {
        return movimientoSaldoService.listar();
    }

    /**
     * Retorna los movimientos de saldo registrados para un usuario.
     *
     * @param id identificador del usuario
     * @return lista de movimientos de saldo del usuario
     */
    @GetMapping("/usuario/{id}")
    public List<MovimientoSaldo> listarPorUsuario(@PathVariable Long id) {
        return movimientoSaldoService.listarPorUsuario(id);
    }

    /**
     * Registra un nuevo movimiento de saldo.
     *
     * @param movimientoSaldo datos del movimiento de saldo a registrar
     * @return 201 con el movimiento de saldo creado
     */
    @PostMapping
    public ResponseEntity<MovimientoSaldo> crear(@RequestBody MovimientoSaldo movimientoSaldo) {
        MovimientoSaldo creado = movimientoSaldoService.crear(movimientoSaldo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
