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

	// Longitudes máximas, iguales a las de las columnas en MySQL
	private static final int MAX_CODIGO = 50;
	private static final int MAX_NOMBRE = 150;
	// Máximo valor de una columna DECIMAL(10,2)
	private static final double MAX_PRECIO = 99999999.99;

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

		// Codificación UTF-8 para leer y escribir tildes y la letra ñ correctamente
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

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

		// IMPORTANTE: debe ejecutarse ANTES de la primera llamada a getParameter().
		// Sin esto, Tomcat interpreta el formulario como ISO-8859-1 y las tildes
		// y la ñ se guardan dañadas en la base de datos.
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		// Leer los datos de texto del formulario
		String codigo = request.getParameter("codigo");
		String nombre = request.getParameter("nombre");
		String descripcion = request.getParameter("descripcion");

		// Se eliminan espacios al inicio y al final
		codigo = (codigo == null) ? null : codigo.trim();
		nombre = (nombre == null) ? null : nombre.trim();

		// Validar los campos de texto antes de convertir los números
		if (codigo == null || codigo.isEmpty() || nombre == null || nombre.isEmpty()) {
			mostrarError(response, "El código y el nombre son obligatorios.");
			return;
		}
		if (codigo.length() > MAX_CODIGO) {
			mostrarError(response, "El código no puede superar los " + MAX_CODIGO + " caracteres.");
			return;
		}
		if (nombre.length() > MAX_NOMBRE) {
			mostrarError(response, "El nombre no puede superar los " + MAX_NOMBRE + " caracteres.");
			return;
		}

		// Se valida cada número por separado para poder mostrar un mensaje
		// de error claro en lugar de una excepción genérica del servidor.
		double precioCompra, precioVenta;
		int stockActual, stockMinimo;
		Integer idCategoria;
		try {
			precioCompra = Double.parseDouble(request.getParameter("precioCompra"));
			precioVenta = Double.parseDouble(request.getParameter("precioVenta"));
			stockActual = Integer.parseInt(request.getParameter("stockActual"));

			String stockMinimoStr = request.getParameter("stockMinimo");
			stockMinimo = (stockMinimoStr == null || stockMinimoStr.isEmpty())
					? 5 : Integer.parseInt(stockMinimoStr);

			String idCategoriaStr = request.getParameter("idCategoria");
			idCategoria = (idCategoriaStr == null || idCategoriaStr.isEmpty())
					? null : Integer.valueOf(idCategoriaStr);
		} catch (NumberFormatException | NullPointerException e) {
			// Alguno de los campos numéricos venía vacío o con texto no numérico
			mostrarError(response, "Los campos de precio, stock y categoría deben ser numéricos.");
			return;
		}

		// Validaciones de negocio antes de tocar la base de datos
		if (Double.isNaN(precioCompra) || Double.isInfinite(precioCompra)
				|| Double.isNaN(precioVenta) || Double.isInfinite(precioVenta)) {
			mostrarError(response, "Los precios deben ser números válidos.");
			return;
		}
		if (precioCompra < 0 || precioVenta < 0 || stockActual < 0 || stockMinimo < 0) {
			mostrarError(response, "Los valores numéricos no pueden ser negativos.");
			return;
		}
		if (precioCompra > MAX_PRECIO || precioVenta > MAX_PRECIO) {
			mostrarError(response, "El precio supera el valor máximo permitido.");
			return;
		}

		// Si viene idProducto, es una actualización; si no, es un producto nuevo
		String idProductoStr = request.getParameter("idProducto");
		boolean operacionExitosa;

		if (idProductoStr != null && !idProductoStr.isEmpty()) {
			// --- ACTUALIZAR ---
			int idProducto;
			try {
				idProducto = Integer.parseInt(idProductoStr);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El identificador del producto no es válido.");
				return;
			}

			Producto producto = productoDAO.buscarPorId(idProducto);
			if (producto == null) {
				// El id no existe en la base de datos: evita el NullPointerException
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"El producto que intenta editar ya no existe.");
				return;
			}
			producto.setCodigo(codigo);
			producto.setNombre(nombre);
			producto.setDescripcion(descripcion);
			producto.setPrecioCompra(precioCompra);
			producto.setPrecioVenta(precioVenta);
			producto.setStockActual(stockActual);
			producto.setStockMinimo(stockMinimo);
			producto.setIdCategoria(idCategoria);

			operacionExitosa = productoDAO.actualizar(producto);
		} else {
			// --- CREAR ---
			Producto producto = new Producto(codigo, nombre, descripcion,
					precioCompra, precioVenta, stockActual, stockMinimo, idCategoria);
			operacionExitosa = productoDAO.guardar(producto);
		}

		if (!operacionExitosa) {
			// El guardado falló (por ejemplo, código duplicado o categoría inexistente):
			// se informa al usuario en lugar de redirigir como si todo hubiera salido bien.
			mostrarError(response, "No se pudo guardar el producto. Verifique que el código no esté repetido "
					+ "y que la categoría exista.");
			return;
		}

		// Después de guardar o actualizar con éxito, redirige al listado
		response.sendRedirect("ProductoServlet?accion=listar");
	}

	/**
	 * Muestra una página sencilla de error con el mensaje indicado y un
	 * enlace para volver al formulario, en lugar de dejar que la
	 * aplicación falle silenciosamente o redirija como si todo hubiera
	 * salido bien.
	 */
	private void mostrarError(HttpServletResponse response, String mensaje) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf(
				"<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
				"<title>Error</title></head><body>" +
				"<h2 style='color:#c0392b;'>%s</h2>" +
				"<a href='javascript:history.back()'>&larr; Volver al formulario</a>" +
				"</body></html>", mensaje);
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
		int id;
		try {
			id = Integer.parseInt(request.getParameter("id"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El identificador del producto no es válido.");
			return;
		}

		Producto producto = productoDAO.buscarPorId(id);
		if (producto == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "El producto solicitado no existe.");
			return;
		}
		request.setAttribute("producto", producto);
		request.getRequestDispatcher("formularioEditar.jsp").forward(request, response);
	}

	/**
	 * Elimina un producto por id y redirige de vuelta al listado.
	 */
	private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int id;
		try {
			id = Integer.parseInt(request.getParameter("id"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El identificador del producto no es válido.");
			return;
		}
		productoDAO.eliminar(id);
		response.sendRedirect("ProductoServlet?accion=listar");
	}
}