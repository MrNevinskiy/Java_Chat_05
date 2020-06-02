import chat.Message;
import chat.message.PrivateMessage;
import chat.message.PublicMessage;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static chat.Command.AUTH_OK;

public class MessageService implements IMessageService {

    private static final String HOST_ADDRESS_PROP = "server.address";
    private static final String HOST_PORT_PROP = "server.port";

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
    public void sendMessage(Message message) {
        network.send(message.toJson());
    }

    @Override
    public void readMessage(Message message) {
        switch (message.command) {
            case AUTH_OK:
                processAuthOk(message);
                break;
            case PRIVATE_MESSAGE: {
                processPrivateMessage(message);
                break;
            }
            case PUBLIC_MESSAGE: {
                processPublicMessage(message);
                break;
            }
            case AUTH_ERROR: {
                controller.showAuthError(message.authErrorMessage.errorMsg);
                break;
            }
            case CLIENT_LIST:
                List<String> onlineUserNicknames = message.clientListMessage.online;
                controller.refreshUsersList(onlineUserNicknames);
                break;
            default:
                throw new IllegalArgumentException("Unknown command type: " + message.command);
        }

    }

    private void processPublicMessage(Message message) {
        PublicMessage publicMessage = message.publicMessage;
        String from = publicMessage.from;
        String msg = publicMessage.message;
        if (from != null) {
            chatWindow.getItems().addAll(String.format("%s: %s%n", from, msg));
        } else {
            chatWindow.getItems().addAll(String.format("%s%n", msg));
        }
    }

    private void processPrivateMessage(Message message) {
        PrivateMessage privateMessage = message.privateMessage;
        String from = privateMessage.from;
        String msg = privateMessage.message;
        String msgToView = String.format("%s (private): %s%n", from, msg);
        chatWindow.getItems().addAll(msgToView);
    }

    private void processAuthOk(Message message) {
        controller.setNickName(message.authOkMessage.nickname);
        controller.showChatPanel();
    }


    @Override
    public void close() throws IOException {
        if (needStopServer) {
            sendMessage(Message.serverEndMessage());
        }
        network.close();
    }


}
