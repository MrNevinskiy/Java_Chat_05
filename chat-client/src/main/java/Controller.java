import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    //Бар меню
    public Menu menuBar;
    public MenuItem menuReconnect;
    public MenuItem menuClose;

    //Доп. функции чата
    public javafx.scene.control.ContextMenu contextMenu;
    public MenuItem contextCopy;
    public MenuItem contextClear;

    //Чат и поле ввода
    public VBox chatPanel;
    public ListView<String> chatWindow;
    public TextArea writeWindow;
    public Button outMessageButton;

    //Строка состояни клиентов
    public ListView onlineClient;

    //Меню авторизации
    public VBox authPanel;
    public TextField loginField;
    public PasswordField passField;

    //Интерфейс
    private IMessageService iMessageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.iMessageService = new MessageService(this, true);
            chatSize();
        } catch (IOException e) {
            error(e);
        }
    }

    public void error(Exception exception){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Server error");
        alert.setContentText(exception.getMessage());
        alert.show();
    }

    public void connectChat(ActionEvent actionEvent) {
        // TODO: 27.04.2020
    }

    public void closeChat(ActionEvent actionEvent) throws IOException {
        iMessageService.close();
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

    public void sendAuth(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passField.getText();
        iMessageService.sendMessage(String.format("/auth %s %s", login, password));
    }

    //Для коректного закрытия окна через крестик
    private final javafx.event.EventHandler<WindowEvent> closeEventHandler = new javafx.event.EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            try {
                iMessageService.close();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public javafx.event.EventHandler<WindowEvent> getCloseEventHandler() {
        return closeEventHandler;
    }

}