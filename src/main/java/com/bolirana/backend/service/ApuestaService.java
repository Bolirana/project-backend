package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApuestaService {

    private final ApuestaRepository apuestaRepository;
    private final OpcionApuestaRepository opcionApuestaRepository;

    public List<Apuesta> listar() {
        return apuestaRepository.findAll();
    }

    public Optional<Apuesta> buscarPorId(Long id) {
        return apuestaRepository.findById(id);
    }

    public List<Apuesta> listarPorApostador(Long apostadorId) {
        return apuestaRepository.findByApostadorId(apostadorId);
    }

    public Apuesta crear(Apuesta apuesta) {
        OpcionApuesta opcion = opcionApuestaRepository.findById(apuesta.getOpcion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Opcion de apuesta no encontrada"));

        apuesta.setOpcion(opcion);
        apuesta.setCuotaCongelada(opcion.getCuotaActual());
        apuesta.setEstado("PENDIENTE");

        return apuestaRepository.save(apuesta);
    }
}
