package com.bolirana.backend.service;

import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.repository.MercadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MercadoService {

    private final MercadoRepository mercadoRepository;

    /** Retorna la lista de todos los mercados registrados en el sistema. */
    public List<Mercado> listar() {
        return mercadoRepository.findAll();
    }

    /**
     * Busca y retorna un mercado por su identificador único.
     *
     * @param id identificador del mercado
     * @return Optional con el mercado si existe, vacío si no se encuentra
     */
    public Optional<Mercado> buscarPorId(Long id) {
        return mercadoRepository.findById(id);
    }

    /**
     * Retorna los mercados asociados a un evento específico.
     *
     * @param eventoId identificador del evento
     * @return lista de mercados asociados al evento
     */
    public List<Mercado> listarPorEvento(Long eventoId) {
        return mercadoRepository.findByEventoId(eventoId);
    }

    /**
     * Crea y persiste un nuevo mercado.
     *
     * @param mercado datos del mercado a crear
     * @return el mercado creado y persistido
     */
    public Mercado crear(Mercado mercado) {
        return mercadoRepository.save(mercado);
    }
}
