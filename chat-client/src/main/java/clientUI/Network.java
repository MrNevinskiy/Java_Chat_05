package clientUI;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {


    private final DataOutputStream outMessage;
    private final DataInputStream inMessage;
    private final Socket socket;

    private boolean running = true;

    public Network(String serverAddress, int port, IMessageService iMessageService) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.inMessage = new DataInputStream(socket.getInputStream());
        this.outMessage = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            while (running) {
                try {
                    String messageFromServer = inMessage.readUTF();
                    Platform.runLater(() -> {
                        try {
                            iMessageService.readMessage(messageFromServer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }


    public void send(String message) throws IOException {
        outMessage.writeUTF(message);
    }
}
