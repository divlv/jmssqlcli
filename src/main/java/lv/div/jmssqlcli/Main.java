package lv.div.jmssqlcli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    private static final Log log = LogFactory.getLog(Main.class);

    public static final String APP_NAME = "(c)2022 MSSQL [very] Simple CLI. v.GIT_VERSION";
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

        log.debug("JMSSQLCLI Started");

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
            !cmd.hasOption("i")
        ) {
            log.debug("Wrong parameters requested. Showing help screen...");
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

        log.info("### " + APP_NAME + " -=- Start time: " + new Date() + " -=- Work mode: " + workMode);

        String connectionUrl =
            "jdbc:sqlserver://" + dbServer + ";database=" + dbName + ";user=" + dbLogin + ";password=" + dbPassword;

//        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
        Connection con = DriverManager.getConnection(connectionUrl);
        Statement stmt = con.createStatement();

            String SQL = loadFileIntoString(inputFile, preview);

            log.info("### Loaded " + SQL.length() + " bytes from " + inputFile);

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
                log.info(tableGenerator.generateTable(headersList, rowsList));
            } else {
                stmt.executeUpdate(SQL);
                log.info("### Update executed");
            }

            displayEndTime();
//        } catch (Exception e) {
//            log.error("SQL Exception thrown: ", e);
//            displayEndTime();
//        }

    }

    private static void displayEndTime() {
        log.info("### End time: " + new Date());
    }

    /**
     * Load SQL data from file to string
     *
     * @param filePath file path to load SQL from
     * @param preview  should we display preview or not
     * @return file-as-a-string
     */
    private static String loadFileIntoString(String filePath, boolean preview) {

        String content = "ERROR"; // Should rise SQL error, and that's correct!
        try {

            final File file = new File(filePath);
            log.info("### FileSize to read from: " + file.length());
            content = FileUtils.readFileToString(file, "UTF-8");

            if (preview) {
                if (content.length() > SQL_PREVIEW_LENGTH) {
                    log.info("### Preview: " + content.substring(0, SQL_PREVIEW_LENGTH - 1));
                } else {
                    log.info("### Preview: " + content);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return content;
        }

    }
}
