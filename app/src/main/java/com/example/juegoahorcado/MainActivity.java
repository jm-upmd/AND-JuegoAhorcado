package com.example.juegoahorcado;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Partida mPartida;
    private GridLayout mTeclado;
    private TextView mTxtVidas;
    private TextView mTxtIntentos;
    private TextView mTxtPalabraCandidata;
    private ImageView mImagenHorca;
    private Chronometer mCronometro;


    // Array con las imágenes de la horca.
    static final int[] HORCA = {R.drawable.ahorcado0, R.drawable.ahorcado1, R.drawable.ahorcado2,
    R.drawable.ahorcado3, R.drawable.ahorcado4, R.drawable.ahorcado5, R.drawable.ahorcado6};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crea referencias a las views

        mTeclado = findViewById(R.id.gridTeclado);
        mTxtVidas = findViewById(R.id.txtVidas);
        mTxtIntentos = findViewById(R.id.txtIntentos);
        mTxtPalabraCandidata = findViewById(R.id.textPalCandidata);
        mImagenHorca = findViewById(R.id.imagenHorca);
        mCronometro = findViewById(R.id.cronometro);


        // Asigna listeners a los botones del teclado
        creaListenersTeclado();

        // Crea partida
        nuevaPartida();


    }

    void tecladoHabilitado(boolean habilitado) {

        View tecla;
        for (int i = 0; i < mTeclado.getChildCount(); i++) {
            tecla = mTeclado.getChildAt(i);
            if (tecla.getResources().getResourceEntryName(tecla.getId()).contains("botonLetra")) {
                tecla.setEnabled(habilitado);
                int color = habilitado ? R.color.blanco : R.color.colorPrimaryDark;
                ((TextView) tecla).setTextColor(getResources().getColor(color));

                //tecla.setPadding(0,0,0,habilitado? (int) getResources().getDimension(R.dimen.boton_altura) : 0);

            }
        }

    }

    private void pintaPalabraBicolor() {

        String palabraOculta = mPartida.getPalabraOculta();
        int n = palabraOculta.length();

        SpannableStringBuilder sb = new SpannableStringBuilder(mPartida.getPalabraCandidataEspaciada());

        int i,j;
        for( i=0, j=0; i< n; i++, j+=2){
            if(sb.charAt(j) == Partida.NO_LETRA){
                sb.replace(j,j+1,palabraOculta.substring(i,i+1));
                sb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.rojo)),j,j+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }

        mTxtPalabraCandidata.setText(sb);
  }

    void nuevaPartida() {

        mPartida = new Partida();

        // Puebla todos los views con valores iniales de la partida

        mTxtPalabraCandidata.setText(mPartida.getPalabraCandidataEspaciada());
        mTxtVidas.setText(String.valueOf(mPartida.getVidas()));
        mTxtIntentos.setText(String.valueOf(mPartida.getIntentos()));

        // Habilita el teclado
        tecladoHabilitado(true);

        // Pinta horca inicial
        pintaHorca(0);

        // Pone cronómetro a cero y lo pone a contar
        mCronometro.setBase(SystemClock.elapsedRealtime() );
        mCronometro.start();


    }

    void pintaHorca(int vida) {
        mImagenHorca.setImageResource(HORCA[vida]);
    }

    void creaListenersTeclado() {


        int numTeclas = mTeclado.getChildCount();

        for (int i = 0; i < numTeclas; i++)
            mTeclado.getChildAt(i).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        {
            if (v instanceof TextView) {
                if (getResources().getResourceEntryName(v.getId()).contains("botonLetra")) {
                    // *** Si se pulsa un botón de letra

                    // Deshabilita la tecla de la letra pulsada
                    TextView letra = (TextView) v;
                    letra.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    letra.setEnabled(false);

                    // Realiza jugada con la letra pulsada
                    // Si jugada falla devuelve null y si no la palabraCandidata actualizada
                    // con nueva letra añadida.
                    String palabraCandidata = mPartida.hazJugada(letra.getText().charAt(0));

                    // Actualiza contador de intentos
                    mTxtIntentos.setText(String.valueOf(mPartida.getIntentos()));

                    if (palabraCandidata == null) {
                        // Si la letra no está en la palabra oculta
                        int vidas = mPartida.getVidas();
                        mTxtVidas.setText(String.valueOf(vidas)); // Decrementa cont. vidas
                        pintaHorca(Partida.TOTAL_VIDAS - vidas); // pinta siguente horca

                        // Si ha consumido la última vida, informa de derrota y deshabilita letras
                        if(mPartida.terminada() == Partida.PERDIDA){
                            mCronometro.stop();
                            tecladoHabilitado(false);
                            Toast.makeText(MainActivity.this,"¡Ooooh, HAS PERDIDO!",Toast.LENGTH_SHORT).show();
                            pintaPalabraBicolor();
                        }

                    } else {
                        // Si la letra si está en la palabra oculta la repinta con nueva letra
                        mTxtPalabraCandidata.setText(palabraCandidata);

                        // Si ha completado todas las letras de la palabra oculta, felicita y
                        // deshabilita letras
                        if (mPartida.terminada() == Partida.GANADA) {
                            mCronometro.stop();
                            tecladoHabilitado(false); // Deshabilita teclas de letras
                            Toast.makeText(MainActivity.this,"¡Enhorabuena, LA HAS RESUELTO!",Toast.LENGTH_SHORT).show();
                        }
                    }

                } else if (v.getId() == R.id.botonNueva) {
                    // *** Si se pulsa boton Nueva Partida
                    nuevaPartida();

                }

            }

        }
    }
}
