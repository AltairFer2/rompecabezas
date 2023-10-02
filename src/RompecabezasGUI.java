import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class RompecabezasGUI extends JFrame {
    private JTextArea outputTextArea;
    private int[][] estadoInicial;
    private int[][] estadoFinal;

    public RompecabezasGUI() {
        // Configurar la ventana
        setTitle("Resolución de Rompecabezas 8-Puzzle");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear un panel para colocar componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Crear un área de texto para mostrar la salida
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);

        // Crear un botón para iniciar la búsqueda
        JButton startButton = new JButton("Iniciar Búsqueda");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Deshabilitar el botón mientras se ejecuta la búsqueda
                startButton.setEnabled(false);

                // Ejecutar la búsqueda en un hilo separado
                SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                    @Override
                    protected Void doInBackground() {
                        // Coloca tu lógica de búsqueda aquí y publica resultados parciales
                        publish("Comenzando la búsqueda...\n");

                        // Ejemplo: Búsqueda ciega en anchura
                        busquedaCiegaAnchura(estadoInicial, estadoFinal);

                        return null;
                    }

                    @Override
                    protected void process(java.util.List<String> chunks) {
                        // Actualizar el área de texto con resultados parciales
                        for (String chunk : chunks) {
                            outputTextArea.append(chunk);
                        }
                    }

                    @Override
                    protected void done() {
                        // Habilitar el botón nuevamente cuando la búsqueda haya terminado
                        startButton.setEnabled(true);
                    }
                };

                worker.execute();
            }
        });

        // Agregar componentes al panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(startButton, BorderLayout.SOUTH);

        // Agregar el panel a la ventana
        add(panel);

        // Estado inicial y final del rompecabezas
        estadoInicial = new int[][] {
                { 4, 1, 3 },
                { 0, 8, 5 },
                { 2, 7, 6 }
        };

        estadoFinal = new int[][] {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 0 }
        };
    }

    public void busquedaCiegaAnchura(int[][] estadoInicial, int[][] estadoFinal) {
        boolean encontro = false;// variable para declarar si encontro solucion

        // se declara el nodo current o actual y se almacena el estadoInicial
        RompecabezasEstado actual = new RompecabezasEstado(estadoInicial);

        // Para almacenar nodos hijos
        RompecabezasEstado hijo = null;

        // lista closed (para nodos cerrados) -- cola
        Queue<RompecabezasEstado> cerrado = new LinkedList<>();
        // lita open (para nodos abiertos) -- hash se empleara para evitar duplicados
        // almacenando
        // los nodos abiertos aqui y reducir nodos duplicados
        Set<RompecabezasEstado> abierto = new HashSet<>();

        // pila empleada para almacenar la ruta solucion
        Stack<RompecabezasEstado> solucion = null;

        // colocal nodo inicial en cerrados
        cerrado.offer(actual);

        // mientras cerrado no este vacion o no se haya encontrado el estado final
        while (!cerrado.isEmpty() && (!encontro)) {
            actual = cerrado.poll();// sacar primer nodo de lista(cola)
            if (!abierto.contains(actual))// comprobar si el nodo actual esta en la lista de nodos abiertos
            {
                abierto.add(actual);// añadir a lista de nodos abiertos

                // iterar mientras existan movimientos y no se haya encontrado el estado final
                for (int i = 0; (i < 4) && (!encontro); i++) {
                    hijo = movimientos(actual, i);// realizar movimientos
                    if (hijo != null) {
                        cerrado.offer(hijo);// añadir nodo hijo al final de la lista(cola)
                        encontro = comparar(estadoFinal, hijo.estado);// comprobar si el nodo hijo es el estado final
                    }
                }
            }
        }

        // se encontro el estado final
        if (encontro)// si se encontro se imprime el camino
        {
            solucion = new Stack<>();// se inicializa la pila
            while (hijo != null)// mientras nodo hijo no sea nullo
            {
                solucion.push(hijo);// se mete el nodo a superficie de la pila
                hijo = hijo.padre;// el nodo se mueve al nodo padre
                // como es una pila el el estado final va a terminar al final de la pila y el
                // inicial al comienzo
            }
            // imprimir camino
            while (!solucion.isEmpty()) {// mientras la pila no este vacia
                actual = solucion.pop();// sacar elemento de la pila
                imprimirMatriz(actual.estado);// imprimir nodo o matriz
            }
        } else// si no se encontro, se e¡imprime el mensaje de salida
        {
            System.out.println("No existe un camino para llegar del estado inicial al final");
        }
    }

    // para realizar movimientos en el rompecabezas
    public RompecabezasEstado movimientos(RompecabezasEstado estado, int mov) {
        int columna = estado.columnaBlanca;
        int fila = estado.filaBlanca;
        switch (mov) {
            case 0:
                fila--;// movimiento arriba
                if (fila < 0) {
                    return null;
                }
                break;
            case 1:
                columna--;// movimiento izquierdad
                if (columna < 0) {
                    return null;
                }
                break;
            case 2:
                columna++;// movimiento derecha
                if (columna == estado.estado.length) {
                    return null;
                }
                break;
            case 3:
                fila++;// movimiento abajo
                if (fila == estado.estado.length) {
                    return null;
                }
                break;
        }
        int[][] nuevaM = replicarMatriz(estado.estado);// replicar matriz
        nuevaM[estado.filaBlanca][estado.columnaBlanca] = nuevaM[fila][columna];// movimiento de vacio a valor
        nuevaM[fila][columna] = 0;// movimiento de estado valor a vacio
        RompecabezasEstado hijo = new RompecabezasEstado(nuevaM, fila, columna, estado);// crear nodo hijo
        return hijo;
    }

    // para crear una replica de una matriz
    public int[][] replicarMatriz(int[][] matriz) {
        int[][] nuevaMatriz = new int[matriz.length][matriz[0].length];
        for (int fila = 0; fila < matriz.length; fila++) {
            for (int columna = 0; columna < matriz[fila].length; columna++) {
                nuevaMatriz[fila][columna] = matriz[fila][columna];
            }
        }
        return nuevaMatriz;
    }

    // para comparar dos matrices y saber si son iguales
    public boolean comparar(int[][] meta, int[][] hijo) {
        for (int fila = 0; fila < meta.length; fila++) {
            for (int columna = 0; columna < meta[fila].length; columna++) {
                if (meta[fila][columna] != hijo[fila][columna])// si hay una diferencia no son iguales
                {
                    return false;
                }
            }
        }
        // si no encontro diferencias son iguales
        return true;
    }

    public void imprimirMatriz(int[][] matriz) {
        for (int fila = 0; fila < matriz.length; fila++) {
            for (int columna = 0; columna < matriz[fila].length; columna++) {
                outputTextArea.append(matriz[fila][columna] + "  ");
            }
            outputTextArea.append("\n");
        }
        outputTextArea.append("\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RompecabezasGUI gui = new RompecabezasGUI();
                gui.setVisible(true);
            }
        });
    }
}

// Se crea esta clase para almacenar la matriz, y un apuntador al nodo padre
// ademas de almacenar el espacio en blanco
class RompecabezasEstado {
    public int[][] estado;
    public int columnaBlanca;
    public int filaBlanca;
    public RompecabezasEstado padre;

    // constructor empleado para nodo inicial
    public RompecabezasEstado(int[][] estado) {
        this.estado = estado;
        encontrarEspacioBlanco();// encontrar espacio blanco para nodo inicial
    }

    // constructor empleado para nodo hijo
    public RompecabezasEstado(int[][] estado, int filaBlanca, int columnaBlanca, RompecabezasEstado padre) {
        this.estado = estado;
        this.columnaBlanca = columnaBlanca;
        this.filaBlanca = filaBlanca;
        this.padre = padre;
    }

    public void encontrarEspacioBlanco() {
        boolean bandera = true;
        for (int fila = 0; fila < estado.length && bandera; fila++) {
            for (int columna = 0; columna < estado[fila].length && bandera; columna++) {
                if (estado[fila][columna] == 0) {
                    filaBlanca = fila;
                    columnaBlanca = columna;
                    bandera = false;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        RompecabezasEstado that = (RompecabezasEstado) obj;
        return Arrays.deepEquals(estado, that.estado);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(estado);
    }

    @Override
    public String toString() {
        return Arrays.deepToString(estado);
    }
}
