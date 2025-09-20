package com.example.lab2_20221957;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistorialManager {
    private static final String TAG = "HistorialManager";
    private static final String PREFS_NAME = "TeleCatHistorial";
    private static final String KEY_HISTORIAL = "historial_items";
    private static final String KEY_CONTADOR = "contador_interacciones";
    
    private static HistorialManager instance;
    private SharedPreferences preferences;
    private Gson gson;
    
    private HistorialManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized HistorialManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistorialManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void agregarInteraccion(int cantidadImagenes, String textoPersonalizado) {
        List<HistorialItem> historial = obtenerHistorial();
        int numeroInteraccion = obtenerSiguienteNumeroInteraccion();
        
        HistorialItem nuevoItem = new HistorialItem(cantidadImagenes, textoPersonalizado, numeroInteraccion);
        historial.add(nuevoItem);
        
        guardarHistorial(historial);
        guardarContadorInteracciones(numeroInteraccion);
        
        Log.d(TAG, "Nueva interacción agregada: " + nuevoItem.toString());
    }
    
    public List<HistorialItem> obtenerHistorial() {
        String historialJson = preferences.getString(KEY_HISTORIAL, "");
        
        if (historialJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<List<HistorialItem>>(){}.getType();
            List<HistorialItem> historial = gson.fromJson(historialJson, listType);
            return historial != null ? historial : new ArrayList<HistorialItem>();
        } catch (Exception e) {
            Log.e(TAG, "Error al deserializar historial: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private void guardarHistorial(List<HistorialItem> historial) {
        try {
            String historialJson = gson.toJson(historial);
            preferences.edit()
                    .putString(KEY_HISTORIAL, historialJson)
                    .apply();
            
            Log.d(TAG, "Historial guardado exitosamente. Total items: " + historial.size());
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar historial: " + e.getMessage());
        }
    }
    
    private int obtenerSiguienteNumeroInteraccion() {
        int ultimoNumero = preferences.getInt(KEY_CONTADOR, 0);
        return ultimoNumero + 1;
    }
    
    private void guardarContadorInteracciones(int numero) {
        preferences.edit()
                .putInt(KEY_CONTADOR, numero)
                .apply();
    }
    
    public void limpiarHistorial() {
        preferences.edit()
                .remove(KEY_HISTORIAL)
                .remove(KEY_CONTADOR)
                .apply();
        
        Log.d(TAG, "Historial limpiado completamente");
    }
    
    public int getTotalInteracciones() {
        return obtenerHistorial().size();
    }
    
    public HistorialItem getUltimaInteraccion() {
        List<HistorialItem> historial = obtenerHistorial();
        if (historial.isEmpty()) {
            return null;
        }
        return historial.get(historial.size() - 1);
    }
}
