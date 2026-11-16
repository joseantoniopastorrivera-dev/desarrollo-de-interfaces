package es.isidraguzman.di.torrent;

import java.awt.BorderLayout;      // Layout de cinco zonas: norte, sur, este, oeste y centro
import java.awt.Component;         // Tipo base de cualquier componente gráfico
import java.awt.Dimension;         // Un ancho y un alto juntos
import java.awt.FlowLayout;        // Layout que coloca los componentes en fila
import java.util.Random;           // Generador de números aleatorios para simular la velocidad

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;    // Cuadros de diálogo ya hechos: avisos y confirmaciones
import javax.swing.JPanel;
import javax.swing.JProgressBar;   // Barra de progreso
import javax.swing.JScrollPane;    // Panel con barras de desplazamiento; la tabla va dentro
import javax.swing.JSpinner;       // Campo numérico con flechas para subir y bajar
import javax.swing.JTable;         // Tabla
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;      // Reglas de selección de filas
import javax.swing.SpinnerNumberModel;      // Modelo del spinner: valor, mínimo, máximo y paso
import javax.swing.SwingUtilities;
import javax.swing.Timer;                   // OJO: el Timer de javax.swing, no el de java.util
import javax.swing.table.DefaultTableModel; // Modelo de datos de la tabla
import javax.swing.table.TableCellRenderer; // Interfaz para decidir cómo se dibuja una celda

/**
 * Simulador de cliente torrent. No descarga nada: un Timer hace avanzar
 * las barras de progreso para que PAREZCA que descarga.
 *
 * Estructura (BorderLayout):
 *   NORTE  -> formulario para añadir una descarga (nombre, tamaño, botón)
 *   CENTRO -> tabla con las descargas, dentro de un JScrollPane
 *   SUR    -> botones Pausar/Reanudar y Eliminar, y un resumen del estado
 */
public class SimuladorTorrent extends JFrame {

    // Posición de cada columna. Con constantes el código se lee mejor que con 0, 1, 2…
    private static final int COL_NOMBRE = 0;
    private static final int COL_TAMANO = 1;
    private static final int COL_DESCARGADO = 2;
    private static final int COL_PROGRESO = 3;
    private static final int COL_VELOCIDAD = 4;
    private static final int COL_ESTADO = 5;

    // Los tres estados posibles de una descarga. Constantes para no equivocarse al escribirlos
    private static final String DESCARGANDO = "Descargando";
    private static final String PAUSADO = "Pausado";
    private static final String COMPLETADO = "Completado";

    private static final int INTERVALO_MS = 200;   // Cada cuánto "late" el reloj de la simulación

    // Componentes que se usan después del constructor: por eso son atributos
    private final DefaultTableModel modelo;        // Los DATOS de la tabla (filas y columnas)
    private final JTable tabla;                    // La VISTA de esos datos
    private final JTextField campoNombre;          // Nombre del fichero a añadir
    private final JSpinner spinnerTamano;          // Tamaño en MB del fichero a añadir
    private final JButton botonPausar;
    private final JButton botonEliminar;
    private final JLabel resumen;                  // Texto de abajo: cuántas descargas hay y cómo van
    private final Random azar = new Random();      // Para que la velocidad varíe en cada tic

    public SimuladorTorrent() {
        super("Simulador de torrent");             // Título de la ventana

        // ---------------- MODELO Y TABLA ----------------

        String[] columnas = {"Nombre", "Tamaño (MB)", "Descargado (MB)", "Progreso", "Velocidad", "Estado"};

        // Clase anónima que hereda de DefaultTableModel para cambiar dos comportamientos
        modelo = new DefaultTableModel(columnas, 0) {       // 0 = la tabla empieza sin filas
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;                               // Ninguna celda se puede editar con doble clic
            }

            @Override
            public Class<?> getColumnClass(int columna) {   // Dice a la tabla qué tipo de dato hay en cada columna
                return switch (columna) {
                    case COL_TAMANO, COL_PROGRESO -> Integer.class; // Números enteros: se alinean a la derecha
                    case COL_DESCARGADO -> Double.class;            // Número con decimales
                    default -> String.class;                        // El resto es texto
                };
            }
        };

        tabla = new JTable(modelo);                                         // La tabla muestra lo que haya en el modelo
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);        // Solo se puede seleccionar una fila a la vez
        tabla.setRowHeight(24);                                             // Filas un poco más altas para que quepa la barra
        tabla.getColumnModel().getColumn(COL_NOMBRE).setPreferredWidth(220); // La columna del nombre, más ancha
        tabla.getColumnModel().getColumn(COL_PROGRESO)
                .setCellRenderer(new RenderizadorBarra());                  // La columna Progreso se dibuja como barra
        tabla.getSelectionModel()
                .addListSelectionListener(e -> actualizarBotones());        // Al cambiar la fila seleccionada, revisa los botones

        JScrollPane scroll = new JScrollPane(tabla);        // Sin JScrollPane la tabla no muestra cabeceras ni barra de scroll
        scroll.setPreferredSize(new Dimension(720, 220));   // Tamaño inicial de la zona de la tabla

        // ---------------- NORTE: formulario para añadir ----------------

        campoNombre = new JTextField(20);                                   // Caja de texto de unas 20 columnas
        spinnerTamano = new JSpinner(new SpinnerNumberModel(500, 10, 5000, 50)); // Valor 500, mínimo 10, máximo 5000, paso 50
        JButton botonAnadir = new JButton("Añadir descarga");

        botonAnadir.addActionListener(e -> anadirDescarga());   // Clic en el botón…
        campoNombre.addActionListener(e -> anadirDescarga());   // …o Intro en la caja de texto: las dos cosas añaden

        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT));    // Fila alineada a la izquierda
        panelNorte.add(new JLabel("Nombre:"));
        panelNorte.add(campoNombre);
        panelNorte.add(new JLabel("Tamaño (MB):"));
        panelNorte.add(spinnerTamano);
        panelNorte.add(botonAnadir);

        // ---------------- SUR: acciones y resumen ----------------

        botonPausar = new JButton("Pausar");
        botonEliminar = new JButton("Eliminar");
        botonPausar.addActionListener(e -> pausarOReanudar());
        botonEliminar.addActionListener(e -> eliminarDescarga());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.add(botonPausar);
        panelBotones.add(botonEliminar);

        resumen = new JLabel();                                             // El texto se rellena en actualizarResumen()
        resumen.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));    // 10 px de margen a la derecha

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelBotones, BorderLayout.WEST);      // Botones a la izquierda
        panelSur.add(resumen, BorderLayout.EAST);           // Resumen a la derecha

        // ---------------- MONTAJE DE LA VENTANA ----------------

        setLayout(new BorderLayout(5, 5));                  // 5 px de separación entre zonas
        add(panelNorte, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);                   // El centro se lleva el espacio sobrante al agrandar la ventana
        add(panelSur, BorderLayout.SOUTH);

        // Dos descargas de ejemplo para que la ventana no arranque vacía
        modelo.addRow(new Object[]{"ubuntu-24.04-desktop.iso", 1200, 0.0, 0, "-", DESCARGANDO});
        modelo.addRow(new Object[]{"apuntes-interfaces.zip", 150, 0.0, 0, "-", DESCARGANDO});

        actualizarBotones();                                // Estado inicial de los botones (no hay fila seleccionada)
        actualizarResumen();

        // ---------------- EL RELOJ DE LA SIMULACIÓN ----------------
        // javax.swing.Timer ejecuta su oyente en el hilo de eventos de Swing (EDT),
        // así que simular() puede tocar la tabla sin problemas de hilos.

        Timer reloj = new Timer(INTERVALO_MS, e -> simular());
        reloj.start();                                      // Empieza a latir en cuanto se crea la ventana

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     // Al cerrar la ventana termina el programa
        pack();                                             // Tamaño justo para que quepa todo
        setLocationRelativeTo(null);                        // Centrada en la pantalla
    }

    // ------------------------------------------------------------------
    // ACCIONES DEL USUARIO
    // ------------------------------------------------------------------

    /** Añade una fila nueva con los datos del formulario. */
    private void anadirDescarga() {
        String nombre = campoNombre.getText().trim();       // Lo escrito, sin espacios sobrantes

        if (nombre.isEmpty()) {                             // Validación: el nombre es obligatorio
            JOptionPane.showMessageDialog(this,             // this = el diálogo sale centrado sobre esta ventana
                    "Escribe el nombre del fichero.",       // Mensaje concreto y en positivo: dice qué hacer
                    "Falta el nombre",                      // Título del diálogo
                    JOptionPane.WARNING_MESSAGE);           // Icono de aviso
            campoNombre.requestFocusInWindow();             // Deja el cursor donde está el problema
            return;                                         // No se añade nada
        }

        int tamano = (Integer) spinnerTamano.getValue();    // El spinner devuelve Object: se convierte a Integer

        // Una fila es un array de Object, con un valor por columna y en el mismo orden
        modelo.addRow(new Object[]{nombre, tamano, 0.0, 0, "-", DESCARGANDO});

        campoNombre.setText("");                            // Limpia el formulario para la siguiente
        campoNombre.requestFocusInWindow();
        actualizarResumen();
    }

    /** Pausa la descarga seleccionada, o la reanuda si ya estaba pausada. */
    private void pausarOReanudar() {
        int fila = tabla.getSelectedRow();                  // Índice de la fila seleccionada; -1 si no hay ninguna
        if (fila == -1) {
            return;                                         // Sin selección no hay nada que hacer
        }

        String estado = (String) modelo.getValueAt(fila, COL_ESTADO);  // Estado actual de esa descarga

        if (estado.equals(DESCARGANDO)) {
            modelo.setValueAt(PAUSADO, fila, COL_ESTADO);   // setValueAt(valor, fila, columna): la tabla se repinta sola
            modelo.setValueAt("-", fila, COL_VELOCIDAD);    // Pausada no tiene velocidad
        } else if (estado.equals(PAUSADO)) {
            modelo.setValueAt(DESCARGANDO, fila, COL_ESTADO);
        }                                                   // Si está completada no se hace nada

        actualizarBotones();
        actualizarResumen();
    }

    /** Elimina la descarga seleccionada, pidiendo confirmación antes. */
    private void eliminarDescarga() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }

        String nombre = (String) modelo.getValueAt(fila, COL_NOMBRE);

        // Acción destructiva: se pide confirmación (heurística de prevención de errores)
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la descarga «" + nombre + "»?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {          // Solo si el usuario pulsa "Sí"
            modelo.removeRow(fila);                         // Quita la fila del modelo; la tabla se actualiza sola
            actualizarBotones();
            actualizarResumen();
        }
    }

    // ------------------------------------------------------------------
    // SIMULACIÓN
    // ------------------------------------------------------------------

    /** Se ejecuta en cada tic del reloj: hace avanzar todas las descargas activas. */
    private void simular() {
        boolean algunaTermino = false;                      // Para saber si hay que revisar los botones al final

        for (int fila = 0; fila < modelo.getRowCount(); fila++) {            // Recorre todas las filas
            if (!DESCARGANDO.equals(modelo.getValueAt(fila, COL_ESTADO))) {  // Las pausadas y completadas…
                continue;                                                    // …se saltan
            }

            int tamano = (Integer) modelo.getValueAt(fila, COL_TAMANO);          // Tamaño total en MB
            double descargado = (Double) modelo.getValueAt(fila, COL_DESCARGADO); // Lo que lleva descargado

            double velocidad = 20 + azar.nextDouble() * 60;         // Velocidad inventada: entre 20 y 80 MB/s
            descargado += velocidad * INTERVALO_MS / 1000.0;        // MB que bajan en este tic = velocidad × tiempo (en segundos)

            if (descargado >= tamano) {                             // Si ya está todo…
                descargado = tamano;                                // …no se pasa del total
                modelo.setValueAt(COMPLETADO, fila, COL_ESTADO);
                modelo.setValueAt("-", fila, COL_VELOCIDAD);
                algunaTermino = true;
            } else {
                modelo.setValueAt(String.format("%.1f MB/s", velocidad), fila, COL_VELOCIDAD); // Un decimal
            }

            double redondeado = Math.round(descargado * 10) / 10.0; // Se guarda con un decimal para que se lea bien
            modelo.setValueAt(redondeado, fila, COL_DESCARGADO);
            modelo.setValueAt((int) (descargado * 100 / tamano), fila, COL_PROGRESO); // Porcentaje entero de 0 a 100
        }

        if (algunaTermino) {                                // Solo si algo ha cambiado de estado
            actualizarBotones();
            actualizarResumen();
        }
    }

    // ------------------------------------------------------------------
    // MÉTODOS DE APOYO
    // ------------------------------------------------------------------

    /** Activa o desactiva los botones según la fila seleccionada. */
    private void actualizarBotones() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {                                   // Sin selección…
            botonPausar.setEnabled(false);                  // …los botones se desactivan: así no se puede pulsar sin sentido
            botonEliminar.setEnabled(false);
            botonPausar.setText("Pausar");
            return;
        }

        String estado = (String) modelo.getValueAt(fila, COL_ESTADO);
        botonEliminar.setEnabled(true);                             // Eliminar vale para cualquier descarga
        botonPausar.setEnabled(!estado.equals(COMPLETADO));         // Una completada ni se pausa ni se reanuda
        botonPausar.setText(estado.equals(PAUSADO) ? "Reanudar" : "Pausar"); // El botón dice lo que hará al pulsarlo
    }

    /** Escribe abajo cuántas descargas hay en cada estado. */
    private void actualizarResumen() {
        int activas = 0;
        int completadas = 0;

        for (int fila = 0; fila < modelo.getRowCount(); fila++) {   // Cuenta recorriendo la columna de estado
            Object estado = modelo.getValueAt(fila, COL_ESTADO);
            if (DESCARGANDO.equals(estado)) {
                activas++;
            } else if (COMPLETADO.equals(estado)) {
                completadas++;
            }
        }

        resumen.setText(modelo.getRowCount() + " descargas · " + activas + " activas · "
                + completadas + " completadas");
    }

    // ------------------------------------------------------------------
    // RENDERIZADOR: cómo se dibuja la columna Progreso
    // ------------------------------------------------------------------

    /**
     * Es una JProgressBar que además sabe actuar como "sello" de celda.
     * La tabla llama a getTableCellRendererComponent para cada celda de la
     * columna y dibuja lo que este método devuelve.
     */
    private static class RenderizadorBarra extends JProgressBar implements TableCellRenderer {

        RenderizadorBarra() {
            super(0, 100);                   // Barra de 0 a 100
            setStringPainted(true);          // Con el porcentaje escrito dentro
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionada, boolean conFoco, int fila, int columna) {
            setValue((Integer) valor);       // valor = lo que hay en el modelo para esa celda (el porcentaje)
            return this;                     // La misma barra se reutiliza para todas las filas
        }
    }

    public static void main(String[] args) {
        // La interfaz se crea en el hilo de eventos de Swing (EDT), como recomienda Oracle
        SwingUtilities.invokeLater(() -> new SimuladorTorrent().setVisible(true));
    }
}
