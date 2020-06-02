import chat.Message;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
    public ListView<String> onlineClient;

    //Меню авторизации
    public VBox authPanel;
    public TextField loginField;
    public TextField nameField;
    public PasswordField passField;

    //Интерфейс
    private IMessageService iMessageService;

    public static final String ALL_ITEM = "All";
    private String nickName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.iMessageService = new MessageService(this, true);
            chatSize();
        } catch (IOException e) {
            error(e);
        }
    }

    public void error(Exception e){
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setHeaderText("Server error");
//        alert.setContentText(exception.getMessage());
//        alert.show();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("oops! Something went wrong!");
        alert.setHeaderText(e.getMessage());

        VBox dialogPaneContent = new VBox();

        Label label = new Label("Stack Trace:");

        String stackTrace = ExceptionUtils.getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);

        dialogPaneContent.getChildren().addAll(label, textArea);

        // Set content for Dialog Pane
        alert.getDialogPane().setContent(dialogPaneContent);
        alert.setResizable(true);
        alert.showAndWait();

        e.printStackTrace();

    }

    public void showAuthError(String errorMsg) {
        if (authPanel.isVisible()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Authentication is failed");
            alert.setContentText(errorMsg);
            alert.showAndWait();
        }
    }

    public void refreshUsersList(List<String> onlineUserNicknames) {
        onlineUserNicknames.add(ALL_ITEM);
        onlineClient.setItems(FXCollections.observableArrayList(onlineUserNicknames));
    }

    public void setNickName(String nickname) {
        this.nickName = nickName;
        refreshWindowTitle(nickName);
    }

    private void refreshWindowTitle(String nickName) {
        Stage stage = (Stage) chatPanel.getScene().getWindow();
        stage.setTitle(nickName);
    }

    public void showChatPanel() {
        authPanel.setVisible(false);
        chatPanel.setVisible(true);
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
        if (StringUtils.isNoneBlank(message)){
            chatWindow.getItems().addAll("Я: " + message);
            Message msg = buildMessage(message);
            iMessageService.sendMessage(msg);
            writeWindow.clear();
        }
    }

    private Message buildMessage(String message) {
        String selectedNickname = onlineClient.getSelectionModel().getSelectedItem();
        if(selectedNickname != null && !selectedNickname.equals(ALL_ITEM)){
            return Message.createPrivate(nickName, selectedNickname, message);
        }
        return Message.createPublic(nickName,message);
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
        iMessageService.sendMessage(Message.createAuth(login,password));
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