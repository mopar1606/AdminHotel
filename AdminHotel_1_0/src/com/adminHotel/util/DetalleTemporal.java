package com.adminHotel.util;

import com.adminHotel.vo.ProductoVO;

public class DetalleTemporal {
    private ProductoVO producto;
    private int cantidad;
    // ----------------------------------------
    // Constructor con parámetros (uso original)
    // ----------------------------------------
    public DetalleTemporal(ProductoVO producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }
    // ----------------------------------------
    // Constructor vacío (para instanciar y setear)
    // ----------------------------------------
    public DetalleTemporal() {
    }
    // ----------------------------------------
    // Getters
    // ----------------------------------------
    public ProductoVO getProducto() { return producto; }
    public int getCantidad()        { return cantidad; }
    public java.math.BigDecimal getSubtotal() {
        if (producto == null || producto.getPrecioVenta() == null) {
            return java.math.BigDecimal.ZERO;
        }
        return producto.getPrecioVenta().multiply(new java.math.BigDecimal(cantidad));
    }
    // ----------------------------------------
    // Setters (nuevos)
    // ----------------------------------------
    public void setProducto(ProductoVO producto) { this.producto = producto; }
    public void setCantidad(int cantidad)         { this.cantidad = cantidad; }
}