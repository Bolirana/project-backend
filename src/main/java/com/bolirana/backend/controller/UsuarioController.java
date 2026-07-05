package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.dto.RegistroUsuarioRequest;
import com.bolirana.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** Retorna la lista de todos los usuarios registrados en el sistema. */
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return 200 con el usuario si existe, 404 si no se encuentra
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo usuario.
     *
     * @param usuario datos del usuario a crear
     * @return 201 con el usuario creado
     */
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        Usuario creado = usuarioService.crear(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Registra un nuevo usuario (rol Apostador, cuenta activa por defecto).
     * La contrasena se hashea antes de persistirse.
     *
     * @param request datos de registro del usuario
     * @return 201 con el usuario creado (sin el hash de la contrasena)
     */
    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrar(@RequestBody RegistroUsuarioRequest request) {
        Usuario creado = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Activa la cuenta de un usuario.
     * NOTA: este endpoint deberia restringirse a llamadas hechas por un Administrador
     * autenticado; la verificacion de quien hace la peticion queda pendiente hasta que
     * el proyecto incorpore sesiones o JWT.
     *
     * @param id identificador del usuario
     * @return 200 con el usuario actualizado, 404 si no existe
     */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Usuario> activar(@PathVariable Long id) {
        return usuarioService.activar(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Suspende la cuenta de un usuario, impidiendole iniciar sesion.
     * NOTA: mismo pendiente de autorizacion que {@link #activar(Long)}.
     *
     * @param id identificador del usuario
     * @return 200 con el usuario actualizado, 404 si no existe
     */
    @PatchMapping("/{id}/suspender")
    public ResponseEntity<Usuario> suspender(@PathVariable Long id) {
        return usuarioService.suspender(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Elimina (logicamente) la cuenta de un usuario. No borra la fila de la base de
     * datos para preservar la integridad historica con Apuesta y MovimientoSaldo.
     * NOTA: mismo pendiente de autorizacion que {@link #activar(Long)}.
     *
     * @param id identificador del usuario
     * @return 200 con el usuario actualizado, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Usuario> eliminar(@PathVariable Long id) {
        return usuarioService.eliminar(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
