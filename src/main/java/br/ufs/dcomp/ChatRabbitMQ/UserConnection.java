package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.google.protobuf.ByteString;

/**
 * Classe para conexão e operações do usuário com o servidor da aplicação
 * @version 1.0
 * @since Finalização da Etapa 2
 */
public class UserConnection implements Runnable {
    private final String HOST = "ec2-54-152-59-230.compute-1.amazonaws.com";
    private final String USERNAME = "zkelvinfps";
    private final String PASSWORD = "0";
    private final String VIRTUAL_HOST = "vh";
    private final String DOWNLOADS_PATH = "/home/jonathankelvin/Documentos/";

    private final SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");

    private MessageInterface messageInterface;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    private String textQueue;
    private String fileQueue;
    private String receiver;
    private String fileMessage;

    /**
     * Método construtor responsável por inicializar as váriaveis de
     * Interface de mensagem e filas, e chamar método para iniciar conexão.
     * @param messageInterface - MessageInterface para envio de mensagens ao usuário.
     * @param usuario - String usada para nomear as filas de mensagens.
     */
    public UserConnection(MessageInterface messageInterface, String usuario) {
        this.textQueue = usuario;
        this.fileQueue = "f" + usuario;
        this.messageInterface = messageInterface;

        this.doConnection();
    }

    /**
     * Método construtor especifico para o envio de arquivos.
     * @param messageInterface - MessageInterface para envio de mensagens ao usuário.
     * @param usuario - String usada para nomear as filas de mensagens.
     */
    public UserConnection(MessageInterface messageInterface, String usuario, Connection connection) {
        this.textQueue = usuario;
        this.messageInterface = messageInterface;
        try{
            this.channel = connection.createChannel();
            this.channel.queueDeclare("f" + usuario, false, false, false, null);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método responsável por inicializar a conexão com servidor e chamar os métodos
     * para criação e cosumo dos canais de mensagens.
     */
    private void doConnection() {
        factory = new ConnectionFactory();

        factory.setHost(this.HOST);
        factory.setUsername(this.USERNAME);
        factory.setPassword(this.PASSWORD);
        factory.setVirtualHost(this.VIRTUAL_HOST);

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.declareChannel();
            this.consumeChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método responsável por declarar uma fila para recebimento de mensagens de
     * textos e outra para arquivos.
     */
    private void declareChannel() {
        try {
            this.channel.queueDeclare(textQueue, false, false, false, null);
            this.channel.queueDeclare(fileQueue, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trata a mensagem recebida de acordo com o tipo 
     * - se arquivo ou texto - e retorna uma mensagem em texto.
     * @param body - Array com conteúdo da mensagem.
     * @return msg - String com o texto pronto para apresentação ao usuário.
     */
    private String receiveMessage(byte[] body) throws IOException{
        MensagemProtos.Mensagem mensagem = MensagemProtos.Mensagem.parseFrom(body);
        String tipo = mensagem.getConteudo().getTipo();
        String msg = "";
        String emissor = mensagem.getEmissor().split("#")[0];

        if (emissor.equals(this.textQueue) || emissor.equals(this.fileQueue));

        else if (tipo.contains(".")) {
            byte[] arquivo = mensagem.getConteudo().getCorpo().toByteArray();
            msg = "(" + mensagem.getData() + " às " + mensagem.getHora() + ") " 
                + "Arquivo \"" + tipo + "\" recebido de @" + emissor + "!";

            FileMessage fMessage = new FileMessage(DOWNLOADS_PATH);

            fMessage.downloadFile(arquivo, tipo);
        }

        else {
            msg = mensagem.getConteudo().getCorpo().toStringUtf8();

            if (msg.charAt(0) == '$') {
                msg = msg.substring(1);
            }

            else {
                msg = "(" + mensagem.getData() + " às " + mensagem.getHora() 
                    + ") " + mensagem.getEmissor() + " diz: " + msg;
            }
        }
        return msg;
    }

    /**
     * Define o canal e as filas a serem ouvidos, e chama 
     * métodos para trato e envio da mensagem para ser apresentada.
     */
    private void consumeChannel() {
        Consumer consumer = new DefaultConsumer(this.channel) {
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) {
                try{
                    String msg = receiveMessage(body);
                    if (!msg.isEmpty()){
                        messageInterface.newMessage(msg);
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            this.channel.basicConsume(this.textQueue, false, consumer);
            this.channel.basicConsume(this.fileQueue, false, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Cria um grupo de mensagens (exchange), e após a criação
     * chama o método para adição do usuário criador ao grupo.
     * @param groupName - String com o nome do grupo que dará nome a exchange.
     * @param userName - String com o username do criador a ser adicionado ao grupo.
     */
    public void addExchangeChannel(String groupName, String userName) {
        try {
            this.channel.exchangeDeclare(groupName, BuiltinExchangeType.FANOUT);
            this.addUserGroup(userName, groupName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Adiciona (liga) um usuario (fila) a um grupo (exchange).
     * @param userName - String com nome da fila a ser ligada à exchange.
     * @param groupName - String com o nome da exchange alvo.
     */
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

    /** Remove (desliga) um usuario (fila) de um grupo (exchange).
     * @param userName - String com nome da fila a ser desligada da exchange.
     * @param groupName - String com o nome da exchange alvo.
     */
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

    /** Remove um grupo de mensagens (exchange).
     * @param groupName - String com o nome da exchange a ser removida.
     */
    public void delExchangeChannel(String groupName) {
        try {
            this.channel.exchangeDelete(groupName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Publica mensagem no canal, destinada a um dado receiver.
     * @param receiver - String com o nome do destinatário da mensagem.
     * @param msg - Array de bytes com conteúdo da mensagem.
     */
    private void publishChannel(String receiver, byte[] msg) {
        try {
            if (receiver.charAt(0) == '@') {
                this.channel.basicPublish("", receiver.substring(1), null, msg);
            } else if (receiver.charAt(0) == '#') {
                this.channel.basicPublish(receiver.substring(1), "", null, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Trata a mensagem e a encaminha para publicação.
     * @param msg - String com conteúdo da mensagem.
     * @param receiver - String com o nome do destinatário da mensagem.
     * @param isFile - boolean que indica se o conteúdo é ou não um arquivo.
     */
    public void sendMessageTo(String msg, String receiver, boolean isFile) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        MensagemProtos.Mensagem.Builder mensagem = MensagemProtos.Mensagem.newBuilder();

        if (receiver.charAt(0) == '#') {
            mensagem.setEmissor(textQueue + "#" + receiver.substring(1));
        } else {
            mensagem.setEmissor(textQueue);
        }

        mensagem.setData(sdfData.format(timestamp));
        mensagem.setHora(sdfHour.format(timestamp));

        MensagemProtos.Conteudo.Builder conteudo = MensagemProtos.Conteudo.newBuilder();

        if (isFile) {
            String[] list_add = msg.split("/");
            FileMessage fMessage = new FileMessage(msg);

            conteudo.setTipo(list_add[list_add.length - 1]);
            conteudo.setCorpo(ByteString.copyFrom(fMessage.getFileBytes()));

        } else {
            conteudo.setTipo("text/plain");
            conteudo.setCorpo(ByteString.copyFrom(msg.getBytes()));
        }
        mensagem.setConteudo(conteudo.build());

        publishChannel(receiver, mensagem.build().toByteArray());
    }

    /** 
     * Fecha o canal e a conexão com o servidor.
     */
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
        sendMessageTo(this.fileMessage, this.receiver, true);
        String msg = "Arquivo \"" + this.fileMessage + "\" foi enviado para " + this.receiver;
        messageInterface.newMessage(msg);
    }

    /** Altera o destinátario atual das mensagens.
     * @param receiver - String a sobrescrever.
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /** Altera o arquivo de mensagem atual.
     * @param fileMessage - String a sobrescever.
     */
    public void setFileMessage(String fileMessage) {
        this.fileMessage = fileMessage;
    }

    /** Retorna o objeto de conexão
     * @return connection - Connection com o servidor
     */
    public Connection getCon() {
        return this.connection;
    }
}
