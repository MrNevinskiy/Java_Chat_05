package server;

import server.auth.Auth;
import server.auth.AuthService;
import server.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MyServer {

    private static final int PORT = 8189;

    private final AuthService authService = new Auth();

    private ConcurrentLinkedDeque<ClientHandler> clients = new ConcurrentLinkedDeque<>();

    public MyServer(){
        System.out.println("Server running.");
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            authService.start();
            while (true){
                System.out.println("Awaiting client...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected...");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.err.println("Error Server " + e.getMessage());
            e.printStackTrace();
        } finally {
            authService.stop();
        }

    }

    public synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public  AuthService getAuthService(){
        return authService;
    }

    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler clientHandler: clients){
            if(clientHandler.getNickName().equals(nick)){
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMessage(String message, ClientHandler... unfilteredClients) {
        List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
        for (ClientHandler clientHandler : clients) {
            if(!unfiltered.contains(clientHandler)){
                clientHandler.sendMessage(message);
            }
        }
    }

    public synchronized void sendPrivateMessage(String nickLogin, String message) {
        for (ClientHandler clientHandler: clients) {
            if (clientHandler.getNickName().equals(nickLogin)){
                clientHandler.sendMessage(message);
                break;
            }
        }
    }
}
