package server.client;

import server.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private MyServer myServer;
    private String clientName;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(()-> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() throws IOException {
        while (true){
            String message = in.readUTF();
            System.out.printf("Message %s from client %s%n", message, clientName);
            if (message.equals("/exit")) {
                return;
            }
            myServer.broadcastMessage(clientName + ": " + message);
        }
    }

    private void authentication() throws IOException {
        String messageAuth = in.readUTF();
        if (messageAuth.startsWith("/auth")){
            String[] loginAndPass = messageAuth.split("\\s+");
            String login = loginAndPass [1];
            String pass = loginAndPass [2];

            String nick = myServer.getAuthService().getNickByLoginPass(login,pass);
            if(nick == null){
                sendMessage("Неверный логин или пароль. ");
                return;
            }

            if(myServer.isNickBusy(nick)){
                sendMessage("Уже есть такой пользователь. ");
                return;
            }
            sendMessage("/authok " + nick);
            clientName = nick;
            myServer.broadcastMessage(clientName + " is online. ");
            myServer.subscribe(this);
        }
    }

    public void sendMessage(String message){
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName(){
        return clientName;
    }
}

