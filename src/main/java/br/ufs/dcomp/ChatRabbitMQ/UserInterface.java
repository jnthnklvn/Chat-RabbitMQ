package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;

public class UserInterface {
    private UserConnection connection;
    private Scanner read;
    private String user;
    private String receiver;
    private String prompt = ">> ";
    private final String HOST_URL = "http://ec2-3-82-21-99.compute-1.amazonaws.com:15672/api/";

    public UserInterface() {
        this.read = new Scanner(System.in);

        System.out.print("User: ");
        this.user = read.nextLine().trim();

        System.out.print(this.prompt);

        this.connection = new UserConnection(this.user);
    }

    private void tratamentoGrupal(String command) {
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
                //long time = System.currentTimeMillis();
                UserConnection fileConnection = new UserConnection(this.user);
                
                fileConnection.setFileMessage(cmd_list[1]);
                fileConnection.setReceiver(this.receiver);

                System.out.println("Enviando \"" + cmd_list[1] + "\" para " + this.receiver + "!");
            
                Thread thread = new Thread(fileConnection);
                thread.start();
                //System.out.println("UserInterface/Iniciar Thread: " + (System.currentTimeMillis() - time));
                break;
            case "listGroups":
                String sJsonKey = "source";
                String uStr = "queues/%2F/" + this.user + "/bindings?columns=" + sJsonKey;
                HTTPrequestAPI uHttpAPI = new HTTPrequestAPI(HOST_URL + uStr, sJsonKey);

                uHttpAPI.setPrompt(this.prompt);

                Thread uThread = new Thread(uHttpAPI);

                uThread.start();
                break;
            case "listUsers":
                String dJsonKey = "destination";
                String gStr = "exchanges/%2F/" + cmd_list[1] + "/bindings/source?columns=" + dJsonKey;
                HTTPrequestAPI gHttpAPI = new HTTPrequestAPI(HOST_URL + gStr, dJsonKey);
                
                Thread gThread = new Thread(gHttpAPI);

                gHttpAPI.setPrompt(this.prompt);

                gThread.start();
                break;
            default:
                System.out.println("404: Not Found Command");
                break;
        }
    }

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
                this.tratamentoGrupal(input.substring(1));
                break;
            default:
                if(this.prompt.equals(">> ")){
                    System.out.println("400: Bad Request");
                }else{
                    this.connection.sendMessageTo(input, this.receiver, false);
                }
                break;
            }this.connection.setPrompt(this.prompt);
        }System.out.print(this.prompt);
    }
}