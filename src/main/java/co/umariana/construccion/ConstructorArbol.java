
package co.umariana.construccion;

import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ArbolInvalidoException;
import co.umariana.modelo.Persona;
import co.umariana.modelo.Posicion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Construye un árbol genealógico a partir de un texto.
 *
 * Formato de cada línea:
 *
 * posicion;nombre;nacimiento;fallecimiento
 *
 * El fallecimiento es opcional.
 *
 * Ejemplo:
 *
 * 1;John Doe;2002;
 * 2;Henry Doe;1920;1990
 * 3;Maria Doe;1921;
 * 1.1;Timmy Doe;2021;
 * 1.2;Amy Doe;2026;
 *
 * ETAPA 3:
 * Construcción del árbol genealógico desde texto.
 */
public final class ConstructorArbol {

    /**
     * Constructor privado para impedir
     * la creación de objetos de esta clase.
     */
    private ConstructorArbol() {
    }

    /**
     * Representa una persona leída desde el texto
     * y conserva el número de línea de origen.
     */
    private static class Registro {

        private final Persona persona;
        private final int numeroLinea;

        public Registro(Persona persona, int numeroLinea) {
            this.persona = persona;
            this.numeroLinea = numeroLinea;
        }
    }

    /**
     * Construye un árbol genealógico a partir del texto recibido.
     *
     * @param texto información de las personas.
     * @return árbol genealógico construido.
     * @throws ArbolInvalidoException si el texto contiene errores.
     */
    public static ArbolGenealogico construir(String texto) {

        // 1. Validar que el texto no sea nulo ni esté vacío.
        if (texto == null || texto.trim().isEmpty()) {

            throw new ArbolInvalidoException(
                    "El texto está vacío o no contiene personas."
            );
        }

        // 2. Separar el texto por líneas.
        // Se admiten saltos de línea LF y CRLF.
        String[] lineas = texto.split("\\r?\\n", -1);

        // 3. Crear la lista donde se almacenarán
        // los registros diferentes del sujeto.
        List<Registro> registros = new ArrayList<>();

        // 4. Variable para almacenar al sujeto principal.
        Registro sujeto = null;

        // 5. Recorrer todas las líneas del texto.
        for (int i = 0; i < lineas.length; i++) {

            int numeroLinea = i + 1;

            // Obtener la línea y eliminar espacios exteriores.
            String linea = lineas[i].trim();

            // Ignorar líneas vacías.
            if (linea.isEmpty()) {
                continue;
            }

            try {

                // 6. Separar los campos utilizando punto y coma.
                // El valor -1 permite conservar campos vacíos.
                String[] campos = linea.split(";", -1);

                // 7. Verificar que existan 3 o 4 campos.
                if (campos.length < 3 || campos.length > 4) {

                    throw new IllegalArgumentException(
                            "La línea debe contener 3 o 4 campos."
                    );
                }

                // 8. Obtener los datos y eliminar espacios.
                String posicion = campos[0].trim();
                String nombre = campos[1].trim();
                String textoNacimiento = campos[2].trim();

                String textoFallecimiento = "";

                if (campos.length == 4) {
                    textoFallecimiento = campos[3].trim();
                }

                // 9. Convertir el nacimiento a número entero.
                int nacimiento;

                try {

                    nacimiento = Integer.parseInt(textoNacimiento);

                } catch (NumberFormatException e) {

                    throw new IllegalArgumentException(
                            "El año de nacimiento debe ser un número entero."
                    );
                }

                // 10. Convertir el fallecimiento si existe.
                Integer fallecimiento = null;

                if (!textoFallecimiento.isEmpty()) {

                    try {

                        fallecimiento = Integer.parseInt(
                                textoFallecimiento
                        );

                    } catch (NumberFormatException e) {

                        throw new IllegalArgumentException(
                                "El año de fallecimiento debe ser un número entero."
                        );
                    }
                }

                // 11. Crear el objeto Persona.
                Persona persona = new Persona(
                        posicion,
                        nombre,
                        nacimiento,
                        fallecimiento
                );

                // 12. Validar los datos de la persona.
                persona.validar();

                // 13. Crear el registro conservando su línea original.
                Registro nuevoRegistro = new Registro(
                        persona,
                        numeroLinea
                );

                // 14. Comprobar si la persona es el sujeto.
                if (Posicion.esSujeto(posicion)) {

                    // CORRECCIÓN:
                    // No permitir que exista más de un sujeto.
                    if (sujeto != null) {

                        throw new ArbolInvalidoException(
                                "La posición 1 está repetida."
                        );
                    }

                    // Registrar el sujeto por primera vez.
                    sujeto = nuevoRegistro;

                } else {

                    // 15. Comprobar posiciones repetidas
                    // entre los demás registros.
                    for (Registro registro : registros) {

                        if (registro.persona.getPosicion()
                                .equals(posicion)) {

                            throw new ArbolInvalidoException(
                                    "La posición " + posicion
                                    + " está repetida."
                            );
                        }
                    }

                    // Guardar el registro.
                    registros.add(nuevoRegistro);
                }

            } catch (IllegalArgumentException e) {

                // 16. Informar el error indicando
                // exactamente el número de línea.
                throw new ArbolInvalidoException(
                        "Línea " + numeroLinea + ": "
                        + e.getMessage()
                );
            }
        }

        // 17. Comprobar que exista el sujeto principal.
        if (sujeto == null) {

            throw new ArbolInvalidoException(
                    "El árbol debe contener la posición 1 (sujeto)."
            );
        }

        // 18. Ordenar los registros para garantizar
        // que cada persona tenga previamente a su progenitor
        // o a la persona de la que depende.
        registros.sort(comparadorRegistros());

        // 19. Crear el árbol utilizando al sujeto como raíz.
        ArbolGenealogico arbol = new ArbolGenealogico(
                sujeto.persona
        );

        // 20. Agregar las demás personas al árbol.
        for (Registro registro : registros) {

            try {

                arbol.agregar(registro.persona);

            } catch (IllegalArgumentException e) {

                // Conservar el número de línea original
                // si el árbol detecta una regla incumplida.
                throw new ArbolInvalidoException(
                        "Línea " + registro.numeroLinea + ": "
                        + e.getMessage()
                );
            }
        }

        // 21. Devolver el árbol construido.
        return arbol;
    }

    /**
     * Define el orden de incorporación de las personas.
     *
     * Primero se agregan los ascendientes por número Ahnentafel.
     * Después se agregan los descendientes por generación.
     */
    private static Comparator<Registro> comparadorRegistros() {

        return (r1, r2) -> {

            String p1 = r1.persona.getPosicion();
            String p2 = r2.persona.getPosicion();

            boolean esAscendiente1 = Posicion.esAhnentafel(p1);
            boolean esAscendiente2 = Posicion.esAhnentafel(p2);

            // Los ascendientes se procesan antes
            // que los descendientes.
            if (esAscendiente1 && !esAscendiente2) {
                return -1;
            }

            if (!esAscendiente1 && esAscendiente2) {
                return 1;
            }

            // Ordenar ascendientes numéricamente.
            if (esAscendiente1) {

                return Integer.compare(
                        Integer.parseInt(p1),
                        Integer.parseInt(p2)
                );
            }

            // Ordenar descendientes por generación.
            int generacion1 = Posicion.generacion(p1);
            int generacion2 = Posicion.generacion(p2);

            int comparacionGeneracion = Integer.compare(
                    generacion1,
                    generacion2
            );

            if (comparacionGeneracion != 0) {
                return comparacionGeneracion;
            }

            // Si están en la misma generación,
            // comparar los números de cada nivel.
            return compararPosicionesDAboville(p1, p2);
        };
    }

    /**
     * Compara dos posiciones D'Aboville numéricamente.
     *
     * Ejemplo:
     * 1.2 es anterior a 1.10.
     *
     * @param p1 primera posición.
     * @param p2 segunda posición.
     * @return resultado de la comparación.
     */
    private static int compararPosicionesDAboville(
            String p1,
            String p2) {

        String[] partes1 = p1.split("\\.");
        String[] partes2 = p2.split("\\.");

        int limite = Math.min(
                partes1.length,
                partes2.length
        );

        for (int i = 0; i < limite; i++) {

            int numero1 = Integer.parseInt(partes1[i]);
            int numero2 = Integer.parseInt(partes2[i]);

            int comparacion = Integer.compare(
                    numero1,
                    numero2
            );

            if (comparacion != 0) {
                return comparacion;
            }
        }

        // Si todos los segmentos coinciden,
        // la posición más corta va primero.
        return Integer.compare(
                partes1.length,
                partes2.length
        );
    }
}
