
package co.umariana.dao;

import co.umariana.conexion.ConexionBD;
import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.Persona;
import co.umariana.modelo.ResumenArbol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object).
 *
 * Es la clase encargada de comunicarse con la base de datos.
 * Permite guardar, consultar, cargar, actualizar y eliminar
 * árboles genealógicos.
 *
 * Trabaja con las tablas:
 * - arboles
 * - miembros
 *
 * Utiliza JDBC y PreparedStatement para ejecutar las consultas.
 */
public class ArbolDAO {

    // =====================================================================
    // MÉTODO AUXILIAR: INSERTAR LOS MIEMBROS DE UN ÁRBOL
    // =====================================================================

    /**
     * Inserta en la tabla miembros todas las personas de un árbol.
     *
     * Se utiliza tanto en guardar() como en actualizar().
     *
     * El método recibe una conexión existente porque las operaciones
     * deben ejecutarse dentro de la misma transacción.
     *
     * @param con conexión con la base de datos
     * @param arbolId identificador del árbol
     * @param arbol árbol cuyos miembros se van a insertar
     * @throws SQLException si ocurre un error durante la inserción
     */
    private void insertarMiembros(
            Connection con,
            long arbolId,
            ArbolGenealogico arbol
    ) throws SQLException {

        String sql = "INSERT INTO miembros "
                + "(arbol_id, posicion, nombre, nacimiento, fallecimiento) "
                + "VALUES (?, ?, ?, ?, ?)";

        // PreparedStatement permite enviar los valores mediante parámetros.
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            // Recorremos los miembros en el orden definido por el árbol.
            for (Persona persona : arbol.miembros()) {

                ps.setLong(1, arbolId);
                ps.setString(2, persona.getPosicion());
                ps.setString(3, persona.getNombre());
                ps.setInt(4, persona.getNacimiento());

                // El fallecimiento es opcional.
                // Si es null, se guarda un NULL en la base de datos.
                if (persona.getFallecimiento() == null) {
                    ps.setNull(5, Types.INTEGER);
                } else {
                    ps.setInt(5, persona.getFallecimiento());
                }

                // Ejecutamos la inserción de esta persona.
                ps.executeUpdate();
            }
        }
    }

    // =====================================================================
    // ETAPA 4.1. GUARDAR UN ÁRBOL NUEVO
    // =====================================================================

    /**
     * Guarda un árbol nuevo y todos sus miembros.
     *
     * Primero inserta el árbol en la tabla arboles.
     * Después obtiene el ID generado por la base de datos.
     * Finalmente inserta sus miembros en la tabla miembros.
     *
     * Todas las operaciones se realizan en una transacción.
     *
     * @param nombre nombre del árbol
     * @param arbol árbol que se desea guardar
     * @return ID generado por la base de datos o -1 si ocurre un error
     */
    public long guardar(String nombre, ArbolGenealogico arbol) {

        // Validamos los datos antes de conectarnos a la base de datos.
        if (nombre == null || nombre.isBlank() || nombre.length() > 100
                || arbol == null) {
            return -1;
        }

        String sql = "INSERT INTO arboles (nombre) VALUES (?)";

        try (Connection con = ConexionBD.conectar()) {

            try {

                // Desactivamos el autocommit para iniciar la transacción.
                con.setAutoCommit(false);

                long arbolId;

                // Insertamos el árbol y solicitamos las claves generadas.
                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, nombre);

                    int filas = ps.executeUpdate();

                    if (filas == 0) {
                        throw new SQLException(
                                "No fue posible insertar el árbol."
                        );
                    }

                    // Recuperamos el ID generado automáticamente.
                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se generó el ID del árbol."
                            );
                        }

                        arbolId = rs.getLong(1);
                    }
                }

                // Insertamos todos los miembros del árbol.
                insertarMiembros(con, arbolId, arbol);

                // Confirmamos todas las operaciones.
                con.commit();

                return arbolId;

            } catch (SQLException e) {

                // Si alguna operación falla, deshacemos la transacción.
                try {
                    con.rollback();
                } catch (SQLException errorRollback) {
                    System.out.println(
                            "Error al realizar rollback: "
                            + errorRollback.getMessage()
                    );
                }

                System.out.println(
                        "Error al guardar el árbol: " + e.getMessage()
                );

                return -1;

            } finally {

                // Restauramos el autocommit antes de cerrar la conexión.
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println(
                            "Error al restaurar autocommit: "
                            + e.getMessage()
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error de conexión al guardar: " + e.getMessage()
            );

            return -1;
        }
    }

    // =====================================================================
    // ETAPA 4.2. LISTAR LOS ÁRBOLES
    // =====================================================================

    /**
     * Consulta todos los árboles almacenados en la base de datos.
     *
     * También cuenta cuántos miembros tiene cada árbol.
     *
     * LEFT JOIN permite mostrar árboles aunque no tengan miembros.
     *
     * @return lista de objetos ResumenArbol
     */
    public List<ResumenArbol> listar() {

        List<ResumenArbol> lista = new ArrayList<>();

        String sql = "SELECT a.id, a.nombre, a.creado, "
                + "COUNT(m.id) AS total "
                + "FROM arboles a "
                + "LEFT JOIN miembros m ON m.arbol_id = a.id "
                + "GROUP BY a.id, a.nombre, a.creado "
                + "ORDER BY a.id";

        try (
                Connection con = ConexionBD.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            // Recorremos cada fila obtenida de la consulta.
            while (rs.next()) {

                long id = rs.getLong("id");

                String nombre = rs.getString("nombre");

                int totalMiembros = rs.getInt("total");

                // Convertimos el Timestamp de SQL a LocalDateTime.
                Timestamp timestamp = rs.getTimestamp("creado");

                java.time.LocalDateTime creado = timestamp == null
                        ? null
                        : timestamp.toLocalDateTime();

                // Construimos el resumen del árbol.
                ResumenArbol resumen = new ResumenArbol(
                        id,
                        nombre,
                        totalMiembros,
                        creado
                );

                lista.add(resumen);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error al listar los árboles: " + e.getMessage()
            );

            // Si ocurre un error, devolvemos una lista vacía.
            lista.clear();
        }

        return lista;
    }

    // =====================================================================
    // ETAPA 4.3. CARGAR UN ÁRBOL
    // =====================================================================

    /**
     * Recupera un árbol almacenado y reconstruye su estructura.
     *
     * La primera persona recuperada debe ser el sujeto.
     * Las demás personas se agregan respetando el orden de almacenamiento.
     *
     * @param id identificador del árbol
     * @return árbol reconstruido o null si no existe o ocurre un error
     */
    public ArbolGenealogico cargar(long id) {

        String sql = "SELECT posicion, nombre, nacimiento, fallecimiento "
                + "FROM miembros "
                + "WHERE arbol_id = ? "
                + "ORDER BY id";

        try (
                Connection con = ConexionBD.conectar();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                ArbolGenealogico arbol = null;

                // Recorremos los miembros recuperados.
                while (rs.next()) {

                    String posicion = rs.getString("posicion");

                    String nombre = rs.getString("nombre");

                    int nacimiento = rs.getInt("nacimiento");

                    // getInt devuelve 0 cuando el valor SQL es NULL.
                    // wasNull permite distinguir ese caso.
                    int valorFallecimiento = rs.getInt("fallecimiento");

                    Integer fallecimiento = rs.wasNull()
                            ? null
                            : valorFallecimiento;

                    // Construimos el objeto Persona.
                    Persona persona = new Persona(
                            posicion,
                            nombre,
                            nacimiento,
                            fallecimiento
                    );

                    if (arbol == null) {

                        // La primera persona debe ser el sujeto.
                        // Con ella se crea el árbol.
                        arbol = new ArbolGenealogico(persona);

                    } else {

                        // Las siguientes personas se agregan al árbol.
                        arbol.agregar(persona);
                    }
                }

                // Si no se recuperaron filas, el árbol no existe
                // o no tiene miembros registrados.
                return arbol;
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al cargar el árbol: " + e.getMessage()
            );

            return null;
        }
    }

    // =====================================================================
    // ETAPA 4.4. ACTUALIZAR UN ÁRBOL
    // =====================================================================

    /**
     * Actualiza los miembros de un árbol existente.
     *
     * El proceso elimina los miembros anteriores e inserta los nuevos.
     * El nombre y el ID del árbol permanecen iguales.
     *
     * Las operaciones se realizan dentro de una transacción para evitar
     * que el árbol quede sin miembros si ocurre algún error.
     *
     * @param id identificador del árbol
     * @param arbol árbol con los nuevos miembros
     * @return true si se actualizó correctamente; false en caso contrario
     */
    public boolean actualizar(long id, ArbolGenealogico arbol) {

        if (arbol == null) {
            return false;
        }

        String sqlExiste = "SELECT id FROM arboles WHERE id = ?";

        String sqlEliminar = "DELETE FROM miembros WHERE arbol_id = ?";

        try (Connection con = ConexionBD.conectar()) {

            try {

                // Iniciamos la transacción.
                con.setAutoCommit(false);

                boolean existe;

                // Comprobamos que el árbol esté registrado.
                try (PreparedStatement ps = con.prepareStatement(sqlExiste)) {

                    ps.setLong(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        existe = rs.next();
                    }
                }

                // Si no existe, no realizamos ninguna modificación.
                if (!existe) {
                    con.rollback();
                    return false;
                }

                // Eliminamos los miembros anteriores.
                try (PreparedStatement ps = con.prepareStatement(sqlEliminar)) {

                    ps.setLong(1, id);

                    ps.executeUpdate();
                }

                // Insertamos los miembros actualizados.
                insertarMiembros(con, id, arbol);

                // Confirmamos los cambios.
                con.commit();

                return true;

            } catch (SQLException e) {

                // Si algo falla, recuperamos los miembros anteriores.
                try {
                    con.rollback();
                } catch (SQLException errorRollback) {
                    System.out.println(
                            "Error al realizar rollback: "
                            + errorRollback.getMessage()
                    );
                }

                System.out.println(
                        "Error al actualizar el árbol: " + e.getMessage()
                );

                return false;

            } finally {

                // Restauramos el autocommit.
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println(
                            "Error al restaurar autocommit: "
                            + e.getMessage()
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error de conexión al actualizar: " + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================================
    // ETAPA 4.5. ELIMINAR UN ÁRBOL
    // =====================================================================

    /**
     * Elimina un árbol de la base de datos.
     *
     * Los miembros asociados se eliminan automáticamente gracias
     * a la restricción ON DELETE CASCADE de la clave foránea.
     *
     * @param id identificador del árbol
     * @return true si se eliminó; false si no existía o hubo un error
     */
    public boolean eliminar(long id) {

        String sql = "DELETE FROM arboles WHERE id = ?";

        try (
                Connection con = ConexionBD.conectar();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            // Asignamos el ID del árbol que queremos eliminar.
            ps.setLong(1, id);

            // executeUpdate devuelve el número de filas afectadas.
            int filas = ps.executeUpdate();

            // Si se eliminó al menos una fila, la operación fue exitosa.
            return filas > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Error al eliminar el árbol: " + e.getMessage()
            );

            return false;
        }
    }
}
