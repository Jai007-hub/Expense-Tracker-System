import java.time.LocalDate;

public class Expense {
    private static int counter = 1;
    private int id;
    private String name;
    private double amount;
    private String category;
    private LocalDate date;
    private String username;

    public Expense(String name, double amount, String category, LocalDate date, String username) {
        this.id       = counter++;
        this.name     = name;
        this.amount   = amount;
        this.category = category;
        this.date     = date;
        this.username = username;
    }

    public Expense(int id, String name, double amount, String category, LocalDate date, String username) {
        this.id       = id;
        this.name     = name;
        this.amount   = amount;
        this.category = category;
        this.date     = date;
        this.username = username;
        if (id >= counter) counter = id + 1;
    }

    public static void resetCounter() {
        counter = 1;
    }

    public static void setCounter(int val) {
        counter = val;
    }

    public static int getCounter() {
        return counter;
    }

    public int       getId()       { return id; }
    public String    getName()     { return name; }
    public double    getAmount()   { return amount; }
    public String    getCategory() { return category; }
    public LocalDate getDate()     { return date; }
    public String    getUsername() { return username; }

    public void setName(String name)         { this.name = name; }
    public void setAmount(double amount)     { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }

    public String toCSV() {
        return id + "," + name + "," + amount + "," + category + "," + date + "," + username;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %.2f (%s) on %s [%s]",
                id, name, amount, category, date, username);
    }
}