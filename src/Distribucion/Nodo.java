
package Distribucion;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase guarda la información de cada nodo.
 * Además tiene los metodos para acceder o modificar dicha informacion.
 * @author Tomeu Rubi y Katia Gonzalez 
 */
public class Nodo {
    
    //////////////////Estructuras internas///////////////////////////////////
    
    /**
     * Indica la relacion <b>rel</b> que un nodo puede tener con otro.
     * Estan definidas tres relaciones:
     * <b>HIJO</b>: un nodo es hijo de otro nodo.
     * <b>PADRE</b>: un nodo es padre de otro nodo.
     * <b>NOFAMILIAR</b>: un nodo no es ni hijo ni padre de otro nodo.
     */
    public enum RelacionNodo{
        HIJO, PADRE, NOFAMILIAR 
    }
    public enum TipoNodo{
        RAIZ, INTERIOR, HOJA, NODEFINIDO
    }
    
    private String nomNodo;
    private List<String> hijos;
    private List<String> padres;
    private static final String SINNOMBRE = String.valueOf(Integer.MAX_VALUE);
    private TipoNodo tipoNodo;
    private String prefijoNomHilo = "hilo_";

    public Nodo() {
        this(SINNOMBRE); //es el equivalente de null. Estrictamente no tiene nombre;
    }
    
    public Nodo(String nom){
        this.nomNodo = nom;
        this.hijos = new ArrayList<>(0);
        this.padres = new ArrayList<>(0);
        this.tipoNodo = TipoNodo.NODEFINIDO;
    }
    
    public String getNomNodo() {
        return nomNodo;
    }
    public int getNumNodo(){
        return Integer.valueOf(this.nomNodo);
    }

    public void setNomNodo(String nomNodo) {
        this.nomNodo = nomNodo;
    }

    public List<String> getHijos() {
        return hijos;
    }

    public void setHijos(List<String> hijos) {
        this.hijos = hijos;
    }
    
    public void addHijo(String nomHijo){
            this.hijos.add(nomHijo);
    }

    public List<String> getPadres() {
        return padres;
    }

    public void setPadres(List<String> padres) {
        this.padres = padres;
    }
    
    public void addPadre(String nomPadre){
            this.padres.add(nomPadre); 
    }
    
    public void setTipo(TipoNodo tipo){
        this.tipoNodo = tipo;
    }
    
    public boolean esPadre(){
        return !this.hijos.isEmpty();
    }
    
    public boolean esHijo(){
        return !this.padres.isEmpty();
    }
    public boolean esRaiz(){
        return this.nomNodo.equals("0");
    }
    
    public boolean existHijo(String nomHijo){
        return this.hijos.contains(nomHijo);        
    }
    public boolean existPadre(String nomPadre){
        return this.hijos.contains(nomPadre);        
    }
    
    public RelacionNodo getRelacionNodo(String nomNodo){
        
        RelacionNodo parentesco = RelacionNodo.NOFAMILIAR;
        
        if (!this.hijos.isEmpty() && this.hijos.contains(nomNodo)) parentesco = RelacionNodo.HIJO;
        else{
            if(!this.padres.isEmpty() && this.padres.contains(nomNodo)) parentesco = RelacionNodo.PADRE;
        }
        return parentesco;
    }

}
