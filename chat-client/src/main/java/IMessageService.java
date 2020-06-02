import chat.Message;

import java.io.Closeable;
import java.io.IOException;

public interface IMessageService extends Closeable {

    void sendMessage(Message message);

    void readMessage(Message message);

    @Override
    default void close() throws IOException {
    }
}
