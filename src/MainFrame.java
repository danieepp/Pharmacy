import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.Comparator;

public class MainFrame extends JFrame {
    private PharmacyInventory inventory = new PharmacyInventory();
    private JPanel itemPanel;
    private JPanel editorPanel;
    private JTextField searchField;
    private JComboBox<String> searchType;
    private JComboBox<String> sortType;
    private String lastSortType = "";
    private boolean ascending = true;

    public MainFrame() {
        setTitle("Аптечен инвентар");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Look & Feel - Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchType = new JComboBox<>(new String[]{"Име", "Код"});
        sortType = new JComboBox<>(new String[]{"Сортирай по: ", "Име", "Код", "Количество", "Годност"});
        JButton searchBtn = createStyledButton("Търси");

        styleComboBox(searchType);
        styleComboBox(sortType);

        sortType.addActionListener(e -> {
            String selected = (String) sortType.getSelectedItem();
            if (selected == null || selected.equals("Сортирай по: ")) return;

            if (!selected.equals(lastSortType)) {
                ascending = true;
                lastSortType = selected;
            } else {
                ascending = !ascending;
            }

            switch (selected) {
                case "Име" -> sortMedicines(Comparator.comparing(Medicine::getName));
                case "Код" -> sortMedicines(Comparator.comparing(Medicine::getCode));
                case "Количество" -> sortMedicines(Comparator.comparingInt(Medicine::getQuantity));
                case "Годност" -> sortMedicines(Comparator.comparing(Medicine::getExpiryDate));
            }
        });

        topPanel.add(new JLabel("Търсене:"));
        topPanel.add(searchField);
        topPanel.add(searchType);
        topPanel.add(searchBtn);
        topPanel.add(sortType);
        add(topPanel, BorderLayout.NORTH);

        // Item panel
        itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(itemPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Editor panel
        editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
        editorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "Редакция",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));
        add(editorPanel, BorderLayout.EAST);
        editorPanel.setVisible(false);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem saveItem = new JMenuItem("Запази");
        JMenuItem loadItem = new JMenuItem("Зареди");
        JMenuItem addItem = new JMenuItem("Добави ново лекарство");
        JMenuItem removeExpiredItem = new JMenuItem("Премахни изтекли продукти");

        saveItem.addActionListener(e -> FileManager.save("inventory.txt", inventory.getMedicines()));
        loadItem.addActionListener(e -> {
            inventory.getMedicines().clear();
            inventory.getMedicines().addAll(FileManager.load("inventory.txt"));
            refreshItems();
        });
        addItem.addActionListener(e -> showEditor(null));
        removeExpiredItem.addActionListener(e -> removeExpiredMedicines());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.add(addItem);
        fileMenu.add(removeExpiredItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        searchBtn.addActionListener(e -> refreshItems());
        refreshItems();
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Arial", Font.PLAIN, 14));
        box.setBackground(new Color(230, 230, 250));
        box.setFocusable(false);
    }

    private void removeExpiredMedicines() {
        StringBuilder sb = new StringBuilder("Ще бъдат премахнати следните продукти:\n");
        GenericLinkedList<Medicine> expiredList = new GenericLinkedList<>();
        for (int i = 0; i < inventory.getMedicines().size(); i++) {
            Medicine m = inventory.getMedicines().get(i);
            if (m.getExpiryDate().isBefore(LocalDate.now())) {
                sb.append("- ").append(m.getName()).append(" (Код: ").append(m.getCode()).append(")\n");
                expiredList.add(m);
            }
        }

        if (expiredList.size() == 0) {
            JOptionPane.showMessageDialog(this, "Няма изтекли продукти.");
            return;
        }

        JCheckBox confirm = new JCheckBox("Сигурен съм, че искам да ги премахна.");
        Object[] message = {sb.toString(), confirm};
        int option = JOptionPane.showConfirmDialog(this, message, "Премахване на изтекли продукти",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.OK_OPTION && confirm.isSelected()) {
            for (int i = 0; i < expiredList.size(); i++) {
                Medicine m = expiredList.get(i);
                inventory.getMedicines().removeIf(x -> x == m);
            }
            refreshItems();
        }
    }

    private void sortMedicines(Comparator<Medicine> comp) {
        int n = inventory.getMedicines().size();
        GenericLinkedList<Medicine> list = inventory.getMedicines();
        for (int i = 0; i < n - 1; i++) {
            int target = i;
            for (int j = i + 1; j < n; j++) {
                Medicine a = list.get(j);
                Medicine b = list.get(target);
                if (ascending ? comp.compare(a, b) < 0 : comp.compare(a, b) > 0) {
                    target = j;
                }
            }
            if (target != i) {
                Medicine temp = list.get(i);
                list.set(i, list.get(target));
                list.set(target, temp);
            }
        }
        refreshItems();
    }

    private void refreshItems() {
        itemPanel.removeAll();
        String query = searchField.getText().toLowerCase();
        String mode = (String) searchType.getSelectedItem();

        for (int i = 0; i < inventory.getMedicines().size(); i++) {
            Medicine m = inventory.getMedicines().get(i);
            boolean show = query.isEmpty()
                    || (mode.equals("Име") && m.getName().toLowerCase().contains(query))
                    || (mode.equals("Код") && m.getCode().toLowerCase().contains(query));

            if (show) {
                JButton btn = new JButton(String.format("%-20s | Код: %-10s | Кол: %-3d | Годност: %s",
                        m.getName(), m.getCode(), m.getQuantity(), m.getExpiryDate()));
                styleItemButton(btn, m);
                btn.setToolTipText("Промяна на продукта");
                btn.setComponentPopupMenu(createPopupMenu(m));
                itemPanel.add(btn);
                itemPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        itemPanel.revalidate();
        itemPanel.repaint();
    }

    private void styleItemButton(JButton btn, Medicine m) {
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Monospaced", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(900, 40));

        if (m.getExpiryDate().isBefore(LocalDate.now())) {
            btn.setBackground(new Color(255, 102, 102));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(204, 0, 0));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(255, 102, 102));
                }
            });
        } else {
            btn.setBackground(new Color(245, 245, 245));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(101, 169, 191));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(245, 245, 245));
                }
            });
        }
    }

    private JPopupMenu createPopupMenu(Medicine m) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem addQty = new JMenuItem("Добави бройки");
        JMenuItem edit = new JMenuItem("Редактирай");
        JMenuItem delete = new JMenuItem("Изтрий");

        addQty.addActionListener(e -> {
            String input = JOptionPane.showInputDialog("Колко бройки да добавя?");
            try {
                int qty = Integer.parseInt(input);
                m.setQuantity(m.getQuantity() + qty);
                refreshItems();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Невалидна стойност.");
            }
        });

        edit.addActionListener(e -> showEditor(m));

        delete.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Ще изтриете следния продукт:\n");
            sb.append("- ").append(m.getName()).append(" (Код: ").append(m.getCode())
                    .append(", Кол: ").append(m.getQuantity())
                    .append(", Годност: ").append(m.getExpiryDate()).append(")\n");

            JCheckBox confirm = new JCheckBox("Сигурен съм, че искам да го премахна.");
            Object[] message = {sb.toString(), confirm};
            int option = JOptionPane.showConfirmDialog(this, message, "Потвърждение за изтриване",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (option == JOptionPane.OK_OPTION && confirm.isSelected()) {
                inventory.getMedicines().removeIf(x -> x == m);
                refreshItems();
            }
        });

        menu.add(addQty);
        menu.add(edit);
        menu.add(delete);
        return menu;
    }

    private void showEditor(Medicine m) {
        editorPanel.removeAll();

        JTextField nameField = new JTextField(m != null ? m.getName() : "");
        JTextField codeField = new JTextField(m != null ? m.getCode() : "");
        JTextField qtyField = new JTextField(m != null ? String.valueOf(m.getQuantity()) : "");
        JTextField dateField = new JTextField(m != null ? m.getExpiryDate().toString() : "YYYY-MM-DD");

        editorPanel.add(new JLabel("Име:"));
        editorPanel.add(nameField);
        editorPanel.add(new JLabel("Код:"));
        editorPanel.add(codeField);
        editorPanel.add(new JLabel("Количество:"));
        editorPanel.add(qtyField);
        editorPanel.add(new JLabel("Срок на годност:"));
        editorPanel.add(dateField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveBtn = createStyledButton("Запази");
        JButton cancelBtn = createStyledButton("Отхвърли");

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String code = codeField.getText();
                int qty = Integer.parseInt(qtyField.getText());
                LocalDate date = LocalDate.parse(dateField.getText());

                if (m != null) {
                    m.setName(name);
                    m.setCode(code);
                    m.setQuantity(qty);
                    m.setExpiryDate(date);
                } else {
                    inventory.addMedicine(new Medicine(name, code, qty, date));
                }
                refreshItems();
                editorPanel.setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Грешка при запис: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> editorPanel.setVisible(false));

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        editorPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        editorPanel.add(buttonPanel);
        editorPanel.setVisible(true);
        editorPanel.revalidate();
        editorPanel.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setBackground(new Color(230, 230, 250));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
