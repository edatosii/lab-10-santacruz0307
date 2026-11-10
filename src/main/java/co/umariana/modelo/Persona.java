
package co.umariana.modelo;

/**
 * ============================================================
 * CLASE: Persona
 * ============================================================
 *
 * Representa a un integrante del árbol genealógico.
 *
 * Cada persona contiene:
 * - Una posición dentro del árbol genealógico.
 * - Su nombre completo.
 * - Su año de nacimiento.
 * - Su año de fallecimiento (opcional).
 *
 * RESPONSABILIDAD:
 * Almacenar la información de una persona y verificar
 * que sus datos cumplan las reglas de validación del sistema.
 *
 * RELACIÓN CON OTRAS CLASES:
 * Utiliza la clase Posicion para comprobar que la ubicación
 * de la persona dentro del árbol genealógico sea válida.
 *
 * ETAPA 1:
 * Implementación del método validar().
 *
 * IMPORTANTE:
 * No se deben modificar las pruebas ubicadas en src/test.
 */
public class Persona {

    // ========================================================
    // 1. CONSTANTES
    // ========================================================

    /**
     * Longitud máxima permitida para el nombre.
     *
     * Se establece en 100 caracteres para respetar
     * el límite definido para el almacenamiento en la base de datos.
     */
    public static final int MAX_NOMBRE = 100;


    // ========================================================
    // 2. ATRIBUTOS
    // ========================================================

    /**
     * Posición de la persona dentro del árbol genealógico.
     *
     * Puede utilizar el sistema Ahnentafel o D'Aboville.
     *
     * Ejemplos:
     * "1"     = Sujeto principal.
     * "2"     = Padre del sujeto.
     * "3"     = Madre del sujeto.
     * "1.1"   = Primer hijo del sujeto.
     */
    private final String posicion;

    /**
     * Nombre de la persona.
     */
    private final String nombre;

    /**
     * Año de nacimiento.
     *
     * Debe ser un número mayor que cero.
     */
    private final int nacimiento;

    /**
     * Año de fallecimiento.
     *
     * Es opcional:
     * - null: la persona sigue viva o se desconoce el dato.
     * - Un número: año de fallecimiento.
     *
     * Se utiliza Integer porque permite almacenar null.
     */
    private final Integer fallecimiento;


    // ========================================================
    // 3. CONSTRUCTOR
    // ========================================================

    /**
     * Construye un objeto Persona con sus datos principales.
     *
     * @param posicion posición dentro del árbol genealógico.
     * @param nombre nombre de la persona.
     * @param nacimiento año de nacimiento.
     * @param fallecimiento año de fallecimiento (puede ser null).
     */
    public Persona(String posicion, String nombre,
                   int nacimiento, Integer fallecimiento) {

        this.posicion = posicion;
        this.nombre = nombre;
        this.nacimiento = nacimiento;
        this.fallecimiento = fallecimiento;
    }


    // ========================================================
    // 4. VALIDACIÓN DE LOS DATOS
    // ========================================================

    /**
     * Verifica que la información de la persona cumpla
     * todas las reglas establecidas por el sistema.
     *
     * VALIDACIONES:
     *
     * 1. La posición debe ser válida.
     * 2. El nombre no puede ser nulo ni estar vacío.
     * 3. El nombre no puede superar los 100 caracteres.
     * 4. El nombre no puede contener punto y coma ni saltos de línea.
     * 5. El año de nacimiento debe ser mayor que cero.
     * 6. El año de fallecimiento no puede ser anterior al nacimiento.
     *
     * Las validaciones se ejecutan en el orden establecido.
     *
     * @throws IllegalArgumentException cuando algún dato no cumple
     * las reglas de validación.
     */
    public void validar() {

        // ----------------------------------------------------
        // VALIDACIÓN 1: POSICIÓN
        // ----------------------------------------------------

        /*
         * Se utiliza el método esValida() de la clase Posicion.
         *
         * Este método comprueba si la posición corresponde
         * al sistema Ahnentafel o al sistema D'Aboville.
         *
         * Si la posición no es válida, se genera una excepción.
         */
        if (!Posicion.esValida(posicion)) {

            throw new IllegalArgumentException(
                "La posición de la persona no es válida."
            );
        }


        // ----------------------------------------------------
        // VALIDACIÓN 2: NOMBRE
        // ----------------------------------------------------

        /*
         * Se comprueba que el nombre:
         *
         * - No sea null.
         * - No esté vacío.
         * - No contenga únicamente espacios.
         *
         * trim() elimina los espacios de los extremos.
         * isEmpty() comprueba si la cadena quedó vacía.
         */
        if (nombre == null || nombre.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "El nombre no puede estar vacío."
            );
        }


        // ----------------------------------------------------
        // VALIDACIÓN 3: LONGITUD DEL NOMBRE
        // ----------------------------------------------------

        /*
         * length() obtiene la cantidad de caracteres
         * que contiene la cadena.
         *
         * El nombre no puede superar MAX_NOMBRE (100).
         */
        if (nombre.length() > MAX_NOMBRE) {

            throw new IllegalArgumentException(
                "El nombre no puede superar los "
                + MAX_NOMBRE + " caracteres."
            );
        }


        // ----------------------------------------------------
        // VALIDACIÓN 4: CARACTERES NO PERMITIDOS
        // ----------------------------------------------------

        /*
         * El punto y coma (;) no está permitido porque
         * se utiliza como separador de campos en el sistema.
         *
         * Tampoco se permiten saltos de línea:
         *
         * \n = salto de línea.
         * \r = retorno de carro.
         *
         * contains() permite comprobar si un texto
         * contiene un carácter o una secuencia determinada.
         */
        if (nombre.contains(";")
                || nombre.contains("\n")
                || nombre.contains("\r")) {

            throw new IllegalArgumentException(
                "El nombre no puede contener punto y coma "
                + "ni saltos de línea."
            );
        }


        // ----------------------------------------------------
        // VALIDACIÓN 5: AÑO DE NACIMIENTO
        // ----------------------------------------------------

        /*
         * El año de nacimiento debe ser positivo.
         *
         * No se permiten:
         * - Cero.
         * - Números negativos.
         */
        if (nacimiento <= 0) {

            throw new IllegalArgumentException(
                "El año de nacimiento debe ser mayor que cero."
            );
        }


        // ----------------------------------------------------
        // VALIDACIÓN 6: AÑO DE FALLECIMIENTO
        // ----------------------------------------------------

        /*
         * El fallecimiento es un atributo opcional.
         *
         * Primero comprobamos que no sea null.
         * Después verificamos que no sea anterior al nacimiento.
         *
         * Se utiliza && (AND lógico) porque ambas condiciones
         * deben cumplirse para generar el error.
         *
         * Si ambos años son iguales, la información es válida.
         */
        if (fallecimiento != null
                && fallecimiento < nacimiento) {

            throw new IllegalArgumentException(
                "El año de fallecimiento no puede ser "
                + "anterior al año de nacimiento."
            );
        }

        /*
         * Si el programa llega hasta aquí significa que
         * todos los datos de la persona son válidos.
         */
    }


    // ========================================================
    // 5. MÉTODOS GETTERS
    // ========================================================

    /**
     * Obtiene la posición de la persona.
     *
     * @return posición genealógica.
     */
    public String getPosicion() {
        return posicion;
    }

    /**
     * Obtiene el nombre de la persona.
     *
     * @return nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el año de nacimiento.
     *
     * @return año de nacimiento.
     */
    public int getNacimiento() {
        return nacimiento;
    }

    /**
     * Obtiene el año de fallecimiento.
     *
     * @return año de fallecimiento o null si no existe.
     */
    public Integer getFallecimiento() {
        return fallecimiento;
    }


    // ========================================================
    // 6. REPRESENTACIÓN EN TEXTO DEL OBJETO
    // ========================================================

    /**
     * Devuelve una representación textual de la persona.
     *
     * Ejemplos:
     *
     * 1 - Daniel (2003-)
     * 2 - María (1975-2020)
     *
     * Si el fallecimiento es null, el segundo año
     * queda vacío.
     *
     * @return información de la persona en formato String.
     */
    @Override
    public String toString() {

        return posicion + " - " + nombre
                + " (" + nacimiento + "-"
                + (fallecimiento == null ? "" : fallecimiento)
                + ")";
    }
}
