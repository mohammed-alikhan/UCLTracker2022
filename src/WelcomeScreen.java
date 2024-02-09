import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WelcomeScreen extends JFrame implements ActionListener {

    private JButton enterButton;
    private DBConnection dbConnection;

    public WelcomeScreen(DBConnection dbConnection) {
        // Store the database connection
        this.dbConnection = dbConnection;

        // Set the frame properties
        setTitle("Welcome");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create a panel and add a label
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to the UCL Tracker");
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(welcomeLabel, BorderLayout.CENTER);

        // Create a button and add it to the panel
        enterButton = new JButton("Enter");
        enterButton.addActionListener(this);
        panel.add(enterButton, BorderLayout.SOUTH);

        // Add the panel to the frame
        add(panel);
    }

    public WelcomeScreen(UCLTrackerApp.DBConnection dbConnection) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enterButton) {
            // Open the subsequent screen and close the welcome screen
            SubsequentScreen subsequentScreen = new SubsequentScreen(dbConnection);
            subsequentScreen.setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DBConnection dbConnection = new DBConnection("jdbc:mysql://localhost:3306/ucl_tracker", "root", "Database12");
            WelcomeScreen welcomeScreen = new WelcomeScreen(dbConnection);
            welcomeScreen.setVisible(true);
        });
    }

    public JButton getButton() {
        return enterButton;
    }
}