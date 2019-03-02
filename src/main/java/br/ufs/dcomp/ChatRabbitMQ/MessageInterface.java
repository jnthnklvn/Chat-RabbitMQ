package br.ufs.dcomp.ChatRabbitMQ;

/**
 * Interface para envio de mensagem entre as classes.
 * @version 1.0
 * @since Processo de desacoplamento e documentação.
 */
public interface MessageInterface {
  public void newMessage(String msg);
}
