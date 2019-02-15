package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.io.IOException;

public class Chat {

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    String usuario, destinatario = "0", mensagem = "0";
    factory.setHost("ec2-3-88-85-161.compute-1.amazonaws.com"); // Alterar
    factory.setUsername("zkelvinfps"); // Alterar
    factory.setPassword("0"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    usuario = sc.next();
    while(destinatario!="quit"){
      System.out.print(">> ");
      destinatario = sc.next();
      if(destinatario.contains("quit")){
        System.exit(0);
      }
      if (destinatario.charAt(0)=='@'){
        while (mensagem!="quit"){
            System.out.print(destinatario + ">> ");
            mensagem = sc.next();
            if (mensagem.charAt(0)=='@'){
                destinatario = mensagem;
            }else{
                if(mensagem.contains("quit")){
                  break;
                }
                System.out.println(mensagem);
            }
        }
      }
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