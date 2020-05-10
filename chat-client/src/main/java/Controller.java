import clientUI.EchoMessageService;
import clientUI.TextMessageService;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Menu menuBar;
    public MenuItem menuReconnect;
    public MenuItem menuClose;
    public Button sendButton;
    public TextArea sendWindow;
    public TextArea readWindow;
    public javafx.scene.control.ContextMenu contextMenu;
    public MenuItem contextCopy;
    public MenuItem contextClear;

    private TextMessageService messageService;

    public void ConnectChat(ActionEvent actionEvent) {
        // TODO: 27.04.2020

    }

    public void closeChat(ActionEvent actionEvent) {
        System.exit(1);
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        sendMessage();
    }

    public void keyListener(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
        }
    }

    public void sendMessage() throws IOException {
        String message = sendWindow.getText();
        messageService.sendMessage(message);
        sendWindow.clear();
    }

    public void copyWindow(ActionEvent actionEvent) {
        readWindow.copy();
    }

    public void clearWindow(ActionEvent actionEvent) {
        readWindow.clear();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.messageService = new EchoMessageService(readWindow,"localhost", 8189);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}