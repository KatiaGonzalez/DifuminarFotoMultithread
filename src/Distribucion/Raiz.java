
package Distribucion;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Esta clase crea e inicia la ejecución de todos los hilos (uno para cada
 * nodo). Lo hace leyendo primero el archivo con extensión .dot para saber
 * cuantos nodos existen.
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class Raiz extends Nodo{
    
    private List<Proceso> procesos; 
    private List<Nodo> familia;
    
    public Raiz(){
        super("0");
        super.setTipo(TipoNodo.RAIZ);
        familia = new ArrayList(0);
        this.procesos = new ArrayList<>(1);
    }
    
    public void inicializarNodos(GraphDot leerGraphDot) throws InterruptedException, IOException{

        List<String[]> relacion;
        Nodo nodo;
        leerGraphDot.leerFichero();
        Proceso procesoRaiz;
        relacion = leerGraphDot.getRelacionList();
        
        for (String[] relacionX : relacion) {
            nodo= this.getFamiliar(relacionX[1]);
            if(nodo==null){
                nodo = new Nodo(relacionX[1]);
                this.familia.add(nodo);
            }
            nodo.addPadre(relacionX[0]);
            if(relacionX[0].equals("0")) this.addHijo(relacionX[1]);
            else {
                nodo = this.getFamiliar(relacionX[0]);
                nodo.addHijo(relacionX[1]);
            }            
        }
        
        procesoRaiz = new Proceso(this.getNomNodo(),this);
        this.procesos.add(procesoRaiz);
        for (Nodo nodoFamilia : this.familia) {
            Proceso proceso = new Proceso(nodoFamilia.getNomNodo(),nodoFamilia);
            this.procesos.add(proceso);           
        }
        
        for (Proceso proceso : this.procesos) {
            proceso.start();
        }
        
        //Esta linea no se tendría que alcanzar si no se produce ninguna excepcion
        for (Proceso proceso : this.procesos) {
            proceso.join();           
        }
        
        
        //this.Print();
    }
    
    public Nodo getFamiliar (String nomNodo){
        for (Nodo nodo : this.familia) {
            if(nomNodo.equals(nodo.getNomNodo())) return nodo;
        }
        return null;
    }
    
    public void Print(){
        System.out.println("Soy el nodo: 0");
        System.out.print("Mis hijos son: ");
        for (String nomHijo : this.getHijos()) {
            System.out.print(nomHijo + ",");
        }
        System.out.println("\n");
        for(Nodo nodo : this.familia){
            System.out.println("Soy el nodo: " + nodo.getNomNodo());
            if(nodo.esHijo()){
                System.out.println("Mis padres son: ");
                for (String nomPadre : nodo.getPadres()) {
                    System.out.print(nomPadre + ",");
                }
            }else System.out.println("No tengo padres.");
            System.out.println();
            if(nodo.esPadre()){
                System.out.println("Mis hijos son: ");
                for (String nomHijo : nodo.getHijos()) {
                    System.out.print(nomHijo + ",");
                }
            }else System.out.println("No tengo hijos");
            System.out.println("\n");
        }
    }    
}
