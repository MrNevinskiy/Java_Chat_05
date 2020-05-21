package clientUI;

import javafx.scene.control.ListView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MessageService implements IMessageService {

    private static final String HOST_ADDRESS_PROP = "server.address";
    private static final String HOST_PORT_PROP = "server.port";

    private String hostAddress;
    private int hostPort;

    private Network network;
    private final ListView<String> chatWindow;

    public MessageService(ListView<String> chatWindow) throws IOException {
        this.chatWindow = chatWindow;
        initialize();
    }

    private void initialize() throws IOException {
        readProperties();
        connectionToServer();
    }

    private void connectionToServer() throws IOException {
        this.network = new Network(hostAddress, hostPort, this);
    }

    private void readProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("/info.properties");
        properties.load(inputStream);
        hostAddress = properties.getProperty(HOST_ADDRESS_PROP);
        hostPort = Integer.parseInt(properties.getProperty(HOST_PORT_PROP));
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public void sendMessage(String message) throws IOException {
        network.send(message);
    }

    @Override
    public void readMessage(String message) {
        chatWindow.getItems().addAll("Server: " + message);
    }


}
