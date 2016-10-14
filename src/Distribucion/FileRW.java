
package Distribucion;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Esta clase se encarga de escribir las cantidades contadas por cada nodo 
 * cuando realizan la tarea contar. En el archivo escribe la suma correspondiente
 * a sumar dicha cantidad parcial a la cifra contenida en el fichero.
 * 
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class FileRW {
    
    final static String R = "r";
    final static String W = "rw";
    final static int INICIO_FICHERO = 0;
    File fichero;
    RandomAccessFile raf;

    public FileRW(String path) throws IOException {
            this.fichero = new File(path);
            this.fichero.createNewFile();
    }
    
    public void borrarFichero() throws IOException{
        
            this.raf= new RandomAccessFile(this.fichero, W);
            this.raf.setLength(0);
            this.raf.close();
    }
    
    private int leer() throws IOException{
        int valor;
        this.raf= new RandomAccessFile(this.fichero, R);
        this.raf.seek(INICIO_FICHERO);
        valor = this.raf.readInt();
        return valor;
    }
    
    private int leerResultado() throws IOException{
        String suma="";
        long longitud = this.fichero.length();
        for(;longitud>0;longitud--){
            suma +=(char)this.raf.read();
        }
        if(suma == "")return 0;
        return Integer.parseInt(suma);
    }
    
    public String leerFichero() throws IOException{
        String suma="";

        this.raf= new RandomAccessFile(this.fichero, W);
        this.raf.seek(INICIO_FICHERO);
        long longitud = this.fichero.length();
        for(;longitud>0;longitud--){
            suma +=(char)this.raf.read();
        }
        return suma; 
    }
    
    private void escribir (int valor)throws  IOException{
        this.raf= new RandomAccessFile(this.fichero, W);
        this.raf.seek(INICIO_FICHERO);
        this.raf.writeInt(valor);
        this.raf.close();
    }
    
    private void escribir(String valor) throws IOException{
        this.raf.writeBytes(valor);
    }
    
    public void sumar(int valor) throws IOException {
        int total;
        this.raf = new RandomAccessFile(this.fichero, W);
        total = leerResultado();
        this.raf.seek(INICIO_FICHERO);
        escribir(String.valueOf(total+valor));
        this.raf.close();
    }
    
    
    
}
