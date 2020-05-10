package clientUI;

import java.io.IOException;

public interface TextMessageService {
    void sendMessage(String message) throws IOException;
    void readMessage(String message) throws IOException;
}
