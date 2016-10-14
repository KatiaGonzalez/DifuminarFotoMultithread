
package Distribucion;


import com.surftools.BeanstalkClientImpl.ClientImpl;
import com.surftools.BeanstalkClientImpl.JobImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Cada instancia de esta clase es un hilo correspondiente a cada nodo (incluido
 * el nodo entorno). Realiza el desplegado del menú para que el usuario introduz-
 * ca la tarea que desa realizar (contar o difumiar).
 * Además en el run de esta clase realiza las tareas de difuminar una imagen o 
 * de contar 1.000.000. Además contiene los metodos auxiliares para realizar estas
 * tareas y para realizar el algoritmo de Dijkstra.
 * Este proceso enciende un proceso para recibir que esta contenido como una
 * subclase (llamada: ProcesoRecibir)dentro de ésta.
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class Proceso extends Thread{

    private ProcesoRecibir hiloRecibir;
    private final String IP ="127.0.0.1";
    private final Integer PORT= new Integer(11300);
    private Nodo nodo;
    private ClientImpl client;    
    private volatile int inDeficit;
    private volatile int outDeficit;
    private volatile int incoming[][];
    private volatile int parentDS;
    private volatile boolean changeParent;
    private Semaphore sDS,sOutDef;
    private MizunoSC msc1,msc2;
    private final String pathSpanningTree="spaningtree.txt";
    private final String pathSumar="totales.txt";
    private final String pathFotoDifuminada="fotito_dif.png";
    private String pathFoto;
    
 
    public Proceso(Nodo nodo) throws InterruptedException {
        super();
        sDS = new Semaphore(1);
        sOutDef = new Semaphore(1);
        this.client = new ClientImpl(IP,PORT,true);
        this.nodo = nodo;
        this.inDeficit=0;
        this.outDeficit = 0;
        this.parentDS = -1;
        if(!nodo.esRaiz()) {
            msc1 = new MizunoSC(this.nodo.getNomNodo(),Integer.parseInt(this.nodo.getPadres().get(0)), this.client, true);
            msc2 = new MizunoSC(this.nodo.getNomNodo(),Integer.parseInt(this.nodo.getPadres().get(0)), this.client, false);
            this.incoming = new int[2][this.nodo.getPadres().size()];
            int i = 0;
            for(String padre: this.nodo.getPadres()){
                this.incoming[0][i]= Integer.parseInt(padre);
                this.incoming[1][i]=0;
                i++;
            }
        }else {
            msc1 = new MizunoSC("0",0, this.client, true);
            msc2 = new MizunoSC("0",0, this.client, false);
        }
        this.hiloRecibir = new ProcesoRecibir(nodo.getNomNodo());
        this.changeParent = false;
    } 
    
    public Proceso(String nomProceso, Nodo nodo) throws InterruptedException {
        this(nodo);
        super.setName(nomProceso);
        
    }
    
    @Override
    public void run(){
        JobImpl job;
        BeansMessage msg; 
        
        this.hiloRecibir.start();
        try{
            if(this.nodo.esRaiz()) {
                this.sOutDef.acquire();
            } 
            while(true){

                if(this.nodo.esRaiz()){
                    msg = mostrarMenu();
                    GraphDot escribir = new GraphDot(pathSpanningTree);
                    escribir.escribirRelacion("-1", nodo.getNomNodo(), false);

                }else{
                    job = this.hiloRecibir.getTrabajoPendiente(); //recibir un beansMessage de getTrabajoPendiente()
                    msg = new BeansMessage(job.getData());
                    if(this.changeParent){//escribir
                        GraphDot escribir = new GraphDot(pathSpanningTree);
                        msc2.entrada_SC();
                        escribir.escribirRelacion(msg.getDatos()[0], nodo.getNomNodo(), true);
                        msc2.salida_SC();
                        this.changeParent=false;
                    }

                }

                if(this.nodo.esRaiz()){
                    this.sOutDef.acquire();
                    String timeFin = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
                    System.out.println("\nHora de final de tarea= "+timeFin);
                }else{
                    int numTrabajadores = this.nodo.getHijos().size()+1;
                    switch(msg.getTipoMensaje()){
                        case JOB:
                            int amount = enviarTareaSumar(msg.getDatos()[1], numTrabajadores);
                            this.sumar(amount);
                            break;
                        case JOBDIF:
                            this.pathFoto = msg.getDatos()[1];
                            int yinicio = Integer.valueOf(msg.getDatos()[2]);
                            int altura = enviarTareaDifuminar(msg.getDatos(), numTrabajadores);
                            this.difuminar(yinicio, altura); 
                            break;
                    }
                    this.sendSignal();
                }
            }
        } catch (IOException ex){
            Logger.getLogger(Proceso.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (InterruptedException ex) {
            Logger.getLogger(Proceso.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private BeansMessage mostrarMenu() throws IOException, InterruptedException{
        String cabecera = "\nMENU\nEscoge una tarea a realizar(introduce el nº):\n";
        String tarea1="1- Sumar.\n";
        String tarea2="2- Difuminar imagen por defecto (fotito.png).\n";
        String tarea3="3- Difuminar imagen (especificando el fichero imagen).\n";
        System.out.print(cabecera+tarea1+tarea2+tarea3);
        return this.escogerOpcion();
    }
    
    private BeansMessage escogerOpcion() throws IOException, InterruptedException{
        boolean opcionValida;
        String opcion = leerOpcion();
        BeansMessage msg=null;
        do{
            opcionValida = true;
            String timeInicio="";
            switch(opcion){            
                case "1": 
                    FileRW totales = new FileRW(pathSumar);
                    totales.borrarFichero();
                    msg = new BeansMessage(BeansMessage.TipoMensaje.JOB);
                    msg.setDatos(this.nodo.getNomNodo(), "1000000");
                    timeInicio=new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
                    System.out.println("\nHora de inicio de tarea = "+timeInicio);
                    this.enviarTareaSumar(msg.getDatos()[1], this.nodo.getHijos().size());
                    break;
                    
                case "2":
                    this.pathFoto = "fotito.png";
                    ImagenIO img = new ImagenIO(this.pathFoto );
                    String alturaImagen = String.valueOf(img.getAltura());
                    msg = new BeansMessage(BeansMessage.TipoMensaje.JOBDIF);
                    msg.setDatos(this.nodo.getNomNodo(),this.pathFoto ,"0",alturaImagen);
                    timeInicio=new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
                    System.out.println("\nHora de inicio de tarea = "+timeInicio);                    enviarTareaDifuminar(msg.getDatos(), this.nodo.getHijos().size());
                    break;
                    
                case "3":
                    System.out.print("\nIntroduce la path de la imagen:");
                    this.pathFoto  = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    
                    ImagenIO imgUsr = new ImagenIO(this.pathFoto );
                    if(!imgUsr.existePath()){
                        System.out.println("La imagen: " + this.pathFoto  + " no existe!");
                        opcionValida=false;
                        break;
                    }
                    String alturaImagenUsr = String.valueOf(imgUsr.getAltura());
                    msg = new BeansMessage(BeansMessage.TipoMensaje.JOBDIF);
                    msg.setDatos(this.nodo.getNomNodo(),this.pathFoto ,"0",alturaImagenUsr);
                    timeInicio=new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
                    System.out.println("\nHora de inicio de tarea = "+timeInicio);                    enviarTareaDifuminar(msg.getDatos(), this.nodo.getHijos().size());
                    break;
                    
                default: 
                    System.out.println("Opcion no valida!!\nIntroduce una opcion del 1 al 3:");
                    opcion = leerOpcion(); 
                    opcionValida=false;
                    break;
            }
        }while(!opcionValida);
        return msg;
    }
    
    private String leerOpcion() throws IOException{
        String opcion; 
        opcion = new BufferedReader(new InputStreamReader(System.in)).readLine();
        opcion = opcion.replace(" ", "");
        return opcion;    
    }
    
    private void difuminar(int inicio, int altura) throws IOException, InterruptedException{
        ImagenIO bufferImagen = new ImagenIO(this.pathFoto);
        System.out.println("Nodo: "+this.nodo.getNomNodo() + " primera linea para difuminar= " 
                + inicio + " numero total de lineas a difuminar "+ altura);
        int[] rgbMap = bufferImagen.difuminar(bufferImagen.leer(inicio, altura));
        this.msc1.entrada_SC();
        bufferImagen.write(rgbMap, pathFotoDifuminada);
        this.msc1.salida_SC();
    }
    private int sumar(final int amount) throws InterruptedException, IOException {
        int suma = 0,decimaParte, partes=10, resto, count,sumaParcial = 0;
        
        FileRW fr = new FileRW(pathSumar);
        decimaParte = amount/partes;
        resto = amount % partes;
        count = decimaParte+resto;
        System.out.println("Nodo: "+this.nodo.getNomNodo()+" ha de contar la cantidad de: "+amount);
        while(suma!=amount){
            while(suma != count){
                suma++;
                sumaParcial++;
            }
            this.msc1.entrada_SC();/*entrada SC*/
            fr.sumar(sumaParcial);
            this.msc1.salida_SC();/*salida SC*/
            System.out.println("Nodo: "+this.nodo.getNomNodo() 
                    +" ha escrito en el fichero la cantidad parcial= "+sumaParcial);
            sumaParcial=0;
            count += decimaParte;
        }
        return suma;
    }
        
    private void sendSignal() throws InterruptedException, IOException{
            sOutDef.acquire();
            this.sDS.acquire();
            if(this.inDeficit > 1){
                for(int i=0; i<this.nodo.getPadres().size(); i++){
                    if(this.incoming[1][i] >1 || this.incoming[1][i] ==1 && this.incoming[0][i]!=this.parentDS){
                        BeansMessage msg = new BeansMessage(BeansMessage.TipoMensaje.SIGNAL);
                        this.send(this.incoming[0][i], msg);
                        --this.inDeficit;
                        --this.incoming[1][i];
                        break;
                    }
                }
                this.sDS.release();
                sOutDef.release();
            }else if(this.inDeficit == 1){
                if(this.outDeficit == 0){
                    BeansMessage msg = new BeansMessage(BeansMessage.TipoMensaje.SIGNAL);
                    this.send(this.parentDS,msg);
                    int parent = this.buscarPadre(String.valueOf(this.parentDS));
                    this.incoming[1][parent] = 0;
                    this.inDeficit = 0;
                    this.parentDS = -1;
                    sOutDef.release();
                    this.sDS.release();
                    /////escritura
                    GraphDot escribir = new GraphDot(pathSpanningTree);
                    msc2.entrada_SC();
                    escribir.escribirRelacion("-1", nodo.getNomNodo(), true);
                    msc2.salida_SC();
                }else{
                    this.sDS.release();
                    sendSignal();       
                }
            }
    }
    
    private int enviarTareaDifuminar(final String[] msg, final int div){
        int yinicio, altura, valor, residuo;
        List<String> listaHijos; 
        BeansMessage beansMessage;
        String alturaHijo;
        yinicio = Integer.valueOf(msg[2]);
        altura = Integer.valueOf(msg[3]);
        valor = altura / div;
        residuo = altura % div;
        listaHijos = new ArrayList<>(this.nodo.getHijos());
        beansMessage = new BeansMessage(BeansMessage.TipoMensaje.JOBDIF);
        alturaHijo = Integer.toString(valor);
        
        if(!this.nodo.esRaiz()){
            yinicio += valor+residuo;
        }else{
            String inicioHijo =Integer.toString(yinicio);
            beansMessage.setDatos(this.nodo.getNomNodo(),this.pathFoto ,inicioHijo,String.valueOf(residuo+valor));
            this.send(listaHijos.get(0),beansMessage);
            this.outDeficit++;
            yinicio += valor+residuo;
            listaHijos.remove(0);
        } 
        
        for (String hijo : listaHijos){
            String inicioHijo =Integer.toString(yinicio);
            beansMessage.setDatos(this.nodo.getNomNodo(),this.pathFoto ,inicioHijo,alturaHijo);
            this.send(hijo,beansMessage);
            this.outDeficit++;
            yinicio += valor;
        }
        this.client.ignore("default");
        return valor+residuo;
    }
    
    private int enviarTareaSumar(final String msg, final int div){
        int valor = Integer.parseInt(msg);
        int residuo = valor % div;
        BeansMessage beansMessage = new BeansMessage(BeansMessage.TipoMensaje.JOB);
        valor = valor/div;
        String tareaEnviar = Integer.toString(valor);
        beansMessage.setDatos(this.nodo.getNomNodo(),tareaEnviar);
        for (String hijo : this.nodo.getHijos()) {
            this.send(hijo,beansMessage);
            this.outDeficit++;
        }
        this.client.ignore("default");
        return valor+residuo;
    }
    
    public void send(int destinatario, BeansMessage msg){
        this.send(String.valueOf(destinatario),msg); 
    }
    
    public void send(String destinatario, BeansMessage msg){
        this.client.useTube("t_"+destinatario); 
        byte [] m =msg.getMensage();
        this.client.put(0, 0, 0, m);
    }
    
    private int buscarPadre(String padre){
        int size = this.nodo.getPadres().size();
        for(int i=0; i<size;i++){
            if(incoming[0][i]==Integer.parseInt(padre)) return i;
        }
        return -1;
    }
    
    /**
     * Esta clase es un hilo que se encarga de recibir los mensajes
     * que le llegan al nodo.
     */
    public class ProcesoRecibir extends Thread{

        private ClientImpl clientRecibir;
        private String watchTube;
        private List<JobImpl> trabajosPendientes;
        private Semaphore sWaitJob;//,waitToken;

        public ProcesoRecibir(final String watchTube) throws InterruptedException{
            super("recibir_"+watchTube);
            this.clientRecibir = new ClientImpl(IP,PORT);
            this.sWaitJob = new Semaphore(1);
            this.watchTube = watchTube;
            this.trabajosPendientes = new ArrayList<>(0);
            sWaitJob.acquire();
            
            
        }  
        
        @Override
        public void run(){
            this.clientRecibir.useTube("t_" +watchTube);
            this.clientRecibir.watch("t_"+watchTube);
            this.clientRecibir.ignore("default");
            try {
                while(true){
                    JobImpl job = (JobImpl)this.clientRecibir.reserve(null);
                    BeansMessage msg = new BeansMessage(job.getData());
                    switch(msg.getTipoMensaje()){
                        case JOB:
                        case JOBDIF:
                            putJob(job);
                            break;
                        case REQUEST1:                              
                            msc1.mRequest(msg);
                            break;
                        case REQUEST2:
                            msc2.mRequest(msg);
                            break;
                        case TOKEN1:
                            msc1.putToken();
                            break;
                        case TOKEN2:
                            msc2.putToken();
                            break;
                        case SIGNAL:
                            --outDeficit;
                            if(outDeficit==0 && sOutDef.hasQueuedThreads())sOutDef.release();
                            break;
                    }
                    this.clientRecibir.delete(job.getJobId());
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Proceso.class.getName()).log(Level.SEVERE, null, ex);
                this.clientRecibir.close(); //este punto jamas se debería alcanzar
            }
        }

        public JobImpl getTrabajoPendiente() throws InterruptedException{
            sWaitJob.acquire();
            JobImpl newJob = this.trabajosPendientes.get(0);
            this.trabajosPendientes.remove(0);
            return newJob;
        }

        private void putJob(JobImpl job) throws InterruptedException{
            sDS.acquire();
            this.trabajosPendientes.add(job);
            BeansMessage msg = new BeansMessage(job.getData());
            String source = msg.getDatos()[0];
            if(parentDS == -1) {
                parentDS = Integer.parseInt(source);
                changeParent=true;
            }
            ++inDeficit;
            ++incoming[1][buscarPadre(source)];
            if(sOutDef.hasQueuedThreads()) sOutDef.release();
            sDS.release();
            sWaitJob.release();
        }
    }
}
