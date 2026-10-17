package com.gestionproductos.dao;

import com.gestionproductos.model.Producto;
import com.gestionproductos.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) del módulo de productos.
 * Encapsula todas las operaciones CRUD sobre la entidad Producto
 * utilizando Hibernate como framework de persistencia (ORM).
 */
public class ProductoDAO {

    /**
     * Guarda un nuevo producto en la base de datos.
     * @param producto objeto a persistir (sin id, se genera automáticamente)
     * @return true si se guardó correctamente, false si ocurrió un error
     *         (por ejemplo, código duplicado que viola la restricción unique)
     */
    public boolean guardar(Producto producto) {
        Session session = null;
        Transaction tx = null;
        try {
            // Ojo: no se usa try-with-resources aquí a propósito. Si se usa,
            // Java cierra la sesión automáticamente ANTES de que el catch
            // pueda hacer tx.rollback(), y eso lanza un segundo error
            // ("session is closed"). Por eso se cierra manualmente al final.
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(producto);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Busca un producto por su identificador (clave primaria).
     * @param idProducto id del producto
     * @return el producto encontrado, o null si no existe
     */
    public Producto buscarPorId(int idProducto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Producto.class, idProducto);
        }
    }

    /**
     * Obtiene el listado completo de productos registrados.
     * @return lista de productos (vacía si no hay ninguno)
     */
    public List<Producto> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery("FROM Producto", Producto.class);
            return query.list();
        }
    }

    /**
     * Actualiza los datos de un producto existente.
     * @param producto objeto con el id ya cargado y los campos modificados
     * @return true si se actualizó correctamente, false si ocurrió un error
     */
    public boolean actualizar(Producto producto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(producto);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Elimina un producto de la base de datos según su id.
     * Si el producto no existe, no realiza ninguna acción.
     * @param idProducto id del producto a eliminar
     */
    public void eliminar(int idProducto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Producto producto = session.get(Producto.class, idProducto);
            if (producto != null) {
                session.delete(producto);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Busca un producto por su código único.
     * Útil para validar duplicados antes de guardar.
     * @param codigo código del producto
     * @return el producto encontrado, o null si no existe
     */
    public Producto buscarPorCodigo(String codigo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto WHERE codigo = :codigo", Producto.class);
            query.setParameter("codigo", codigo);
            return query.uniqueResult();
        }
    }
}