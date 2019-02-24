package br.ufs.dcomp.ChatRabbitMQ;

public class Main {

  public static void main(String[] argv) throws Exception {
    UserInterface userInterface = new UserInterface();
    
    while(true){
        userInterface.readInput();
    }
  }
}