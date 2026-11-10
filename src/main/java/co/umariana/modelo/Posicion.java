
package co.umariana.modelo;

/**
 * ============================================================
 * CLASE: Posicion
 * ============================================================
 *
 * Representa, valida y calcula las posiciones de las personas
 * dentro de un árbol genealógico.
 *
 * La clase utiliza dos sistemas de numeración:
 *
 * 1. AHNENTAFEL:
 *    Se utiliza para identificar a los ascendientes:
 *    padres, madres, abuelos, bisabuelos, etc.
 *
 * 2. D'ABOVILLE:
 *    Se utiliza para identificar a los descendientes:
 *    hijos, nietos, bisnietos, etc.
 *
 * RESPONSABILIDAD:
 * Controlar que las posiciones sean válidas y proporcionar
 * operaciones para calcular las relaciones genealógicas.
 *
 * IMPORTANTE:
 * Esta clase es final porque no está diseñada para heredarse.
 * Sus métodos son static porque no necesita crear objetos
 * para realizar las operaciones.
 *
 * ETAPA 1:
 * Implementación de los métodos de validación y cálculo.
 */
public final class Posicion {


    // ========================================================
    // 1. CONSTANTES
    // ========================================================

    /**
     * Posición correspondiente al sujeto principal del árbol.
     */
    public static final String SUJETO = "1";

    /**
     * Número máximo permitido para el sistema Ahnentafel.
     *
     * Permite representar hasta 10 generaciones de ascendientes.
     */
    public static final int MAX_AHNENTAFEL = 2047;

    /**
     * Número máximo de orden permitido para cada nivel
     * del sistema D'Aboville.
     */
    public static final int MAX_ORDEN = 9999;

    /**
     * Longitud máxima permitida para una posición D'Aboville.
     *
     * Se establece en 50 caracteres para respetar
     * el límite definido para el almacenamiento.
     */
    public static final int MAX_LONGITUD = 50;


    // ========================================================
    // 2. CONSTRUCTOR PRIVADO
    // ========================================================

    /**
     * Constructor privado.
     *
     * Evita que se creen objetos de esta clase.
     *
     * Posicion solamente contiene constantes y métodos
     * estáticos, por lo que no necesita instancias.
     */
    private Posicion() {
    }


    // ========================================================
    // 3. VALIDACIÓN DEL SUJETO
    // ========================================================

    /**
     * Comprueba si una posición corresponde exactamente
     * al sujeto principal del árbol genealógico.
     *
     * La posición del sujeto siempre es "1".
     *
     * @param posicion posición que se desea comprobar.
     * @return true si corresponde al sujeto, false en caso contrario.
     */
    public static boolean esSujeto(String posicion) {

        /*
         * equals() compara el contenido de dos cadenas.
         *
         * Se utiliza SUJETO.equals(posicion) para evitar
         * NullPointerException cuando posicion es null.
         */
        return SUJETO.equals(posicion);
    }


    // ========================================================
    // 4. VALIDACIÓN AHNENTAFEL
    // ========================================================

    /**
     * Comprueba si una posición cumple las reglas
     * del sistema Ahnentafel.
     *
     * REGLAS:
     *
     * - Debe ser un número entero positivo.
     * - No puede tener ceros a la izquierda.
     * - No puede contener signos, espacios ni puntos.
     * - Debe estar entre 1 y MAX_AHNENTAFEL.
     *
     * EJEMPLOS VÁLIDOS:
     * "1", "2", "13", "2047".
     *
     * EJEMPLOS INVÁLIDOS:
     * null, "", "0", "01", "-2", "2.0", "2048".
     *
     * @param posicion posición que se desea validar.
     * @return true si es Ahnentafel válida.
     */
    public static boolean esAhnentafel(String posicion) {

        // Verificar que la posición no sea null
        // y que no supere la longitud permitida.
        if (posicion == null || posicion.length() > 4) {
            return false;
        }

        /*
         * Expresión regular:
         *
         * [1-9] = el primer dígito debe estar entre 1 y 9.
         * [0-9] = los siguientes dígitos pueden estar entre 0 y 9.
         * *     = permite cero o más dígitos adicionales.
         *
         * Esto evita ceros iniciales y caracteres no numéricos.
         */
        if (!posicion.matches("[1-9][0-9]*")) {
            return false;
        }

        /*
         * Convertimos la cadena String a un número entero.
         *
         * Integer.parseInt() permite realizar la comparación
         * numérica con el límite MAX_AHNENTAFEL.
         */
        try {

            int numero = Integer.parseInt(posicion);

            // Comprobar que el número esté dentro del intervalo.
            return numero >= 1 && numero <= MAX_AHNENTAFEL;

        } catch (NumberFormatException e) {

            /*
             * Si el texto no puede convertirse a entero,
             * la posición no es válida.
             */
            return false;
        }
    }


    // ========================================================
    // 5. VALIDACIÓN D'ABOVILLE
    // ========================================================

    /**
     * Comprueba si una posición cumple las reglas
     * del sistema D'Aboville.
     *
     * REGLAS:
     *
     * - Debe comenzar con "1".
     * - Debe contener uno o más grupos ".k".
     * - Cada número debe estar entre 1 y MAX_ORDEN.
     * - No se permiten ceros a la izquierda.
     * - La longitud total no puede superar MAX_LONGITUD.
     *
     * EJEMPLOS VÁLIDOS:
     * "1.1", "1.2.3", "1.9999".
     *
     * EJEMPLOS INVÁLIDOS:
     * "1", "2.1", "1.", "1..1", "1.0", "1.01".
     *
     * @param posicion posición que se desea validar.
     * @return true si es D'Aboville válida.
     */
    public static boolean esDAboville(String posicion) {

        // Verificar que la posición exista y no supere
        // la longitud máxima permitida.
        if (posicion == null || posicion.length() > MAX_LONGITUD) {
            return false;
        }

        /*
         * Expresión regular:
         *
         * 1                  = comienza obligatoriamente con 1.
         * (?: ... )          = grupo que no captura.
         * \\.                = punto literal.
         * [1-9]              = primer dígito entre 1 y 9.
         * [0-9]{0,3}         = permite hasta tres dígitos adicionales.
         * +                  = exige uno o más grupos.
         *
         * Cada grupo representa un descendiente.
         */
        return posicion.matches(
            "1(?:\\.[1-9][0-9]{0,3})+"
        );
    }


    // ========================================================
    // 6. VALIDACIÓN GENERAL DE POSICIONES
    // ========================================================

    /**
     * Comprueba si una posición pertenece a alguno
     * de los dos sistemas genealógicos.
     *
     * @param posicion posición que se desea validar.
     * @return true si es Ahnentafel o D'Aboville válida.
     */
    public static boolean esValida(String posicion) {

        /*
         * Una posición es válida cuando cumple al menos
         * uno de los dos sistemas.
         *
         * || representa el operador lógico OR.
         */
        return esAhnentafel(posicion)
                || esDAboville(posicion);
    }


    // ========================================================
    // 7. IDENTIFICACIÓN DE ASCENDIENTES
    // ========================================================

    /**
     * Comprueba si una persona corresponde a un ascendiente.
     *
     * Se considera ascendiente cualquier posición Ahnentafel
     * válida diferente de "1".
     *
     * Ejemplos:
     * "2" = padre.
     * "3" = madre.
     * "4" = abuelo.
     *
     * @param posicion posición que se desea comprobar.
     * @return true si es un ascendiente.
     */
    public static boolean esAscendiente(String posicion) {

        /*
         * Debe cumplir dos condiciones:
         *
         * 1. Ser una posición Ahnentafel válida.
         * 2. No ser el sujeto principal.
         */
        return esAhnentafel(posicion)
                && !esSujeto(posicion);
    }


    // ========================================================
    // 8. IDENTIFICACIÓN DE DESCENDIENTES
    // ========================================================

    /**
     * Comprueba si una persona corresponde a un descendiente.
     *
     * El sujeto "1" no se considera descendiente de sí mismo.
     *
     * Ejemplos:
     * "1.1" = primer hijo.
     * "1.2" = segundo hijo.
     * "1.1.1" = primer nieto.
     *
     * @param posicion posición que se desea comprobar.
     * @return true si es un descendiente.
     */
    public static boolean esDescendiente(String posicion) {

        return esDAboville(posicion);
    }


    // ========================================================
    // 9. CÁLCULO DE LA POSICIÓN DEL PADRE
    // ========================================================

    /**
     * Calcula la posición del padre de una persona.
     *
     * REGLA:
     *
     * Padre = 2 * n
     *
     * EJEMPLOS:
     * "1" -> "2".
     * "5" -> "10".
     *
     * IMPORTANTE:
     * El resultado puede superar MAX_AHNENTAFEL.
     *
     * @param ahnentafel posición de la persona.
     * @return posición Ahnentafel del padre.
     * @throws IllegalArgumentException si la entrada no es válida.
     */
    public static String posicionDelPadre(String ahnentafel) {

        // Comprobar que la posición de entrada sea válida.
        if (!esAhnentafel(ahnentafel)) {

            throw new IllegalArgumentException(
                "La posición Ahnentafel no es válida."
            );
        }

        // Convertir la posición a entero.
        int numero = Integer.parseInt(ahnentafel);

        // Calcular la posición del padre.
        int padre = numero * 2;

        // Convertir el resultado nuevamente a String.
        return String.valueOf(padre);
    }


    // ========================================================
    // 10. CÁLCULO DE LA POSICIÓN DE LA MADRE
    // ========================================================

    /**
     * Calcula la posición de la madre de una persona.
     *
     * REGLA:
     *
     * Madre = 2 * n + 1
     *
     * EJEMPLOS:
     * "1" -> "3".
     * "5" -> "11".
     *
     * @param ahnentafel posición de la persona.
     * @return posición Ahnentafel de la madre.
     * @throws IllegalArgumentException si la entrada no es válida.
     */
    public static String posicionDeLaMadre(String ahnentafel) {

        // Validar la posición recibida.
        if (!esAhnentafel(ahnentafel)) {

            throw new IllegalArgumentException(
                "La posición Ahnentafel no es válida."
            );
        }

        // Convertir la cadena a entero.
        int numero = Integer.parseInt(ahnentafel);

        // Calcular la posición de la madre.
        int madre = numero * 2 + 1;

        // Retornar el resultado como String.
        return String.valueOf(madre);
    }


    // ========================================================
    // 11. CÁLCULO DEL HIJO EN LÍNEA DIRECTA
    // ========================================================

    /**
     * Calcula la posición del hijo o hija que conecta
     * a un ascendiente con el sujeto principal.
     *
     * REGLA:
     *
     * Hijo en línea = n / 2
     *
     * Se utiliza división entera.
     *
     * EJEMPLOS:
     * "2" -> "1".
     * "3" -> "1".
     * "5" -> "2".
     * "11" -> "5".
     *
     * @param ahnentafel posición del ascendiente.
     * @return posición del hijo en línea directa.
     * @throws IllegalArgumentException si no es un ascendiente.
     */
    public static String posicionDelHijoEnLinea(String ahnentafel) {

        /*
         * El sujeto no tiene un hijo en línea hacia sí mismo.
         * Por eso se exige una posición de ascendiente.
         */
        if (!esAscendiente(ahnentafel)) {

            throw new IllegalArgumentException(
                "La posición debe corresponder a un ascendiente."
            );
        }

        // Convertir la posición a entero.
        int numero = Integer.parseInt(ahnentafel);

        // Obtener el número de la generación anterior.
        int hijo = numero / 2;

        // Retornar el resultado como texto.
        return String.valueOf(hijo);
    }


    // ========================================================
    // 12. CÁLCULO DE LA POSICIÓN DEL PROGENITOR
    // ========================================================

    /**
     * Calcula la posición del progenitor de un descendiente.
     *
     * REGLA:
     *
     * Se elimina el último grupo numérico de la posición.
     *
     * EJEMPLOS:
     * "1.2" -> "1".
     * "1.2.3" -> "1.2".
     * "1.1.2.3" -> "1.1.2".
     *
     * @param daboville posición del descendiente.
     * @return posición D'Aboville del progenitor.
     * @throws IllegalArgumentException si la posición no es válida.
     */
    public static String posicionDelProgenitor(String daboville) {

        // Validar que la posición corresponda a un descendiente.
        if (!esDAboville(daboville)) {

            throw new IllegalArgumentException(
                "La posición D'Aboville no es válida."
            );
        }

        /*
         * lastIndexOf('.') encuentra la ubicación del último punto.
         *
         * substring(0, ultimoPunto) obtiene el texto desde
         * el inicio hasta antes del último punto.
         */
        int ultimoPunto = daboville.lastIndexOf('.');

        return daboville.substring(0, ultimoPunto);
    }


    // ========================================================
    // 13. OBTENCIÓN DEL ORDEN ENTRE HERMANOS
    // ========================================================

    /**
     * Obtiene el número de orden de un descendiente
     * entre sus hermanos.
     *
     * REGLA:
     *
     * El orden corresponde al último número de la posición.
     *
     * EJEMPLOS:
     * "1.1" -> 1.
     * "1.2" -> 2.
     * "1.2.3" -> 3.
     *
     * @param daboville posición del descendiente.
     * @return número de orden entre sus hermanos.
     * @throws IllegalArgumentException si la posición no es válida.
     */
    public static int orden(String daboville) {

        // Verificar que sea una posición D'Aboville válida.
        if (!esDAboville(daboville)) {

            throw new IllegalArgumentException(
                "La posición D'Aboville no es válida."
            );
        }

        /*
         * split("\\.") divide la cadena utilizando el punto
         * como separador.
         *
         * Ejemplo:
         *
         * "1.2.3" produce:
         * partes[0] = "1"
         * partes[1] = "2"
         * partes[2] = "3"
         */
        String[] partes = daboville.split("\\.");

        // Obtener el último elemento del arreglo.
        String ultimoNumero = partes[partes.length - 1];

        // Convertirlo a entero y retornarlo.
        return Integer.parseInt(ultimoNumero);
    }


    // ========================================================
    // 14. CÁLCULO DE GENERACIONES
    // ========================================================

    /**
     * Calcula la distancia generacional de una persona
     * respecto al sujeto principal.
     *
     * SISTEMA AHNENTAFEL:
     *
     * "1"       -> generación 0.
     * "2" y "3" -> generación 1.
     * "4" a "7" -> generación 2.
     * "8" a "15" -> generación 3.
     *
     * Se calcula mediante la parte entera del logaritmo
     * en base 2 del número.
     *
     * SISTEMA D'ABOVILLE:
     *
     * "1.1"     -> generación 1.
     * "1.2.3"   -> generación 2.
     * "1.1.2.3" -> generación 3.
     *
     * En este sistema se cuenta la cantidad de puntos.
     *
     * @param posicion posición de la persona.
     * @return número de generaciones respecto al sujeto.
     * @throws IllegalArgumentException si la posición no es válida.
     */
    public static int generacion(String posicion) {

        // ----------------------------------------------------
        // PRIMER PASO: VALIDAR LA POSICIÓN
        // ----------------------------------------------------

        if (!esValida(posicion)) {

            throw new IllegalArgumentException(
                "La posición no es válida."
            );
        }


        // ----------------------------------------------------
        // SEGUNDO PASO: SISTEMA AHNENTAFEL
        // ----------------------------------------------------

        if (esAhnentafel(posicion)) {

            // Convertir la posición a entero.
            int numero = Integer.parseInt(posicion);

            /*
             * Integer.numberOfLeadingZeros(numero):
             * devuelve la cantidad de ceros iniciales
             * en la representación binaria de un entero.
             *
             * Como un int utiliza 32 bits:
             *
             * 31 - numberOfLeadingZeros(numero)
             *
             * equivale a la parte entera del logaritmo
             * en base 2 para números positivos.
             */
            return 31 - Integer.numberOfLeadingZeros(numero);
        }


        // ----------------------------------------------------
        // TERCER PASO: SISTEMA D'ABOVILLE
        // ----------------------------------------------------

        /*
         * chars() convierte la cadena en un flujo de caracteres.
         *
         * filter() conserva únicamente los puntos.
         *
         * count() cuenta cuántos puntos existen.
         *
         * Cada punto representa una generación adicional.
         */
        return (int) posicion.chars()
                .filter(caracter -> caracter == '.')
                .count();
    }

}
