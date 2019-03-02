package br.ufs.dcomp.ChatRabbitMQ;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe responsável pelo tratamento de arquivos de mensagem.
 * Transformação e download de arquivos de arquivo em bytes.
 * @version 1.0
 * @since Finalização da Etapa 3
 */
public class FileMessage {
    private String filePath;

    /**
     * Método construtor, responsável por inicializar a váriavel de caminho para o arquivo.
     * @param filePath - String com endereço de um arquivo ou diretório.
     */
    public FileMessage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Encontra o tipo de um arquivo e o retorna numa String.
     * @return tipoMime - String com o tipo do arquivo.
     */
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

    /**
     * Transforma uma cópia od arquivo, a partir de seu endereço, em um Array de bytes.
     * @return dataAsByte - Array de bytes com representação do arquivo.
     */
    public byte[] getFileBytes() {
        BufferedInputStream bis;
        byte[] dataAsByte = null;
        
        try {
            FileInputStream inputstream = new FileInputStream(this.filePath);
            int x = 0;
            inputstream.mark(8192*8192);//limite//8192*8192//64MB

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
        return dataAsByte;
    }

    /**
     * Escreve um arquivo a partir de um Array de bytes em determinado local.
     * @param buf - Array de bytes com os bytes do arquivo.
     * @param fileName - String com o nome do arquivo (incluindo o caminho).
     */
    public void downloadFile(byte[] buf, String fileName) {
        File file = new File(filePath);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file + "/" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            fos.write(buf, 0, buf.length);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}