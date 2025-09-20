package com.example.lab2_20221957;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistorialItem {
    private int cantidadImagenes;
    private String textoPersonalizado;
    private long timestamp;
    private int numeroInteraccion;
    
    public HistorialItem(int cantidadImagenes, String textoPersonalizado, int numeroInteraccion) {
        this.cantidadImagenes = cantidadImagenes;
        this.textoPersonalizado = textoPersonalizado;
        this.timestamp = System.currentTimeMillis();
        this.numeroInteraccion = numeroInteraccion;
    }
    
    // Constructor para restaurar desde preferencias
    public HistorialItem(int cantidadImagenes, String textoPersonalizado, long timestamp, int numeroInteraccion) {
        this.cantidadImagenes = cantidadImagenes;
        this.textoPersonalizado = textoPersonalizado;
        this.timestamp = timestamp;
        this.numeroInteraccion = numeroInteraccion;
    }
    
    public int getCantidadImagenes() {
        return cantidadImagenes;
    }
    
    public String getTextoPersonalizado() {
        return textoPersonalizado;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public int getNumeroInteraccion() {
        return numeroInteraccion;
    }
    
    public String getFechaFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    public String getDescripcionCompleta() {
        return "Interaccion" + numeroInteraccion + ": " + cantidadImagenes + " imagenes";
    }
    
    @Override
    public String toString() {
        return "HistorialItem{" +
                "cantidadImagenes=" + cantidadImagenes +
                ", textoPersonalizado='" + textoPersonalizado + '\'' +
                ", numeroInteraccion=" + numeroInteraccion +
                ", fecha='" + getFechaFormateada() + '\'' +
                '}';
    }
}
