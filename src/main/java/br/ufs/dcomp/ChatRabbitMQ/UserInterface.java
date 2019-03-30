package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;

/**
 * Classe para interação com usuário, tratamento de entrada
 * e direcionamento dos serviços.
 * @version 1.0
 * @since Finalização da Etapa 2
 */
public class UserInterface implements MessageInterface {
    private UserConnection connection;
    //private UserConnection fConnection;

    private Scanner read;
    private String user;
    private String receiver;
    private String prompt = ">> ";

    private final String HOST_URL = "http://chathttpbalancer-1406853435.us-east-1.elb.amazonaws.com:80/api/";

    /**
     * Inicializa o scanner de entrada, a conexão com servidor
     * e apresentação da interface de interação com o usuário.
     */
    public UserInterface() {
        this.read = new Scanner(System.in);

        System.out.print("User: ");
        this.user = read.nextLine().trim();

        System.out.print(this.prompt);

        this.connection = new UserConnection(this, this.user);
        //this.fConnection = new UserConnection(this, this.user, this.connection.getCon());
    }

    /**
     * Recebe um comando, caso o comando seja válido, executa as operações correspondentes
     * e imprimi o resultado, caso contrário, imprimi uma mensagem de erro para o usuário.
     * @param command - String com operação - e parâmetros - a ser realizada.
     */
    private void dealWithCommand(String command) {
        String[] cmd_list = command.split("\\s+", 3);
        String commandKey = cmd_list[0];
        String msg;

        if (!commandKey.equals("listGroups") && cmd_list.length<2) {
            System.out.println("406: Not Acceptable");
        }
        
        else
        switch (commandKey) {
            case "addGroup":
                this.connection.addExchangeChannel(cmd_list[1], this.user);

                System.out.println("Group " + cmd_list[1] + " created.");
                break;
            case "addUser":
                this.connection.addUserGroup(cmd_list[1], cmd_list[2]);
            
                msg = "$User " + cmd_list[1] + " has joined the group " + cmd_list[2];
            
                this.connection.sendMessageTo(msg, "#" + cmd_list[2], false);
                break;
            case "delFromGroup":
                this.connection.delUserGroup(cmd_list[1], cmd_list[2]);
            
                msg = "$User " + cmd_list[1] + " has been removed from the group " + cmd_list[2];
            
                this.connection.sendMessageTo(msg, "#" + cmd_list[2], false);
                break;
            case "removeGroup":
                this.connection.delExchangeChannel(cmd_list[1]);

                System.out.println("Group " + cmd_list[1] + " has been removed.");
                break;
            case "upload":
                connection.sendMessageTo(cmd_list[1], this.receiver, true);
                
                System.out.println("Enviando \"" + cmd_list[1] + "\" para " + this.receiver + "!");
                break;
            case "listGroups":
                String sJsonKey = "source";
                String uStr = "queues/vh/" + this.user + "/bindings?columns=" + sJsonKey;
                HTTPrequestAPI uHttpAPI = new HTTPrequestAPI(HOST_URL + uStr, sJsonKey);
                msg = uHttpAPI.getJsonMsg();

                System.out.println(msg);
                break;
            case "listUsers":
                String dJsonKey = "destination";
                String gStr = "exchanges/vh/" + cmd_list[1] + "/bindings/source?columns=" + dJsonKey;
                HTTPrequestAPI gHttpAPI = new HTTPrequestAPI(HOST_URL + gStr, dJsonKey);
                msg = gHttpAPI.getJsonMsg();

                System.out.println(msg);
                break;
            default:
                System.out.println("404: Not Found Command");
                break;
        }
    }

    /**
     * Lê entrada do usuário, verifica se é válida, se for, realiza a operação
     * indicada, e atualiza o estado do prompt(indicador de referência do usuário).
     */
    public void readInput() {
        String input = read.nextLine();

        if (input.isEmpty());

        else if (input.equals("quit")) {
            this.connection.close();
            System.exit(0);
        }

        else {
            switch (input.charAt(0)) {
            case '@':
                this.receiver = input.trim();
                this.prompt = this.receiver + ">> ";
                break;
            case '#':
                this.receiver = input.trim();
                this.prompt = this.receiver + ">> ";
                break;
            case '!':
                this.dealWithCommand(input.substring(1));
                break;
            default:
                if(this.prompt.equals(">> ")){
                    System.out.println("400: Bad Request");
                }else{
                    this.connection.sendMessageTo(input, this.receiver, false);
                }
                break;
            }
        }System.out.print(this.prompt);
    }
    
    /**
     * Imprimi uma mensagem e o estado do prompt em seguida.
     * @param msg - String com a msg a ser impressa.
     */
    public void newMessage(String msg){
        System.out.println("\n" + msg);
        System.out.print(this.prompt);
    }
}
