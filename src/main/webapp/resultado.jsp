<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Resultado - Gestión de Productos</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .contenedor {
            background: #fff;
            padding: 30px 40px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.15);
            min-width: 320px;
        }
        h2 {
            color: #2c3e50;
            margin-top: 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        td {
            padding: 8px;
            border-bottom: 1px solid #eee;
        }
        .etiqueta {
            font-weight: bold;
            color: #555;
        }
        .total {
            font-size: 1.2em;
            color: #27ae60;
            font-weight: bold;
        }
        a {
            display: inline-block;
            margin-top: 20px;
            text-decoration: none;
            color: #2980b9;
        }
    </style>
</head>
<body>
    <div class="contenedor">
        <h2>Resultado del registro</h2>
        <table>
            <tr>
                <td class="etiqueta">Nombre del producto:</td>
                <td>${nombre}</td>
            </tr>
            <tr>
                <td class="etiqueta">Precio unitario:</td>
                <td>$${precio}</td>
            </tr>
            <tr>
                <td class="etiqueta">Cantidad:</td>
                <td>${cantidad}</td>
            </tr>
            <tr>
                <td class="etiqueta">Total:</td>
                <td class="total">$${total}</td>
            </tr>
        </table>
        <a href="formulario.html">&larr; Registrar otro producto</a>
    </div>
</body>
</html>