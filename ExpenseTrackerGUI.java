import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class ExpenseTrackerGUI extends JFrame {

    private static final String[][] CURRENCIES = {
        {"INR","₹","🇮🇳 INR"},{"USD","$","🇺🇸 USD"},
        {"EUR","€","🇪🇺 EUR"},{"GBP","£","🇬🇧 GBP"},
        {"JPY","¥","🇯🇵 JPY"},{"CAD","CA$","🇨🇦 CAD"},
        {"AUD","A$","🇦🇺 AUD"},{"CHF","Fr","🇨🇭 CHF"},
        {"AED","د.إ","🇦🇪 AED"},{"SGD","S$","🇸🇬 SGD"},
    };
    private String currentSymbol = "₹";
    private String currentCode   = "INR";

    private final ExpenseManager service = new ExpenseManager();    private final UserManager    userMgr = new UserManager();
    private final BackgroundSaver saver;
    private String currentUser;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nameField, amountField, categoryField, searchField;
    private JLabel totalLabel, currencyLabel, welcomeLabel;

    private final Deque<Expense> undoStack = new ArrayDeque<>();

    private static final Font BASE_FONT  = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font BOLD_FONT  = new Font("SansSerif", Font.BOLD,  15);
    private static final Font MONO_FONT  = new Font("Monospaced", Font.PLAIN, 14);

    public ExpenseTrackerGUI() {
        saver = new BackgroundSaver(service, "data/expenses.csv");
        service.loadFromFile("data/expenses.csv");
        setGlobalFont();
        askUsername();
        saver.start();
        buildMainWindow();
        refreshTable();
        setVisible(true);
    }

    private void setGlobalFont() {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof Font) UIManager.put(key, BASE_FONT);
        }
    }

    private void askUsername() {
        List<String> existing = userMgr.getAllUsers();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JTextField inputField = new JTextField(20);
        inputField.setFont(BASE_FONT);

        JLabel topLabel = new JLabel(existing.isEmpty()
            ? "Welcome! No users yet — enter your name to start."
            : "Existing users: " + String.join(", ", existing));
        topLabel.setFont(BASE_FONT);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel nameLabel = new JLabel("Your name:");
        nameLabel.setFont(BOLD_FONT);
        center.add(nameLabel);
        center.add(inputField);

        JLabel hint = new JLabel("New name = new account   |   Existing name = load your data");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 13));
        hint.setForeground(Color.GRAY);

        panel.add(topLabel, BorderLayout.NORTH);
        panel.add(center,   BorderLayout.CENTER);
        panel.add(hint,     BorderLayout.SOUTH);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                null, panel, "Expense Tracker — Who are you?",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) System.exit(0);
            String name = inputField.getText().trim();
            if (!name.isEmpty()) { currentUser = name; userMgr.addUser(name); break; }
            JOptionPane.showMessageDialog(null, "Please enter your name.");
        }
    }

    private void buildMainWindow() {
        setTitle("Expense Tracker — " + currentUser);
        setSize(960, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildTopBar(),      BorderLayout.NORTH);
        add(buildTablePanel(),  BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));

        welcomeLabel = new JLabel("👤 " + currentUser);
        welcomeLabel.setFont(BOLD_FONT);

        currencyLabel = new JLabel(currentCode + " " + currentSymbol);
        currencyLabel.setFont(BOLD_FONT);

        JButton switchUserBtn = makeButton("🔄 Switch User");
        JButton currencyBtn   = makeButton("💱 Currency");
        JButton summaryBtn    = makeButton("📊 Summary");
        JButton allUsersBtn   = makeButton("👥 All Users");

        switchUserBtn.addActionListener(e -> {
            askUsername();
            setTitle("Expense Tracker — " + currentUser);
            welcomeLabel.setText("👤 " + currentUser);
            refreshTable();
        });
        currencyBtn.addActionListener(e -> openCurrencyDialog());
        summaryBtn.addActionListener(e  -> openSummaryDialog());
        allUsersBtn.addActionListener(e -> showAllUsersDialog());

        toolbar.add(welcomeLabel);
        toolbar.add(new JLabel("|"));
        toolbar.add(new JLabel("Currency:"));
        toolbar.add(currencyLabel);
        toolbar.add(currencyBtn);
        toolbar.add(switchUserBtn);
        toolbar.add(summaryBtn);
        toolbar.add(allUsersBtn);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        searchField = new JTextField(22);
        searchField.setFont(BASE_FONT);
        JButton searchBtn = makeButton("🔍 Search");
        JButton clearBtn  = makeButton("✖ Clear");

        searchBtn.addActionListener(e -> filterTable(searchField.getText().trim()));
        clearBtn.addActionListener(e  -> { searchField.setText(""); refreshTable(); });

        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        JPanel form = new JPanel(new GridLayout(2, 4, 10, 8));
        form.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Add Expense"));

        nameField     = new JTextField(); nameField.setFont(BASE_FONT);
        amountField   = new JTextField(); amountField.setFont(BASE_FONT);
        categoryField = new JTextField(); categoryField.setFont(BASE_FONT);
        JButton addBtn = makeButton("➕ Add Expense");
        addBtn.addActionListener(e -> addExpense());

        form.add(label("Name:"));        form.add(nameField);
        form.add(label("Amount:"));      form.add(amountField);
        form.add(label("Category:"));    form.add(categoryField);
        form.add(new JLabel(""));        form.add(addBtn);

        wrapper.add(toolbar,   BorderLayout.NORTH);
        wrapper.add(searchBar, BorderLayout.CENTER);
        wrapper.add(form,      BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID","Name","Amount","Category","Date","User"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(BASE_FONT);
        table.setRowHeight(30);
        table.getTableHeader().setFont(BOLD_FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) openEditDialog(row);
                }
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        totalLabel = new JLabel("My Total: " + currentSymbol + "0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JTextField deleteField = new JTextField(5);
        deleteField.setFont(BASE_FONT);

        JButton deleteBtn = makeButton("🗑 Delete ID");
        JButton editBtn   = makeButton("✏ Edit Row");
        JButton undoBtn   = makeButton("↩ Undo");
        JButton saveBtn   = makeButton("💾 Save Now");

        deleteBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(deleteField.getText().trim());
                service.getExpenses().stream()
                       .filter(ex -> ex.getId() == id)
                       .findFirst().ifPresent(undoStack::push);
                if (service.deleteExpense(id)) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Deleted #" + id);
                } else {
                    JOptionPane.showMessageDialog(this, "ID not found.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid ID.");
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) JOptionPane.showMessageDialog(this, "Select a row first.");
            else openEditDialog(row);
        });

        undoBtn.addActionListener(e -> {
            if (undoStack.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nothing to undo.");
            } else {
                Expense last = undoStack.pop();
                service.addExpense(last);
                refreshTable();
                JOptionPane.showMessageDialog(this, "Restored: " + last.getName());
            }
        });

        saveBtn.addActionListener(e -> {
            service.saveToFile("data/expenses.csv");
            JOptionPane.showMessageDialog(this, "Saved successfully!");
        });

        panel.add(totalLabel);
        panel.add(new JLabel("  Delete ID:"));
        panel.add(deleteField);
        panel.add(deleteBtn);
        panel.add(editBtn);
        panel.add(undoBtn);
        panel.add(saveBtn);
        return panel;
    }

    private void addExpense() {
        String name    = nameField.getText().trim();
        String amtText = amountField.getText().trim();
        String cat     = categoryField.getText().trim();

        if (name.isEmpty() || amtText.isEmpty() || cat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
        try {
            double amount = Double.parseDouble(amtText);
            service.addExpense(new Expense(
                name, amount, cat, LocalDate.now(), currentUser
            ));
            nameField.setText(""); amountField.setText(""); categoryField.setText("");
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Expense e : service.getExpensesByUser(currentUser)) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getName(),
                currentSymbol + String.format("%.2f", e.getAmount()),
                e.getCategory(), e.getDate(), e.getUsername()
            });
        }
        totalLabel.setText(String.format("My Total: %s%.2f",
                currentSymbol, service.getUserTotal(currentUser)));
    }

    private void filterTable(String keyword) {
        tableModel.setRowCount(0);
        List<Expense> results = keyword.isEmpty()
                ? service.getExpensesByUser(currentUser)
                : service.searchExpenses(keyword, currentUser);
        for (Expense e : results) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getName(),
                currentSymbol + String.format("%.2f", e.getAmount()),
                e.getCategory(), e.getDate(), e.getUsername()
            });
        }
    }

    private void openEditDialog(int row) {
        int    id    = (int)    tableModel.getValueAt(row, 0);
        String cName = (String) tableModel.getValueAt(row, 1);
        String cAmt  = ((String) tableModel.getValueAt(row, 2))
                           .replace(currentSymbol, "").trim();
        String cCat  = (String) tableModel.getValueAt(row, 3);

        JDialog dialog = new JDialog(this, "Edit Expense #" + id, true);
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 10));
        fields.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JTextField eName = new JTextField(cName); eName.setFont(BASE_FONT);
        JTextField eAmt  = new JTextField(cAmt);  eAmt.setFont(BASE_FONT);
        JTextField eCat  = new JTextField(cCat);  eCat.setFont(BASE_FONT);

        fields.add(label("Name:"));     fields.add(eName);
        fields.add(label("Amount:"));   fields.add(eAmt);
        fields.add(label("Category:")); fields.add(eCat);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton save   = makeButton("💾 Save");
        JButton cancel = makeButton("Cancel");

        save.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(eAmt.getText().trim());
                service.editExpense(id, eName.getText().trim(), amt, eCat.getText().trim());
                refreshTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Updated successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Amount must be a number.");
            }
        });
        cancel.addActionListener(e -> dialog.dispose());

        btns.add(cancel); btns.add(save);
        dialog.add(fields, BorderLayout.CENTER);
        dialog.add(btns,   BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openSummaryDialog() {
        Map<String, Double> catSum   = service.getSummaryByCategory(currentUser);
        Map<String, Double> monthSum = service.getMonthlySummary(currentUser);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(currentUser).append("'s Summary ===\n\n");
        sb.append("By Category:\n");
        if (catSum.isEmpty()) sb.append("  No expenses yet.\n");
        else catSum.forEach((cat, total) ->
            sb.append(String.format("  %-20s %s%.2f%n", cat, currentSymbol, total)));

        sb.append("\nBy Month:\n");
        if (monthSum.isEmpty()) sb.append("  No expenses yet.\n");
        else monthSum.forEach((month, total) ->
            sb.append(String.format("  %-20s %s%.2f%n", month, currentSymbol, total)));

        sb.append(String.format("%nGrand Total: %s%.2f",
                currentSymbol, service.getUserTotal(currentUser)));

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(MONO_FONT);
        area.setEditable(false);
        area.setPreferredSize(new Dimension(420, 300));

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "📊 Summary — " + currentUser, JOptionPane.PLAIN_MESSAGE);
    }

    private void showAllUsersDialog() {
        List<String> users = userMgr.getAllUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users found."); return;
        }
        String[] arr = users.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(
            this, "Select a user to view their expenses:",
            "👥 All Users", JOptionPane.PLAIN_MESSAGE,
            null, arr, arr[0]);
        if (selected == null) return;

        List<Expense> list = service.getExpensesByUser(selected);
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, selected + " has no expenses."); return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(selected).append("'s Expenses ===\n\n");
        double total = 0;
        for (Expense e : list) {
            sb.append(String.format("[%d] %-20s %s%-10.2f  %-15s  %s%n",
                e.getId(), e.getName(), currentSymbol,
                e.getAmount(), e.getCategory(), e.getDate()));
            total += e.getAmount();
        }
        sb.append(String.format("%nTotal: %s%.2f", currentSymbol, total));

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(MONO_FONT);
        area.setEditable(false);
        area.setPreferredSize(new Dimension(520, 320));

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "Expenses of " + selected, JOptionPane.PLAIN_MESSAGE);
    }

    private void openCurrencyDialog() {
        JDialog dialog = new JDialog(this, "Select Currency", true);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        ButtonGroup group = new ButtonGroup();
        for (String[] c : CURRENCIES) {
            String code = c[0], symbol = c[1], lbl = c[2];
            JToggleButton btn = new JToggleButton(lbl + "  " + symbol);
            btn.setFont(BASE_FONT);
            btn.setFocusPainted(false);
            if (code.equals(currentCode)) {
                btn.setSelected(true);
                btn.setBackground(new Color(220, 235, 255));
            }
            btn.addActionListener(e -> {
                currentCode = code; currentSymbol = symbol;
                currencyLabel.setText(currentCode + " " + currentSymbol);
                refreshTable(); dialog.dispose();
            });
            group.add(btn); grid.add(btn);
        }
        JLabel top = new JLabel("  Choose your currency:");
        top.setFont(BOLD_FONT);
        dialog.add(top, BorderLayout.NORTH);
        dialog.add(new JScrollPane(grid), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BASE_FONT);
        btn.setFocusPainted(false);
        return btn;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(BOLD_FONT);
        return lbl;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}