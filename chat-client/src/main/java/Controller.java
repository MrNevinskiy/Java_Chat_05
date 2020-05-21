import clientUI.MessageService;
import clientUI.Network;
import clientUI.IMessageService;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Menu menuBar;
    public MenuItem menuReconnect;
    public MenuItem menuClose;
    public Button outMessageButton;
    public TextArea writeWindow;
    public ListView<String> chatWindow;
    public javafx.scene.control.ContextMenu contextMenu;
    public MenuItem contextCopy;
    public MenuItem contextClear;

    private IMessageService iMessageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.iMessageService = new MessageService(chatWindow);
            chatSize();
        } catch (IOException e) {
            error(e);
        }
    }

    public void ConnectChat(ActionEvent actionEvent) {
        // TODO: 27.04.2020
    }

    public void closeChat(ActionEvent actionEvent) throws IOException {
        iMessageService.sendMessage("/exit");
        System.exit(1);
    }

    public void copyWindow(ActionEvent actionEvent) {
        String copyText = chatWindow.getItems().toString();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(copyText);
        clipboard.setContent(content);
        //System.out.println(copyText);
    }

    public void clearWindow(ActionEvent actionEvent) {
        chatWindow.getItems().clear();
    }

    public void keyListener(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
        }
    }

    public void outMessageButton(ActionEvent actionEvent) throws IOException {
        sendMessage();
    }

    public void sendMessage() throws IOException {
        String message = writeWindow.getText();
        iMessageService.sendMessage(message);
        chatWindow.getItems().addAll("Я: " + message);
        writeWindow.clear();
    }

    public void error(Exception exception){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Server error");
        alert.setContentText(exception.getMessage());
        alert.show();
    }

    public void chatSize(){
        chatWindow.setCellFactory(param -> new ListCell<String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setMinWidth(100);
                    setMaxWidth(300);
                    setPrefWidth(300);
                    setWrapText(true);
                    setText(item);
                }
            }
        });
    }

    //Для коректного закрытия окна
    private javafx.event.EventHandler<WindowEvent> closeEventHandler = new javafx.event.EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            try {
                iMessageService.sendMessage("/exit");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    public javafx.event.EventHandler<WindowEvent> getCloseEventHandler() {return closeEventHandler;}
}