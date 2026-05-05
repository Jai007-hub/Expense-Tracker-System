import java.io.*;
import java.util.*;

public class UserManager {
    private static final String USER_FILE = "data/users.txt";
    private List<String> users = new ArrayList<>();

    public UserManager() {
        new File("data").mkdirs();
        loadUsers();
    }

    public boolean userExists(String username) {
        return users.stream()
                .anyMatch(u -> u.equalsIgnoreCase(username));
    }

    public void addUser(String username) {
        if (!userExists(username)) {
            users.add(username);
            saveUsers();
        }
    }

    public List<String> getAllUsers() {
        return Collections.unmodifiableList(users);
    }

    private void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE))) {
            for (String u : users) pw.println(u);
        } catch (IOException ex) {
            System.err.println("User save error: " + ex.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) users.add(line.trim());
            }
        } catch (IOException ex) {
            System.err.println("User load error: " + ex.getMessage());
        }
    }
}