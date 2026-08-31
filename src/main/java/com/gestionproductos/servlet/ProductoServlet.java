package com.gestionproductos.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gestionproductos.dao.ProductoDAO;
import com.gestionproductos.model.Producto;

/**
 * Servlet encargado de gestionar las operaciones CRUD
 * (Crear, Listar, Editar, Eliminar) sobre la entidad Producto.
 */
@WebServlet("/ProductoServlet")
public class ProductoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// DAO reutilizado para todas las operaciones de acceso a datos
	private final ProductoDAO productoDAO = new ProductoDAO();

	public ProductoServlet() {
		super();
	}

	/**
	 * Maneja las peticiones GET. Según el parámetro "accion" decide
	 * si listar todos los productos, mostrar el formulario de edición
	 * o eliminar un producto.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accion = request.getParameter("accion");
		if (accion == null) {
			accion = "listar"; // acción por defecto
		}

		switch (accion) {
			case "eliminar":
				eliminarProducto(request, response);
				break;
			case "editar":
				mostrarFormularioEdicion(request, response);
				break;
			case "listar":
			default:
				listarProductos(request, response);
				break;
		}
	}

	/**
	 * Maneja las peticiones POST: creación de un nuevo producto
	 * o actualización de uno existente, según venga o no el idProducto.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Leer los datos comunes del formulario
		String codigo = request.getParameter("codigo");
		String nombre = request.getParameter("nombre");
		String descripcion = request.getParameter("descripcion");
		double precioCompra = Double.parseDouble(request.getParameter("precioCompra"));
		double precioVenta = Double.parseDouble(request.getParameter("precioVenta"));
		int stockActual = Integer.parseInt(request.getParameter("stockActual"));

		String stockMinimoStr = request.getParameter("stockMinimo");
		int stockMinimo = (stockMinimoStr == null || stockMinimoStr.isEmpty())
				? 5 : Integer.parseInt(stockMinimoStr);

		String idCategoriaStr = request.getParameter("idCategoria");
		Integer idCategoria = (idCategoriaStr == null || idCategoriaStr.isEmpty())
				? null : Integer.valueOf(idCategoriaStr);

		// Si viene idProducto, es una actualización; si no, es un producto nuevo
		String idProductoStr = request.getParameter("idProducto");

		if (idProductoStr != null && !idProductoStr.isEmpty()) {
			// --- ACTUALIZAR ---
			Producto producto = productoDAO.buscarPorId(Integer.parseInt(idProductoStr));
			producto.setCodigo(codigo);
			producto.setNombre(nombre);
			producto.setDescripcion(descripcion);
			producto.setPrecioCompra(precioCompra);
			producto.setPrecioVenta(precioVenta);
			producto.setStockActual(stockActual);
			producto.setStockMinimo(stockMinimo);
			producto.setIdCategoria(idCategoria);

			productoDAO.actualizar(producto);
		} else {
			// --- CREAR ---
			Producto producto = new Producto(codigo, nombre, descripcion,
					precioCompra, precioVenta, stockActual, stockMinimo, idCategoria);
			productoDAO.guardar(producto);
		}

		// Después de guardar o actualizar, redirige al listado
		response.sendRedirect("ProductoServlet?accion=listar");
	}

	/**
	 * Obtiene todos los productos y los envía a listaProductos.jsp
	 */
	private void listarProductos(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Producto> productos = productoDAO.listarTodos();
		request.setAttribute("productos", productos);
		request.getRequestDispatcher("listaProductos.jsp").forward(request, response);
	}

	/**
	 * Busca un producto por id y lo envía a formularioEditar.jsp
	 * para precargar los campos.
	 */
	private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		Producto producto = productoDAO.buscarPorId(id);
		request.setAttribute("producto", producto);
		request.getRequestDispatcher("formularioEditar.jsp").forward(request, response);
	}

	/**
	 * Elimina un producto por id y redirige de vuelta al listado.
	 */
	private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		productoDAO.eliminar(id);
		response.sendRedirect("ProductoServlet?accion=listar");
	}
}