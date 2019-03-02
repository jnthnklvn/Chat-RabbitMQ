package br.ufs.dcomp.ChatRabbitMQ;

/**
 * Classe principal, responsável por manter a
 * interação até que o usuário solicite a parada.
 * @version 1.0
 * @since Finalização da Etapa 2
 */
public class Main {
  
  /**
   * Cria um objeto de interface de interação
   * com o usuário e um loop para manter a leitura de dados. 
   */
  public static void main(String[] argv) throws Exception {

    UserInterface userInterface = new UserInterface();
    
    while(true){
        userInterface.readInput();
    }
  }
}