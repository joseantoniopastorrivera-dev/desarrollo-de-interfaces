package es.isidraguzman.di.componentes;

import java.awt.event.KeyAdapter;  // Clase con los tres métodos de teclado vacíos: solo sobrescribimos el que hace falta
import java.awt.event.KeyEvent;    // El evento que se genera al pulsar una tecla

import javax.swing.JTextField;     // El componente de Swing que vamos a ampliar

/**
 * Componente propio: una caja de texto que solo deja escribir números enteros.
 *
 * Es un JavaBean visual porque cumple las cuatro reglas:
 *   1. Es una clase pública con un constructor público SIN parámetros.
 *   2. Sus propiedades son atributos privados con getter y setter públicos.
 *   3. Es serializable (lo hereda de JTextField, que ya implementa Serializable).
 *   4. Avisa de los cambios de sus propiedades con eventos (firePropertyChange).
 *
 * Forma de crearlo: HERENCIA de un componente que ya existe. Se elige cuando el
 * componente nuevo "es un" componente conocido con algo más (aquí, un JTextField
 * con filtro). Así heredamos gratis todo lo que ya sabe hacer: pintarse,
 * seleccionar texto, copiar, pegar, getText(), setText()…
 */
public class CampoNumerico extends JTextField {

    // ------------------------------------------------------------------
    // PROPIEDADES del componente. Son privadas: desde fuera solo se tocan
    // con sus getters y setters. NetBeans las muestra en la ventana
    // Properties precisamente porque encuentra esa pareja get/set.
    // ------------------------------------------------------------------

    private int maximoDigitos = 9;              // Cuántas cifras se pueden escribir como mucho (9 caben siempre en un int)
    private boolean permitirNegativos = false;  // Si se acepta un signo menos al principio

    /**
     * Constructor sin parámetros. Es OBLIGATORIO en un JavaBean: el editor visual
     * de NetBeans crea el componente con "new CampoNumerico()" cuando lo arrastras
     * desde la paleta, y no sabría qué parámetros pasarle.
     */
    public CampoNumerico() {
        super(10);                                   // Llama al constructor de JTextField: ancho para unas 10 columnas
        setHorizontalAlignment(JTextField.RIGHT);    // Los números se leen mejor alineados a la derecha

        // Oyente de teclado. KeyAdapter evita tener que escribir keyPressed y
        // keyReleased vacíos, que es lo que obligaría la interfaz KeyListener.
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evento) {  // keyTyped se dispara cuando la tecla produce un carácter
                char letra = evento.getKeyChar();    // El carácter que el usuario intenta escribir

                if (Character.isISOControl(letra)) { // Teclas de control (borrar, Intro, Tab): no son texto,
                    return;                          // se dejan pasar para que el campo siga funcionando normal
                }

                boolean esDigito = Character.isDigit(letra);              // ¿Es una cifra del 0 al 9?
                boolean hayHueco = contarDigitos() < maximoDigitos        // ¿Queda sitio para otra cifra…
                        || getSelectedText() != null;                     // …o hay texto seleccionado que se va a reemplazar?
                boolean esSignoValido = letra == '-'                      // ¿Es un signo menos…
                        && permitirNegativos                              // …y la propiedad lo permite…
                        && getCaretPosition() == 0                        // …y el cursor está al principio…
                        && !getText().contains("-");                      // …y todavía no hay otro signo?

                if (!(esDigito && hayHueco) && !esSignoValido) {          // Si no es ni cifra con hueco ni signo válido…
                    evento.consume();                                     // …"consume" el evento: la tecla no llega al campo
                }
            }
        });
    }

    // ------------------------------------------------------------------
    // GETTERS y SETTERS de las propiedades.
    // Patrón de nombres: getNombre/setNombre (isNombre si es boolean).
    // ------------------------------------------------------------------

    public int getMaximoDigitos() {
        return maximoDigitos;                        // Devuelve el valor actual de la propiedad
    }

    public void setMaximoDigitos(int maximoDigitos) {
        if (maximoDigitos < 1 || maximoDigitos > 9) {             // Validación: fuera de 1..9 no tiene sentido (o desborda un int)
            throw new IllegalArgumentException("El máximo de dígitos debe estar entre 1 y 9");
        }
        int anterior = this.maximoDigitos;           // Guarda el valor viejo para poder avisar del cambio
        this.maximoDigitos = maximoDigitos;          // Asigna el nuevo
        // Propiedad "ligada" (bound): avisa a quien esté escuchando de que ha cambiado.
        // firePropertyChange lo hereda de JComponent y no avisa si viejo y nuevo son iguales.
        firePropertyChange("maximoDigitos", anterior, maximoDigitos);
    }

    public boolean isPermitirNegativos() {           // En propiedades boolean el getter empieza por "is"
        return permitirNegativos;
    }

    public void setPermitirNegativos(boolean permitirNegativos) {
        boolean anterior = this.permitirNegativos;
        this.permitirNegativos = permitirNegativos;
        firePropertyChange("permitirNegativos", anterior, permitirNegativos);
    }

    // ------------------------------------------------------------------
    // MÉTODOS propios: lo que el componente ofrece a quien lo usa.
    // ------------------------------------------------------------------

    /**
     * Devuelve el número escrito. Quien use el componente ya no necesita
     * Integer.parseInt ni try/catch: el campo garantiza que solo hay cifras.
     */
    public int getValor() {
        String texto = getText().trim();             // Lo escrito, sin espacios sobrantes
        if (texto.isEmpty() || texto.equals("-")) {  // Vacío o solo el signo: todavía no hay número
            return 0;                                // Devolvemos 0 como valor neutro
        }
        try {
            return Integer.parseInt(texto);          // Convierte el texto en número entero
        } catch (NumberFormatException e) {          // Solo puede pasar si alguien PEGÓ texto raro o usó setText
            return 0;                                // El componente nunca lanza el error hacia fuera
        }
    }

    /** Escribe un número en el campo. */
    public void setValor(int valor) {
        setText(String.valueOf(valor));              // String.valueOf convierte el int en texto
    }

    /** Cuenta las cifras escritas, sin contar el signo. Privado: es un detalle interno. */
    private int contarDigitos() {
        return getText().replace("-", "").length();  // Quita el signo y mide lo que queda
    }
}
