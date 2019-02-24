package br.ufs.dcomp.ChatRabbitMQ;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage {
    private String filePath;

    public FileMessage(String filePath) {
        this.filePath = filePath;
    }

    public String getTipo() {
        Path source = Paths.get(filePath);
        String tipoMime = null;
        try {
            tipoMime = Files.probeContentType(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tipoMime;
    }

    public byte[] getFileBytes() {
        //long time = System.currentTimeMillis();
        //InputStream inputstream;
        BufferedInputStream bis;
        byte[] dataAsByte = null;
        
        try {
            FileInputStream inputstream = new FileInputStream(this.filePath);
            //inputstream = new BufferedInputStream();
            int x = 0;
            inputstream.mark(8192*1024);//limite//8192*1024//8MB

            while(inputstream.read()!=-1){
                x += 1;
            }

            dataAsByte = new byte[x];
            
            if(inputstream.markSupported()){
                inputstream.reset();
            }else{
                inputstream.close();
                inputstream = new FileInputStream(this.filePath);
            }

            while(inputstream.read(dataAsByte) != -1);
            
            inputstream.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("FileMessage/Get Bytes: " + (System.currentTimeMillis() - time));
        return dataAsByte;
    }

    public void downloadFile(byte[] buf, String fileName, String msg) {
        //long time = System.currentTimeMillis();
        File file = new File(filePath);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            fos.write(buf, 0, buf.length);
            fos.flush();
            fos.close();
            System.out.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("FileMessage/Baixar arquivo: " + (System.currentTimeMillis() - time));
    }
}