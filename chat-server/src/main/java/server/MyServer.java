package server;

import chat.Message;
import server.auth.Auth;
import server.auth.AuthService;
import server.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MyServer {

    private static final int PORT = 8189;

    private ServerSocket serverSocket = null;

    private final AuthService authService = new Auth();

    private ConcurrentLinkedDeque<ClientHandler> clients = new ConcurrentLinkedDeque<>();

    public MyServer(){
        System.out.println("Server running.");
        try {
            serverSocket = new ServerSocket(PORT);
            authService.start();
            while (true) {
                System.out.println("Awaiting client connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Client has connected");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.err.println("Error Server " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdownServer();
        }
    }

    private void shutdownServer() {
        try {
            authService.stop();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastClientsList() {
        List<String> nicknames = new ArrayList<>();
        for (ClientHandler client : clients) {
            nicknames.add(client.getClientName());
        }

        Message message = Message.createClientList(nicknames);
        broadcastMessage(message.toJson());
    }

    public synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public  AuthService getAuthService(){
        return authService;
    }

    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler clientHandler: clients){
            if(clientHandler.getClientName().equals(nick)){
                return true;
            }
        }
        return false;
    }

    public void broadcastMessage(Message message, ClientHandler... unfilteredClients) {
        broadcastMessage(message.toJson(), unfilteredClients);
    }

    public void broadcastMessage(String message, ClientHandler... unfilteredClients) {
        List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
        for (ClientHandler clientHandler : clients) {
            if(!unfiltered.contains(clientHandler)){
                clientHandler.sendMessage(message);
            }
        }
    }

    public void sendPrivateMessage(Message message) {
        for (ClientHandler clientHandler: clients) {
            if (clientHandler.getClientName().equals(message.privateMessage.to)){
                clientHandler.sendMessage(message.toJson());
                break;
            }
        }
    }
}
