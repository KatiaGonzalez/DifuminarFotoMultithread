
package Distribucion;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.imageio.IIOException;
/**
 *
 * Esta clase tiene metodos para leer una imagen en formato .png así como el 
 * metodo para difuminar y escribir la imagen.
 * @author Tomeu Rubi y Katia Gonzalez
 */
public class ImagenIO {
    String path;
    ImageIO imagen;
    private int anchura, altura, pinicio;
    private int alturaOriginal;
    private boolean overUp, overDown;
    
    public ImagenIO(String path){
            this.path = path;
            this.overUp = false;
            this.overDown = false;
            this.anchura = 0;
            this.altura = 0;
            this.alturaOriginal = 0;
    }
    
    public boolean existePath(){
        File f = new File(this.path);
        return f.exists();
    }
    

    public int getAltura() throws IOException{
        BufferedImage image = ImageIO.read(new File(this.path));
        this.anchura = image.getWidth();
        this.alturaOriginal=image.getHeight();
        return this.alturaOriginal;
    }
    
    //TIPO ARGB (alfa, rojo, verde x azul)
    public void write(int[] rgbArray, String path) throws IOException{
        BufferedImage bi;
        File archivoDestino = new File(path);
        try{
            bi = ImageIO.read(archivoDestino);
            if(bi.getHeight()!=alturaOriginal || bi.getWidth() != this.anchura){
                bi = new BufferedImage(this.anchura,alturaOriginal,BufferedImage.TYPE_INT_ARGB);
            }
        }catch(IIOException e){
            bi = new BufferedImage(this.anchura,alturaOriginal,BufferedImage.TYPE_INT_ARGB);
        }
        bi.setRGB(0, this.pinicio, this.anchura, this.altura, rgbArray, 0, this.anchura);
        ImageIO.write(bi, "png", archivoDestino);
    } 
    
    public int[][] leer(int yinicio, int altura) throws IOException {
        BufferedImage image = ImageIO.read(new File(this.path));
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        this.anchura = image.getWidth();
        this.alturaOriginal=image.getHeight();
        this.altura = altura;
        this.pinicio = yinicio;
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
        final int pixelLength;
        int row = 0, col = 0;
        
        if(hasAlphaChannel) pixelLength = 4;
        else pixelLength=3;
        //final int pixelLength = 4;
        int pixel=0, pfin;
        if(yinicio!=0){ //overUp=t
            altura++;
            pixel = pixelLength*anchura*(yinicio-1);
            this.overUp=true;
        } //empiezo a leer el primer pixel de la linea superior a mi area
        pfin=pixel+altura*anchura*pixelLength;
        if(pfin!=pixels.length){ //overDown = t
            altura++;
            pfin +=anchura*pixelLength;
            this.overDown = true;
        }
        int[][] result = new int[altura][anchura];
        if (hasAlphaChannel) {
            for (; pixel <pfin; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // System.out.print("["+Byte.toUnsignedInt(pixels[pixel])+", ");// alpha
                argb += ((int) pixels[pixel + 1] & 0xff); //System.out.print(Byte.toUnsignedInt(pixels[pixel+1])+", "); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); //System.out.print(Byte.toUnsignedInt(pixels[pixel+2])+", ");// green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); //System.out.print(Byte.toUnsignedInt(pixels[pixel+3])+"] ");// red
                result[row][col] = argb;
                col++;
                if (col == anchura) {
                   col = 0;
                   row++;
                }
             }
        }else{
            for (; pixel <pfin; pixel += pixelLength){
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == anchura) {
                    col = 0;
                    row++;
                }
            }
        }
      return result;
    }
    
    public int[] difuminar(int[][] rgbMap){//, boolean overUp, boolean overDown){
        int lineas, colsDifuminar, y, lastcolDif, numPixelDif;
        int[] areaDif, red, green, blue;
        lineas = rgbMap[0].length; 
        colsDifuminar = rgbMap.length; 
        numPixelDif =y=0; 
        lastcolDif = colsDifuminar;
        red = new int[5];
        green = new int[5];
        blue = new int[5];
        if(this.overUp) {
            y=1;
            colsDifuminar--;
        }
        if(this.overDown) {
                colsDifuminar--;
                lastcolDif--;
        }
        areaDif = new int[colsDifuminar*lineas];
        
        for(;y<lastcolDif;y++){
            for(int x=0;x<lineas;x++){
                int index =0;
                for(int i=-2; i<=2;i++){
                    int r = i%2, q=i/2;
                    try{
                        red[index] = (rgbMap[y+r][x+q]>>16) & 0x000000ff;
                        green[index] = (rgbMap[y+r][x+q]>>8) & 0x000000ff;                
                        blue[index++] = rgbMap[y+r][x+q] & 0x000000ff;
                        }catch(IndexOutOfBoundsException e){
                            red[index] = (rgbMap[y][x]>>16) & 0x000000ff;
                            green[index] = (rgbMap[y][x]>>8) & 0x000000ff;                
                            blue[index++] = rgbMap[y][x] & 0x000000ff;
                        }
                }
                int r = (red[0]*4+red[1]+red[2]+red[3]+red[4])/8;
                int g = (green[0]*4+green[1]+green[2]+green[3]+green[4])/8;
                int b = (blue[0]*4+blue[1]+blue[2]+blue[3]+blue[4])/8;
                areaDif[numPixelDif++]=(r<<16 & 0xff0000) | (g<<8 & 0xff00) | b | (rgbMap[y][x]& 0xff000000);
            }
        }
        return areaDif;
    }
    
    
}
