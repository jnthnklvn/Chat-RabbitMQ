package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Chat {
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à's' HH:mm");
  private static final String EXCHANGE_PRIVATE = "direct";
  private static final String EXCHANGE_GROUP = "group";
  private static final String HOST = "ec2-3-88-85-161.compute-1.amazonaws.com";
  private static final String USERNAME = "zkelvinfps";
  private static final String PASSWORD = "0";
  private static final String VIRTUAL_HOST = "/";
  private static final String QUEUE_NAME = "minha-fila";
  private static Channel channel;

  private static void createChannel() {
    ConnectionFactory factory = new ConnectionFactory();

    factory.setHost(HOST);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    factory.setVirtualHost(VIRTUAL_HOST);

    Connection connection;
    
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }
  
  private static void declareChannel() {
    try {
      channel.exchangeDeclare(EXCHANGE_PRIVATE, BuiltinExchangeType.DIRECT);

      // (queue-name, durable, exclusive, auto-delete, params);
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void consumeChannel() {
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(message);
      }
    };

    try {
      // (queue-name, autoAck, consumer);
      channel.basicConsume(QUEUE_NAME, true, consumer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void addExchangeChannel(String GROUP_NAME) {
    try {
      channel.exchangeDeclare(GROUP_NAME, BuiltinExchangeType.FANOUT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void addUserGroup(String USER_NAME, String GROUP_NAME) {
    try {
      channel.queueBind(QUEUE_NAME, GROUP_NAME, USER_NAME);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void FacadeCreateChannel() {
    createChannel();
    declareChannel();
    consumeChannel();
  }

  private static void tratamentoGrupal(String comando) {
    String [] cmd_list = comando.split("\\s+", 3);
    if (cmd_list[0].contains("!addGroup")) {
      addExchangeChannel(cmd_list[1]);
      System.out.println("Grupo " + cmd_list[1] + " adicionado"); //teste
    }else if (cmd_list[0].contains("!addUser")){
      addUserGroup(cmd_list[1], cmd_list[2]);
      System.out.println("Usuario " + cmd_list[1] + " adicionado ao grupo " + cmd_list[2]); //teste
    }
  }
  
  private static void publishChannel(String destinatario, String msg) {
    try {
      if (destinatario.charAt(0)=='@'){
        channel.basicPublish(EXCHANGE_PRIVATE, destinatario, null, msg.getBytes());
      }else{
        channel.basicPublish(EXCHANGE_GROUP, destinatario, null, msg.getBytes());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] argv) {
    Timestamp timestamp;

    FacadeCreateChannel();

    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    String usuario = sc.nextLine();
    String destinatario = "";

    while (true) {
      System.out.print(">> ");

      destinatario = sc.nextLine();

      if (destinatario.contains("quit")) {
        System.exit(0);
      }
      else if (destinatario.charAt(0) == '!'){
        tratamentoGrupal(destinatario);
      }
      else if (destinatario.charAt(0) == '@' || destinatario.charAt(0) == '#') {
        String mensagem = "";

        while (true) {
          System.out.print(destinatario + ">> ");

          mensagem = sc.nextLine();

          if (mensagem.charAt(0) == '@' || mensagem.charAt(0) == '#') {
            destinatario = mensagem;
          }
          else if (mensagem.charAt(0) == '!'){
            tratamentoGrupal(mensagem);
          }
          else {
            if (mensagem.contains("quit")) {
              break;
            }

            timestamp = new Timestamp(System.currentTimeMillis());
            String txt_msg = "(" + sdf.format(timestamp) + ") " + usuario + " diz: " + mensagem;
            
            publishChannel(destinatario, txt_msg);
          }
        }
      }
    }
  }
}