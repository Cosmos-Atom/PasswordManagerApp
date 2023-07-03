import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PasswordManagerApp extends JFrame {
    private List<Password> passwordList;
    private JPanel cards;
    private CardLayout cardLayout;
    private JTable passwordTable;
    private DefaultTableModel tableModel;
    private String masterKey;
    private boolean authenticated;

    private static final String CSV_FILE = "passwords.csv";
    private static final String[] CSV_HEADER = {"Website", "Username", "Password"};

    private JTextField websiteField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public PasswordManagerApp() {
        passwordList = new ArrayList<>();
        masterKey = "kuks"; // Replace with your master key
        authenticated = false;

        // Create UI components
        JPanel mainPanel = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        JPanel masterKeyPanel = createMasterKeyPanel();
        JPanel homePanel = createHomePanel();
        JPanel addPanel = createAddPasswordPanel();
        JPanel removePanel = createRemovePasswordPanel();
        JPanel showPanel = createShowPasswordsPanel();
        JPanel generatePanel = createGeneratePasswordPanel();

        cards.add(masterKeyPanel, "masterKeyPanel");
        cards.add(homePanel, "homePanel");
        cards.add(addPanel, "addPanel");
        cards.add(removePanel, "removePanel");
        cards.add(showPanel, "showPanel");
        cards.add(generatePanel, "generatePanel");

        mainPanel.add(cards, BorderLayout.CENTER);

        add(mainPanel);
        setTitle("Password Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);
    }

    private JPanel createMasterKeyPanel() {
        JPanel masterKeyPanel = new JPanel(new GridLayout(2, 1));

        JLabel masterKeyLabel = new JLabel("Master Key:");
        JPasswordField masterKeyField = new JPasswordField();
        masterKeyField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] input = masterKeyField.getPassword();
                String enteredMasterKey = new String(input);
                if (enteredMasterKey.equals(masterKey)) {
                    authenticated = true;
                    loadPasswordsFromCSV();
                    cardLayout.show(cards, "homePanel");
                } else {
                    JOptionPane.showMessageDialog(masterKeyPanel, "Invalid master key!", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
                masterKeyField.setText("");
            }
        });

        masterKeyPanel.add(masterKeyLabel);
        masterKeyPanel.add(masterKeyField);

        return masterKeyPanel;
    }

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new GridLayout(3, 1));

        JButton addPasswordButton = new JButton("Add Password");
        addPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "addPanel");
            }
        });


        JButton showPasswordsButton = new JButton("Show Passwords");
        showPasswordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "showPanel");
                showPasswords();
            }
        });

        JButton generatePasswordButton = new JButton("Generate Password");
        generatePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "generatePanel");
            }
        });

        homePanel.add(addPasswordButton);
        homePanel.add(showPasswordsButton);
        homePanel.add(generatePasswordButton);

        return homePanel;
    }

    private JPanel createAddPasswordPanel() {
        JPanel addPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        JLabel websiteLabel = new JLabel("Website:");
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        websiteField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        formPanel.add(websiteLabel);
        formPanel.add(websiteField);
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        JButton addButton = new JButton("Add Password");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String website = websiteField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                Password newPassword = new Password(website, username, password);
                passwordList.add(newPassword);
                tableModel.addRow(new Object[]{website, username, password});
                savePasswordsToCSV();
                JOptionPane.showMessageDialog(addPanel, "Password added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            }
        });

        // Create a back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
                cardLayout.show(cards, "homePanel");
            }
        });

        // Add the form and buttons to the addPanel
        addPanel.add(formPanel, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.SOUTH);
        addPanel.add(backButton, BorderLayout.NORTH);

        return addPanel;
    }

    private void clearFields() {
        websiteField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    private void loadPasswordsFromCSV() {
        BufferedReader reader = null;
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    String website = data[0];
                    String username = data[1];
                    String password = data[2];
                    Password storedPassword = new Password(website, username, password);
                    passwordList.add(storedPassword);
                    tableModel.addRow(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void savePasswordsToCSV() {
        BufferedWriter writer = null;
        try {
            File file = new File(CSV_FILE);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.join(",", CSV_HEADER));
            writer.newLine();
            for (Password password : passwordList) {
                writer.write(password.toCSVString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private JPanel createRemovePasswordPanel() {
        JPanel removePanel = new JPanel(new BorderLayout());

        JLabel passwordsLabel = new JLabel("Stored Passwords:");

        tableModel = new DefaultTableModel(CSV_HEADER, 0);
        passwordTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(passwordTable);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = passwordTable.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(removePanel, "Are you sure you want to remove this password?", "Confirm Password Removal", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        tableModel.removeRow(selectedRow);
                        passwordList.remove(selectedRow);
                        savePasswordsToCSV();
                        JOptionPane.showMessageDialog(removePanel, "Password removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(removePanel, "Please select a password to remove!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removePanel.add(passwordsLabel, BorderLayout.NORTH);
        removePanel.add(scrollPane, BorderLayout.CENTER);
        removePanel.add(removeButton, BorderLayout.SOUTH);

        // Create a back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "homePanel");
            }
        });

        removePanel.add(backButton, BorderLayout.NORTH);

        return removePanel;
    }

    private JPanel createShowPasswordsPanel() {
        JPanel showPanel = new JPanel(new BorderLayout());

        JLabel passwordsLabel = new JLabel("Stored Passwords:");

        tableModel = new DefaultTableModel(CSV_HEADER, 0);
        passwordTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(passwordTable);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = passwordTable.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(showPanel, "Are you sure you want to remove this password?", "Confirm Password Removal", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        tableModel.removeRow(selectedRow);
                        passwordList.remove(selectedRow);
                        savePasswordsToCSV();
                        JOptionPane.showMessageDialog(showPanel, "Password removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(showPanel, "Please select a password to remove!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        showPanel.add(passwordsLabel, BorderLayout.NORTH);
        showPanel.add(scrollPane, BorderLayout.CENTER);
        showPanel.add(removeButton, BorderLayout.SOUTH);

        // Create a back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "homePanel");
            }
        });

        showPanel.add(backButton, BorderLayout.NORTH);

        return showPanel;
    }

   private void showPasswords() {
    // Clear the existing table data
    tableModel.setRowCount(0);

    // Reload the passwords from the CSV file
    loadPasswordsFromCSV();

    // Populate the table with the updated data
    for (Password password : passwordList) {
        Object[] rowData = { password.getWebsite(), password.getUsername(), password.getPassword() };
        tableModel.addRow(rowData);
    }
}



    private JPanel createGeneratePasswordPanel() {
        JPanel generatePanel = new JPanel(new GridLayout(5, 1));

        JLabel websiteLabel = new JLabel("Website:");
        JTextField generateWebsiteField = new JTextField();

        JLabel usernameLabel = new JLabel("Username:");
        JTextField generateUsernameField = new JTextField();

        JLabel generatedPasswordLabel = new JLabel("Generated Password:");
        JTextField generatedPasswordField = new JTextField();

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String website = generateWebsiteField.getText();
                String username = generateUsernameField.getText();
                String password = generatePassword(10);
                generatedPasswordField.setText(password);
                saveGeneratedPasswordToCSV(website, username, password);
            }
        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "homePanel");
            }
        });

        generatePanel.add(websiteLabel);
        generatePanel.add(generateWebsiteField);
        generatePanel.add(usernameLabel);
        generatePanel.add(generateUsernameField);
        generatePanel.add(generateButton);
        generatePanel.add(generatedPasswordLabel);
        generatePanel.add(generatedPasswordField);
        generatePanel.add(backButton);

        return generatePanel;
    }

    private String generatePassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }

    private void saveGeneratedPasswordToCSV(String website, String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
            writer.write(website + "," + username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PasswordManagerApp();
            }
        });
    }
}

class Password {
    private String website;
    private String username;
    private String password;

    public Password(String website, String username, String password) {
        this.website = website;
        this.username = username;
        this.password = password;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String toCSVString() {
        return website + "," + username + "," + password;
    }
}
