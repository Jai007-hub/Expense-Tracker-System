import java.util.List;

public interface ExpenseService {
    void addExpense(Expense expense);
    List<Expense> getExpenses();
    boolean deleteExpense(int id);
    double getTotalByCategory(String category);
    double getGrandTotal();
    List<String> getCategories();
    void saveToFile(String filename);
    void loadFromFile(String filename);
}