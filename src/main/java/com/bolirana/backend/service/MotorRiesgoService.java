package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.ConfiguracionRiesgo;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.dto.ExposicionEventoDTO;
import com.bolirana.backend.dto.ExposicionMercadoDTO;
import com.bolirana.backend.dto.ExposicionOpcionDTO;
import com.bolirana.backend.dto.SugerenciaCuotaDTO;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.MercadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Motor de Riesgo (M4): calcula la exposición económica por opción de apuesta (RF-12),
 * genera alertas cuando esa exposición supera el límite configurado (RF-13) y sugiere
 * un ajuste de cuota para mitigarla (RF-14).
 *
 * RNF-05: se declara explícitamente con scope singleton para dejar constancia de que
 * existe una única instancia compartida por todo el contenedor de Spring (el scope por
 * defecto ya es singleton; la anotación solo lo hace explícito).
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@RequiredArgsConstructor
public class MotorRiesgoService {

    private static final double LIMITE_ALERTA_DEFECTO = 500_000.0;
    private static final BigDecimal CUOTA_MINIMA_SUGERIDA = new BigDecimal("1.01");

    private final EventoService eventoService;
    private final MercadoRepository mercadoRepository;
    private final ApuestaRepository apuestaRepository;
    private final ConfiguracionRiesgoService configuracionRiesgoService;

    /** RF-12/RF-13/RF-14: exposición, alertas y sugerencias de cuota para cada opción del evento. */
    @Transactional(readOnly = true)
    public ExposicionEventoDTO calcularExposicion(Long eventoId) {
        Evento evento = eventoService.obtenerEventoPorId(eventoId);
        List<Mercado> mercados = mercadoRepository.findByEventoId(eventoId);
        Map<Long, Double> exposicionPorOpcion = exposicionPorOpcion(eventoId);

        List<ExposicionMercadoDTO> mercadosDTO = mercados.stream()
                .map(mercado -> calcularExposicionMercado(mercado, exposicionPorOpcion))
                .toList();

        return new ExposicionEventoDTO(evento.getId(), evento.getNombre(), mercadosDTO);
    }

    private Map<Long, Double> exposicionPorOpcion(Long eventoId) {
        List<Apuesta> apuestasRegistradas =
                apuestaRepository.findByOpcionMercadoEventoIdAndEstado(eventoId, EstadoApuesta.REGISTRADA);

        return apuestasRegistradas.stream()
                .collect(Collectors.groupingBy(
                        apuesta -> apuesta.getOpcion().getId(),
                        Collectors.summingDouble(apuesta -> apuesta.getMonto() * apuesta.getCuotaCongelada())));
    }

    private ExposicionMercadoDTO calcularExposicionMercado(Mercado mercado, Map<Long, Double> exposicionPorOpcion) {
        double limite = configuracionRiesgoService.buscarPorMercado(mercado.getId())
                .map(ConfiguracionRiesgo::getLimiteAlerta)
                .orElse(LIMITE_ALERTA_DEFECTO);

        List<ExposicionOpcionDTO> opcionesDTO = mercado.getOpciones().stream()
                .map(opcion -> calcularExposicionOpcion(
                        opcion, exposicionPorOpcion.getOrDefault(opcion.getId(), 0.0), limite))
                .toList();

        return new ExposicionMercadoDTO(mercado.getId(), mercado.getNombre(), opcionesDTO);
    }

    private ExposicionOpcionDTO calcularExposicionOpcion(OpcionApuesta opcion, double exposicion, double limite) {
        boolean alerta = exposicion > limite;
        SugerenciaCuotaDTO sugerencia = alerta ? sugerirAjusteCuota(opcion, exposicion, limite) : null;
        return new ExposicionOpcionDTO(
                opcion.getId(), opcion.getNombre(), opcion.getCuotaActual(), exposicion, limite, alerta, sugerencia);
    }

    /** RF-14: cuanto más se exceda el límite, más se reduce la cuota sugerida (con piso de 1.01). */
    private SugerenciaCuotaDTO sugerirAjusteCuota(OpcionApuesta opcion, double exposicion, double limite) {
        BigDecimal cuotaActual = opcion.getCuotaActual();
        BigDecimal factorAjuste = BigDecimal.valueOf(limite / exposicion);
        BigDecimal cuotaSugerida = cuotaActual.multiply(factorAjuste).setScale(2, RoundingMode.HALF_UP);
        if (cuotaSugerida.compareTo(CUOTA_MINIMA_SUGERIDA) < 0) {
            cuotaSugerida = CUOTA_MINIMA_SUGERIDA;
        }

        String razon = String.format(
                "La exposición de esta opción ($%.2f) supera el límite configurado ($%.2f). "
                        + "Se sugiere reducir la cuota para desincentivar nuevas apuestas en esta opción.",
                exposicion, limite);

        return new SugerenciaCuotaDTO(cuotaActual, cuotaSugerida, razon);
    }
}
