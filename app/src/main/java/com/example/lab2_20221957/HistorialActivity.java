package com.example.lab2_20221957;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private static final String TAG = "HistorialActivity";
    
    private LinearLayout layoutHistorial;
    private Button btnVolverAJugar;
    private HistorialManager historialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        
        inicializarComponentes();
        cargarHistorial();
        configurarEventos();
    }
    
    private void inicializarComponentes() {
        layoutHistorial = findViewById(R.id.layoutHistorial);
        btnVolverAJugar = findViewById(R.id.btnVolverAJugar);
        
        historialManager = HistorialManager.getInstance(this);
    }
    
    private void cargarHistorial() {
        List<HistorialItem> historial = historialManager.obtenerHistorial();
        
        Log.d(TAG, "Cargando historial con " + historial.size() + " elementos");
        
        // Limpiar layout antes de agregar nuevos elementos
        layoutHistorial.removeAllViews();
        
        if (historial.isEmpty()) {
            // Mostrar mensaje si no hay historial
            mostrarMensajeVacio();
        } else {
            // Mostrar cada item del historial
            for (HistorialItem item : historial) {
                agregarItemHistorial(item);
            }
        }
    }
    
    private void mostrarMensajeVacio() {
        TextView tvVacio = new TextView(this);
        tvVacio.setText("No hay interacciones registradas aún.\n¡Empieza a jugar para crear tu historial!");
        tvVacio.setTextSize(16);
        tvVacio.setTextColor(0xFF666666);
        tvVacio.setPadding(24, 48, 24, 48);
        tvVacio.setGravity(android.view.Gravity.CENTER);
        
        layoutHistorial.addView(tvVacio);
    }
    
    private void agregarItemHistorial(HistorialItem item) {
        // Crear contenedor para el item
        LinearLayout itemContainer = new LinearLayout(this);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        itemContainer.setPadding(0, 0, 0, 24);
        
        // Texto principal de la interacción
        TextView tvInteraccion = new TextView(this);
        tvInteraccion.setText(item.getDescripcionCompleta());
        tvInteraccion.setTextSize(18);
        tvInteraccion.setTextColor(0xFF2C3E50);
        tvInteraccion.setPadding(0, 12, 0, 4);
        
        // Información adicional si hay texto personalizado
        if (item.getTextoPersonalizado() != null && !item.getTextoPersonalizado().trim().isEmpty()) {
            TextView tvTexto = new TextView(this);
            tvTexto.setText("Texto: \"" + item.getTextoPersonalizado() + "\"");
            tvTexto.setTextSize(14);
            tvTexto.setTextColor(0xFF666666);
            tvTexto.setPadding(16, 0, 0, 4);
            itemContainer.addView(tvTexto);
        }
        
        // Fecha y hora
        TextView tvFecha = new TextView(this);
        tvFecha.setText("Fecha: " + item.getFechaFormateada());
        tvFecha.setTextSize(12);
        tvFecha.setTextColor(0xFF888888);
        tvFecha.setPadding(16, 0, 0, 8);
        
        // Agregar elementos al contenedor
        itemContainer.addView(tvInteraccion);
        itemContainer.addView(tvFecha);
        
        // Línea separadora
        View separador = new View(this);
        separador.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            2
        ));
        separador.setBackgroundColor(0xFFE0E0E0);
        itemContainer.addView(separador);
        
        // Agregar al layout principal
        layoutHistorial.addView(itemContainer);
        
        Log.d(TAG, "Item agregado al historial: " + item.getDescripcionCompleta());
    }
    
    private void configurarEventos() {
        btnVolverAJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogConfirmacion();
            }
        });
    }
    
    private void mostrarDialogConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar acción");
        builder.setMessage("¿Está seguro que desea volver a jugar?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        
        // Botón "Si"
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                volverAlMenuPrincipal();
            }
        });
        
        // Botón "No"
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Solo cerrar el dialog, quedarse en la misma pantalla
                dialog.dismiss();
                Toast.makeText(HistorialActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Configurar para que no se cierre al tocar fuera
        builder.setCancelable(false);
        
        // Mostrar dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void volverAlMenuPrincipal() {
        // Crear intent para volver al MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        
        // Limpiar el stack de actividades para empezar fresh
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        Toast.makeText(this, "Volviendo al menú principal...", Toast.LENGTH_SHORT).show();
        
        startActivity(intent);
        finish(); // Terminar esta actividad
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar historial por si hay cambios
        cargarHistorial();
    }
    
    @Override
    public void onBackPressed() {
        // Interceptar botón back para mostrar confirmación también
        mostrarDialogConfirmacion();
    }
}
