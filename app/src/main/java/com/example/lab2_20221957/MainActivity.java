package com.example.lab2_20221957;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText etCantidad;
    private Spinner spinnerTexto;
    private EditText etEscribirTexto;
    private Button btnComprobarConexion;
    private Button btnComenzar;
    
    private boolean conexionVerificada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        inicializarComponentes();
        configurarSpinner();
        configurarEventos();
        
        // Limpiar estado del timer anterior si se reinicia el juego
        limpiarEstadoTimerAnterior();
    }
    
    private void limpiarEstadoTimerAnterior() {
        // Reset del estado de conexión para empezar fresh
        conexionVerificada = false;
        
        // Limpiar variables estáticas del timer de TeleCatActivity
        TeleCatActivity.limpiarEstadoGlobal();
        
        // Reset visual de botones
        resetearEstadoBotones();
    }
    
    private void resetearEstadoBotones() {
        // Asegurar que los botones estén en su estado inicial
        if (btnComprobarConexion != null) {
            btnComprobarConexion.setText("⚠️ Comprobar Conexión");
            btnComprobarConexion.setBackgroundColor(0xFFE74C3C); // Rojo
        }
        
        if (btnComenzar != null) {
            btnComenzar.setEnabled(false);
            btnComenzar.setBackgroundColor(0xFF95A5A6); // Gris
        }
    }
    
    private void inicializarComponentes() {
        etCantidad = findViewById(R.id.etCantidad);
        spinnerTexto = findViewById(R.id.spinnerTexto);
        etEscribirTexto = findViewById(R.id.etEscribirTexto);
        btnComprobarConexion = findViewById(R.id.btnComprobarConexion);
        btnComenzar = findViewById(R.id.btnComenzar);
    }
    
    private void configurarSpinner() {
        // Configurar el adapter para el Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.opciones_texto,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTexto.setAdapter(adapter);
    }
    
    private void configurarEventos() {
        // Evento para el Spinner - habilitar/deshabilitar campo de texto
        spinnerTexto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String opcionSeleccionada = parent.getItemAtPosition(position).toString();
                
                if ("Si".equals(opcionSeleccionada)) {
                    etEscribirTexto.setEnabled(true);
                    etEscribirTexto.setBackground(getDrawable(android.R.drawable.edit_text));
                } else {
                    etEscribirTexto.setEnabled(false);
                    etEscribirTexto.setText("");
                    etEscribirTexto.setBackground(getDrawable(android.R.drawable.edit_text));
                    etEscribirTexto.setAlpha(0.5f);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etEscribirTexto.setEnabled(false);
            }
        });
        
        // Evento para el botón de comprobar conexión
        btnComprobarConexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarConexionInternet();
            }
        });
        
        // Evento para el botón comenzar
        btnComenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarFormulario()) {
                    iniciarSiguienteActividad();
                }
            }
        });
    }
    
    private void comprobarConexionInternet() {
        // Mostrar mensaje de verificación
        Toast.makeText(this, getString(R.string.verificando_conexion), Toast.LENGTH_SHORT).show();
        
        // Verificar conectividad
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        
        if (networkInfo != null && networkInfo.isConnected()) {
            // Hay conexión
            Toast.makeText(this, getString(R.string.conexion_exitosa), Toast.LENGTH_LONG).show();
            conexionVerificada = true;
            
            // Habilitar botón comenzar
            btnComenzar.setEnabled(true);
            btnComenzar.setBackground(getDrawable(android.R.drawable.btn_default));
            btnComenzar.setBackgroundColor(0xFF27AE60); // Color verde
            
            // Cambiar apariencia del botón de conexión
            btnComprobarConexion.setText("✅ Conexión Verificada");
            btnComprobarConexion.setBackgroundColor(0xFF27AE60); // Verde
            
        } else {
            // No hay conexión
            Toast.makeText(this, getString(R.string.sin_conexion), Toast.LENGTH_LONG).show();
            conexionVerificada = false;
            
            // Mantener botón comenzar deshabilitado
            btnComenzar.setEnabled(false);
            btnComenzar.setBackgroundColor(0xFF95A5A6); // Gris
        }
    }
    
    private boolean validarFormulario() {
        String cantidad = etCantidad.getText().toString().trim();
        String textoSeleccionado = spinnerTexto.getSelectedItem().toString();
        
        if (cantidad.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese la cantidad", Toast.LENGTH_SHORT).show();
            etCantidad.requestFocus();
            return false;
        }
        
        if ("Elegir".equals(textoSeleccionado)) {
            Toast.makeText(this, "Por favor seleccione una opción en Texto", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if ("Si".equals(textoSeleccionado)) {
            String textoEscrito = etEscribirTexto.getText().toString().trim();
            if (textoEscrito.isEmpty()) {
                Toast.makeText(this, "Por favor escriba el texto solicitado", Toast.LENGTH_SHORT).show();
                etEscribirTexto.requestFocus();
                return false;
            }
        }
        
        if (!conexionVerificada) {
            Toast.makeText(this, "Primero debe verificar la conexión a internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void iniciarSiguienteActividad() {
        String cantidad = etCantidad.getText().toString();
        String textoOpcion = spinnerTexto.getSelectedItem().toString();
        String textoEscrito = etEscribirTexto.getText().toString();
        
        // Crear Intent para TeleCatActivity
        Intent intent = new Intent(this, TeleCatActivity.class);
        
        // Pasar parámetros
        intent.putExtra("cantidad", Integer.parseInt(cantidad));
        
        // Solo pasar texto personalizado si se seleccionó "Si"
        if ("Si".equals(textoOpcion) && !textoEscrito.trim().isEmpty()) {
            intent.putExtra("texto", textoEscrito.trim());
        }
        
        // Mostrar mensaje de confirmación
        String mensaje = "Iniciando TeleCat:\n" +
                "Cantidad: " + cantidad + " imágenes\n" +
                "Tiempo total: " + (Integer.parseInt(cantidad) * 4) + " segundos";
        
        if ("Si".equals(textoOpcion)) {
            mensaje += "\nTexto: " + textoEscrito;
        }
        
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        
        // Iniciar TeleCatActivity
        startActivity(intent);
    }
}