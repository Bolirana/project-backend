package com.bolirana.backend.dto;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.Usuario;

import java.time.LocalDateTime;

/**
 * Forma de respuesta para las apuestas: reemplaza la serialización directa
 * de la entidad Apuesta, que via opcion-&gt;mercado-&gt;evento-&gt;mercados
 * dependía de @JsonIdentityInfo para no recursar infinitamente. Ese mecanismo
 * falla cuando dos apuestas distintas comparten la misma opción/mercado/evento
 * en una misma respuesta (la segunda aparición queda colapsada a solo un id).
 * Al usar un DTO plano construido de nuevo para cada apuesta, cada una lleva
 * siempre sus datos completos sin importar cuántas compartan la misma opción.
 */
public record ApuestaRespuestaDTO(
        Long id,
        Usuario apostador,
        OpcionResumenDTO opcion,
        Double monto,
        Double cuotaCongelada,
        EstadoApuesta estado,
        LocalDateTime creadoEn) {

    public static ApuestaRespuestaDTO desdeEntidad(Apuesta apuesta) {
        return new ApuestaRespuestaDTO(
                apuesta.getId(),
                apuesta.getApostador(),
                OpcionResumenDTO.desdeEntidad(apuesta.getOpcion()),
                apuesta.getMonto(),
                apuesta.getCuotaCongelada(),
                apuesta.getEstado(),
                apuesta.getCreadoEn());
    }
}
