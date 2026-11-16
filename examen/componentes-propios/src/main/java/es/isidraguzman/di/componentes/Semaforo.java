package es.isidraguzman.di.componentes;

import java.awt.Color;           // Colores
import java.awt.Dimension;       // Un ancho y un alto juntos
import java.awt.Graphics;        // El "pincel" que Swing nos pasa para dibujar
import java.awt.Graphics2D;      // Versión moderna del pincel, con suavizado
import java.awt.RenderingHints;  // Opciones de calidad del dibujo

import javax.swing.JPanel;       // Base del componente: un panel vacío sobre el que pintamos

/**
 * Componente propio dibujado a mano: un semáforo con tres luces.
 *
 * Forma de crearlo: heredar de JPanel y sobrescribir paintComponent. Se elige
 * cuando ningún componente de Swing se parece a lo que queremos y hay que
 * dibujarlo desde cero.
 *
 * También es un JavaBean: constructor público sin parámetros, propiedad
 * "estado" con getter y setter, serializable (lo hereda) y con eventos de cambio.
 */
public class Semaforo extends JPanel {

    // Constantes públicas para no usar "números mágicos" al llamar a setEstado
    public static final int ROJO = 0;
    public static final int AMBAR = 1;
    public static final int VERDE = 2;

    private int estado = ROJO;   // PROPIEDAD: qué luz está encendida. Empieza en rojo

    /** Constructor sin parámetros, obligatorio para poder arrastrarlo desde la paleta. */
    public Semaforo() {
        setPreferredSize(new Dimension(70, 190)); // Tamaño que el componente "pide" al layout que lo coloque
        setOpaque(false);                         // Fondo transparente: solo se ve lo que dibujamos
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        if (estado < ROJO || estado > VERDE) {    // Validación: solo existen los estados 0, 1 y 2
            throw new IllegalArgumentException("Estado no válido: " + estado);
        }
        int anterior = this.estado;               // Valor viejo, para avisar del cambio
        this.estado = estado;                     // Valor nuevo
        firePropertyChange("estado", anterior, estado); // Avisa a los oyentes (propiedad ligada)
        repaint();                                // Pide a Swing que vuelva a dibujar el componente. Sin esto no se vería el cambio
    }

    /** MÉTODO propio: pasa a la luz siguiente (rojo, verde, ámbar y vuelta a rojo). */
    public void siguiente() {
        switch (estado) {
            case ROJO -> setEstado(VERDE);        // Usa el setter, no el atributo, para que avise y repinte
            case VERDE -> setEstado(AMBAR);
            default -> setEstado(ROJO);
        }
    }

    /**
     * Swing llama a este método cada vez que hay que dibujar el componente
     * (al mostrarse, al redimensionar la ventana, tras un repaint()…).
     * Nunca se llama a mano: se pide con repaint().
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);                  // Primero deja que JPanel haga su parte (limpiar el fondo)

        Graphics2D g2 = (Graphics2D) g.create();  // Copia del pincel: lo que cambiemos no afecta a otros componentes
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   // Suavizado de bordes,
                RenderingHints.VALUE_ANTIALIAS_ON);            // para que los círculos no salgan con "dientes"

        int ancho = getWidth();                   // Tamaño REAL del componente ahora mismo (puede no ser el preferido)
        int alto = getHeight();

        g2.setColor(Color.DARK_GRAY);                           // Color de la carcasa
        g2.fillRoundRect(0, 0, ancho, alto, 20, 20);            // Rectángulo con esquinas redondeadas que ocupa todo

        int diametro = Math.min(ancho - 20, (alto - 40) / 3);   // Las luces se adaptan al tamaño: caben 3 con márgenes
        int x = (ancho - diametro) / 2;                         // Centradas en horizontal
        int hueco = (alto - 3 * diametro) / 4;                  // Espacio vertical repartido por igual (4 huecos)

        // Cada luz se pinta con su color si está encendida y en gris si está apagada
        pintarLuz(g2, x, hueco, diametro, estado == ROJO ? Color.RED : Color.GRAY);
        pintarLuz(g2, x, 2 * hueco + diametro, diametro, estado == AMBAR ? Color.ORANGE : Color.GRAY);
        pintarLuz(g2, x, 3 * hueco + 2 * diametro, diametro, estado == VERDE ? Color.GREEN : Color.GRAY);

        g2.dispose();                             // Libera la copia del pincel
    }

    /** Dibuja un círculo relleno. Método auxiliar para no repetir dos líneas tres veces. */
    private void pintarLuz(Graphics2D g2, int x, int y, int diametro, Color color) {
        g2.setColor(color);                       // Elige el color
        g2.fillOval(x, y, diametro, diametro);    // Óvalo con ancho = alto, es decir, un círculo
    }
}
