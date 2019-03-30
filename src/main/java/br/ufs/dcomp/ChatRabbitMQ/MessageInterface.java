package br.ufs.dcomp.ChatRabbitMQ;

/**
 * Interface para envio de mensagem entre as classes.
 * @version 1.0
 * @since 2019-03-02.
 */
public interface MessageInterface {
  public void newMessage(String msg);
}
