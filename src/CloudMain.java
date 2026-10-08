import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CloudMain {

    public static void main(String[] args) throws Exception {

        System.out.println("==============================================");
        System.out.println("       CLOUD SCHEDULING PROJECT");
        System.out.println("       CLOUD DEPLOYMENT WORKLOAD");
        System.out.println("==============================================");

        List<Task> tasks = new ArrayList<>();

        String filePath = "/app/data/cloud_tasks.csv";

        if (!new java.io.File(filePath).exists()) {
        filePath = "../data/cloud_tasks.csv";
        }   

BufferedReader br =
        new BufferedReader(
                new FileReader(filePath)
        );

        String line;

        br.readLine();

        while ((line = br.readLine()) != null) {

            String[] parts = line.split(",");

            int taskId = Integer.parseInt(parts[0]);
            int arrivalTime = Integer.parseInt(parts[1]);
            int length = Integer.parseInt(parts[2]);
            int priority = Integer.parseInt(parts[3]);

            double cpu =
                    Double.parseDouble(parts[4]);

            double memory =
                    Double.parseDouble(parts[5]);

            Task task =
                    new Task(
                            taskId,
                            arrivalTime,
                            length,
                            priority
                    );

            task.setCpuDemand(cpu);
            task.setMemoryDemand(memory);

            tasks.add(task);
        }

        br.close();

        System.out.println();
        System.out.println(
                "Cloud workload loaded: "
                        + tasks.size()
                        + " tasks"
        );

        runFCFS(tasks);
        runSJF(tasks);
        runMORAS(tasks);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("       ALL SCHEDULING COMPLETED");
        System.out.println("==============================================");
    }


    private static List<VM> createVMs() {

        List<VM> vms = new ArrayList<>();

        vms.add(new VM(1, 1.5, 0.007));
        vms.add(new VM(2, 1.5, 0.007));
        vms.add(new VM(3, 1.5, 0.007));
        vms.add(new VM(4, 1.5, 0.007));

        return vms;
    }


    private static List<Task> copyTasks(
            List<Task> original) {

        List<Task> copy =
                new ArrayList<>();

        for (Task t : original) {

            Task task =
                    new Task(
                            t.taskId,
                            t.arrivalTime,
                            t.length,
                            t.priority
                    );

            task.setCpuDemand(
                    t.getCpuDemand()
            );

            task.setMemoryDemand(
                    t.getMemoryDemand()
            );

            copy.add(task);
        }

        return copy;
    }


    private static void runFCFS(
            List<Task> original) {

        System.out.println();
        System.out.println("========== FCFS ==========");

        List<Task> tasks =
                copyTasks(original);

        List<VM> vms =
                createVMs();

        Scheduler scheduler =
                new Scheduler();

        scheduler.scheduleFCFS(
                tasks,
                vms
        );

        scheduler.calculateMetrics(
                tasks,
                vms
        );
    }


    private static void runSJF(
            List<Task> original) {

        System.out.println();
        System.out.println("========== SJF ==========");

        List<Task> tasks =
                copyTasks(original);

        List<VM> vms =
                createVMs();

        Scheduler scheduler =
                new Scheduler();

        scheduler.scheduleSJF(
                tasks,
                vms
        );

        scheduler.calculateMetrics(
                tasks,
                vms
        );
    }


    private static void runMORAS(
            List<Task> original) {

        System.out.println();
        System.out.println("========== MORAS ==========");

        List<Task> tasks =
                copyTasks(original);

        List<VM> vms =
                createVMs();

        Scheduler scheduler =
                new Scheduler();

        scheduler.scheduleMORAS(
                tasks,
                vms
        );

        scheduler.calculateMetrics(
                tasks,
                vms
        );
    }
}