package lv.div.jmssqlcli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static final String APP_NAME = "(c)2021 MSSQL [very] Simple CLI";
    public static final String SELECT_MODE = "select";
    public static final String UPDATE_MODE = "update";
    private static String dbServer;
    private static String dbName;
    private static String dbLogin;
    private static String dbPassword;
    private static String workMode;
    private static String inputFile;

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption("s", "server", true, "Database server address, e.g. db.example.com:1433");
        options.addOption("d", "database", true, "Database name");
        options.addOption("l", "login", true, "Database login or full login like user@servername");
        options.addOption("p", "password", true, "Database password");
        options.addOption("m", "mode", true, "Worker mode [" + SELECT_MODE + " | " + UPDATE_MODE + "]");
        options.addOption("i", "inputfile", true, "Input file with SQL statement(s)");

        options.addOption("h", "help", false, "This help screen");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h") || !cmd.hasOption("s") || !cmd.hasOption("d") || !cmd.hasOption("l") ||
            !cmd.hasOption("p") || !cmd.hasOption("m") ||
            !cmd.hasOption("i")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(APP_NAME + ": java -jar jmssqlcli", options);
            return;
        } else {
            dbServer = cmd.getOptionValue("s");
            dbName = cmd.getOptionValue("d");
            dbLogin = cmd.getOptionValue("l");
            dbPassword = cmd.getOptionValue("p");
            workMode = cmd.getOptionValue("m");
            inputFile = cmd.getOptionValue("i");
        }

        System.out.println("### " + APP_NAME + " -=- Start time: " + new Date() + " -=- Work mode: " + workMode);

        String connectionUrl =
            "jdbc:sqlserver://" + dbServer + ";database=" + dbName + ";user=" + dbLogin + ";password=" + dbPassword;

        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {

            String SQL = loadFileIntoString(inputFile);

            System.out.println("### Loaded " + SQL.length() + " bytes from " + inputFile);

            if (SELECT_MODE.equalsIgnoreCase(workMode)) {
                ResultSet rs = stmt.executeQuery(SQL);
                final ResultSetMetaData metaData = rs.getMetaData();
                final int columnCount = metaData.getColumnCount();

                TableGenerator tableGenerator = new TableGenerator();

                List<String> headersList = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    headersList.add(metaData.getColumnName(i));
                }

                List<List<String>> rowsList = new ArrayList<>();

                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        row.add(rs.getString(columnName));
                    }
                    rowsList.add(row);
                }
                System.out.println(tableGenerator.generateTable(headersList, rowsList));
            } else {
                stmt.executeUpdate(SQL);
                System.out.println("### Update executed");
            }

            displayEndTime();
        } catch (SQLException e) {
            e.printStackTrace();
            displayEndTime();
        }

    }

    private static void displayEndTime() {
        System.out.println("### End time: " + new Date());
    }

    /**
     * @param filePath file path to load SQL from
     * @return string containing file data
     */
    private static String loadFileIntoString(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
