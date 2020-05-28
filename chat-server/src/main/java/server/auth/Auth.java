package server.auth;

import com.google.common.collect.Lists;

import java.util.List;

public class Auth implements AuthService {

    private static class Entry {
        private String login;
        private String password;
        private String nick;

        public Entry(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

    private final List<Entry> entries = Lists.newArrayList(
            new Entry("User1", "User1", "User1"),
            new Entry("User2", "User2", "User2"),
            new Entry("User3", "User3", "User3"),
            new Entry("User4", "User4", "User4")
    );


    @Override
    public void start() {
        System.out.println("start auth");
    }

    @Override
    public void stop() {
        System.out.println("stop auth");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry entry : entries) {
            if(entry.login.equals(login) && entry.password.equals(pass)){
                return entry.nick;
            }
        }
        return null;
    }
}
