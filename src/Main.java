import java.util.ArrayList;
import java.util.List;

public class Main {

    // =========================================================
    // COPY TASKS
    // =========================================================

    public static List<Task> copyTasks(
            List<Task> original) {

        List<Task> copy =
                new ArrayList<>();

        for (Task t : original) {

            Task newTask =
                    new Task(
                            t.getId(),
                            t.getArrivalTime(),
                            t.getBurstTime(),
                            t.getPriority()
                    );

            newTask.setCpuDemand(
                    t.getCpuDemand()
            );

            newTask.setMemoryDemand(
                    t.getMemoryDemand()
            );

            copy.add(newTask);
        }

        return copy;
    }


    // =========================================================
    // CREATE VMs
    // =========================================================

    public static List<VM> createVMs() {

        List<VM> vms =
                new ArrayList<>();

        vms.add(
                new VM(
                        1,
                        1.0,
                        0.010
                )
        );

        vms.add(
                new VM(
                        2,
                        1.5,
                        0.015
                )
        );

        vms.add(
                new VM(
                        3,
                        2.0,
                        0.020
                )
        );

        vms.add(
                new VM(
                        4,
                        2.5,
                        0.025
                )
        );

        return vms;
    }


    // =========================================================
    // PRINT WORKLOAD
    // =========================================================

    public static void printWorkload(
            List<Task> tasks) {

        System.out.println();

        System.out.println(
                "========== REAL ALIBABA WORKLOAD =========="
        );

        System.out.println(
                "Tasks loaded: "
                        + tasks.size()
        );

        System.out.println();

        System.out.println(
                "Task\tArrival\tLength\tPriority\tCPU\tMemory"
        );

        System.out.println(
                "--------------------------------------------------------"
        );

        for (Task task : tasks) {

            System.out.println(
                    task.getId()
                    + "\t"
                    + task.getArrivalTime()
                    + "\t"
                    + task.getBurstTime()
                    + "\t"
                    + task.getPriority()
                    + "\t\t"
                    + String.format(
                            "%.2f",
                            task.getCpuDemand()
                    )
                    + "\t"
                    + String.format(
                            "%.4f",
                            task.getMemoryDemand()
                    )
            );
        }
    }


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) {

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       CLOUD SCHEDULING PROJECT"
        );

        System.out.println(
                "       REAL ALIBABA WORKLOAD"
        );

        System.out.println(
                "=============================================="
        );


        // =====================================================
        // FILE PATHS
        // =====================================================

        String taskFile =
                "C:\\Users\\dhyan\\Downloads\\alibaba-trace-2017\\batch_task.csv";

        String instanceFile =
                "C:\\Users\\dhyan\\Downloads\\alibaba-trace-2017\\batch_instance.csv";


        // =====================================================
        // LOAD REAL WORKLOAD
        // =====================================================

        TraceReader traceReader =
                new TraceReader(
                        taskFile,
                        instanceFile
                );


        /*
         * Load only 100 tasks.
         */
        List<Task> realTasks =
                traceReader.readTasks(100);


        if (realTasks.isEmpty()) {

            System.out.println();

            System.out.println(
                    "ERROR: No tasks were loaded."
            );

            System.out.println(
                    "Check the Alibaba CSV file paths."
            );

            return;
        }


        System.out.println();

        System.out.println(
                "Real Alibaba tasks loaded: "
                        + realTasks.size()
        );


        printWorkload(realTasks);


        // =====================================================
        // CREATE SCHEDULER
        // =====================================================

        Scheduler scheduler =
                new Scheduler();


        // =====================================================
        // FCFS
        // =====================================================

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "                 FCFS"
        );

        System.out.println(
                "=============================================="
        );


        List<Task> fcfsTasks =
                copyTasks(realTasks);

        List<VM> fcfsVMs =
                createVMs();


        scheduler.scheduleFCFS(
                fcfsTasks,
                fcfsVMs
        );


        scheduler.calculateMetrics(
                fcfsTasks,
                fcfsVMs
        );


        // =====================================================
        // SJF
        // =====================================================

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "                 SJF"
        );

        System.out.println(
                "=============================================="
        );


        List<Task> sjfTasks =
                copyTasks(realTasks);

        List<VM> sjfVMs =
                createVMs();


        scheduler.scheduleSJF(
                sjfTasks,
                sjfVMs
        );


        scheduler.calculateMetrics(
                sjfTasks,
                sjfVMs
        );


        // =====================================================
        // MORAS
        // =====================================================

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "                MORAS"
        );

        System.out.println(
                "=============================================="
        );


        List<Task> morasTasks =
                copyTasks(realTasks);

        List<VM> morasVMs =
                createVMs();


        scheduler.scheduleMORAS(
                morasTasks,
                morasVMs
        );


        scheduler.calculateMetrics(
                morasTasks,
                morasVMs
        );


        // =====================================================
        // COMPLETED
        // =====================================================

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       ALL SCHEDULING COMPLETED"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println();

        System.out.println(
                "Algorithms executed:"
        );

        System.out.println(
                "1. FCFS"
        );

        System.out.println(
                "2. SJF"
        );

        System.out.println(
                "3. MORAS"
        );

        System.out.println();

        System.out.println(
                "Metrics:"
        );

        System.out.println(
                "1. Makespan"
        );

        System.out.println(
                "2. Total Waiting Time"
        );

        System.out.println(
                "3. Average Waiting Time"
        );

        System.out.println(
                "4. Resource Utilization"
        );

        System.out.println(
                "5. Estimated Cost"
        );

        System.out.println();

        System.out.println(
                "PROJECT RUN COMPLETED SUCCESSFULLY."
        );
    }
}