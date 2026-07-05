package com.bolirana.backend.controller;

import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.dto.LoginRequest;
import com.bolirana.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * Inicia sesion validando correo y contrasena. Bloquea el acceso si la cuenta
     * esta suspendida o eliminada. El rol retornado permite al frontend mostrar
     * unicamente las funciones correspondientes a ese rol.
     *
     * @param request credenciales de acceso (correo y contrasena en texto plano)
     * @return 200 con el usuario autenticado (sin el hash de la contrasena)
     */
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.login(request);
        return ResponseEntity.ok(usuario);
    }
}
