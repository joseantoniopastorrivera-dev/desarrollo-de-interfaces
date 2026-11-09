# Calculadora sencilla con Swing

Ejemplo de referencia para repasar la Unidad 2. Está basado en `SimpleCalc`, de **Javanotes 9 Swing Edition** (capítulo 6, en [`recursos/libros/swing/javanotes-9-swing-edition.pdf`](../../recursos/libros/swing/javanotes-9-swing-edition.pdf)). Está traducido y comentado línea a línea. Todo el código está en [`Calculadora.java`](src/main/java/es/isidraguzman/di/calculadora/Calculadora.java).

```
┌──────────────────────────────┐
│        x = [ 0         ]     │  ← panelX  (FlowLayout)
│        y = [ 0         ]     │  ← panelY  (FlowLayout)
│  [ + ][ - ][ * ][ / ]        │  ← panelBotones (GridLayout 1×4)
│        x + y = 0             │  ← resultado (JLabel rojo sobre blanco)
└──────────────────────────────┘
   panel principal: GridLayout 4×1 con 3 px de separación
```

## Cómo abrirlo y ejecutarlo en NetBeans

1. *File → Open Project* y elige la carpeta `calculadora-simple` (NetBeans la reconoce por el `pom.xml`).
2. Pulsa **Run (F6)**. La primera vez Maven descarga lo que necesita y tarda un poco.
3. Prueba: `6` y `3` con cada botón, `6 / 0`, y escribe letras en x.

Desde terminal (con el JDK 17 o superior en el PATH): `mvn package` y después `java -cp target/classes es.isidraguzman.di.calculadora.Calculadora`.

## Orden para leer el código

1. **`main`**: crea la ventana (`JFrame`), mete dentro la calculadora y la muestra.
2. **Constructor**: crea los componentes y los coloca con *layouts*. Aquí está casi todo lo de diseño de interfaz.
3. **`actionPerformed`**: lo que pasa al pulsar un botón. Leer, validar, calcular y mostrar.

## Conceptos que tienes que saber explicar

| Concepto | Dónde se ve | En una frase |
|---|---|---|
| Contenedor y componentes | `JFrame`, `JPanel`, `JButton`… | Los paneles contienen componentes, y se pueden anidar paneles dentro de paneles |
| Layout manager | `GridLayout`, `FlowLayout` | Decide el tamaño y la posición de cada componente; no se colocan a mano con coordenadas |
| Evento y oyente | `addActionListener(this)` + `actionPerformed` | El botón avisa al oyente registrado cuando se pulsa |
| Action command | `evt.getActionCommand()` | Un solo método atiende a los cuatro botones distinguiéndolos por su texto |
| Validación de entrada | `try { Double.parseDouble(...) } catch (NumberFormatException e)` | Si el usuario escribe algo que no es un número, se avisa en vez de fallar |
| Hilo de Swing (EDT) | `SwingUtilities.invokeLater` | La interfaz se crea y se modifica solo desde el hilo de eventos de Swing |

## Diferencias con el original de Javanotes

- Textos de la interfaz y nombres de variables en español.
- `main` arranca la ventana con `SwingUtilities.invokeLater`, que es la forma que recomienda Oracle, y la centra con `setLocationRelativeTo(null)`.
- `trim()` al leer los números, para que `" 4 "` también valga.
- `switch` con flechas (Java 14+) en lugar de la cadena de `if/else`.

## Para hacerlo tú con el editor visual de NetBeans (lo que pide la Unidad 2)

Este ejemplo está escrito a mano. En clase usaréis el **editor visual**, que genera el código de la interfaz por ti. Los pasos equivalentes son:

1. *New Project → Java with Maven → Java Application*.
2. Botón derecho en el paquete → *New → JFrame Form* → `CalculadoraForm`.
3. Desde la **Palette**, arrastra 2 `JLabel` («x =», «y =»), 2 `JTextField`, 4 `JButton` y 1 `JLabel` para el resultado.
4. En **Properties**, cambia el `text` de cada componente. Con botón derecho → *Change Variable Name* ponles nombres claros: `campoX`, `botonSumar`…
5. Doble clic en cada botón: NetBeans crea `botonSumarActionPerformed(...)`. Ahí va la lógica de `actionPerformed` de este ejemplo: leer con `parseDouble` dentro de `try/catch`, calcular y hacer `resultado.setText(...)`.
6. El código en gris (`initComponents()`) lo genera el editor y **no se puede editar a mano**. Para cambiarlo, usa la vista *Design* y las *Properties*.

Diferencia clave: con el editor visual cada botón tiene **su propio método** de evento. Aquí hay **un único** `actionPerformed` que mira `getActionCommand()`. Las dos formas son correctas; conviene saber explicar ambas.

## Licencia

Adaptación de `SimpleCalc` de David J. Eck (<https://math.hws.edu/javanotes-swing/>), con licencia CC BY-NC-SA 4.0. Esta versión mantiene la misma licencia.
