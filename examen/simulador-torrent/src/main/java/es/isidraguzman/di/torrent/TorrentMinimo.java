package es.isidraguzman.di.torrent;

import java.awt.GridLayout;        // Layout en rejilla de celdas iguales

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;   // Barra de progreso
import javax.swing.SwingUtilities;
import javax.swing.Timer;          // OJO: el Timer de javax.swing, no el de java.util

/**
 * Versión MÍNIMA de una descarga simulada: una barra, un botón de
 * iniciar/pausar y otro de reiniciar. Es lo mínimo que hay que saber escribir
 * de memoria: JProgressBar + javax.swing.Timer.
 */
public class TorrentMinimo extends JFrame {

    private final JProgressBar barra = new JProgressBar(0, 100);      // Barra de 0 a 100 (porcentaje)
    private final JButton botonIniciar = new JButton("Iniciar");
    private final JLabel estado = new JLabel("Detenido");
    private final Timer reloj;                                        // Dispara un evento cada cierto tiempo

    public TorrentMinimo() {
        super("Descarga simulada");
        barra.setStringPainted(true);                // Escribe el porcentaje ("37%") dentro de la barra

        // Timer(retardo en ms, oyente): cada 100 ms ejecuta avanzar() en el hilo de Swing (EDT),
        // por eso desde ahí se puede tocar la interfaz sin problemas de hilos.
        reloj = new Timer(100, e -> avanzar());

        botonIniciar.addActionListener(e -> iniciarOPausar());       // Un mismo botón inicia y pausa
        JButton botonReiniciar = new JButton("Reiniciar");
        botonReiniciar.addActionListener(e -> reiniciar());

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));       // 4 filas, 1 columna, 5 px de separación
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen interior
        panel.add(new JLabel("ubuntu-24.04.iso"));                   // Nombre del fichero que "se descarga"
        panel.add(barra);
        panel.add(estado);
        JPanel botones = new JPanel();                               // FlowLayout por defecto: botones en fila
        botones.add(botonIniciar);
        botones.add(botonReiniciar);
        panel.add(botones);

        setContentPane(panel);                       // El panel ocupa todo el interior de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    /** Lo que pasa en cada "tic" del reloj: la barra sube un punto. */
    private void avanzar() {
        barra.setValue(barra.getValue() + 1);        // Sube el progreso en 1
        if (barra.getValue() >= barra.getMaximum()) { // Si ha llegado al 100…
            reloj.stop();                            // …se para el reloj: ya no hay nada que simular
            estado.setText("Completado");
            botonIniciar.setEnabled(false);          // Se desactiva el botón: no tiene sentido iniciar algo terminado
        }
    }

    private void iniciarOPausar() {
        if (reloj.isRunning()) {                     // Si está descargando, se pausa
            reloj.stop();
            botonIniciar.setText("Reanudar");        // El texto del botón dice siempre lo que hará al pulsarlo
            estado.setText("Pausado");
        } else {                                     // Si está parado, se arranca
            reloj.start();
            botonIniciar.setText("Pausar");
            estado.setText("Descargando…");
        }
    }

    private void reiniciar() {
        reloj.stop();                                // Para el reloj
        barra.setValue(0);                           // Barra a cero
        botonIniciar.setText("Iniciar");             // Todo vuelve al estado inicial
        botonIniciar.setEnabled(true);
        estado.setText("Detenido");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TorrentMinimo().setVisible(true)); // Arranque en el hilo de Swing
    }
}
