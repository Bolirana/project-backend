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

    /** Retorna la lista de todas las apuestas registradas en el sistema. */
    public List<Apuesta> listar() {
        return apuestaRepository.findAll();
    }

    /**
     * Busca y retorna una apuesta por su identificador único.
     *
     * @param id identificador de la apuesta
     * @return Optional con la apuesta si existe, vacío si no se encuentra
     */
    public Optional<Apuesta> buscarPorId(Long id) {
        return apuestaRepository.findById(id);
    }

    /**
     * Retorna las apuestas realizadas por un apostador específico.
     *
     * @param apostadorId identificador del usuario apostador
     * @return lista de apuestas realizadas por el apostador
     */
    public List<Apuesta> listarPorApostador(Long apostadorId) {
        return apuestaRepository.findByApostadorId(apostadorId);
    }

    /**
     * Registra una nueva apuesta, congelando la cuota vigente de la opción
     * seleccionada y marcando la apuesta como pendiente.
     *
     * @param apuesta datos de la apuesta a registrar
     * @return la apuesta creada y persistida
     * @throws IllegalArgumentException si la opción de apuesta no existe
     */
    public Apuesta crear(Apuesta apuesta) {
        OpcionApuesta opcion = opcionApuestaRepository.findById(apuesta.getOpcion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Opcion de apuesta no encontrada"));

        apuesta.setOpcion(opcion);
        apuesta.setCuotaCongelada(opcion.getCuotaActual());
        apuesta.setEstado("PENDIENTE");

        return apuestaRepository.save(apuesta);
    }
}
