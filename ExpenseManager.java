import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseManager implements ExpenseService {
    private List<Expense> expenses = new ArrayList<>();

    @Override
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    @Override
    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public List<Expense> getExpensesByUser(String username) {
        return expenses.stream()
                .filter(e -> e.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public List<Expense> searchExpenses(String keyword, String username) {
        String kw = keyword.toLowerCase();
        return expenses.stream()
                .filter(e -> e.getUsername().equalsIgnoreCase(username))
                .filter(e -> e.getName().toLowerCase().contains(kw)
                          || e.getCategory().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public Map<String, Double> getSummaryByCategory(String username) {
        return expenses.stream()
                .filter(e -> e.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    public Map<String, Double> getMonthlySummary(String username) {
        return expenses.stream()
                .filter(e -> e.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.groupingBy(
                        e -> e.getDate().getMonth().toString(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    @Override
    public boolean deleteExpense(int id) {
        boolean removed = expenses.removeIf(e -> e.getId() == id);

        if (removed && expenses.isEmpty()) {
            Expense.resetCounter();
        } else if (removed) {
            int maxId = expenses.stream()
                    .mapToInt(Expense::getId)
                    .max()
                    .orElse(0);
            Expense.setCounter(maxId + 1);
        }

        return removed;
    }

    public boolean editExpense(int id, String newName, double newAmount, String newCategory) {
        for (Expense e : expenses) {
            if (e.getId() == id) {
                e.setName(newName);
                e.setAmount(newAmount);
                e.setCategory(newCategory);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTotalByCategory(String category) {
        return expenses.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Expense::getAmount).sum();
    }

    @Override
    public double getGrandTotal() {
        return expenses.stream()
                .mapToDouble(Expense::getAmount).sum();
    }

    public double getUserTotal(String username) {
        return expenses.stream()
                .filter(e -> e.getUsername().equalsIgnoreCase(username))
                .mapToDouble(Expense::getAmount).sum();
    }

    @Override
    public List<String> getCategories() {
        return expenses.stream()
                .map(Expense::getCategory)
                .distinct().sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void saveToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Expense e : expenses) pw.println(e.toCSV());
        } catch (IOException ex) {
            System.err.println("Save error: " + ex.getMessage());
        }
    }

    @Override
    public void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return;
        expenses.clear();
        Expense.resetCounter();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", 6);
                if (p.length == 6) {
                    expenses.add(new Expense(
                        Integer.parseInt(p[0]), p[1],
                        Double.parseDouble(p[2]), p[3],
                        LocalDate.parse(p[4]), p[5]
                    ));
                }
            }
        } catch (IOException ex) {
            System.err.println("Load error: " + ex.getMessage());
        }
    }
}