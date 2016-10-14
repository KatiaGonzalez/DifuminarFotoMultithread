
package Distribucion;

import java.util.Arrays;

/**
 *
 * Esta clase se encarga de crear un mensaje para usar en el beanstalkd queue.
 * Tambien tiene metodos para manipular y obtener información del mensaje.
 * Simplemente simplifica las tareas propias de crear y manipular los mensajes 
 * que se desean utilizar para el beanstalkd.
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class BeansMessage {

    private TipoMensaje tipo;
    private byte [] datos;
    private static final char separador= ';';
    
    public enum TipoMensaje{
        JOB,JOBDIF,REQUEST1,REQUEST2,TOKEN1,TOKEN2, SIGNAL, STOP
    };
    
    public BeansMessage(TipoMensaje tipo){
        this.tipo = tipo;
        this.datos = new byte[0];
    }
    public BeansMessage(byte[] msg){
        this(TipoMensaje.values()[msg[0]]);
        this.datos = Arrays.copyOfRange(msg, 2, msg.length);
    }
    
    public TipoMensaje getTipoMensaje(){
        return this.tipo;
    }
    
    public String [] getDatos(){
        return new String(this.datos).split(String.valueOf(separador));
    }
    
    public void setDatos(String... datos){
        switch(tipo){
            case JOB:     //0=fuente, 1=cantidadAsumar
            case JOBDIF:  //0=fuente, 1=path, 2=yinicio, 3=altura
            case REQUEST1:
            case REQUEST2:
                //this.datos=datos[0].concat(String.valueOf(separador)).concat(datos[1]).concat(String.valueOf(separador)).concat(datos[2]).getBytes();
                this.datos=this.codificaMsg(datos);
                break;
            case TOKEN1:
            case TOKEN2:
            case SIGNAL:
            case STOP:
                break;
        }
    }
    
    private byte[] codificaMsg(String... datos){
        String newMsg="",token = String.valueOf(this.separador);
        
        for(String dato: datos){
            newMsg= newMsg.concat(dato.concat(token));
        }
        newMsg.substring(0, newMsg.length()-2);
        return newMsg.getBytes();
    }
    
    public byte[] getMensage(){
        byte [] msg = new byte[this.datos.length+2];
        msg[0] = this.codificarTipo();
        msg[1] = separador;
        for(int i=0; i<this.datos.length;i++){
            msg[i+2] = this.datos[i];
        }

        return msg;
    }
    
    private byte codificarTipo(){
        byte tipo = (byte)this.tipo.ordinal();
        return tipo;
    }
    
    
    
}
