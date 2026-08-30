package com.gestionproductos.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ProductoServlet
 */
@WebServlet("/ProductoServlet")
public class ProductoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public ProductoServlet() {
		super();
	}

	/**
	 * Maneja las peticiones GET (por ejemplo, si alguien entra directo a la URL del servlet)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<h2>Este servlet espera datos enviados por POST desde el formulario.</h2>");
	}

	/**
	 * Maneja las peticiones POST (cuando se envía el formulario)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. Recibir los datos que vienen del formulario (deben coincidir con los "name" del HTML)
		String nombre = request.getParameter("nombre");
		String precioStr = request.getParameter("precio");
		String cantidadStr = request.getParameter("cantidad");

		// 2. Convertir los valores numéricos
		double precio = Double.parseDouble(precioStr);
		int cantidad = Integer.parseInt(cantidadStr);

		// 3. Hacer un cálculo simple (ejemplo: valor total del producto)
		double total = precio * cantidad;

		// 4. Guardar los datos en el "request" para poder mostrarlos en el JSP
		request.setAttribute("nombre", nombre);
		request.setAttribute("precio", precio);
		request.setAttribute("cantidad", cantidad);
		request.setAttribute("total", total);

		// 5. Redirigir hacia la página JSP que muestra el resultado
		request.getRequestDispatcher("resultado.jsp").forward(request, response);
	}
}