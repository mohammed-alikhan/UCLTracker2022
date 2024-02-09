import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class UCLTrackerApp {

    public static void main(String[] args) {
        loadAndInsertExcelData("D:\\IDEA Projects\\UCLTracker\\data\\Players.xlsx", "Players");
        loadAndInsertExcelData("D:\\IDEA Projects\\UCLTracker\\data\\Teams.xlsx", "Teams");
        loadAndInsertExcelData("D:\\IDEA Projects\\UCLTracker\\data\\Matches.xlsx", "Matches");
        loadAndInsertExcelData("D:\\IDEA Projects\\UCLTracker\\data\\Player_Statistics.xlsx", "Player_Statistics");
        loadAndInsertExcelData("D:\\IDEA Projects\\UCLTracker\\data\\Team_Statistics.xlsx", "Team_Statistics");
        loadAndInsertExcelData("D:\\IDEA Projects\\UCLTracker\\data\\Player_Belongs_to_Team.xlsx", "Player_Belongs_to_Team");

        createMainWindow();

    }

    private static void createMainWindow() {
        JFrame frame = new JFrame("UCL Tracker App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Title label
        JLabel titleLabel = new JLabel("UCL Tracker App", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        frame.add(titleLabel, gbc);

        // Description label
        JLabel descLabel = new JLabel("<html><p style=\"text-align:center;\">UCL Tracker is an application that allows users to view and analyze the performance of football players participating in the UEFA Champions League (UCL) 2021-22 season.</p></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Serif", Font.ITALIC, 14));
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 10, 10);
        frame.add(descLabel, gbc);

        // Buttons
        JButton[] playerButtons = new JButton[8];
        String[] buttonTexts = {
                "Query 1: List all players in the specific team",
                "Query 2: Top 5 goal scorers in the tournament",
                "Query 3: Players with more than 200 minutes played",
                "Query 4: Total yellow and red cards for each team",
                "Query 5: Top 5 teams with the highest win percentage",
                "Query 6: Teams who conceded the least number of goals",
                "Query 7: Players who received the least number of cards in the whole tournament",
                "Query 8: Player of the Tournament 21-22"
        };

        for (int i = 0; i < playerButtons.length; i++) {
            playerButtons[i] = new JButton(buttonTexts[i]);
            playerButtons[i].setFont(new Font("Arial", Font.PLAIN, 15));
            playerButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
            gbc.gridy = i + 2;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 30, 24, 30);
            frame.add(playerButtons[i], gbc);
        }


        JTable playerTable = new JTable();
        playerTable.setBackground(new Color(240, 240, 240));
        playerTable.setFont(new Font("Arial", Font.PLAIN, 12));
        playerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        playerTable.getTableHeader().setBackground(new Color(59, 89, 182));
        playerTable.getTableHeader().setForeground(Color.WHITE);
        playerTable.setRowHeight(26);


        playerButtons[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String teamName = JOptionPane.showInputDialog(frame, "Enter the team name:", "Team name input", JOptionPane.PLAIN_MESSAGE);
                if (teamName != null && !teamName.trim().isEmpty()) {
                    DBConnection dbConnection = new DBConnection();
                    try {
                        dbConnection.connect();

                        String query = "SELECT Players.Player_ID, Players.Player_Name FROM Players " +
                                "JOIN Player_Belongs_to_Team ON Players.Player_ID = Player_Belongs_to_Team.Player_ID " +
                                "JOIN Teams ON Player_Belongs_to_Team.Team_ID = Teams.Team_ID " +
                                "WHERE Teams.Team_Name = ?;";

                        PreparedStatement preparedStatement = dbConnection.prepareStatement(query);
                        preparedStatement.setString(1, teamName);
                        ResultSet resultSet = preparedStatement.executeQuery();

                        DefaultTableModel model = new DefaultTableModel(new Object[]{"Player ID", "Player Name"}, 0);
                        while (resultSet.next()) {
                            int playerId = resultSet.getInt("Player_ID");
                            String playerName = resultSet.getString("Player_Name");
                            model.addRow(new Object[]{playerId, playerName});
                        }
                        playerTable.setModel(model);
                        dbConnection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid team name.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        playerButtons[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT p.Player_Name, Total_Goals\n" +
                            "FROM Players as p\n" +
                            "JOIN Player_Statistics as ps ON p.Player_ID = ps.Player_ID\n" +
                            "ORDER BY Total_Goals DESC\n" +
                            "LIMIT 5;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Player_Name", "Total_Goals"}, 0);
                    while (resultSet.next()) {
                        int totalgoals = resultSet.getInt("Total_Goals");
                        String playerName = resultSet.getString("Player_Name");
                        model.addRow(new Object[]{totalgoals, playerName});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playerButtons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT Players.Player_Name, Player_Statistics.Total_Minutes_Played " +
                            "FROM Players " +
                            "JOIN Player_Statistics ON Players.Player_ID = Player_Statistics.Player_ID " +
                            "WHERE Player_Statistics.Total_Minutes_Played > 200;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Player Name", "Total Minutes Played"}, 0);
                    while (resultSet.next()) {
                        String playerName = resultSet.getString("Player_Name");
                        int totalMinutesPlayed = resultSet.getInt("Total_Minutes_Played");
                        model.addRow(new Object[]{playerName, totalMinutesPlayed});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playerButtons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT Teams.Team_Name, SUM(Player_Statistics.Total_Yellow_Cards) AS Total_Yellow_Cards, " +
                            "SUM(Player_Statistics.Total_Red_Cards) AS Total_Red_Cards " +
                            "FROM Teams " +
                            "JOIN Player_Belongs_to_Team ON Teams.Team_ID = Player_Belongs_to_Team.Team_ID " +
                            "JOIN Player_Statistics ON Player_Belongs_to_Team.Player_ID = Player_Statistics.Player_ID " +
                            "GROUP BY Teams.Team_Name;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Team Name", "Total Yellow Cards", "Total Red Cards"}, 0);
                    while (resultSet.next()) {
                        String teamName = resultSet.getString("Team_Name");
                        int totalYellowCards = resultSet.getInt("Total_Yellow_Cards");
                        int totalRedCards = resultSet.getInt("Total_Red_Cards");
                        model.addRow(new Object[]{teamName, totalYellowCards, totalRedCards});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playerButtons[4].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT Teams.Team_Name, (Team_Statistics.Wins / (Team_Statistics.Wins + Team_Statistics.Draws + Team_Statistics.Losses)) * 100 AS Win_Percentage " +
                            "FROM Teams " +
                            "JOIN Team_Statistics ON Teams.Team_ID = Team_Statistics.Team_ID " +
                            "ORDER BY Win_Percentage DESC " +
                            "LIMIT 5;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Team Name", "Win Percentage"}, 0);
                    while (resultSet.next()) {
                        String teamName = resultSet.getString("Team_Name");
                        double winPercentage = resultSet.getDouble("Win_Percentage");
                        String winPercentageFormatted = String.format("%.2f%%", winPercentage);
                        model.addRow(new Object[]{teamName, winPercentageFormatted});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playerButtons[5].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT Teams.Team_Name, Team_Statistics.Goals_Conceded " +
                            "FROM Teams " +
                            "JOIN Team_Statistics ON Teams.Team_ID = Team_Statistics.Team_ID " +
                            "ORDER BY Team_Statistics.Goals_Conceded ASC " +
                            "LIMIT 1;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Team Name", "Goals Conceded"}, 0);
                    while (resultSet.next()) {
                        String teamName = resultSet.getString("Team_Name");
                        int goalsConceded = resultSet.getInt("Goals_Conceded");
                        model.addRow(new Object[]{teamName, goalsConceded});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playerButtons[6].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT Players.Player_Name, (Player_Statistics.Total_Yellow_Cards + Player_Statistics.Total_Red_Cards) AS Combined_Cards " +
                            "FROM Players " +
                            "JOIN Player_Statistics ON Players.Player_ID = Player_Statistics.Player_ID " +
                            "ORDER BY Combined_Cards ASC;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Player Name"}, 0);
                    while (resultSet.next()) {
                        String playerName = resultSet.getString("Player_Name");
                        model.addRow(new Object[]{playerName});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playerButtons[7].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnection dbConnection = new DBConnection();
                try {
                    dbConnection.connect();

                    String query = "SELECT Players.Player_Name, (Player_Statistics.Total_Goals + Player_Statistics.Total_Assists) AS Goals_Assists, " +
                            "Player_Statistics.Total_Minutes_Played, (Player_Statistics.Total_Goals / Player_Statistics.Total_Minutes_Played) AS Goals_Per_Minute " +
                            "FROM Players " +
                            "JOIN Player_Statistics ON Players.Player_ID = Player_Statistics.Player_ID " +
                            "ORDER BY Goals_Assists DESC " +
                            "LIMIT 1;";

                    ResultSet resultSet = dbConnection.executeQuery(query);
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"Player Name", "Goals + Assists", "Total Minutes Played", "Goals per Minute"}, 0);
                    while (resultSet.next()) {
                        String playerName = resultSet.getString("Player_Name");
                        int goalsAssists = resultSet.getInt("Goals_Assists");
                        int totalMinutesPlayed = resultSet.getInt("Total_Minutes_Played");
                        double goalsPerMinute = resultSet.getDouble("Goals_Per_Minute");
                        model.addRow(new Object[]{playerName, goalsAssists, totalMinutesPlayed, goalsPerMinute});
                    }
                    playerTable.setModel(model);
                    dbConnection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(playerTable);
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.gridheight = 7;
        gbc.insets = new Insets(5, 5, 5, 10);
        frame.add(scrollPane, gbc);

        // Set background color
        frame.getContentPane().setBackground(new Color(224, 236, 255));
        frame.setVisible(true);
    }

    static class DBConnection {
        private Connection connection;
        private Statement statement;

        public void connect() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/ucl_tracker";
            String username = "root";
            String password = "Database12";

            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        }

        public ResultSet executeQuery(String query) throws SQLException {
            return statement.executeQuery(query);
        }

        public void close() throws SQLException {
            statement.close();
            connection.close();
        }

        public void executeNonQuery(String query) throws SQLException {
            statement.executeUpdate(query);
        }

        public Connection getConnection() {
            return connection;
        }

        public PreparedStatement prepareStatement(String query) throws SQLException {
            return connection.prepareStatement(query);
        }
    }
    private static void loadAndInsertExcelData(String filePath, String tableName) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {


            Sheet sheet = workbook.getSheetAt(0);

            DBConnection dbConnection = new DBConnection();
            dbConnection.connect();


            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }
                switch (tableName) {
                    case "Players":
                        int playerId = (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) ? (int) row.getCell(0).getNumericCellValue() : -1;
                        String playerName = (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.STRING) ? row.getCell(1).getStringCellValue() : null;
                        if (playerId != -1 && playerName != null) {
                            insertOrUpdatePlayers(dbConnection, playerId, playerName);
                        }
                        break;
                    case "Teams":
                        int teamId = (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) ? (int) row.getCell(0).getNumericCellValue() : -1;
                        String teamName = (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.STRING) ? row.getCell(1).getStringCellValue() : null;
                        if (teamId != -1 && teamName != null) {
                            insertOrUpdateTeams(dbConnection, teamId, teamName);
                        }
                        break;
                    case "Matches":
                        int matchId = (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) ? (int) row.getCell(0).getNumericCellValue() : -1;
                        String location = (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.STRING) ? row.getCell(1).getStringCellValue() : null;
                        if (matchId != -1 && location != null) {
                            insertOrUpdateMatches(dbConnection, matchId, location);
                        }
                        break;
                    case "Player_Statistics":
                        int playerId1 = (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) ? (int) row.getCell(0).getNumericCellValue() : -1;
                        int totalGoals = (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.NUMERIC) ? (int) row.getCell(1).getNumericCellValue() : -1;
                        int totalAssists = (row.getCell(2) != null && row.getCell(2).getCellType() == CellType.NUMERIC) ? (int) row.getCell(2).getNumericCellValue() : -1;
                        int totalMinutesPlayed = (row.getCell(3) != null && row.getCell(3).getCellType() == CellType.NUMERIC) ? (int) row.getCell(3).getNumericCellValue():-1;
                        int totalYellowCards = (row.getCell(4) != null && row.getCell(4).getCellType() == CellType.NUMERIC) ? (int) row.getCell(4).getNumericCellValue() : -1;
                        int totalRedCards = (row.getCell(5) != null && row.getCell(5).getCellType() == CellType.NUMERIC) ? (int) row.getCell(5).getNumericCellValue() : -1;
                        if (playerId1 != -1 && totalGoals != -1 && totalAssists != -1 && totalMinutesPlayed != -1 && totalYellowCards != -1 && totalRedCards != -1) {
                            insertOrUpdatePlayerStatistics(dbConnection, playerId1, totalGoals, totalAssists, totalMinutesPlayed, totalYellowCards, totalRedCards);
                        }
                        break;
                    case "Team_Statistics":
                        int teamId1 = (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) ? (int) row.getCell(0).getNumericCellValue() : -1;
                        int goalsScored = (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.NUMERIC) ? (int) row.getCell(1).getNumericCellValue() : -1;
                        int goalsConceded = (row.getCell(2) != null && row.getCell(2).getCellType() == CellType.NUMERIC) ? (int) row.getCell(2).getNumericCellValue() : -1;
                        int wins = (row.getCell(3) != null && row.getCell(3).getCellType() == CellType.NUMERIC) ? (int) row.getCell(3).getNumericCellValue() : -1;
                        int draws = (row.getCell(4) != null && row.getCell(4).getCellType() == CellType.NUMERIC) ? (int) row.getCell(4).getNumericCellValue() : -1;
                        int losses = (row.getCell(5) != null && row.getCell(5).getCellType() == CellType.NUMERIC) ? (int) row.getCell(5).getNumericCellValue() : -1;
                        if (teamId1 != -1 && goalsScored != -1 && goalsConceded != -1 && wins != -1 && draws != -1 && losses != -1) {
                            insertOrUpdateTeamStatistics(dbConnection, teamId1, goalsScored, goalsConceded, wins, draws, losses);
                        }
                        break;
                    case "Player_Belongs_to_Team":
                        int playerId2 = (row.getCell(0) != null && row.getCell(0).getCellType() == CellType.NUMERIC) ? (int) row.getCell(0).getNumericCellValue() : -1;
                        int teamId2 = (row.getCell(1) != null && row.getCell(1).getCellType() == CellType.NUMERIC) ? (int) row.getCell(1).getNumericCellValue() : -1;
                        if (playerId2 != -1 && teamId2 != -1) {
                            insertOrUpdatePlayerBelongsToTeam(dbConnection, playerId2, teamId2);
                        }
                        break;
                    default:
                        System.out.println("Invalid table name: " + tableName);
                }
            }
            dbConnection.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error reading the file: " + filePath);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error processing the file: " + filePath);
            e.printStackTrace();
        }
    }

    private static void insertOrUpdatePlayers(DBConnection dbConnection, int playerId, String playerName) throws SQLException {
        ResultSet resultSet = dbConnection.executeQuery("SELECT COUNT(*) FROM Players WHERE Player_ID = " + playerId);
        resultSet.next();
        int count = resultSet.getInt(1);

        if (count == 0) {
            String query = "INSERT INTO Players (Player_ID, Player_Name) VALUES (" + playerId + ", '" + playerName + "')";
            dbConnection.executeNonQuery(query);
        }
    }
    private static void insertOrUpdateTeams(DBConnection dbConnection, int teamId, String teamName) throws SQLException {
        ResultSet resultSet = dbConnection.executeQuery("SELECT COUNT(*) FROM Teams WHERE Team_ID = " + teamId);
        resultSet.next();
        int count = resultSet.getInt(1);

        if (count == 0) {
            String query = "INSERT INTO Teams (Team_ID, Team_Name) VALUES (" + teamId + ", '" + teamName + "')";
            dbConnection.executeNonQuery(query);
        }
    }
    private static void insertOrUpdateMatches(DBConnection dbConnection, int matchId, String location) throws SQLException {
        ResultSet resultSet = dbConnection.executeQuery("SELECT COUNT(*) FROM Matches WHERE Match_ID = " + matchId);
        resultSet.next();
        int count = resultSet.getInt(1);

        if (count == 0) {
            String query = "INSERT INTO Matches (Match_ID, Location) VALUES (" + matchId + ", '" + location + "')";
            dbConnection.executeNonQuery(query);
        }
    }

    private static void insertOrUpdatePlayerStatistics(DBConnection dbConnection, int playerId1, int totalGoals, int totalAssists, int totalMinutesPlayed, int totalYellowCards, int totalRedCards) throws SQLException {
        ResultSet resultSet = dbConnection.executeQuery("SELECT COUNT(*) FROM Player_Statistics WHERE Player_ID = " + playerId1);
        resultSet.next();
        int count = resultSet.getInt(1);

        if (count == 0) {
            String query = "INSERT INTO Player_Statistics (Player_ID, Total_Goals, Total_Assists, Total_Minutes_Played, Total_Yellow_Cards, Total_Red_Cards) VALUES (" + playerId1 + ", " + totalGoals + ", " + totalAssists + ", " + totalMinutesPlayed + ", " + totalYellowCards + ", " + totalRedCards + ")";
            dbConnection.executeNonQuery(query);
        }
    }

    private static void insertOrUpdateTeamStatistics(DBConnection dbConnection, int teamId1, int goalsScored, int goalsConceded, int wins, int draws, int losses) throws SQLException {
        ResultSet resultSet = dbConnection.executeQuery("SELECT COUNT(*) FROM Team_Statistics WHERE Team_ID = " + teamId1);
        resultSet.next();
        int count = resultSet.getInt(1);

        if (count == 0) {
            String query = "INSERT INTO Team_Statistics (Team_ID, Goals_Scored, Goals_Conceded, Wins, Draws, Losses) VALUES (" + teamId1 + ", " + goalsScored + ", " + goalsConceded + ", " + wins + ", " + draws + ", " + losses + ")";
            dbConnection.executeNonQuery(query);
        }
    }

    private static void insertOrUpdatePlayerBelongsToTeam(DBConnection dbConnection, int playerId2, int teamId2) throws SQLException {
        ResultSet resultSet = dbConnection.executeQuery("SELECT COUNT(*) FROM Player_Belongs_to_Team WHERE Player_ID = " + playerId2 + " AND Team_ID = " + teamId2);
        resultSet.next();
        int count = resultSet.getInt(1);

        if (count == 0) {
            String query = "INSERT INTO Player_Belongs_to_Team (Player_ID, Team_ID) VALUES (" + playerId2 + ", " + teamId2 + ")";
            dbConnection.executeNonQuery(query);

            dbConnection.getConnection().commit();
        }
    }
}