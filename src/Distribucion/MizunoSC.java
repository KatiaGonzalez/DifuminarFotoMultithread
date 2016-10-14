
package Distribucion;

import com.surftools.BeanstalkClientImpl.ClientImpl;
import java.util.concurrent.Semaphore;

/**
 *
 * Esta clase tiene los métodos correspondientes al algoritmo de 
 * Nielsen Mizuno para la entrada y salida de seccion crítica.
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class MizunoSC {
    private volatile boolean holding;//synchronized
    private volatile int parents;//synchronized
    private volatile int  deferred;//synchronized
    Semaphore sNielsen, waitToken;
    String nomNodo;
    private volatile int numNodo;
    ClientImpl client;
    BeansMessage.TipoMensaje REQUEST;
    BeansMessage.TipoMensaje TOKEN;
    
    public MizunoSC(String nomNodo, int parent, ClientImpl client, boolean tipo) throws InterruptedException{
        if(tipo){
            this.REQUEST = BeansMessage.TipoMensaje.REQUEST1;
            this.TOKEN = BeansMessage.TipoMensaje.TOKEN1;
        }else{
            this.REQUEST = BeansMessage.TipoMensaje.REQUEST2;
            this.TOKEN = BeansMessage.TipoMensaje.TOKEN2;
        }
        this.client = client;
        waitToken = new Semaphore(1);
        sNielsen = new Semaphore(1);
        this.deferred = Integer.parseInt(nomNodo);
        this.numNodo = deferred;
        this.nomNodo = nomNodo;
        waitToken.acquire();
        if(deferred == 0) {
            this.parents = 0;
            this.holding = true;
        }else{
            this.parents = parent;
            this.holding = false;
        }
    }
    
    public void entrada_SC() throws InterruptedException{
            this.sNielsen.acquire();
            if (!holding) {
                BeansMessage msg = new BeansMessage(this.REQUEST);
                msg.setDatos(nomNodo,nomNodo);
                this.send(this.parents,msg);
                this.parents = numNodo;
                this.sNielsen.release();
                this.getToken();
                this.sNielsen.acquire();
            }
            this.holding = false;
            this.sNielsen.release();
    }
    
    public void salida_SC() throws InterruptedException{
            this.sNielsen.acquire();
            if (numNodo != this.deferred){
                this.send(this.deferred, new BeansMessage(this.TOKEN));
                this.deferred = numNodo;
            } else {
                this.holding = true;
            }
            this.sNielsen.release();
    }
    
    public void mRequest(BeansMessage msg) throws InterruptedException{
        String source,originator;
        sNielsen.acquire();
        boolean h = holding;
        int p=parents;
        source = msg.getDatos()[0];
        originator = msg.getDatos()[1];
        if(p == numNodo){
            if(h){
                this.send(originator, new BeansMessage(this.TOKEN));
                holding = false;
            } else {
                deferred = Integer.parseInt(originator);
            }
        } else {
            BeansMessage rmsg = new BeansMessage(this.REQUEST);
            rmsg.setDatos(nomNodo,originator);
            this.send(p, rmsg);
        }
        parents = Integer.parseInt(source);
        sNielsen.release();
    }
    
    public void getToken() throws InterruptedException {
        waitToken.acquire();
    }
    public void putToken(){
        waitToken.release();
    }
    
    
    
    private void send(int destinatario, BeansMessage msg){
        this.send(String.valueOf(destinatario),msg); 
    }
    
    private void send(String destinatario, BeansMessage msg){
        this.client.useTube("t_"+destinatario); 
        byte [] m =msg.getMensage();
        this.client.put(0, 0, 0, m);
    }
    
}
