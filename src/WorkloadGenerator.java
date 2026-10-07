import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkloadGenerator {

    private Random random;

    // Default constructor
    public WorkloadGenerator() {
        random = new Random();
    }

    // Constructor with fixed seed
    public WorkloadGenerator(long seed) {
        random = new Random(seed);
    }

    public List<Task> generateTasks(int numberOfTasks) {

        List<Task> tasks = new ArrayList<>();

        for (int i = 1; i <= numberOfTasks; i++) {

            // Random arrival time between 0 and 20
            int arrivalTime = random.nextInt(21);

            // Random execution time between 2 and 15
            int executionTime = 2 + random.nextInt(14);

            // Random priority between 1 and 3
            int priority = 1 + random.nextInt(3);

            tasks.add(
                new Task(
                    i,
                    arrivalTime,
                    executionTime,
                    priority
                )
            );
        }

        return tasks;
    }
}