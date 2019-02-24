package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.google.protobuf.ByteString;

public class UserConnection implements Runnable{
    private final String HOST = "ec2-3-88-85-161.compute-1.amazonaws.com";
    private final String USERNAME = "zkelvinfps";
    private final String PASSWORD = "0";
    private final String VIRTUAL_HOST = "/";
    private final String DOWNLOADS_PATH = "/home/jonathankelvin/Documentos/JavaProjects/chat-em-linha-de-comando-via-rabbitmq/src/main/java/br/ufs/dcomp/";

    private final SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
    private Timestamp timestamp;

    private String textQueue;
    private String fileQueue;
    private String prompt = ">> ";
    private Connection connection;
    private Channel channel;

    private String receiver;
    private String fileMessage;

    public UserConnection(String usuario) {
        this.textQueue = usuario;
        this.fileQueue = "f" + usuario;

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(HOST);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST);

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            declareChannel();
            consumeChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void declareChannel() {
        try {
            this.channel.queueDeclare(textQueue, false, false, false, null);
            this.channel.queueDeclare(fileQueue, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void consumeChannel() {
        Consumer consumer = new DefaultConsumer(this.channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                MensagemProtos.Mensagem mensagem = MensagemProtos.Mensagem.parseFrom(body);
                
                String tipo = mensagem.getConteudo().getTipo();
                String msg = " ";
                String emissor = mensagem.getEmissor().split("#")[0];

                if (emissor.equals(textQueue) || emissor.equals(fileQueue));

                else if(tipo.contains(".")){
                    //long time = System.currentTimeMillis();

                    byte[] arquivo = mensagem.getConteudo().getCorpo().toByteArray();
                    msg = "\n(" + mensagem.getData() + " às " + mensagem.getHora()
                    + ") " + "Arquivo \"" + tipo + "\" recebido de @" + emissor + "!";
                    FileMessage fMessage = new FileMessage(DOWNLOADS_PATH);

                    fMessage.downloadFile(arquivo, tipo, msg);
                    System.out.print(prompt);
                    //System.out.println("UserConnection/Consummer download: " + (System.currentTimeMillis() - time));
                }

                else{
                    msg = mensagem.getConteudo().getCorpo().toStringUtf8();

                    if (msg.charAt(0) == '$') {
                        System.out.println("\n" + msg.substring(1));
                        System.out.print(prompt);
                    }

                    else {
                        System.out.println("\n(" + mensagem.getData() + " às " + mensagem.getHora()
                            + ") " + mensagem.getEmissor() + " diz: " + msg);
                        System.out.print(prompt);
                    }
                }
            }
        };

        try {
            this.channel.basicConsume(textQueue, false, consumer);
            this.channel.basicConsume(fileQueue, false, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addExchangeChannel(String groupName, String userName) {
        try {
            this.channel.exchangeDeclare(groupName, BuiltinExchangeType.FANOUT);
            addUserGroup(userName, groupName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUserGroup(String userName, String groupName) {
        if (userName.charAt(0) == '@') {
            userName = userName.substring(1);
        }
        try {
            this.channel.queueBind(userName, groupName, userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delUserGroup(String userName, String groupName) {
        if (userName.charAt(0) == '@') {
            userName = userName.substring(1);
        }
        try {
            this.channel.queueUnbind(userName, groupName, userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delExchangeChannel(String groupName) {
        try {
            this.channel.exchangeDelete(groupName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void publishChannel(String receiver, byte[] msg) {
        //long time = System.currentTimeMillis();
        try {
            if (receiver.charAt(0) == '@') {
                this.channel.basicPublish("", receiver.substring(1), null, msg);
            } else if (receiver.charAt(0) == '#') {
                this.channel.basicPublish(receiver.substring(1), "", null, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("UserConnection/Publicar no canal: " + (System.currentTimeMillis() - time));
    }

    public void sendMessageTo(String msg, String receiver, boolean isFile) {
        //long time = System.currentTimeMillis();
        timestamp = new Timestamp(System.currentTimeMillis());

        MensagemProtos.Mensagem.Builder mensagem = MensagemProtos.Mensagem.newBuilder();
        
        if(receiver.charAt(0)=='#'){
            mensagem.setEmissor(textQueue + "#" + receiver.substring(1));
        }
        else{
            mensagem.setEmissor(textQueue);
        }
        
        mensagem.setData(sdfData.format(timestamp));
        mensagem.setHora(sdfHour.format(timestamp));

        MensagemProtos.Conteudo.Builder conteudo = MensagemProtos.Conteudo.newBuilder();
        
        if(isFile){
            String[] list_add = msg.split("/");
            FileMessage fMessage = new FileMessage(msg);

            conteudo.setTipo(list_add[list_add.length-1]);
            conteudo.setCorpo(ByteString.copyFrom(fMessage.getFileBytes()));

        }else{
            conteudo.setTipo("text/plain");
            conteudo.setCorpo(ByteString.copyFrom(msg.getBytes()));
        }
        mensagem.setConteudo(conteudo.build());

        publishChannel(receiver, mensagem.build().toByteArray());
        //System.out.println("UserConnection/Enviar mensagem: " + (System.currentTimeMillis() - time));
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void close() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        //long time = System.currentTimeMillis();
        sendMessageTo(this.fileMessage, this.receiver, true);
        //System.out.println("UserConnection/Rodar thread: " + (System.currentTimeMillis() - time));
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    /**
     * @param fileMessage the fileMessage to set
     */
    public void setFileMessage(String fileMessage) {
        this.fileMessage = fileMessage;
    }
}