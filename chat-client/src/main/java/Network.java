import chat.Message;
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
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public Network(String serverAddress, int port, IMessageService iMessageService) throws IOException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.iMessageService = iMessageService;
    }

    private void initNetwork(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());

        Thread readServerTread = new Thread(this::readMessageFromServer);
        readServerTread.setDaemon(true);
        readServerTread.start();
    }

    public void readMessageFromServer(){
        while (true){
            try {
                String message = inputStream.readUTF();
                Message msg = Message.fromJson(message);
                Platform.runLater(()-> iMessageService.readMessage(msg));
            } catch (Exception exception){
                System.out.println("Ссоединение с сервером было потеряно.");
                break;
            }
        }
    }

    public void send(String message) {
        try {
            if (outputStream == null) {
                initNetwork(serverAddress, port);
            }else {
                outputStream.writeUTF(message);
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с сервером.");
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
