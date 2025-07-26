package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.naming.InitialContext;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import entity.Owner;
import entity.Pet;
import entity.Vaccine;
import service.PetService;
import service.UserService;

public class MainFrame extends JFrame {
    private PetService petService;
    private UserService userService;
    private JTextField searchField;
    private JTable petTable;
    private DefaultTableModel tableModel;
    private JDialog loginDialog;

    public MainFrame() {
        try {
            petService = (PetService) new InitialContext().lookup("java:global/pet-management/PetService");
            userService = (UserService) new InitialContext().lookup("java:global/pet-management/UserService");
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Pet Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        showLoginDialog();
    }

    private void showLoginDialog() {
        loginDialog = new JDialog(this, "Login", true);
        loginDialog.setSize(300, 200);
        loginDialog.setLayout(new GridLayout(3, 2, 10, 10));
        loginDialog.setLocationRelativeTo(this);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (userService.authenticate(username, password)) {
                loginDialog.dispose();
                initComponents();
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Invalid credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginDialog.add(new JLabel("Username:"));
        loginDialog.add(usernameField);
        loginDialog.add(new JLabel("Password:"));
        loginDialog.add(passwordField);
        loginDialog.add(new JLabel());
        loginDialog.add(loginButton);
        loginDialog.setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> updateTable());
        searchPanel.add(new JLabel("Search by name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Table
        String[] columns = {"Pet Name", "Age", "Last Vaccination", "Owner Name", "Owner Telephone"};
        tableModel = new DefaultTableModel(columns, 0);
        petTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(petTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editPetButton = new JButton("Edit Pet");
        JButton editOwnerButton = new JButton("Edit Owner");
        JButton editVaccineButton = new JButton("Edit Vaccination");
        editPetButton.addActionListener(e -> showEditPetDialog());
        editOwnerButton.addActionListener(e -> showEditOwnerDialog());
        editVaccineButton.addActionListener(e -> showEditVaccineDialog());
        buttonPanel.add(editPetButton);
        buttonPanel.add(editOwnerButton);
        buttonPanel.add(editVaccineButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        updateTable();
        setVisible(true);
    }

    private void updateTable() {
        String search = searchField.getText();
        List<Pet> pets = search.isEmpty() ? petService.findAll() : petService.findByName(search);
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Pet pet : pets) {
            String lastVaccination = pet.getVaccines().isEmpty() ? "None" : 
                pet.getVaccines().get(pet.getVaccines().size() - 1).getVaccinationDate().format(formatter);
            tableModel.addRow(new Object[]{
                pet.getName(),
                pet.getAge(),
                lastVaccination,
                pet.getOwner().getName(),
                pet.getOwner().getTelephone()
            });
        }
    }

    private void showEditPetDialog() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pet to edit", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pet pet = petService.findByName((String) tableModel.getValueAt(selectedRow, 0)).get(0);
        JDialog dialog = new JDialog(this, "Edit Pet", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField nameField = new JTextField(pet.getName());
        JTextField ageField = new JTextField(String.valueOf(pet.getAge()));
        JButton saveButton = new JButton("Save");

        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String ageStr = ageField.getText();

            if (name.length() > 50 || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Pet name must be 1-50 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 0) {
                    JOptionPane.showMessageDialog(dialog, "Age must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                pet.setName(name);
                pet.setAge(age);
                petService.save(pet);
                updateTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid age format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Age:"));
        dialog.add(ageField);
        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void showEditOwnerDialog() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pet to edit owner", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pet pet = petService.findByName((String) tableModel.getValueAt(selectedRow, 0)).get(0);
        Owner owner = pet.getOwner();
        JDialog dialog = new JDialog(this, "Edit Owner", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField nameField = new JTextField(owner.getName());
        JTextField telephoneField = new JTextField(owner.getTelephone());
        JButton saveButton = new JButton("Save");

        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String telephone = telephoneField.getText();

            if (name.length() > 100 || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Owner name must be 1-100 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!telephone.matches("\\+?[1-9]\\d{1,14}")) {
                JOptionPane.showMessageDialog(dialog, "Invalid telephone format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            owner.setName(name);
            owner.setTelephone(telephone);
            petService.save(pet);
            updateTable();
            dialog.dispose();
        });

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Telephone:"));
        dialog.add(telephoneField);
        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void showEditVaccineDialog() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pet to edit vaccination", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pet pet = petService.findByName((String) tableModel.getValueAt(selectedRow, 0)).get(0);
        JDialog dialog = new JDialog(this, "Edit Vaccination", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(2, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField dateField = new JTextField();
        JButton saveButton = new JButton("Save");

        saveButton.addActionListener(e -> {
            try {
                LocalDateTime vaccinationDate = LocalDateTime.parse(dateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                if (vaccinationDate.isAfter(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(dialog, "Vaccination date cannot be in the future", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Vaccine vaccine = new Vaccine();
                vaccine.setPet(pet);
                vaccine.setVaccinationDate(vaccinationDate);
                pet.getVaccines().add(vaccine);
                petService.save(pet);
                updateTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format (use yyyy-MM-dd HH:mm)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel("Vaccination Date (yyyy-MM-dd HH:mm):"));
        dialog.add(dateField);
        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}