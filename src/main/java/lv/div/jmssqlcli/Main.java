package lv.div.jmssqlcli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static final String APP_NAME = "(c)2021 MSSQL [very] Simple CLI. v.GIT_VERSION";
    public static final String SELECT_MODE = "select";
    public static final String UPDATE_MODE = "update";
    public static final int SQL_PREVIEW_LENGTH = 128;
    private static String dbServer;
    private static String dbName;
    private static String dbLogin;
    private static String dbPassword;
    private static String workMode;
    private static String inputFile;
    private static boolean preview = false;

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption("s", "server", true, "Database server address, e.g. db.example.com:1433");
        options.addOption("d", "database", true, "Database name");
        options.addOption("l", "login", true, "Database login or full login like user@servername");
        options.addOption("r", "preview", false, "Preview first " + SQL_PREVIEW_LENGTH + " symbols from SQL script");
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
        preview = cmd.hasOption("r");

        System.out.println("### " + APP_NAME + " -=- Start time: " + new Date() + " -=- Work mode: " + workMode);

        String connectionUrl =
            "jdbc:sqlserver://" + dbServer + ";database=" + dbName + ";user=" + dbLogin + ";password=" + dbPassword;

        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {

            String SQL = loadFileIntoString(inputFile, preview);

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
    private static String loadFileIntoString(String filePath, boolean preview) {

        String content = "ERROR"; // Should rise SQL error, and that's correct!
        try {

            final File file = new File(filePath);
            System.out.println("### FileSize to read from: " + file.length());
            content = FileUtils.readFileToString(file, "UTF-8");

            if (content.length() > SQL_PREVIEW_LENGTH) {
                System.out.println("### Preview: " + content.substring(0, SQL_PREVIEW_LENGTH - 1));
            } else {
                System.out.println("### Preview: " + content);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return content;
        }

    }
}
