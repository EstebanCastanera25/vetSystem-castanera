package com.vetSystem.util;

/**
 * Normaliza el texto que llega de la API antes de guardarlo.
 *
 * <p>El problema que resuelve: si uno carga "juan perez" y otro "JUAN PEREZ", la base
 * termina con dos formas distintas del mismo dato y la tabla del frontend se ve despareja.
 * Con esto, no importa cómo lo escriba el usuario: siempre se guarda igual.
 *
 * <p>Es una clase de utilidades: sólo métodos {@code static}, sin estado y sin dependencias.
 * No es un {@code @Service} porque no tiene nada que inyectar ni nada que recordar; por eso
 * tampoco hace falta mockearla para probarla.
 *
 * <p>Los tres métodos son null-safe: hay campos opcionales (la raza de una mascota) que
 * pueden llegar en null, y devolver null es distinto de devolver una cadena vacía.
 */
public final class TextoUtil {

    // Constructor privado: esta clase no se instancia, se usan sus métodos estáticos.
    private TextoUtil() {
    }

    /**
     * Pasa un texto a "Primera Letra De Cada Palabra En Mayúscula".
     *
     * <p>Ejemplos: {@code "  JUAN   carlos "} queda {@code "Juan Carlos"},
     * {@code "josé maría"} queda {@code "José María"} y {@code "o'connor"} queda
     * {@code "O'Connor"}.
     *
     * <p>Se aplica UNA regla, sin excepciones: cada palabra arranca en mayúscula. Eso
     * significa que "de la torre" queda "De La Torre" y no "de la Torre" — es una decisión
     * tomada a propósito para no tener que mantener una lista de partículas del castellano.
     */
    public static String aTitulo(String texto) {
        if (texto == null) {
            return null;
        }

        // trim() saca los espacios de las puntas y el replaceAll deja UN solo espacio
        // entre palabras: "juan   carlos" y "juan carlos" tienen que guardarse igual.
        String limpio = texto.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            return limpio;
        }

        StringBuilder resultado = new StringBuilder(limpio.length());
        boolean empiezaPalabra = true;

        for (int i = 0; i < limpio.length(); i++) {
            char letra = limpio.charAt(i);

            if (empiezaPalabra) {
                // Se usa Character.toUpperCase y no String.toUpperCase porque el de String
                // depende del idioma configurado en la máquina. Con los acentos funciona
                // igual: la é en mayúscula es É.
                resultado.append(Character.toUpperCase(letra));
            } else {
                // El resto en minúscula es lo que hace que "JUAN" se guarde "Juan"
                resultado.append(Character.toLowerCase(letra));
            }

            // Después de un espacio, un guion o un apóstrofo arranca otra palabra:
            // así "maría-luisa" queda "María-Luisa" y no "María-luisa".
            empiezaPalabra = (letra == ' ' || letra == '-' || letra == '\'');
        }

        return resultado.toString();
    }

    /**
     * Pasa un texto a minúsculas, sin espacios en las puntas. Se usa para el email: el
     * dominio no distingue mayúsculas, así que "JUAN@Mail.COM" y "juan@mail.com" son la
     * misma casilla y no tiene sentido guardarlas distinto.
     */
    public static String aMinusculas(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.trim().toLowerCase();
    }

    /**
     * Pasa un texto a MAYÚSCULAS, sin espacios en las puntas. Se usa para la matrícula del
     * veterinario, que por contrato tiene la forma MV-1234.
     */
    public static String aMayusculas(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.trim().toUpperCase();
    }
}
