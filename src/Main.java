import java.sql.*;
import java.util.Scanner;

public class Main implements CommandConst{
    private static Connection connection;
    private static Statement statement;

    public static void main(String[] args) {
        try {
            connect();
            createTable();
            clearTable();
            preparedStatementBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String recive = scanner.nextLine();
                        if (recive.startsWith(END)){
                            break;
                        }
                        if (recive.startsWith(PRICE)) {
                            String[] strings = recive.split(" ");
                            findPrice(strings[1]);
                        }
                        else if (recive.startsWith(CHANGE_PRICE)){
                            String[] strings = recive.split(" ");
                            changePrice(strings[1], Integer.valueOf(strings[2]));
                        }
                        else if (recive.startsWith(ITEMS_AT_PRICE)){
                            String[] strings = recive.split(" ");
                            itemsAtPrices(Integer.valueOf(strings[1]), Integer.valueOf(strings[2]));
                        }
                        else
                            System.out.println("No such commands");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e){
                    System.out.println("Wrong command");
                }
            }
        }).start();
    }

    public static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:jcp12_db.db");
        statement = connection.createStatement();
    }

    public static void createTable() throws SQLException{
        statement.execute("CREATE TABLE IF NOT EXISTS goods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "prodid STRING, " +
                "title STRING, " +
                "cost INTEGER);");
    }

    public static void dropTable() throws SQLException {
        statement.execute("DROP TABLE goods");
    }

    public static void clearTable() throws SQLException{
        statement.execute("DELETE FROM goods");
    }

    public static void preparedStatementBatch() throws SQLException{
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement("INSERT INTO goods(prodid, title, cost) VALUES(?, ?, ?)");
        for (int i = 0; i < 10000; i++) {
            ps.setString(1, "id_" + i);
            ps.setString(2, "good" + i);
            ps.setInt(3, i * 10);
            ps.executeUpdate();
        }
        connection.commit();
    }

    public static void findPrice(String name) throws SQLException{
        PreparedStatement ps = connection.prepareStatement("SELECT title, cost FROM goods WHERE title = ?");
        ps.setString(1, name);
        ResultSet resultSet = ps.executeQuery();
        if (resultSet.next()) {
            do {
                System.out.println(resultSet.getString("title") + " " + resultSet.getInt("cost"));
            } while (resultSet.next());
        }
        else {
            System.out.println("There is no such item");
        }
    }

    private static void changePrice(String name, int price) throws SQLException{
        PreparedStatement ps = connection.prepareStatement("UPDATE goods SET cost = ? WHERE title = ?");
        ps.setInt(1, price);
        ps.setString(2, name);
        int count = ps.executeUpdate();
        boolean result = false;
        if (count > 0)
            result = true;
        System.out.println("Price has been changed: " + result);
    }

    private static void itemsAtPrices(int min, int max) throws SQLException{
        PreparedStatement ps = connection.prepareStatement(
                "SELECT title, cost FROM goods WHERE cost > ? AND cost < ?");
        ps.setInt(1, min);
        ps.setInt(2, max);
        ResultSet resultSet = ps.executeQuery();
        if (resultSet.next()) {
            do {
                System.out.println(resultSet.getString("title") + " " + resultSet.getInt("cost"));
            } while (resultSet.next());
        }
        else {
            System.out.println("There is no such item");
        }
    }
}