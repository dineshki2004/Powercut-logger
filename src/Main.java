import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Outage {
    String location;
    LocalDateTime startTime;
    int durationInMinutes;
    String reason;

    public Outage(String location, LocalDateTime startTime, int durationInMinutes, String reason) {
        this.location = location;
        this.startTime = startTime;
        this.durationInMinutes = durationInMinutes;
        this.reason = reason;
    }

    public String toFileString() {
        return location + "," + startTime.toString() + "," + durationInMinutes + "," + reason;
    }

    public static Outage fromFileString(String line) {
        String[] parts = line.split(",");
        return new Outage(
                parts[0],
                LocalDateTime.parse(parts[1]),
                Integer.parseInt(parts[2]),
                parts[3]
        );
    }

    @Override
    public String toString() {
        return "Location: " + location + ", Start: " + startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                ", Duration: " + durationInMinutes + " mins, Reason: " + reason;
    }
}

public class Main {
    static final String DATA_FILE = "data/outages.txt";
    static List<Outage> outages = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new File("data").mkdir();
        loadOutages();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== PowerCut Logger ===");
            System.out.println("1. Add Outage Record");
            System.out.println("2. View All Records");
            System.out.println("3. Generate Report");
            System.out.println("4. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine();
            if (choice == 1) addOutage(sc);
            else if (choice == 2) viewOutages();
            else if (choice == 3) generateReport();
            else break;
        }
    }

    static void addOutage(Scanner sc) throws IOException {
        System.out.print("Enter location: ");
        String location = sc.nextLine();
        System.out.print("Start time (yyyy-MM-dd HH:mm): ");
        String timeStr = sc.nextLine();
        LocalDateTime startTime = LocalDateTime.parse(timeStr.replace(" ", "T"));
        System.out.print("Duration (minutes): ");
        int duration = sc.nextInt();
        sc.nextLine();
        System.out.print("Reason: ");
        String reason = sc.nextLine();
        Outage outage = new Outage(location, startTime, duration, reason);
        outages.add(outage);
        saveOutage(outage);
        System.out.println("Outage logged successfully.");
    }

    static void viewOutages() {
        if (outages.isEmpty()) System.out.println("No outage records found.");
        else for (Outage o : outages) System.out.println(o);
    }

    static void generateReport() throws IOException {
        int total = outages.size();
        int totalMinutes = outages.stream().mapToInt(o -> o.durationInMinutes).sum();
        int maxDuration = outages.stream().mapToInt(o -> o.durationInMinutes).max().orElse(0);
        Map<String, Integer> countByLocation = new HashMap<>();
        for (Outage o : outages) {
            countByLocation.put(o.location, countByLocation.getOrDefault(o.location, 0) + 1);
        }
        new File("reports").mkdir();
        FileWriter fw = new FileWriter("reports/summary_report.txt");
        fw.write("Total Records: " + total + "\n");
        fw.write("Total Duration: " + totalMinutes + " minutes\n");
        fw.write("Longest Outage: " + maxDuration + " minutes\n");
        fw.write("\nOutages by Location:\n");
        for (String loc : countByLocation.keySet()) {
            fw.write(loc + ": " + countByLocation.get(loc) + "\n");
        }
        fw.close();
        System.out.println("Report generated at reports/summary_report.txt");
    }

    static void loadOutages() throws IOException {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            outages.add(Outage.fromFileString(line));
        }
        br.close();
    }

    static void saveOutage(Outage o) throws IOException {
        FileWriter fw = new FileWriter(DATA_FILE, true);
        fw.write(o.toFileString() + "\n");
        fw.close();
    }
}
