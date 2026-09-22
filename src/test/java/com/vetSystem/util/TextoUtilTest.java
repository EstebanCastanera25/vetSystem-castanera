package com.vetSystem.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de la normalizacion de texto.
 *
 * <p>Es el test unitario mas puro del proyecto: NO tiene mocks, ni @ExtendWith, ni contexto
 * de Spring. Eso no es casualidad: TextoUtil no tiene dependencias (es una clase de metodos
 * estaticos sin estado), asi que no hay nada que simular. Cuando una clase no necesita
 * mocks, es senal de que esta bien aislada.
 */
@DisplayName("Tests unitarios de TextoUtil")
class TextoUtilTest {

    @Test
    @DisplayName("aTitulo pone en mayuscula la primera letra de cada palabra")
    void aTitulo_cuandoElTextoEstaEnMinusculas_capitalizaCadaPalabra() {
        // ARRANGE + ACT
        String resultado = TextoUtil.aTitulo("juan carlos perez");

        // ASSERT
        assertThat(resultado).isEqualTo("Juan Carlos Perez");
    }

    @Test
    @DisplayName("aTitulo baja a minuscula el resto de cada palabra")
    void aTitulo_cuandoElTextoEstaTodoEnMayusculas_dejaSoloLaInicial() {
        // ARRANGE + ACT: este es el caso que motiva todo. Da igual como lo escriba el
        // usuario, el dato se guarda siempre de la misma forma.
        String resultado = TextoUtil.aTitulo("JUAN CARLOS PEREZ");

        // ASSERT
        assertThat(resultado).isEqualTo("Juan Carlos Perez");
    }

    @Test
    @DisplayName("aTitulo saca los espacios de las puntas y los repetidos del medio")
    void aTitulo_cuandoHayEspaciosDeMas_losColapsa() {
        // ARRANGE + ACT
        String resultado = TextoUtil.aTitulo("   juan    carlos   ");

        // ASSERT: "juan   carlos" y "juan carlos" tienen que quedar guardados igual
        assertThat(resultado).isEqualTo("Juan Carlos");
    }

    @Test
    @DisplayName("aTitulo respeta los acentos")
    void aTitulo_cuandoElTextoTieneAcentos_losMantiene() {
        // ARRANGE + ACT
        String resultado = TextoUtil.aTitulo("josé maría gonzález");

        // ASSERT
        assertThat(resultado).isEqualTo("José María González");
    }

    @Test
    @DisplayName("aTitulo tambien capitaliza despues de un guion o un apostrofo")
    void aTitulo_cuandoHayGuionOApostrofo_capitalizaLaPalabraSiguiente() {
        // ARRANGE + ACT
        String conGuion = TextoUtil.aTitulo("maría-luisa");
        String conApostrofo = TextoUtil.aTitulo("o'connor");

        // ASSERT: sin esto quedarian "María-luisa" y "O'connor"
        assertThat(conGuion).isEqualTo("María-Luisa");
        assertThat(conApostrofo).isEqualTo("O'Connor");
    }

    @Test
    @DisplayName("aTitulo capitaliza TODAS las palabras, tambien las particulas")
    void aTitulo_cuandoElApellidoTieneParticulas_lasCapitalizaTambien() {
        // ARRANGE + ACT
        String resultado = TextoUtil.aTitulo("de la torre");

        // ASSERT: es una decision tomada a proposito. La alternativa ("de la Torre") obliga
        // a mantener una lista de particulas del castellano; se eligio una sola regla sin
        // excepciones porque es predecible y no hay nada que recordar.
        assertThat(resultado).isEqualTo("De La Torre");
    }

    @Test
    @DisplayName("aTitulo devuelve null cuando recibe null")
    void aTitulo_cuandoElTextoEsNull_devuelveNull() {
        // ARRANGE + ACT + ASSERT: la raza de una mascota es opcional y puede llegar en null.
        // Devolver null NO es lo mismo que devolver una cadena vacia: si se lo cambiara por
        // "", una mascota sin raza pasaria a tener una raza vacia guardada.
        assertThat(TextoUtil.aTitulo(null)).isNull();
    }

    @Test
    @DisplayName("aTitulo devuelve una cadena vacia cuando recibe solo espacios")
    void aTitulo_cuandoElTextoEsSoloEspacios_devuelveCadenaVacia() {
        // ARRANGE + ACT + ASSERT: el @NotBlank del DTO ya rechaza este caso antes de llegar
        // al service, pero el metodo no tiene que romperse igual.
        assertThat(TextoUtil.aTitulo("     ")).isEmpty();
        assertThat(TextoUtil.aTitulo("")).isEmpty();
    }

    @Test
    @DisplayName("aMinusculas normaliza el email")
    void aMinusculas_cuandoElEmailTieneMayusculas_loBajaTodo() {
        // ARRANGE + ACT
        String resultado = TextoUtil.aMinusculas("  JUAN@Mail.COM ");

        // ASSERT: el dominio no distingue mayusculas, asi que es la misma casilla
        assertThat(resultado).isEqualTo("juan@mail.com");
    }

    @Test
    @DisplayName("aMayusculas normaliza la matricula")
    void aMayusculas_cuandoLaMatriculaVieneEnMinusculas_laSube() {
        // ARRANGE + ACT
        String resultado = TextoUtil.aMayusculas(" mv-4521 ");

        // ASSERT
        assertThat(resultado).isEqualTo("MV-4521");
    }

    @Test
    @DisplayName("aMinusculas y aMayusculas tambien son null-safe")
    void aMinusculasYAMayusculas_cuandoElTextoEsNull_devuelvenNull() {
        // ARRANGE + ACT + ASSERT
        assertThat(TextoUtil.aMinusculas(null)).isNull();
        assertThat(TextoUtil.aMayusculas(null)).isNull();
    }
}
