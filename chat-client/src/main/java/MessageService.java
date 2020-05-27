
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MessageService implements IMessageService {

    private static final String HOST_ADDRESS_PROP = "server.address";
    private static final String HOST_PORT_PROP = "server.port";
    private static final String STOP_SERVER = "/exit";


    private String hostAddress;
    private int hostPort;

    private Controller controller;
    private Network network;
    private ListView<String> chatWindow;
    private boolean needStopServer;


    public MessageService(Controller controller, boolean needStopServer) throws IOException {
        this.chatWindow = controller.chatWindow;
        this.controller = controller;
        this.needStopServer = needStopServer;
        initialize();
    }

    private void initialize() throws IOException {
        readProperties();
        connectionToServer();
    }

    private void connectionToServer() throws IOException {
        this.network = new Network(hostAddress, hostPort, this);
    }

    private void readProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/info.properties")) {
            properties.load(inputStream);
            hostAddress = properties.getProperty(HOST_ADDRESS_PROP);
            hostPort = Integer.parseInt(properties.getProperty(HOST_PORT_PROP));
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        network.send(message);
    }

    @Override
    public void readMessage(String message) {
        if (message.startsWith("/authok")) {
            controller.authPanel.setVisible(false);
            controller.chatPanel.setVisible(true);
        } else if (controller.authPanel.isVisible()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка авторизации: ");
            alert.setContentText(message);
            alert.showAndWait();
        } else {
            chatWindow.getItems().addAll("Server: " + message);
        }
    }

    @Override
    public void close() throws IOException {
        if (needStopServer) {
            sendMessage(STOP_SERVER);
        }
        network.close();
    }


}
