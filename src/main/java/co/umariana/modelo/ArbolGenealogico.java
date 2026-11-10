
package co.umariana.modelo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Árbol genealógico formado por un sujeto, sus ascendientes
 * y sus descendientes.
 *
 * Los ascendientes se organizan mediante referencias al padre
 * y a la madre.
 *
 * Los descendientes se organizan mediante una lista de hijos.
 *
 * No se utilizan Map ni Set.
 */
public class ArbolGenealogico {

    // ========================================================
    // 1. CLASE INTERNA NODO
    // ========================================================

    /**
     * Representa un nodo del árbol genealógico.
     *
     * Cada nodo almacena:
     * - Una persona.
     * - Su padre.
     * - Su madre.
     * - Una lista de hijos.
     */
    private static class Nodo {

        private final Persona persona;

        private Nodo padre;
        private Nodo madre;

        private final List<Nodo> hijos;

        private Nodo(Persona persona) {
            this.persona = persona;
            this.padre = null;
            this.madre = null;
            this.hijos = new ArrayList<>();
        }
    }


    // ========================================================
    // 2. ATRIBUTOS DEL ÁRBOL
    // ========================================================

    /**
     * Nodo principal del árbol.
     */
    private final Nodo raiz;

    /**
     * Cantidad total de personas registradas.
     */
    private int cantidad;


    // ========================================================
    // 3. CONSTRUCTOR
    // ========================================================

    /**
     * Crea un árbol que inicialmente contiene solamente al sujeto.
     *
     * @param sujeto persona principal.
     * @throws IllegalArgumentException si la persona no es válida.
     * @throws ArbolInvalidoException si su posición no es "1".
     */
    public ArbolGenealogico(Persona sujeto) {

        if (sujeto == null) {
            throw new IllegalArgumentException(
                "El sujeto no puede ser null."
            );
        }

        sujeto.validar();

        if (!Posicion.esSujeto(sujeto.getPosicion())) {
            throw new ArbolInvalidoException(
                "El sujeto debe tener la posición 1."
            );
        }

        this.raiz = new Nodo(sujeto);
        this.cantidad = 1;
    }


    // ========================================================
    // 4. OBTENER EL SUJETO
    // ========================================================

    /**
     * @return persona principal del árbol.
     */
    public Persona sujeto() {
        return raiz.persona;
    }


    // ========================================================
    // 5. OBTENER EL TAMAÑO
    // ========================================================

    /**
     * @return cantidad total de personas.
     */
    public int tamano() {
        return cantidad;
    }


    // ========================================================
    // 6. BUSCAR UNA PERSONA
    // ========================================================

    /**
     * Busca una persona utilizando su posición genealógica.
     *
     * Para Ahnentafel se recorren los bits del número.
     * Para D'Aboville se recorren los números de sus niveles.
     *
     * @param posicion posición que se desea buscar.
     * @return persona encontrada o null.
     */
    public Persona buscar(String posicion) {

        if (!Posicion.esValida(posicion)) {
            return null;
        }

        Nodo encontrado = buscarNodo(posicion);

        return encontrado == null ? null : encontrado.persona;
    }


    /**
     * Busca internamente un nodo sin exponer la estructura.
     */
    private Nodo buscarNodo(String posicion) {

        if (!Posicion.esValida(posicion)) {
            return null;
        }

        // ----------------------------------------------------
        // BÚSQUEDA AHNENTAFEL
        // ----------------------------------------------------

        if (Posicion.esAhnentafel(posicion)) {

            if (Posicion.esSujeto(posicion)) {
                return raiz;
            }

            String binario = Integer.toBinaryString(
                Integer.parseInt(posicion)
            );

            Nodo actual = raiz;

            // Se omite el primer bit, que siempre es 1.
            for (int i = 1; i < binario.length(); i++) {

                char bit = binario.charAt(i);

                if (bit == '0') {
                    actual = actual.padre;
                } else {
                    actual = actual.madre;
                }

                if (actual == null) {
                    return null;
                }
            }

            return actual;
        }


        // ----------------------------------------------------
        // BÚSQUEDA D'ABOVILLE
        // ----------------------------------------------------

        String[] niveles = posicion.split("\\.");

        Nodo actual = raiz;

        for (int i = 1; i < niveles.length; i++) {

            int numeroHijo = Integer.parseInt(niveles[i]);

            actual = buscarHijo(actual, numeroHijo);

            if (actual == null) {
                return null;
            }
        }

        return actual;
    }


    /**
     * Busca un hijo por su número de orden.
     *
     * No supone que los números sean consecutivos.
     */
    private Nodo buscarHijo(Nodo progenitor, int orden) {

        if (progenitor == null) {
            return null;
        }

        for (Nodo hijo : progenitor.hijos) {

            if (Posicion.orden(hijo.persona.getPosicion()) == orden) {
                return hijo;
            }
        }

        return null;
    }


    // ========================================================
    // 7. COMPROBAR SI EXISTE UNA PERSONA
    // ========================================================

    /**
     * @param posicion posición que se desea comprobar.
     * @return true si existe una persona en esa posición.
     */
    public boolean contiene(String posicion) {
        return buscarNodo(posicion) != null;
    }


    // ========================================================
    // 8. AGREGAR UNA PERSONA
    // ========================================================

    /**
     * Agrega una persona utilizando la posición que trae el objeto.
     *
     * @param persona persona que se desea agregar.
     * @return la misma persona recibida.
     */
    public Persona agregar(Persona persona) {

        if (persona == null) {
            throw new IllegalArgumentException(
                "La persona no puede ser null."
            );
        }

        // Primero se validan los datos de la persona.
        persona.validar();

        String posicion = persona.getPosicion();

        // El sujeto no puede volver a agregarse.
        if (Posicion.esSujeto(posicion)) {
            throw new ArbolInvalidoException(
                "La posición 1 ya pertenece al sujeto."
            );
        }

        // Comprobar que la posición no esté ocupada.
        if (contiene(posicion)) {
            throw new ArbolInvalidoException(
                "Ya existe una persona en la posición " + posicion + "."
            );
        }


        // ----------------------------------------------------
        // AGREGAR UN ASCENDIENTE
        // ----------------------------------------------------

        if (Posicion.esAhnentafel(posicion)) {

            int numero = Integer.parseInt(posicion);
            String posicionHijo = String.valueOf(numero / 2);

            Nodo hijo = buscarNodo(posicionHijo);

            if (hijo == null) {
                throw new ArbolInvalidoException(
                    "No existe la persona de la que depende "
                    + "el ascendiente " + posicion + "."
                );
            }

            // El ascendiente debe nacer antes que su hijo.
            if (persona.getNacimiento()
                    >= hijo.persona.getNacimiento()) {

                throw new ArbolInvalidoException(
                    "El ascendiente debe haber nacido antes que su hijo."
                );
            }

            Nodo nuevo = new Nodo(persona);

            // Los números pares representan padres.
            if (numero % 2 == 0) {

                if (hijo.padre != null) {
                    throw new ArbolInvalidoException(
                        "El padre ya existe."
                    );
                }

                hijo.padre = nuevo;

            } else {

                // Los números impares representan madres.
                if (hijo.madre != null) {
                    throw new ArbolInvalidoException(
                        "La madre ya existe."
                    );
                }

                hijo.madre = nuevo;
            }

            cantidad++;

            return persona;
        }


        // ----------------------------------------------------
        // AGREGAR UN DESCENDIENTE
        // ----------------------------------------------------

        if (Posicion.esDAboville(posicion)) {

            String posicionProgenitor =
                Posicion.posicionDelProgenitor(posicion);

            Nodo progenitor = buscarNodo(posicionProgenitor);

            if (progenitor == null) {
                throw new ArbolInvalidoException(
                    "No existe el progenitor de la posición "
                    + posicion + "."
                );
            }

            // El descendiente debe nacer después de su progenitor.
            if (persona.getNacimiento()
                    <= progenitor.persona.getNacimiento()) {

                throw new ArbolInvalidoException(
                    "El descendiente debe haber nacido después "
                    + "de su progenitor."
                );
            }

            Nodo nuevo = new Nodo(persona);

            progenitor.hijos.add(nuevo);

            cantidad++;

            return persona;
        }

        throw new ArbolInvalidoException(
            "La posición genealógica no es válida."
        );
    }


    // ========================================================
    // 9. AGREGAR PADRE
    // ========================================================

    /**
     * Agrega el padre de una persona.
     */
    public Persona agregarPadre(
            String posicion,
            String nombre,
            int nacimiento,
            Integer fallecimiento) {

        Nodo persona = buscarNodo(posicion);

        if (persona == null) {
            throw new ArbolInvalidoException(
                "No existe la persona en la posición " + posicion + "."
            );
        }

        if (!Posicion.esAhnentafel(posicion)) {
            throw new ArbolInvalidoException(
                "Solo se pueden agregar padres al sujeto "
                + "y a sus ascendientes."
            );
        }

        String nuevaPosicion = Posicion.posicionDelPadre(posicion);

        Persona padre = new Persona(
            nuevaPosicion,
            nombre,
            nacimiento,
            fallecimiento
        );

        return agregar(padre);
    }


    // ========================================================
    // 10. AGREGAR MADRE
    // ========================================================

    /**
     * Agrega la madre de una persona.
     */
    public Persona agregarMadre(
            String posicion,
            String nombre,
            int nacimiento,
            Integer fallecimiento) {

        Nodo persona = buscarNodo(posicion);

        if (persona == null) {
            throw new ArbolInvalidoException(
                "No existe la persona en la posición " + posicion + "."
            );
        }

        if (!Posicion.esAhnentafel(posicion)) {
            throw new ArbolInvalidoException(
                "Solo se pueden agregar madres al sujeto "
                + "y a sus ascendientes."
            );
        }

        String nuevaPosicion = Posicion.posicionDeLaMadre(posicion);

        Persona madre = new Persona(
            nuevaPosicion,
            nombre,
            nacimiento,
            fallecimiento
        );

        return agregar(madre);
    }


    // ========================================================
    // 11. AGREGAR HIJO
    // ========================================================

    /**
     * Agrega un hijo utilizando el siguiente número disponible
     * después del mayor número de hijo existente.
     */
    public Persona agregarHijo(
            String posicion,
            String nombre,
            int nacimiento,
            Integer fallecimiento) {

        Nodo progenitor = buscarNodo(posicion);

        if (progenitor == null) {
            throw new ArbolInvalidoException(
                "No existe la persona en la posición " + posicion + "."
            );
        }

        if (!Posicion.esSujeto(posicion)
                && !Posicion.esDescendiente(posicion)) {

            throw new ArbolInvalidoException(
                "Solo se pueden agregar hijos al sujeto "
                + "y a sus descendientes."
            );
        }

        // Encontrar el mayor orden entre los hijos existentes.
        int mayorOrden = 0;

        for (Nodo hijo : progenitor.hijos) {

            int ordenHijo = Posicion.orden(
                hijo.persona.getPosicion()
            );

            if (ordenHijo > mayorOrden) {
                mayorOrden = ordenHijo;
            }
        }

        if (mayorOrden >= Posicion.MAX_ORDEN) {
            throw new ArbolInvalidoException(
                "Se alcanzó el máximo número de hijos permitido."
            );
        }

        int siguienteOrden = mayorOrden + 1;

        String nuevaPosicion = posicion + "." + siguienteOrden;

        if (!Posicion.esDAboville(nuevaPosicion)) {
            throw new ArbolInvalidoException(
                "La nueva posición D'Aboville supera los límites permitidos."
            );
        }

        Persona hijo = new Persona(
            nuevaPosicion,
            nombre,
            nacimiento,
            fallecimiento
        );

        return agregar(hijo);
    }


    // ========================================================
    // 12. ELIMINAR UNA PERSONA
    // ========================================================

    /**
     * Elimina una persona y todos los miembros que dependen de ella.
     *
     * @param posicion posición de la persona que se eliminará.
     * @return cantidad de personas eliminadas.
     */
    public int eliminar(String posicion) {

        if (Posicion.esSujeto(posicion)) {
            throw new ArbolInvalidoException(
                "El sujeto principal no se puede eliminar."
            );
        }

        Nodo objetivo = buscarNodo(posicion);

        if (objetivo == null) {
            throw new ArbolInvalidoException(
                "No existe una persona en la posición " + posicion + "."
            );
        }


        // ----------------------------------------------------
        // ELIMINAR ASCENDIENTES
        // ----------------------------------------------------

        if (Posicion.esAhnentafel(posicion)) {

            int numero = Integer.parseInt(posicion);

            String posicionHijo = String.valueOf(numero / 2);

            Nodo hijo = buscarNodo(posicionHijo);

            // Contar el nodo y todos sus ascendientes.
            int eliminados = contarAscendientes(objetivo);

            // Desconectar el nodo de su hijo.
            if (numero % 2 == 0) {
                hijo.padre = null;
            } else {
                hijo.madre = null;
            }

            cantidad -= eliminados;

            return eliminados;
        }


        // ----------------------------------------------------
        // ELIMINAR DESCENDIENTES
        // ----------------------------------------------------

        if (Posicion.esDAboville(posicion)) {

            String posicionProgenitor =
                Posicion.posicionDelProgenitor(posicion);

            Nodo progenitor = buscarNodo(posicionProgenitor);

            // Contar el nodo y todos sus descendientes.
            int eliminados = contarDescendientes(objetivo);

            // Desconectar el nodo de su progenitor.
            progenitor.hijos.remove(objetivo);

            cantidad -= eliminados;

            return eliminados;
        }

        throw new ArbolInvalidoException(
            "No se puede eliminar una posición no válida."
        );
    }


    /**
     * Cuenta un ascendiente y todos sus propios ascendientes.
     */
    private int contarAscendientes(Nodo nodo) {

        if (nodo == null) {
            return 0;
        }

        return 1
                + contarAscendientes(nodo.padre)
                + contarAscendientes(nodo.madre);
    }


    /**
     * Cuenta un descendiente y todos sus descendientes.
     */
    private int contarDescendientes(Nodo nodo) {

        if (nodo == null) {
            return 0;
        }

        int total = 1;

        for (Nodo hijo : nodo.hijos) {
            total += contarDescendientes(hijo);
        }

        return total;
    }


    // ========================================================
    // 13. OBTENER TODOS LOS MIEMBROS
    // ========================================================

    /**
     * Devuelve los miembros en el siguiente orden:
     *
     * 1. Sujeto.
     * 2. Ascendientes por número Ahnentafel ascendente.
     * 3. Descendientes en preorden.
     *
     * La lista devuelta es una copia nueva.
     */
    public List<Persona> miembros() {

        List<Persona> resultado = new ArrayList<>();

        resultado.add(raiz.persona);

        resultado.addAll(ascendientes());

        resultado.addAll(descendientes());

        return resultado;
    }


    // ========================================================
    // 14. OBTENER ASCENDIENTES
    // ========================================================

    /**
     * Devuelve todos los ascendientes ordenados por número.
     */
    public List<Persona> ascendientes() {

        List<Nodo> nodos = new ArrayList<>();

        recolectarAscendientes(raiz.padre, nodos);
        recolectarAscendientes(raiz.madre, nodos);

        // Ordenar numéricamente las posiciones Ahnentafel.
        nodos.sort(
            Comparator.comparingInt(
                nodo -> Integer.parseInt(nodo.persona.getPosicion())
            )
        );

        List<Persona> resultado = new ArrayList<>();

        for (Nodo nodo : nodos) {
            resultado.add(nodo.persona);
        }

        return resultado;
    }


    /**
     * Recorre recursivamente las ramas de ascendientes.
     */
    private void recolectarAscendientes(
            Nodo nodo,
            List<Nodo> resultado) {

        if (nodo == null) {
            return;
        }

        resultado.add(nodo);

        recolectarAscendientes(nodo.padre, resultado);
        recolectarAscendientes(nodo.madre, resultado);
    }


    // ========================================================
    // 15. OBTENER DESCENDIENTES
    // ========================================================

    /**
     * Devuelve todos los descendientes en preorden.
     */
    public List<Persona> descendientes() {

        List<Persona> resultado = new ArrayList<>();

        recolectarDescendientes(raiz, resultado);

        return resultado;
    }


    /**
     * Recorre los hijos en preorden:
     * primero el hijo y después sus propios hijos.
     */
    private void recolectarDescendientes(
            Nodo nodo,
            List<Persona> resultado) {

        if (nodo == null) {
            return;
        }

        // Crear una copia para ordenar sin modificar el árbol.
        List<Nodo> hijosOrdenados = new ArrayList<>(nodo.hijos);

        hijosOrdenados.sort(
            Comparator.comparingInt(
                hijo -> Posicion.orden(hijo.persona.getPosicion())
            )
        );

        for (Nodo hijo : hijosOrdenados) {

            resultado.add(hijo.persona);

            recolectarDescendientes(hijo, resultado);
        }
    }
}
