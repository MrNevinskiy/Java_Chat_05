package clientUI;

import javafx.scene.control.ListView;
import java.io.IOException;

public class MessageService implements IMessageService {

    private Network network;
    private final ListView<String> chatWindow;

    public MessageService(ListView<String> chatWindow){
        this.chatWindow = chatWindow;
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
        chatWindow.getItems().addAll(message);
    }


}
