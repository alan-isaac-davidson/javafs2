package java_fs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Todo extends JFrame {
    private static final long serialVersionUID = 1L;

    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private JTextField taskInputField;
    private JButton addTaskButton, deleteTaskButton, markCompletedButton;

    public Todo() {
        setTitle("To-Do List");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(taskList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        taskInputField = new JTextField();
        inputPanel.add(taskInputField, BorderLayout.CENTER);

        addTaskButton = new JButton("Add Task");
        inputPanel.add(addTaskButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        markCompletedButton = new JButton("Mark Completed");
        deleteTaskButton = new JButton("Delete Task");
        buttonPanel.add(markCompletedButton);
        buttonPanel.add(deleteTaskButton);

        add(buttonPanel, BorderLayout.SOUTH);

        loadTasksFromDatabase();

        addTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        deleteTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });

        markCompletedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markTaskAsCompleted();
            }
        });
    }

    private void loadTasksFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT task_name FROM tasks WHERE is_completed = FALSE");

            while (rs.next()) {
                taskListModel.addElement(rs.getString("task_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTask() {
        String task = taskInputField.getText();
        if (task.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task name cannot be empty.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO tasks (task_name) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, task);
                stmt.executeUpdate();

                taskListModel.addElement(task);
                taskInputField.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String task = taskListModel.getElementAt(selectedIndex);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM tasks WHERE task_name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, task);
                    stmt.executeUpdate();

                    taskListModel.remove(selectedIndex);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void markTaskAsCompleted() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String task = taskListModel.getElementAt(selectedIndex);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE tasks SET is_completed = TRUE WHERE task_name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, task);
                    stmt.executeUpdate();

                    taskListModel.remove(selectedIndex);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Todo app = new Todo();
                app.setVisible(true);
            }
        });
    }
}
