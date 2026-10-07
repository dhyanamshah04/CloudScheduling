public class Task {

    int taskId;
    int arrivalTime;
    int length;
    int priority;

    int startTime;
    int finishTime;
    int waitingTime;

    int assignedVM;

    double cpuDemand;
    double memoryDemand;


    public Task(
            int taskId,
            int arrivalTime,
            int length,
            int priority) {

        this.taskId = taskId;
        this.arrivalTime = arrivalTime;
        this.length = length;
        this.priority = priority;

        this.startTime = 0;
        this.finishTime = 0;
        this.waitingTime = 0;

        this.assignedVM = -1;

        this.cpuDemand = 0.0;
        this.memoryDemand = 0.0;
    }


    public int getId() {
        return taskId;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return length;
    }

    public int getPriority() {
        return priority;
    }

    public double getCpuDemand() {
        return cpuDemand;
    }

    public double getMemoryDemand() {
        return memoryDemand;
    }


    public void setCpuDemand(double cpuDemand) {
        this.cpuDemand = cpuDemand;
    }

    public void setMemoryDemand(double memoryDemand) {
        this.memoryDemand = memoryDemand;
    }
}