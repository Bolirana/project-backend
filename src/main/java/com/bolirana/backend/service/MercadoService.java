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

    public List<Mercado> listar() {
        return mercadoRepository.findAll();
    }

    public Optional<Mercado> buscarPorId(Long id) {
        return mercadoRepository.findById(id);
    }

    public List<Mercado> listarPorEvento(Long eventoId) {
        return mercadoRepository.findByEventoId(eventoId);
    }

    public Mercado crear(Mercado mercado) {
        return mercadoRepository.save(mercado);
    }
}
