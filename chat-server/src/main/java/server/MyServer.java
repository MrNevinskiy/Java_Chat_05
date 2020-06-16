package server;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(MyServer.class.getName());

    private ConcurrentLinkedDeque<ClientHandler> clients = new ConcurrentLinkedDeque<>();


    public MyServer(){
        System.out.println("Server running.");
        logger.info("Server running.");
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            authService.start();
            Auth.dbDriver();
            while (true){
                System.out.println("Awaiting client...");
                logger.info("Awaiting client...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected...");
                logger.info("Client connected...");
                new ClientHandler(this, socket);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error Server " + e.getMessage());
            logger.error("MyServer, Exception: ", e);
            e.printStackTrace();
        } finally {
            authService.stop();
            logger.info("finally case: authService.stop();");
        }

    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public  AuthService getAuthService(){
        return authService;
    }

    public boolean isNickBusy(String nick){
        for (ClientHandler clientHandler: clients){
            if(clientHandler.getNickName().equals(nick)){
                return true;
            }
        }
        return false;
    }

    public void broadcastMessage(String message, ClientHandler... unfilteredClients) {
        List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
        for (ClientHandler clientHandler : clients) {
            if(!unfiltered.contains(clientHandler)){
                clientHandler.sendMessage(message);
            }
        }
    }

    public void sendPrivateMessage(String nickLogin, String message) {
        for (ClientHandler clientHandler: clients) {
            if (clientHandler.getNickName().equals(nickLogin)){
                clientHandler.sendMessage(message);
                break;
            }
        }
    }
}
