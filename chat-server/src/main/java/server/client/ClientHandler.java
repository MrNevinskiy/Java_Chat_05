package server.client;

import server.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    public static final int TIMEOUT = 200 * 1000;
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

            new Thread(() -> {
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
        while (true) {
            String clientMessage = in.readUTF();
            System.out.printf("Message %s from client %s%n", clientMessage, clientName);

            if (clientMessage.equals("/exit")) {
                myServer.unsubscribe(this);
                return;
            }
            else if (clientMessage.startsWith("/w")) {
                String[] parts = clientMessage.split("\\s+");
                if (parts.length < 3) {
                    sendMessage("Error!" + clientMessage);
                    continue;
                }

                String receivedLogin = parts[1];
                String message = parts[2];

                myServer.sendPrivateMessage(receivedLogin, clientName + ": " + message);

            } else {
                myServer.broadcastMessage(clientName + ": " + clientMessage, this);
            }
        }
    }

    private void closeConnection() {

        myServer.unsubscribe(this);
        myServer.broadcastMessage(clientName + " is offline");
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket!");
            e.printStackTrace();
        }
    }

    private void authentication() throws IOException {

        while (true) {
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            if (clientName == null) {
                                System.out.println("auth timeout");
                                Thread.sleep(100);
                                closeConnection();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, TIMEOUT);

            String messageAuth = in.readUTF();
            if (messageAuth.startsWith("/auth")) {
                String[] loginAndPass = messageAuth.split("\\s+");
                String login = loginAndPass[1];
                String pass = loginAndPass[2];

                if (messageAuth.equals("/exit")){
                    closeConnection();
                }

                String nick = myServer.getAuthService().getNickByLoginPass(login, pass);
                if (nick == null) {
                    sendMessage("Неверный логин или пароль.");
                    continue;
                }

                if (myServer.isNickBusy(nick)) {
                    sendMessage("Уже есть такой пользователь. ");
                    continue;
                }
                sendMessage("/authok " + nick);
                clientName = nick;
                myServer.broadcastMessage(clientName + " is online. ");
                myServer.subscribe(this);
                break;

            } else if (messageAuth.startsWith("/reg")) {

                String[] loginAndPass = messageAuth.split("\\s+");
//                String userName = loginAndPass[1];
                String userLogin = loginAndPass[1];
                String userPassword = loginAndPass[2];

                if (messageAuth.equals("/exit")){
                    closeConnection();
                }

                boolean nick = myServer.getAuthService().sighUpLoginPass(userLogin,userPassword);
                if (!nick) {
                    sendMessage("Данный профиль уже существует.");
                    continue;
                }

                sendMessage("/authok " + userLogin);
                clientName = userLogin;
                myServer.broadcastMessage(clientName + " is online. ");
                myServer.subscribe(this);
                break;

            } else if (messageAuth.startsWith("/exit")){
                closeConnection();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return clientName;
    }
}

