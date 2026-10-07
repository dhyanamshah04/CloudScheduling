public class VM {

    int vmId;

    double processingSpeed;
    double costPerTime;

    int availableTime;
    int totalBusyTime;


    public VM(
            int vmId,
            double processingSpeed,
            double costPerTime) {

        this.vmId = vmId;
        this.processingSpeed = processingSpeed;
        this.costPerTime = costPerTime;

        this.availableTime = 0;
        this.totalBusyTime = 0;
    }
}