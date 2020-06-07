package server.auth;

import com.sun.istack.internal.Nullable;

public interface AuthService {

    void start();
    void stop();

    @Nullable
    boolean sighUpLoginPass(String login, String password);

    @Nullable
    String getNickByLoginPass(String login, String password);
}
