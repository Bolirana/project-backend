package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApuestaServiceTest {

    @Mock
    private ApuestaRepository apuestaRepository;

    @Mock
    private OpcionApuestaRepository opcionApuestaRepository;

    @InjectMocks
    private ApuestaService apuestaService;

    @Test
    @DisplayName("crear() asigna cuotaCongelada igual a cuotaActual de la opción en el momento de registrar")
    void crear_opcionExistente_congelajCuotaActual() {
        // Caso límite: la cuota puede cambiar después; lo que se guarda debe ser el valor vigente al momento de crear
        OpcionApuesta opcion = new OpcionApuesta();
        opcion.setId(1L);
        opcion.setCuotaActual(2.5);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(1L)).thenReturn(Optional.of(opcion));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.crear(apuesta);

        assertThat(resultado.getCuotaCongelada()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("crear() inicializa el estado de la apuesta en PENDIENTE")
    void crear_opcionExistente_inicializaEstadoPendiente() {
        // Caso límite: el estado inicial siempre debe ser PENDIENTE sin importar lo que el cliente envíe
        OpcionApuesta opcion = new OpcionApuesta();
        opcion.setId(2L);
        opcion.setCuotaActual(1.8);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setMonto(5000.0);

        when(opcionApuestaRepository.findById(2L)).thenReturn(Optional.of(opcion));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.crear(apuesta);

        assertThat(resultado.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando la opción no existe")
    void crear_opcionInexistente_lanzaExcepcionSinGuardar() {
        // Caso límite: el id de opción no existe en la base de datos, la apuesta no debe persistirse
        OpcionApuesta opcionInexistente = new OpcionApuesta();
        opcionInexistente.setId(999L);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcionInexistente);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apuestaService.crear(apuesta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Opcion de apuesta no encontrada");

        verify(apuestaRepository, never()).save(apuesta);
    }
}
