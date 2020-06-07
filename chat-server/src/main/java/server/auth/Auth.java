package server.auth;

import java.sql.*;

public class Auth implements AuthService {

    Connection connection;

    public static void dbDriver() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
    }

    @Override
    public void start() {
        System.out.println("start auth");
    }

    @Override
    public void stop() {
        System.out.println("stop auth");
    }

    @Override
    public boolean sighUpLoginPass(String login, String password) {
        System.out.println("Adding client " + login);
        connection = openConnection();
        String createUser =
                "INSERT INTO USERS (login, password, created_at) VALUES (\'"+ login + "','" + password
                        + "', datetime('now','localtime'));";
        try {
            try (Statement statement = connection.createStatement()) {
                System.out.println(createUser);
                if (isClientInDbByName(login)) {
                    System.out.println("Client exist!");
                    closeConnection(connection);
                    return false;
                } else {
                    statement.execute(createUser);
                    closeConnection(connection);
                    return true;
                }

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }


    @Override
    public String getNickByLoginPass(String login, String password) {
        String query = "SELECT * FROM users WHERE login = " + "'" + login + "'" + "and password =" + "'" + password + "'";
        connection = openConnection();
        try {
            try (
                    Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery(query)) {
                while (result.next()) {
                    if (result.getString("login").toLowerCase().equalsIgnoreCase(login) && result.getString("password").toLowerCase().equalsIgnoreCase(password) ) {
                        String userLogin = result.getString("login");
                        closeConnection(connection);
                        return userLogin;
                    }
                }

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("User does not exist.");
        closeConnection(connection);
        return null;
    }

    public boolean isClientInDbByName(String login) {

        connection = openConnection();
        String query = "SELECT login from USERS where login = '" + login + "'";
        try {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query);) {
                while (resultSet.next()) {
                    if (resultSet.getString("login").toLowerCase().equalsIgnoreCase(login)) {
                        System.out.println("isClientInDbByName " + true);
                        closeConnection(connection);
                        return true;
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        closeConnection(connection);
        return false;

    }

    public Connection openConnection() {

        System.out.println("Openning connection to DB...");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:Chat_DB");
            if (connection.isClosed()) {
                connection = DriverManager.getConnection(
                        "jdbc:sqlite:Chat_DB");
            } else {
                System.out.println("Connection open!");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }

    public boolean closeConnection(Connection connection) {

        System.out.println("Closing Connection ...");
        try {
            if (!connection.isClosed()) {
                connection.close();
                System.out.println("Connection is closed - " + connection.isClosed());
                return connection.isClosed();
            }
            System.out.println("WAS already Closed by try()");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

}
