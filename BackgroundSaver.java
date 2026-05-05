import java.util.concurrent.*;

public class BackgroundSaver {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExpenseService service;
    private final String filename;

    public BackgroundSaver(ExpenseService service, String filename) {
        this.service = service;
        this.filename = filename;
    }

    // Start auto-saving every 30 seconds on a background thread
    public void start() {
        Runnable saveTask = () -> {
            System.out.println("[AutoSave] Saving at " + java.time.LocalTime.now());
            service.saveToFile(filename);
        };
        scheduler.scheduleAtFixedRate(saveTask, 30, 30, TimeUnit.SECONDS);
        System.out.println("[AutoSave] Background saver started (every 30s).");
    }

    public void stop() {
        scheduler.shutdown();
        System.out.println("[AutoSave] Background saver stopped.");
    }
}