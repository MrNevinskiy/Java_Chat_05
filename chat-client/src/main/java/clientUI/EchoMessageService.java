package clientUI;

import javafx.scene.control.TextArea;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class EchoMessageService implements TextMessageService {

    private final DataOutputStream outMessage;
    private final DataInputStream inMessage;
    private final Socket socket;
    private final TextArea readWindow;
    private boolean running = true;

    public EchoMessageService(TextArea readWindow, String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        inMessage = new DataInputStream(socket.getInputStream());
        outMessage = new DataOutputStream(socket.getOutputStream());
        this.readWindow = readWindow;
        readMessage(null);
    }

    @Override
    public void sendMessage(String message) throws IOException {
        outMessage.writeUTF(message);
    }

    @Override
    public void readMessage(String message) {
        Thread thread = new Thread(()-> {
            try {
                while (running) {
                    String messageFromServer = inMessage.readUTF();
                    readWindow.appendText(messageFromServer + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
