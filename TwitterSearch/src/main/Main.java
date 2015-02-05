package main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.postgresql.util.PSQLException;

public class Main {

    private static final String FILEPATH = "C:\\Users\\Matheus\\git\\superstars\\";
    private static final String[] BANDAS = { "suricato", "jamz", "luan", "malta" };
    private static final String[] MONTHS_PT = { "jan", "fev", "mar", "abr", "jun", "jul", "ago", "set", "out", "nov", "dez" };
    private static final String[] MONTHS_EN = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

    private static HashMap<String, Integer> months = new HashMap<String, Integer>();

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/superstars";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "hackfest";
    private static final String DB_TABLE = "tweets";

    private static Connection connectionDB;
    private static Statement statementDB;

    public static void main(final String[] args) throws IOException, SQLException, ClassNotFoundException {

        createMonths();

        startDB(DB_URL, DB_USER, DB_PASSWORD);

        int rowsAdd = 0;

        for (String banda : BANDAS) {
            rowsAdd = addTweets(banda);
            System.out.println(String.format("%d linhas inseridas na banda %s", rowsAdd, banda));

            rowsAdd = addTweetsWithSentimento(banda, "=)", 1);
            System.out.println(String.format("%d linhas =) inseridas na banda %s", rowsAdd, banda));

            rowsAdd = addTweetsWithSentimento(banda, "=(", 1);
            System.out.println(String.format("%d linhas =( inseridas na banda %s", rowsAdd, banda));
        }

        closeDB();

    }

    private static int addTweets(String banda) throws IOException, SQLException {
        File input = new File(FILEPATH + banda + ".html");
        Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

        Elements content = doc.select(".tweet.original-tweet");

        int count = 0;

        for (Element e : content) {

            String id = e.attr("data-tweet-id");
            DateTime dateTime = getDateTime(e.select(".tweet-timestamp").attr("title"));
            String text = e.select(".tweet-text").text();

            do {
                text = text.replace("'", "");
            } while (text.contains("'"));

            ResultSet resultSet = executeQuery(String.format("SELECT * FROM %s WHERE id_tweet = '%s'", DB_TABLE, id));

            if (resultSet != null && !resultSet.next()) {
                executeUpdate(String.format("INSERT INTO %s VALUES (DEFAULT, '%s', '%s', '%s', '%s', '', '', DEFAULT)", DB_TABLE, id, text, dateTime, banda));
                count++;
            }

        }

        return count;
    }

    private static int addTweetsWithSentimento(String banda, String filePos, Integer sentimento) throws IOException, SQLException {
        File input = new File(FILEPATH + banda + "_" + filePos + ".html");
        Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

        Elements content = doc.select(".tweet.original-tweet");

        int count = 0;

        for (Element e : content) {

            String id = e.attr("data-tweet-id");
            DateTime dateTime = getDateTime(e.select(".tweet-timestamp").attr("title"));

            if (dateTime == null) {
                continue;
            }

            String text = e.select(".tweet-text").text();

            do {
                text = text.replace("'", "");
            } while (text.contains("'"));

            ResultSet resultSet = executeQuery(String.format("SELECT * FROM %s WHERE id_tweet = '%s'", DB_TABLE, id));

            if (resultSet != null && !resultSet.next()) {
                executeUpdate(String.format("INSERT INTO %s VALUES (DEFAULT, '%s', '%s', '%s', '%s', '', '', %d)", DB_TABLE, id, text, dateTime, banda,
                        sentimento));
                count++;
            } else {
                executeUpdate(String.format("UPDATE %s SET sentimento = %d WHERE id_tweet = '%s'", DB_TABLE, sentimento, id));
            }

        }

        return count;
    }

    private static void executeUpdate(String query) throws SQLException {
        try {
            statementDB.executeUpdate(query);
        } catch (PSQLException psqlE) {
            System.err.println(psqlE.getMessage());
        }
    }

    private static ResultSet executeQuery(String query) throws SQLException {
        try {
            return statementDB.executeQuery(query);
        } catch (PSQLException psqlE) {
            System.err.println(psqlE.getMessage());
        }
        return null;
    }

    private static DateTime getDateTime(String date) {
        try {
            int year = Integer.parseInt(date.split(" ")[5]);
            int month = months.get(date.split(" ")[4].toLowerCase());
            int day = Integer.parseInt(date.split(" ")[3]);
            int hour = Integer.parseInt(date.split(" ")[0].split(":")[0]);
            int minute = Integer.parseInt(date.split(" ")[0].split(":")[1]);

            return new DateTime(year, month, day, hour, minute, 0, 0);

        } catch (Exception e) {
            System.err.println(date + " : " + e.getMessage());
        }

        return null;
    }

    private static void closeDB() throws SQLException {
        connectionDB.close();
    }

    private static void startDB(String url, String usuario, String senha) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");

        connectionDB = DriverManager.getConnection(url, usuario, senha);

        System.out.println("Conexão realizada com sucesso.");

        statementDB = connectionDB.createStatement();
    }

    private static void createMonths() {
        for (int i = 0; i < MONTHS_PT.length; i++) {
            months.put(MONTHS_PT[i], i + 1);
        }

        for (int i = 0; i < MONTHS_EN.length; i++) {
            months.put(MONTHS_EN[i], i + 1);
        }
    }
}
