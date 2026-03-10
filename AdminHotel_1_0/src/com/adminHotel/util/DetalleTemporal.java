package com.adminHotel.util;

import com.adminHotel.vo.ProductoVO;

public class DetalleTemporal {
    private ProductoVO producto;
    private int cantidad;

    public DetalleTemporal(ProductoVO producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }
    // Getters
    public ProductoVO getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public java.math.BigDecimal getSubtotal() {
        return producto.getPrecioVenta().multiply(new java.math.BigDecimal(cantidad));
    }
}