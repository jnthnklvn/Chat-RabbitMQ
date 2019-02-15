package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.io.IOException;

public class Chat {

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("ec2-3-88-85-161.compute-1.amazonaws.com"); // Alterar
    factory.setUsername("zkelvinfps"); // Alterar
    factory.setPassword("0"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    String usuario = sc.next();
    System.out.print(">> ");
    while(true){
      String destinatario = sc.next();
      if ()
    }
    String QUEUE_NAME = "minha-fila";
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {

        String message = new String(body, "UTF-8");
        System.out.println(message);

      }
    };
                      //(queue-name, autoAck, consumer);    
    channel.basicConsume(QUEUE_NAME, true,    consumer);
    
  }
}