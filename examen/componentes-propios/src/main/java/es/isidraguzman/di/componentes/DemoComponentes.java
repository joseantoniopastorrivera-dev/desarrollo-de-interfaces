package es.isidraguzman.di.componentes;

import java.awt.BorderLayout;      // Layout de cinco zonas: norte, sur, este, oeste y centro
import java.awt.FlowLayout;        // Layout que coloca los componentes en fila

import javax.swing.BorderFactory;  // Fábrica de bordes
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Ventana de prueba: usa los dos componentes propios igual que se usaría
 * un JButton o un JTextField. Demuestra que son reutilizables.
 */
public class DemoComponentes extends JFrame {

    public DemoComponentes() {
        super("Demo de componentes propios");                 // Título de la ventana

        // --- Zona de arriba: el CampoNumerico ---

        CampoNumerico campoEdad = new CampoNumerico();        // Se crea con el constructor sin parámetros
        campoEdad.setMaximoDigitos(3);                        // Se configura con sus propiedades: como mucho 3 cifras
        JButton botonLeer = new JButton("Leer valor");
        JLabel etiquetaValor = new JLabel("Valor: -");

        // Al pulsar el botón se usa el método propio getValor(): devuelve un int, sin parseInt ni try/catch
        botonLeer.addActionListener(e -> etiquetaValor.setText("Valor: " + campoEdad.getValor()));

        JPanel panelCampo = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Fila alineada a la izquierda
        panelCampo.setBorder(BorderFactory.createTitledBorder("CampoNumerico (solo cifras, máximo 3)"));
        panelCampo.add(new JLabel("Edad:"));
        panelCampo.add(campoEdad);
        panelCampo.add(botonLeer);
        panelCampo.add(etiquetaValor);

        // --- Zona central: el Semaforo ---

        Semaforo semaforo = new Semaforo();
        JButton botonCambiar = new JButton("Cambiar luz");
        JLabel etiquetaEstado = new JLabel("Estado: 0");

        botonCambiar.addActionListener(e -> semaforo.siguiente()); // El botón solo le pide al componente que cambie

        // Oyente del EVENTO del componente: se entera de que la propiedad "estado" ha cambiado
        // y actualiza la etiqueta. El semáforo no sabe que esta etiqueta existe: eso es bajo acoplamiento.
        semaforo.addPropertyChangeListener("estado",
                evento -> etiquetaEstado.setText("Estado: " + evento.getNewValue()));

        JPanel panelSemaforo = new JPanel();                  // FlowLayout por defecto, centrado
        panelSemaforo.setBorder(BorderFactory.createTitledBorder("Semaforo (dibujado con paintComponent)"));
        panelSemaforo.add(semaforo);
        panelSemaforo.add(botonCambiar);
        panelSemaforo.add(etiquetaEstado);

        // --- Montaje de la ventana ---

        setLayout(new BorderLayout(5, 5));                    // 5 píxeles de separación entre zonas
        add(panelCampo, BorderLayout.NORTH);                  // El campo, arriba
        add(panelSemaforo, BorderLayout.CENTER);              // El semáforo, en el centro (se lleva el espacio sobrante)

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       // Al cerrar la ventana termina el programa
        pack();                                               // Tamaño justo para que quepa todo
        setLocationRelativeTo(null);                          // Centrada en la pantalla
    }

    public static void main(String[] args) {
        // La interfaz se crea en el hilo de eventos de Swing (EDT), como recomienda Oracle
        SwingUtilities.invokeLater(() -> new DemoComponentes().setVisible(true));
    }
}
