package server.client;

import chat.Command;
import chat.Message;
import chat.message.AuthMessage;
import server.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    public static final int TIMEOUT = 20 * 1000;
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
            System.out.printf("Message '%s' from client %s%n", clientMessage, clientName);
            Message m = Message.fromJson(clientMessage);
            switch (m.command) {
                case PUBLIC_MESSAGE:
                    myServer.broadcastMessage(m, this);
                    break;
                case PRIVATE_MESSAGE:
                    myServer.sendPrivateMessage(m);
                    break;
                case END:
                    return;
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
            Timer timeoutTimer = new Timer(true);
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            if (clientName == null) {
                                System.out.println("authentication is terminated caused by timeout expired");
                                sendMessage(Message.createAuthError("Истекло время ожидания подключения!"));
                                Thread.sleep(100);
                                socket.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, TIMEOUT);
            String clientMessage = in.readUTF();
            synchronized (this) {
                Message message = Message.fromJson(clientMessage);
                if (message.command == Command.AUTH_MESSAGE) {
                    AuthMessage authMessage = message.authMessage;
                    String login = authMessage.login;
                    String password = authMessage.password;
                    String nick = myServer.getAuthService().getNickByLoginPass(login, password);
                    if (nick == null) {
                        sendMessage(Message.createAuthError("Неверные логин/пароль"));
                        continue;
                    }

                    if (myServer.isNickBusy(nick)) {
                        sendMessage(Message.createAuthError("Учетная запись уже используется"));
                        continue;
                    }
                    clientName = nick;
                    sendMessage(Message.createAuthOk(clientName));
                    myServer.broadcastMessage(Message.createPublic(null, clientName + " is online"));
                    myServer.subscribe(this);
                    break;
                }
            }
        }
    }

    private void sendMessage(Message message) {
        sendMessage(message.toJson());
    }

    public void sendMessage(String message)  {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to user " + clientName + " : " + message);
            e.printStackTrace();
        }
    }


    public String getClientName() {
        return clientName;
    }
}

