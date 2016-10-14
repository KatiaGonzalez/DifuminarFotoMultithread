
package Distribucion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * Esta clase tiene los metodos necesarios para leer un archivo con extension
 * .dot. También tiene métodos para escribir en un archivo la relacion de 
 * cada nodo con su padre (respecto el spanning tree).
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class GraphDot {
    
    private final String path;
    private final File arxivo;
    private FileReader streamArchivo;
    private BufferedReader bufLect;
    List<String[]> lineas;
    private int iterador;
    private String flecha = "->";
    
    public GraphDot(String path) throws IOException{
        this.path = path;
        this.arxivo = new File(this.path);
        this.arxivo.createNewFile();
        this.iterador = 0;
        this.lineas = new ArrayList<>(0);
    }
    
    public void leerFichero() throws IOException{

            this.streamArchivo = new FileReader(this.arxivo);
            this.bufLect = new BufferedReader(this.streamArchivo);
            String linea;
            do{
                linea = this.leerLinea();
            } while(!linea.contains("->")); ////////se salta la cabecera
            
            do{
                linea = linea.replaceAll(" ", "");/////////quita todos los espacios en blanco de la linea
                if(!linea.isEmpty()){
                    String mapaLinea[] = linea.split("->"); //////////flecha
                    this.lineas.add(mapaLinea);
                }
                linea = this.leerLinea();
            }while((linea != null)&& (!linea.contains("}")));
            this.streamArchivo.close();
            this.bufLect.close();
            
    }
    
    public String[] getRelacion(int iterador){
        
        return this.lineas.get(iterador);
    }
    
    public String[] getRelacion(){
        String [] relacion = this.lineas.get(iterador);
        
        this.iterador = (this.iterador+1)%this.lineas.size();
        return relacion;
    }
    
    public List<String[]> getRelacionList(){
        return this.lineas;
    }
    private String leerLinea() throws IOException{

        return this.bufLect.readLine();
    }
    
    public void escribirRelacion(int padre, int hijo, boolean wfinal) throws IOException{
        this.escribirRelacion(String.valueOf(padre), String.valueOf(hijo),wfinal);
    }
    
    public void escribirRelacion(String padre, String hijo, boolean wfinal) throws IOException{
        String linea = "Nodo: "+hijo+this.flecha+"Padre Dijkstra: "+padre+"\n";
        FileWriter fw = new FileWriter(this.arxivo,wfinal);
        fw.write(linea);
        fw.close();
    }
}
