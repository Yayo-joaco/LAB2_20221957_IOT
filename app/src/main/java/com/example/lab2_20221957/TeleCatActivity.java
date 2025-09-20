package com.example.lab2_20221957;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeleCatActivity extends AppCompatActivity {

    private static final String TAG = "TeleCatActivity";
    private static final long TIEMPO_POR_IMAGEN = 4000; // 4 segundos por imagen
    
    // UI Components
    private TextView tvTiempoRestante;
    private TextView tvCantidad;
    private ImageView ivGato;
    private Button btnSiguiente;
    
    // Timer y threading
    private CountDownTimer countDownTimer;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    // Variables de estado
    private int cantidadImagenes;
    private long tiempoTotalMs;
    private long tiempoRestanteMs;
    private int imagenActualIndex = 0;
    private String textoPersonalizado = "";
    private boolean timerTerminado = false;
    
    // Variables para persistencia durante rotación
    private static long tiempoRestanteGlobal = 0;
    private static boolean timerActivoGlobal = false;
    
    // Método estático para limpiar variables globales al reiniciar juego
    public static void limpiarEstadoGlobal() {
        tiempoRestanteGlobal = 0;
        timerActivoGlobal = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telecat);
        
        inicializarComponentes();
        obtenerDatosIntent();
        configurarExecutorService();
        
        if (savedInstanceState != null) {
            restaurarEstado(savedInstanceState);
        } else if (timerActivoGlobal && tiempoRestanteGlobal > 0) {
            // Restaurar desde variables estáticas (rotación de pantalla)
            tiempoRestanteMs = tiempoRestanteGlobal;
            iniciarTimer();
        } else {
            // Primera vez que se abre la actividad
            inicializarTimer();
        }
        
        cargarPrimeraImagen();
        configurarEventos();
    }
    
    private void inicializarComponentes() {
        tvTiempoRestante = findViewById(R.id.tvTiempoRestante);
        tvCantidad = findViewById(R.id.tvCantidad);
        ivGato = findViewById(R.id.ivGato);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    private void obtenerDatosIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cantidadImagenes = extras.getInt("cantidad", 3);
            textoPersonalizado = extras.getString("texto", "");
            
            tvCantidad.setText("Cantidad = " + cantidadImagenes);
            tiempoTotalMs = cantidadImagenes * TIEMPO_POR_IMAGEN;
        } else {
            // Valores por defecto
            cantidadImagenes = 3;
            tiempoTotalMs = cantidadImagenes * TIEMPO_POR_IMAGEN;
            tvCantidad.setText("Cantidad = " + cantidadImagenes);
        }
    }
    
    private void configurarExecutorService() {
        executorService = Executors.newFixedThreadPool(2);
    }
    
    private void inicializarTimer() {
        tiempoRestanteMs = tiempoTotalMs;
        timerActivoGlobal = true;
        tiempoRestanteGlobal = tiempoRestanteMs;
        iniciarTimer();
    }
    
    private void iniciarTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        countDownTimer = new CountDownTimer(tiempoRestanteMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteMs = millisUntilFinished;
                tiempoRestanteGlobal = millisUntilFinished;
                
                // Actualizar UI del tiempo
                long segundos = millisUntilFinished / 1000;
                long minutos = segundos / 60;
                segundos = segundos % 60;
                
                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                tvTiempoRestante.setText(tiempoFormateado);
                
                // Verificar si es tiempo de cambiar imagen (cada 4 segundos)
                long tiempoTranscurridoMs = tiempoTotalMs - millisUntilFinished;
                int imagenQueDebeMostrar = (int) (tiempoTranscurridoMs / TIEMPO_POR_IMAGEN);
                
                if (imagenQueDebeMostrar > imagenActualIndex && imagenQueDebeMostrar < cantidadImagenes) {
                    imagenActualIndex = imagenQueDebeMostrar;
                    cargarSiguienteImagen();
                }
            }
            
            @Override
            public void onFinish() {
                timerTerminado = true;
                timerActivoGlobal = false;
                tiempoRestanteGlobal = 0;
                
                tvTiempoRestante.setText("00:00");
                btnSiguiente.setEnabled(true);
                btnSiguiente.setBackgroundColor(0xFF27AE60); // Verde
                
                Toast.makeText(TeleCatActivity.this, "¡Tiempo terminado! Presiona Siguiente", 
                              Toast.LENGTH_LONG).show();
            }
        };
        
        countDownTimer.start();
    }
    
    private void cargarPrimeraImagen() {
        cargarImagenDeAPI(0);
    }
    
    private void cargarSiguienteImagen() {
        cargarImagenDeAPI(imagenActualIndex);
    }
    
    private void cargarImagenDeAPI(final int indiceImagen) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlImagen;
                    
                    // Construir URL según si hay texto personalizado o no
                    if (textoPersonalizado != null && !textoPersonalizado.trim().isEmpty()) {
                        // URL con texto personalizado
                        urlImagen = "https://cataas.com/cat/says/" + 
                                   java.net.URLEncoder.encode(textoPersonalizado.trim(), "UTF-8") +
                                   "?fontSize=30&fontColor=white";
                    } else {
                        // URL para imagen aleatoria sin texto
                        urlImagen = "https://cataas.com/cat?t=" + System.currentTimeMillis(); // timestamp para evitar cache
                    }
                    
                    Log.d(TAG, "Cargando imagen " + (indiceImagen + 1) + " desde: " + urlImagen);
                    
                    // Realizar petición HTTP
                    URL url = new URL(urlImagen);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(10000); // 10 segundos timeout
                    connection.setReadTimeout(10000);
                    connection.connect();
                    
                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    connection.disconnect();
                    
                    // Actualizar UI en el hilo principal
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null) {
                                ivGato.setImageBitmap(bitmap);
                                Log.d(TAG, "Imagen " + (indiceImagen + 1) + " cargada exitosamente");
                            } else {
                                Log.e(TAG, "Error al decodificar imagen " + (indiceImagen + 1));
                                mostrarImagenError();
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar imagen " + (indiceImagen + 1) + ": " + e.getMessage());
                    
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mostrarImagenError();
                        }
                    });
                }
            }
        });
    }
    
    private void mostrarImagenError() {
        // Mostrar imagen por defecto en caso de error
        ivGato.setImageResource(android.R.drawable.ic_menu_gallery);
        Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
    }
    
    private void configurarEventos() {
        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerTerminado) {
                    // Ir a la siguiente actividad (Vista de Finalización)
                    irAVistaFinalizacion();
                } else {
                    Toast.makeText(TeleCatActivity.this, 
                                  "Espera a que termine el tiempo", 
                                  Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void irAVistaFinalizacion() {
        // Guardar la interacción en el historial
        guardarInteraccionEnHistorial();
        
        // Navegar a HistorialActivity
        Intent intent = new Intent(this, HistorialActivity.class);
        startActivity(intent);
        
        // Terminar esta actividad
        finish();
    }
    
    private void guardarInteraccionEnHistorial() {
        try {
            HistorialManager historialManager = HistorialManager.getInstance(this);
            
            // Guardar la interacción actual
            historialManager.agregarInteraccion(cantidadImagenes, textoPersonalizado);
            
            Log.d(TAG, "Interacción guardada en historial: " + cantidadImagenes + " imágenes, texto: '" + textoPersonalizado + "'");
            
            Toast.makeText(this, "¡Sesión guardada en el historial!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar en historial: " + e.getMessage());
            Toast.makeText(this, "Error al guardar sesión", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("tiempoRestante", tiempoRestanteMs);
        outState.putInt("imagenActualIndex", imagenActualIndex);
        outState.putBoolean("timerTerminado", timerTerminado);
    }
    
    private void restaurarEstado(Bundle savedInstanceState) {
        tiempoRestanteMs = savedInstanceState.getLong("tiempoRestante", tiempoTotalMs);
        imagenActualIndex = savedInstanceState.getInt("imagenActualIndex", 0);
        timerTerminado = savedInstanceState.getBoolean("timerTerminado", false);
        
        tiempoRestanteGlobal = tiempoRestanteMs;
        
        if (!timerTerminado && tiempoRestanteMs > 0) {
            iniciarTimer();
        } else if (timerTerminado) {
            btnSiguiente.setEnabled(true);
            btnSiguiente.setBackgroundColor(0xFF27AE60);
            tvTiempoRestante.setText("00:00");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // Solo limpiar variables estáticas si el timer terminó
        if (timerTerminado) {
            timerActivoGlobal = false;
            tiempoRestanteGlobal = 0;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // El timer continúa corriendo en background
        Log.d(TAG, "Actividad pausada, timer continúa");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Actividad resumida");
        
        // Verificar si necesitamos sincronizar el timer
        if (timerActivoGlobal && !timerTerminado) {
            // El timer sigue corriendo, solo actualizamos la UI
            Log.d(TAG, "Timer sigue activo, tiempo restante: " + tiempoRestanteGlobal + "ms");
        }
    }
}
