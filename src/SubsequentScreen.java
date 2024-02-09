import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubsequentScreen extends JFrame {

    private DBConnection dbConnection;

    public SubsequentScreen(DBConnection dbConnection) {
        // Store the database connection
        this.dbConnection = dbConnection;

        // Set the frame properties
        setTitle("UCL Tracker");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create a panel and add buttons
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        // Add as many buttons as needed for your queries
        JButton queryButton1 = new JButton("Query 1");
        panel.add(queryButton1);

        // Add action listener for queryButton1
        queryButton1.addActionListener(e -> {
            // Perform your first query using dbConnection
        });

        JButton queryButton2 = new JButton("Query 2");
        panel.add(queryButton2);

        // Add action listener for queryButton2
        queryButton2.addActionListener(e -> {
            // Perform your second query using dbConnection
        });

        // Add the panel to the frame
        add(panel);
    }

    public SubsequentScreen(UCLTrackerApp.DBConnection dbConnection) {
    }

    private void listPlayersInTeam(String teamName) {
        String query = "SELECT Players.Player_Name " +
                "FROM Players " +
                "INNER JOIN Player_Belongs_to_Team ON Players.Player_ID = Player_Belongs_to_Team.Player_ID " +
                "INNER JOIN Teams ON Player_Belongs_to_Team.Team_ID = Teams.Team_ID " +
                "WHERE Teams.Team_Name = ?";
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, teamName);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display player names
            StringBuilder playersList = new StringBuilder("Players in " + teamName + ":\n");
            while (resultSet.next()) {
                playersList.append(resultSet.getString("Player_Name")).append("\n");
            }

            JOptionPane.showMessageDialog(null, playersList.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }
}

