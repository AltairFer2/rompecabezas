
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Queue;
import java.util.List;

public class RompecabezasGUI extends JFrame {
    private JTable puzzleTable; // Usar JTable para mostrar la matriz
    private int[][] estadoInicial;
    private int[][] estadoFinal;
    private List<String> iteraciones; // Lista para almacenar las iteraciones
    private int currentIterationIndex; // Índice de la iteración actual
    private DefaultTableModel tableModel;
    private JButton startButton; // Botón de inicio de búsqueda
    private JButton reiniciarButton; // Botón de reinicio del puzzle

    public RompecabezasGUI() {
        setTitle("Resolución de Rompecabezas 8-Puzzle");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Crear un panel para colocar componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Crear un JTable para mostrar la matriz
        tableModel = new DefaultTableModel(3, 3);
        puzzleTable = new JTable(tableModel);
        puzzleTable.setCellSelectionEnabled(false);
        puzzleTable.setRowHeight(50);
        puzzleTable.setFont(new Font("Arial", Font.PLAIN, 24));

        // Agregar el JTable a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(puzzleTable);
        reiniciarButton = createReiniciarButton();

        // Agregar componentes al panel
        panel.add(scrollPane, BorderLayout.CENTER);

        // Inicializar el botón de inicio aquí
        startButton = new JButton("Iniciar Búsqueda");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Deshabilitar el botón mientras se ejecuta la búsqueda
                startButton.setEnabled(false);

                // Ejecutar la búsqueda en un hilo separado
                SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                    @Override
                    protected Void doInBackground() {
                        // Limpiar iteraciones anteriores
                        iteraciones.clear();
                        currentIterationIndex = 0;
                        // Ejemplo: Búsqueda ciega en anchura
                        busquedaCiegaAnchura(estadoInicial, estadoFinal);
                        reiniciarButton.setEnabled(true);
                        return null;
                    }
                };

                worker.execute();
            }
        });

        // Continuar con la adición de componentes
        panel.add(startButton, BorderLayout.LINE_END);
        panel.add(reiniciarButton, BorderLayout.PAGE_END);

        // Agregar componentes al panel
        panel.add(scrollPane, BorderLayout.CENTER);

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

        // Inicializar la lista de iteraciones
        iteraciones = new ArrayList<>();

        // Inicializar la tabla con el estado inicial
        updateTableModel(estadoInicial);
    }

    public void busquedaCiegaAnchura(int[][] estadoInicial, int[][] estadoFinal) {
        boolean encontro = false;
        RompecabezasEstado actual = new RompecabezasEstado(estadoInicial);
        RompecabezasEstado hijo = null;
        Queue<RompecabezasEstado> cerrado = new LinkedList<>();
        Set<RompecabezasEstado> abierto = new HashSet<>();
        Stack<RompecabezasEstado> solucion = null;
        cerrado.offer(actual);

        while (!cerrado.isEmpty() && (!encontro)) {
            actual = cerrado.poll();
            if (!abierto.contains(actual)) {
                abierto.add(actual);

                for (int i = 0; (i < 4) && (!encontro); i++) {
                    hijo = movimientos(actual, i);
                    if (hijo != null) {
                        cerrado.offer(hijo);
                        encontro = comparar(estadoFinal, hijo.estado);
                    }
                }

            }
        }

        if (encontro) {
            solucion = new Stack<>();
            while (hijo != null) {
                solucion.push(hijo);
                hijo = hijo.padre;
            }
            while (!solucion.isEmpty()) {
                actual = solucion.pop();// sacar elemento de la pila
                // Agregar la iteración formateada a la lista
                iteraciones.add(imprimirMatriz(actual.estado));
            }

            // Usar un temporizador para mostrar las iteraciones con pausas
            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentIterationIndex < iteraciones.size()) {
                        mostrarIteracion(iteraciones.get(currentIterationIndex));
                        currentIterationIndex++;
                    } else {
                        // Cuando se muestran todas las iteraciones, detener el temporizador
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            timer.start();
        } else {
            // Si no se encontró una solución
            mostrarIteracion("No existe un camino para llegar del estado inicial al final");
        }
    }

    // Método para crear y configurar el botón de reinicio
    private JButton createReiniciarButton() {
        JButton button = new JButton("Reiniciar Puzzle");
        button.setEnabled(false); // Desactivar el botón al inicio

        // Agregar ActionListener para reiniciar el puzzle
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Restablecer la tabla con el estado inicial
                updateTableModel(estadoInicial);

                startButton.setEnabled(true);

                // Desactivar el botón de reinicio
                reiniciarButton.setEnabled(false);
            }
        });

        return button;
    }

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
        int[][] nuevaM = clonarMatriz(estado.estado);
        nuevaM[estado.filaBlanca][estado.columnaBlanca] = nuevaM[fila][columna];
        nuevaM[fila][columna] = 0;
        RompecabezasEstado hijo = new RompecabezasEstado(nuevaM, fila, columna, estado);
        return hijo;
    }

    // para clonar una matriz
    public int[][] clonarMatriz(int[][] matriz) {
        int[][] nuevaMatriz = new int[matriz.length][matriz[0].length];
        for (int fila = 0; fila < matriz.length; fila++) {
            for (int columna = 0; columna < matriz[fila].length; columna++) {
                nuevaMatriz[fila][columna] = matriz[fila][columna];
            }
        }
        return nuevaMatriz;
    }

    public boolean comparar(int[][] meta, int[][] hijo) {
        for (int fila = 0; fila < meta.length; fila++) {
            for (int columna = 0; columna < meta[fila].length; columna++) {
                if (meta[fila][columna] != hijo[fila][columna]) {
                    return false;
                }
            }
        }
        return true;
    }

    public String imprimirMatriz(int[][] matriz) {
        StringBuilder sb = new StringBuilder();
        for (int fila = 0; fila < matriz.length; fila++) {
            for (int columna = 0; columna < matriz[fila].length; columna++) {
                sb.append(matriz[fila][columna]).append("  ");
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public void mostrarIteracion(String iteracion) {
        // Limpiar la tabla antes de mostrar la nueva iteración
        clearTableModel();
        String[] filas = iteracion.split("\n");
        for (int i = 0; i < filas.length; i++) {
            String[] numeros = filas[i].trim().split("\\s+");
            for (int j = 0; j < numeros.length; j++) {
                tableModel.setValueAt(Integer.parseInt(numeros[j]), i, j);
            }
        }
    }

    public void clearTableModel() {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                tableModel.setValueAt("", row, col);
            }
        }
    }

    public void updateTableModel(int[][] matriz) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                tableModel.setValueAt(matriz[row][col], row, col);
            }
        }
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

class RompecabezasEstado {
    public int[][] estado;
    public int columnaBlanca;
    public int filaBlanca;
    public RompecabezasEstado padre;

    public RompecabezasEstado(int[][] estado) {
        this.estado = estado;
        encontrarEspacioBlanco();
    }

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
