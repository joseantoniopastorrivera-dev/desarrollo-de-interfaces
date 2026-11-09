/*
 * Calculadora sencilla con Swing.
 *
 * Basada en el ejemplo SimpleCalc de "Introduction to Programming Using Java",
 * versión 9.0 Swing Edition, de David J. Eck (sección 6.7), con licencia
 * CC BY-NC-SA 4.0: https://math.hws.edu/javanotes-swing/
 * Esta versión traduce la interfaz al español, añade comentarios línea a
 * línea para repasar y arranca la ventana en el hilo de Swing (ver main).
 * Se distribuye con la misma licencia CC BY-NC-SA 4.0.
 */

// El paquete agrupa las clases del proyecto. La carpeta src/main/java/es/isidraguzman/di/calculadora
// tiene que llamarse exactamente igual, o el proyecto no compila.
package es.isidraguzman.di.calculadora;

// AWT (Abstract Window Toolkit) es la base gráfica de Java: colores, layouts y eventos.
import java.awt.Color;           // Colores (Color.GRAY, Color.RED…)
import java.awt.GridLayout;      // Layout que coloca los componentes en una rejilla de celdas iguales
import java.awt.event.ActionEvent;     // El "evento" que se genera al pulsar un botón
import java.awt.event.ActionListener;  // La interfaz que debe cumplir quien quiera escuchar esos eventos

// Swing son los componentes de la interfaz (ventanas, botones, cajas de texto), construidos sobre AWT.
import javax.swing.BorderFactory;  // Fábrica de bordes (aquí, un borde vacío que actúa de margen)
import javax.swing.JButton;        // Botón
import javax.swing.JFrame;         // La ventana con barra de título y botones de cerrar/minimizar
import javax.swing.JLabel;         // Etiqueta de texto que el usuario no puede editar
import javax.swing.JPanel;         // Panel: un contenedor donde se colocan otros componentes
import javax.swing.JTextField;     // Caja de texto de una línea que el usuario sí puede editar
import javax.swing.SwingConstants; // Constantes de alineación (CENTER, LEFT…)
import javax.swing.SwingUtilities; // Utilidades de Swing; aquí, invokeLater para arrancar bien

/**
 * Panel con dos cajas de texto (x e y), cuatro botones (+, -, *, /) y una
 * etiqueta donde aparece el resultado.
 *
 * Hereda de JPanel: la calculadora ES un panel que luego metemos en una ventana.
 * Implementa ActionListener: el propio panel escucha los clics de sus botones.
 */
public class Calculadora extends JPanel implements ActionListener {

    // ------------------------------------------------------------------
    // Atributos: los componentes que necesitamos leer o cambiar DESPUÉS de
    // construir la ventana (en actionPerformed). Por eso son atributos y no
    // variables locales del constructor. Los botones no hacen falta aquí
    // porque, una vez creados, ya no los volvemos a tocar.
    // ------------------------------------------------------------------

    private final JTextField campoX;   // Caja de texto para el primer número
    private final JTextField campoY;   // Caja de texto para el segundo número
    private final JLabel resultado;    // Etiqueta donde se muestra el resultado o un error

    /**
     * Punto de entrada del programa. Crea la ventana y le pone dentro la calculadora.
     */
    public static void main(String[] args) {
        // Swing no es "thread-safe": todo lo que toque la interfaz debe ejecutarse en un
        // hilo especial, el Event Dispatch Thread (EDT). invokeLater encarga ese trabajo
        // al EDT. El ejemplo original lo hace directamente en main; funciona casi siempre,
        // pero esta es la forma correcta que recomienda Oracle.
        SwingUtilities.invokeLater(() -> {           // Expresión lambda: el código que se ejecutará en el EDT
            JFrame ventana = new JFrame("Calculadora sencilla"); // Ventana con su título
            ventana.setContentPane(new Calculadora()); // El panel de la calculadora ocupa todo el interior
            ventana.pack();                            // Ajusta el tamaño de la ventana al que piden sus componentes
            ventana.setLocationRelativeTo(null);       // null = centrar la ventana en la pantalla
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Al cerrar la ventana, termina el programa
            ventana.setVisible(true);                  // Las ventanas nacen invisibles: hay que mostrarla al final
        });
    }

    /**
     * Constructor: crea y coloca todos los componentes. Se ejecuta una sola vez,
     * al hacer new Calculadora().
     */
    public Calculadora() {

        // --- Aspecto del panel principal ---

        setBackground(Color.GRAY);  // Fondo gris: se verá en los huecos entre componentes
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margen de 5 píxeles por arriba, izquierda, abajo y derecha

        // --- Cajas de texto ---

        campoX = new JTextField("0", 10);   // Texto inicial "0" y ancho para unos 10 caracteres
        campoX.setBackground(Color.WHITE);  // Fondo blanco (suele serlo ya, pero así no depende del tema del sistema)
        campoY = new JTextField("0", 10);   // Igual para el segundo número
        campoY.setBackground(Color.WHITE);

        // --- Filas "x =" y "y =" ---
        // Cada fila es un panel pequeño con su etiqueta y su caja. Un JPanel nuevo usa
        // FlowLayout por defecto: coloca los componentes uno detrás de otro, centrados.

        JPanel panelX = new JPanel();          // Panel de la fila de x
        panelX.add(new JLabel(" x = "));       // Etiqueta delante de la caja (no la guardamos: no cambia nunca)
        panelX.add(campoX);                    // Y la caja de texto a su derecha

        JPanel panelY = new JPanel();          // Lo mismo para y
        panelY.add(new JLabel(" y = "));
        panelY.add(campoY);

        // --- Fila de botones ---
        // GridLayout(1, 4): 1 fila y 4 columnas, todas del mismo tamaño. Así los
        // cuatro botones miden igual y llenan el ancho del panel.

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(1, 4));

        JButton botonSumar = new JButton("+");  // El texto del botón es también su "action command"
        botonSumar.addActionListener(this);    // this = esta calculadora escuchará sus clics
        panelBotones.add(botonSumar);          // Lo añade a la siguiente celda libre de la rejilla

        JButton botonRestar = new JButton("-");
        botonRestar.addActionListener(this);
        panelBotones.add(botonRestar);

        JButton botonMultiplicar = new JButton("*");
        botonMultiplicar.addActionListener(this);
        panelBotones.add(botonMultiplicar);

        JButton botonDividir = new JButton("/");
        botonDividir.addActionListener(this);
        panelBotones.add(botonDividir);

        // --- Etiqueta del resultado ---

        resultado = new JLabel("x + y = 0", SwingConstants.CENTER); // Texto inicial, centrado en la etiqueta
        resultado.setForeground(Color.RED);   // Color del texto
        resultado.setBackground(Color.WHITE); // Color del fondo…
        resultado.setOpaque(true);            // …que solo se pinta si la etiqueta es opaca (por defecto es transparente)

        // --- Montaje final ---
        // GridLayout(4, 1, 3, 3): 4 filas, 1 columna, y 3 píxeles de separación
        // horizontal y vertical entre celdas (ese hueco deja ver el fondo gris).

        setLayout(new GridLayout(4, 1, 3, 3));
        add(panelX);        // Fila 1
        add(panelY);        // Fila 2
        add(panelBotones);  // Fila 3
        add(resultado);     // Fila 4

    } // fin del constructor

    /**
     * Se ejecuta automáticamente cada vez que se pulsa uno de los cuatro botones,
     * porque el constructor registró este objeto con addActionListener(this).
     * Lee los dos números, hace la operación del botón pulsado y muestra el resultado.
     *
     * @param evt información del evento; de ahí sacamos qué botón se pulsó
     */
    @Override  // Indica que implementamos un método de ActionListener; si lo escribimos mal, el compilador avisa
    public void actionPerformed(ActionEvent evt) {

        double x;  // Primer número. double admite decimales (2.5) además de enteros
        double y;  // Segundo número

        // --- Leer x ---
        // getText() devuelve lo escrito en la caja como String. parseDouble lo convierte
        // a número, y si no es un número válido ("hola", "") lanza NumberFormatException.
        // Con try/catch capturamos ese error en vez de dejar que el programa falle.

        try {
            x = Double.parseDouble(campoX.getText().trim()); // trim() quita espacios sobrantes al principio y al final
        } catch (NumberFormatException e) {
            resultado.setText("x no es un número válido.");  // Avisamos en la propia etiqueta
            campoX.requestFocusInWindow(); // Pone el cursor en la caja que tiene el error, para corregirlo
            return;                        // Sale del método: sin x no se puede calcular nada
        }

        // --- Leer y (igual que x) ---

        try {
            y = Double.parseDouble(campoY.getText().trim());
        } catch (NumberFormatException e) {
            resultado.setText("y no es un número válido.");
            campoY.requestFocusInWindow();
            return;
        }

        // --- Saber qué botón se pulsó ---
        // getActionCommand() devuelve el "action command" del botón, que por defecto
        // es su texto: "+", "-", "*" o "/". Así un solo método sirve para los cuatro.

        String operacion = evt.getActionCommand();

        // switch sobre un String (válido desde Java 7). Cada case compara con equals().
        switch (operacion) {
            case "+" -> resultado.setText("x + y = " + (x + y)); // Paréntesis: primero suma, luego concatena con el texto
            case "-" -> resultado.setText("x - y = " + (x - y));
            case "*" -> resultado.setText("x * y = " + (x * y));
            case "/" -> {
                if (y == 0) {
                    // Con double, dividir entre 0 no da error: da Infinity o NaN.
                    // Lo comprobamos a mano para mostrar un mensaje claro.
                    resultado.setText("No se puede dividir entre cero.");
                } else {
                    resultado.setText("x / y = " + (x / y));
                }
            }
            default -> resultado.setText("Operación desconocida: " + operacion); // No debería pasar nunca
        }

    } // fin de actionPerformed

} // fin de la clase Calculadora
