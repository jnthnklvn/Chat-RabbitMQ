package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Chat {
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à's' HH:mm");
  
  public static void main(String[] argv) throws Exception {
    String usuario, destinatario = "0", mensagem = "0";
    Timestamp timestamp;
    ConnectionFactory factory = new ConnectionFactory();

    factory.setHost("ec2-3-88-85-161.compute-1.amazonaws.com"); // Alterar
    factory.setUsername("zkelvinfps"); // Alterar
    factory.setPassword("0"); // Alterar
    factory.setVirtualHost("/");

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String QUEUE_NAME = "minha-fila";
    // (queue-name, durable, exclusive, auto-delete, params);
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(message);
      }
    };
    // (queue-name, autoAck, consumer);
    channel.basicConsume(QUEUE_NAME, true, consumer);

    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    usuario = sc.nextLine();

    while (destinatario != "quit") {
      System.out.print(">> ");
      destinatario = sc.nextLine();
      if (destinatario.contains("quit")) {
        System.exit(0);
      }
      else if (destinatario.charAt(0) == '@') {
        while (mensagem != "quit") {
          System.out.print(destinatario + ">> ");
          mensagem = sc.nextLine();
          if (mensagem.charAt(0) == '@') {
            destinatario = mensagem;
          } else {
            if (mensagem.contains("quit")) {
              break;
            }
            timestamp = new Timestamp(System.currentTimeMillis());
            String txt_msg = "(" + sdf.format(timestamp) + ") " + usuario + " diz: " + mensagem;
            System.out.println(txt_msg);
          }
        }
      }
    }
  }
}