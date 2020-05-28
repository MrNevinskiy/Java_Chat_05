import javafx.application.Platform;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network implements Closeable {

    private final String serverAddress;
    private final int port;
    private final IMessageService iMessageService;

    private Socket socket;
    private DataOutputStream outMessage;
    private DataInputStream inMessage;

    private boolean running = true;

    public Network(String serverAddress, int port, IMessageService iMessageService) throws IOException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.iMessageService = iMessageService;
    }

    private void initNetwork(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.inMessage = new DataInputStream(socket.getInputStream());
        this.outMessage = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            while (running) {
                try {
                    String messageFromServer = inMessage.readUTF();
                    Platform.runLater(() -> iMessageService.readMessage(messageFromServer));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    public void send(String message) {
        try {
            if (outMessage == null) {
                initNetwork(serverAddress, port);
            }else {
                outMessage.writeUTF(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
