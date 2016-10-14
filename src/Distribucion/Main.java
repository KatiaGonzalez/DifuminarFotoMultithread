
package Distribucion;

import java.io.IOException;

/**
 *
 * Realiza la ejecucion del hilo principal del programa y llama al metodo 
 * inicializrNodos de la clase Raiz para comenzar la ejecucion de los hilos
 * de cada nodo.
 * El hilo principal del programa es un hilo aparte del hilo del nodo entorno.
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class Main {
    final static String nomArxivo = "graph";
    final static String extension = ".dot";

    /**
     * 
     * @param args
     * @throws InterruptedException
     * @throws IOException 
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        String pathCompleta = nomArxivo.concat(extension);
        Raiz nodoRaiz = new Raiz();
        GraphDot leerGraphDot = new GraphDot(pathCompleta);
        
        nodoRaiz.inicializarNodos(leerGraphDot);
    }
    
}
