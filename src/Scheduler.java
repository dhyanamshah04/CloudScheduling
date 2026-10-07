import java.util.*;

public class Scheduler {

    public static void scheduleFCFS(List<Task> tasks, List<VM> vms) {

        reset(tasks, vms);

        List<Task> sorted = new ArrayList<>(tasks);

        sorted.sort(
                Comparator.comparingInt((Task t) -> t.arrivalTime)
                        .thenComparingInt(t -> t.taskId)
        );

        for (Task task : sorted) {

            VM vm = findEarliestVM(vms);

            int start = Math.max(task.arrivalTime, vm.availableTime);

            executeTask(task, vm, start);
        }

        printAssignments(tasks);
        calculateMetrics(tasks, vms);
    }


    public static void scheduleSJF(List<Task> tasks, List<VM> vms) {

        reset(tasks, vms);

        Set<Task> remaining = new HashSet<>(tasks);

        int currentTime = getMinimumArrival(tasks);

        while (!remaining.isEmpty()) {

            List<VM> availableVMs = new ArrayList<>();

            for (VM vm : vms) {

                if (vm.availableTime <= currentTime) {
                    availableVMs.add(vm);
                }
            }

            if (availableVMs.isEmpty()) {

                currentTime = calculateNextTime(remaining, vms, currentTime);

                continue;
            }

            List<Task> availableTasks = new ArrayList<>();

            for (Task task : remaining) {

                if (task.arrivalTime <= currentTime) {
                    availableTasks.add(task);
                }
            }

            if (availableTasks.isEmpty()) {

                currentTime = calculateNextTime(remaining, vms, currentTime);

                continue;
            }

            availableTasks.sort(
                    Comparator.comparingInt((Task t) -> t.length)
                            .thenComparingInt(t -> t.arrivalTime)
                            .thenComparingInt(t -> t.taskId)
            );

            for (VM vm : availableVMs) {

                if (availableTasks.isEmpty()) {
                    break;
                }

                Task task = availableTasks.remove(0);

                int start = Math.max(
                        currentTime,
                        Math.max(task.arrivalTime, vm.availableTime)
                );

                executeTask(task, vm, start);

                remaining.remove(task);
            }

            currentTime = calculateNextTime(remaining, vms, currentTime);
        }

        printAssignments(tasks);
        calculateMetrics(tasks, vms);
    }


    public static void scheduleMORAS(List<Task> tasks, List<VM> vms) {

        reset(tasks, vms);

        Set<Task> remaining = new HashSet<>(tasks);

        int currentTime = getMinimumArrival(tasks);

        while (!remaining.isEmpty()) {

            List<VM> availableVMs = new ArrayList<>();

            for (VM vm : vms) {

                if (vm.availableTime <= currentTime) {
                    availableVMs.add(vm);
                }
            }

            if (availableVMs.isEmpty()) {

                currentTime = calculateNextTime(
                        remaining,
                        vms,
                        currentTime
                );

                continue;
            }

            List<Task> availableTasks = new ArrayList<>();

            for (Task task : remaining) {

                if (task.arrivalTime <= currentTime) {
                    availableTasks.add(task);
                }
            }

            if (availableTasks.isEmpty()) {

                currentTime = calculateNextTime(
                        remaining,
                        vms,
                        currentTime
                );

                continue;
            }

            double maxExecution = 1;
            double maxWaiting = 1;
            double maxCost = 1;
            double maxPriority = 1;
            double maxAging = 1;

            for (Task task : availableTasks) {

                int waiting = Math.max(
                        0,
                        currentTime - task.arrivalTime
                );

                double aging = waiting;

                for (VM vm : availableVMs) {

                    double execution =
                            calculateExecutionTime(task, vm);

                    double cost =
                            execution * vm.costPerTime;

                    maxExecution =
                            Math.max(maxExecution, execution);

                    maxWaiting =
                            Math.max(maxWaiting, waiting);

                    maxCost =
                            Math.max(maxCost, cost);

                    maxPriority =
                            Math.max(maxPriority, task.priority);

                    maxAging =
                            Math.max(maxAging, aging);
                }
            }

            Task bestTask = null;
            VM bestVM = null;

            double bestScore = Double.MAX_VALUE;

            for (VM vm : availableVMs) {

                for (Task task : availableTasks) {

                    double execution =
                            calculateExecutionTime(task, vm);

                    int waiting =
                            Math.max(
                                    0,
                                    currentTime - task.arrivalTime
                            );

                    double cost =
                            execution * vm.costPerTime;

                    double aging = waiting;

                    double executionScore =
                            execution / maxExecution;

                    double waitingScore =
                            waiting / maxWaiting;

                    double costScore =
                            cost / maxCost;

                    double priorityScore =
                            1.0 -
                            (task.priority / maxPriority);

                    double agingScore =
                            1.0 -
                            (aging / maxAging);

                    double score =
                            0.35 * executionScore
                            +
                            0.20 * waitingScore
                            +
                            0.20 * costScore
                            +
                            0.15 * priorityScore
                            +
                            0.10 * agingScore;

                    if (score < bestScore) {

                        bestScore = score;
                        bestTask = task;
                        bestVM = vm;
                    }
                }
            }

            if (bestTask != null && bestVM != null) {

                int start =
                        Math.max(
                                currentTime,
                                Math.max(
                                        bestTask.arrivalTime,
                                        bestVM.availableTime
                                )
                        );

                executeTask(
                        bestTask,
                        bestVM,
                        start
                );

                remaining.remove(bestTask);
            }

            currentTime =
                    calculateNextTime(
                            remaining,
                            vms,
                            currentTime
                    );
        }

        printMORASAssignments(tasks);
        calculateMetrics(tasks, vms);
    }


    private static void executeTask(
            Task task,
            VM vm,
            int start) {

        int executionTime =
                calculateExecutionTime(task, vm);

        task.startTime = start;

        task.finishTime =
                start + executionTime;

        task.waitingTime =
                start - task.arrivalTime;

        task.assignedVM =
                vm.vmId;

        vm.availableTime =
                task.finishTime;

        vm.totalBusyTime +=
                executionTime;
    }


    private static int calculateExecutionTime(
            Task task,
            VM vm) {

        return Math.max(
                1,
                (int) Math.ceil(
                        task.length / vm.processingSpeed
                )
        );
    }


    private static VM findEarliestVM(
            List<VM> vms) {

        VM best = vms.get(0);

        for (VM vm : vms) {

            if (vm.availableTime < best.availableTime) {
                best = vm;
            }
        }

        return best;
    }


    private static int calculateNextTime(
            Set<Task> remaining,
            List<VM> vms,
            int currentTime) {

        int nextTime = Integer.MAX_VALUE;

        for (Task task : remaining) {

            if (task.arrivalTime > currentTime) {

                nextTime =
                        Math.min(
                                nextTime,
                                task.arrivalTime
                        );
            }
        }

        for (VM vm : vms) {

            if (vm.availableTime > currentTime) {

                nextTime =
                        Math.min(
                                nextTime,
                                vm.availableTime
                        );
            }
        }

        if (nextTime == Integer.MAX_VALUE) {
            return currentTime + 1;
        }

        return nextTime;
    }


    private static int getMinimumArrival(
            List<Task> tasks) {

        int minimum = Integer.MAX_VALUE;

        for (Task task : tasks) {

            minimum =
                    Math.min(
                            minimum,
                            task.arrivalTime
                    );
        }

        return minimum;
    }


    private static void reset(
            List<Task> tasks,
            List<VM> vms) {

        for (Task task : tasks) {

            task.startTime = 0;
            task.finishTime = 0;
            task.waitingTime = 0;
            task.assignedVM = -1;
        }

        for (VM vm : vms) {

            vm.availableTime = 0;
            vm.totalBusyTime = 0;
        }
    }


    private static void printAssignments(
            List<Task> tasks) {

        List<Task> sorted =
                new ArrayList<>(tasks);

        sorted.sort(
                Comparator.comparingInt(
                        t -> t.startTime
                )
        );

        for (Task task : sorted) {

            System.out.println(
                    "Task " + task.taskId
                    + " -> VM " + task.assignedVM
                    + " | Start: " + task.startTime
                    + " | Finish: " + task.finishTime
                    + " | Waiting: " + task.waitingTime
            );
        }
    }


    private static void printMORASAssignments(
            List<Task> tasks) {

        List<Task> sorted =
                new ArrayList<>(tasks);

        sorted.sort(
                Comparator.comparingInt(
                        t -> t.startTime
                )
        );

        for (Task task : sorted) {

            System.out.println(
                    "Task " + task.taskId
                    + " -> VM " + task.assignedVM
                    + " | Start: " + task.startTime
                    + " | Finish: " + task.finishTime
                    + " | Waiting: " + task.waitingTime
                    + " | Priority: " + task.priority
            );
        }
    }


    public static void calculateMetrics(
            List<Task> tasks,
            List<VM> vms) {

        int makespan = 0;
        long totalWaiting = 0;
        long totalBusy = 0;

        double totalCost = 0;

        for (Task task : tasks) {

            makespan =
                    Math.max(
                            makespan,
                            task.finishTime
                    );

            totalWaiting +=
                    task.waitingTime;

            for (VM vm : vms) {

                if (vm.vmId ==
                        task.assignedVM) {

                    int executionTime =
                            task.finishTime
                            - task.startTime;

                    totalCost +=
                            executionTime
                            * vm.costPerTime;

                    break;
                }
            }
        }

        for (VM vm : vms) {

            totalBusy +=
                    vm.totalBusyTime;
        }

        double averageWaiting =
                (double) totalWaiting
                / tasks.size();

        double utilization = 0;

        if (makespan > 0) {

            utilization =
                    ((double) totalBusy
                    /
                    (makespan
                    * vms.size()))
                    * 100.0;
        }

        System.out.println();
        System.out.println(
                "===== PERFORMANCE METRICS ====="
        );

        System.out.println(
                "Makespan              : "
                + makespan
        );

        System.out.println(
                "Total Waiting Time    : "
                + totalWaiting
        );

        System.out.printf(
                "Average Waiting Time  : %.2f%n",
                averageWaiting
        );

        System.out.printf(
                "Resource Utilization  : %.2f%%%n",
                utilization
        );

        System.out.printf(
                "Estimated Cost        : %.2f%n",
                totalCost
        );
    }
}