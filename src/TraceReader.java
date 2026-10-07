import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceReader {

    private String taskFile;
    private String instanceFile;

    public TraceReader(String taskFile, String instanceFile) {
        this.taskFile = taskFile;
        this.instanceFile = instanceFile;
    }

    // ============================================================
    // READ REAL ALIBABA TASKS
    // ============================================================

    public List<Task> readTasks(int limit) {

        List<Task> tasks = new ArrayList<>();

        try {

            System.out.println("Reading Alibaba task data...");

            List<TaskRecord> taskRecords =
                    readTaskFile(limit);

            System.out.println(
                    "Valid task records found: "
                            + taskRecords.size()
            );

            if (taskRecords.isEmpty()) {
                System.out.println(
                        "No valid task records found."
                );
                return tasks;
            }

            System.out.println(
                    "Reading Alibaba instance data..."
            );

            /*
             * IMPORTANT:
             *
             * We do NOT load the complete instance file.
             *
             * The Alibaba trace contains millions of
             * instance records.
             *
             * We only search for instances belonging to
             * the selected tasks.
             */
            Map<String, InstanceRecord> instanceMap =
                    readMatchingInstances(taskRecords);

            System.out.println(
                    "Matching instance records found: "
                            + instanceMap.size()
            );

            int count = 0;

            for (TaskRecord tr : taskRecords) {

                if (count >= limit) {
                    break;
                }

                /*
                 * Find matching instance using:
                 *
                 * job_id + task_id
                 */
                String key =
                        createKey(
                                tr.jobId,
                                tr.taskId
                        );

                InstanceRecord matching =
                        instanceMap.get(key);

                /*
                 * =================================================
                 * DETERMINE EXECUTION TIME
                 * =================================================
                 *
                 * Primary:
                 *
                 * instance end - instance start
                 *
                 * If the instance data is unavailable,
                 * use task create/modify timestamps as a
                 * fallback.
                 */

                long executionTime = 0;

                if (matching != null &&
                    matching.startTime > 0 &&
                    matching.endTime > matching.startTime) {

                    executionTime =
                            matching.endTime -
                            matching.startTime;
                }

                /*
                 * Fallback to task timestamps.
                 */
                if (executionTime <= 0 &&
                    tr.modifyTime > tr.createTime) {

                    executionTime =
                            tr.modifyTime -
                            tr.createTime;
                }

                /*
                 * Minimum execution time.
                 */
                if (executionTime <= 0) {
                    executionTime = 1;
                }

                /*
                 * Our Task class uses int.
                 *
                 * Alibaba timestamps are relatively small
                 * for the 2017 trace, but protect against
                 * overflow anyway.
                 */
                int executionTimeInt;

                if (executionTime > Integer.MAX_VALUE) {
                    executionTimeInt =
                            Integer.MAX_VALUE;
                } else {
                    executionTimeInt =
                            (int) executionTime;
                }

                /*
                 * =================================================
                 * ARRIVAL TIME
                 * =================================================
                 *
                 * Use task creation timestamp.
                 */
                int arrivalTime;

                if (tr.createTime > Integer.MAX_VALUE) {
                    arrivalTime =
                            Integer.MAX_VALUE;
                } else {
                    arrivalTime =
                            (int) tr.createTime;
                }

                /*
                 * =================================================
                 * PRIORITY
                 * =================================================
                 */

                int priority =
                        calculatePriority(
                                executionTimeInt
                        );

                /*
                 * =================================================
                 * CREATE TASK
                 * =================================================
                 */

                Task task =
                        new Task(
                                count + 1,
                                arrivalTime,
                                executionTimeInt,
                                priority
                        );

                /*
                 * =================================================
                 * RESOURCE DEMANDS
                 * =================================================
                 *
                 * CPU and memory come from batch_task.csv
                 * (plan_cpu and plan_mem).
                 *
                 * If an instance exists, we also keep its
                 * real average usage information.
                 */

                if (tr.planCpu > 0) {

                    task.setCpuDemand(
                            tr.planCpu
                    );

                } else if (matching != null) {

                    task.setCpuDemand(
                            matching.realCpuAvg
                    );
                }

                if (tr.planMemory > 0) {

                    task.setMemoryDemand(
                            tr.planMemory
                    );

                } else if (matching != null) {

                    task.setMemoryDemand(
                            matching.realMemoryAvg
                    );
                }

                tasks.add(task);

                count++;
            }

            /*
             * Sort by arrival time.
             *
             * This makes FCFS operate on chronological
             * workload rather than raw file ordering.
             */
            tasks.sort(
                    Comparator.comparingInt(
                            Task::getArrivalTime
                    )
            );

            /*
             * Reassign sequential task IDs after sorting.
             */
            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).taskId = i + 1;
            }

        } catch (IOException e) {

            System.out.println(
                    "Error reading Alibaba trace:"
            );

            System.out.println(
                    e.getMessage()
            );
        }

        return tasks;
    }


    // ============================================================
    // READ ONLY REQUIRED TASK RECORDS
    // ============================================================

    private List<TaskRecord> readTaskFile(int limit)
            throws IOException {

        List<TaskRecord> records =
                new ArrayList<>();

        try (
            BufferedReader br =
                    new BufferedReader(
                            new FileReader(taskFile)
                    )
        ) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] data =
                        line.split(",", -1);

                /*
                 * Alibaba batch_task.csv has 8 columns.
                 */
                if (data.length < 8) {
                    continue;
                }

                try {

                    /*
                     * Official Alibaba schema:
                     *
                     * 0 create_timestamp
                     * 1 modify_timestamp
                     * 2 job_id
                     * 3 task_id
                     * 4 instance_num
                     * 5 status
                     * 6 plan_cpu
                     * 7 plan_mem
                     */

                    long createTime =
                            Long.parseLong(
                                    data[0].trim()
                            );

                    long modifyTime =
                            Long.parseLong(
                                    data[1].trim()
                            );

                    int jobId =
                            Integer.parseInt(
                                    data[2].trim()
                            );

                    int taskId =
                            Integer.parseInt(
                                    data[3].trim()
                            );

                    int instanceNum =
                            Integer.parseInt(
                                    data[4].trim()
                            );

                    String status =
                            data[5].trim();

                    double planCpu =
                            Double.parseDouble(
                                    data[6].trim()
                            );

                    double planMemory =
                            Double.parseDouble(
                                    data[7].trim()
                            );

                    /*
                     * Ignore invalid timestamps.
                     */
                    if (createTime < 0) {
                        continue;
                    }

                    /*
                     * Ignore cancelled/failed tasks.
                     *
                     * Keep only useful workload.
                     */
                    if (status.equalsIgnoreCase("Failed") ||
                        status.equalsIgnoreCase("Cancelled")) {

                        continue;
                    }

                    records.add(
                            new TaskRecord(
                                    createTime,
                                    modifyTime,
                                    jobId,
                                    taskId,
                                    instanceNum,
                                    status,
                                    planCpu,
                                    planMemory
                            )
                    );

                    /*
                     * We only need 'limit' tasks.
                     */
                    if (records.size() >= limit) {
                        break;
                    }

                } catch (NumberFormatException e) {

                    /*
                     * Ignore malformed rows.
                     */
                }
            }
        }

        return records;
    }


    // ============================================================
    // READ ONLY MATCHING INSTANCES
    // ============================================================

    private Map<String, InstanceRecord>
    readMatchingInstances(
            List<TaskRecord> taskRecords)
            throws IOException {

        Map<String, InstanceRecord> matches =
                new HashMap<>();

        /*
         * Create a set of required job/task combinations.
         */
        Map<String, Boolean> required =
                new HashMap<>();

        for (TaskRecord task : taskRecords) {

            required.put(
                    createKey(
                            task.jobId,
                            task.taskId
                    ),
                    true
            );
        }

        try (
            BufferedReader br =
                    new BufferedReader(
                            new FileReader(instanceFile)
                    )
        ) {

            String line;

            long lineNumber = 0;

            while ((line = br.readLine()) != null) {

                lineNumber++;

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] data =
                        line.split(",", -1);

                /*
                 * Alibaba batch_instance.csv has
                 * 12 columns.
                 */
                if (data.length < 12) {
                    continue;
                }

                try {

                    /*
                     * Official Alibaba schema:
                     *
                     * 0 start_timestamp
                     * 1 end_timestamp
                     * 2 job_id
                     * 3 task_id
                     * 4 machineID
                     * 5 status
                     * 6 seq_no
                     * 7 total_seq_no
                     * 8 real_cpu_max
                     * 9 real_cpu_avg
                     * 10 real_mem_max
                     * 11 real_mem_avg
                     */

                    long startTime =
                            Long.parseLong(
                                    data[0].trim()
                            );

                    long endTime =
                            Long.parseLong(
                                    data[1].trim()
                            );

                    int jobId =
                            Integer.parseInt(
                                    data[2].trim()
                            );

                    int taskId =
                            Integer.parseInt(
                                    data[3].trim()
                            );

                    int machineId =
                            Integer.parseInt(
                                    data[4].trim()
                            );

                    String status =
                            data[5].trim();

                    int seqNo =
                            Integer.parseInt(
                                    data[6].trim()
                            );

                    int totalSeqNo =
                            Integer.parseInt(
                                    data[7].trim()
                            );

                    double realCpuMax =
                            parseDouble(
                                    data[8]
                            );

                    double realCpuAvg =
                            parseDouble(
                                    data[9]
                            );

                    double realMemoryMax =
                            parseDouble(
                                    data[10]
                            );

                    double realMemoryAvg =
                            parseDouble(
                                    data[11]
                            );

                    String key =
                            createKey(
                                    jobId,
                                    taskId
                            );

                    /*
                     * Ignore instances that don't belong
                     * to our selected tasks.
                     */
                    if (!required.containsKey(key)) {
                        continue;
                    }

                    /*
                     * We prefer a successfully terminated
                     * instance with valid execution time.
                     *
                     * If multiple trials exist, replace the
                     * previous one only if this one has a
                     * better execution record.
                     */
                    InstanceRecord newRecord =
                            new InstanceRecord(
                                    startTime,
                                    endTime,
                                    jobId,
                                    taskId,
                                    machineId,
                                    status,
                                    seqNo,
                                    totalSeqNo,
                                    realCpuMax,
                                    realCpuAvg,
                                    realMemoryMax,
                                    realMemoryAvg
                            );

                    InstanceRecord oldRecord =
                            matches.get(key);

                    if (oldRecord == null ||
                        isBetterInstance(
                                newRecord,
                                oldRecord
                        )) {

                        matches.put(
                                key,
                                newRecord
                        );
                    }

                } catch (NumberFormatException e) {

                    /*
                     * Ignore malformed rows.
                     */
                }
            }

            System.out.println(
                    "Instance file scanned successfully."
            );

            System.out.println(
                    "Instance records scanned: "
                            + lineNumber
            );
        }

        return matches;
    }


    // ============================================================
    // CHOOSE BEST INSTANCE TRIAL
    // ============================================================

    private boolean isBetterInstance(
            InstanceRecord newRecord,
            InstanceRecord oldRecord) {

        /*
         * Prefer records with valid execution time.
         */
        boolean newValid =
                newRecord.startTime > 0 &&
                newRecord.endTime >
                        newRecord.startTime;

        boolean oldValid =
                oldRecord.startTime > 0 &&
                oldRecord.endTime >
                        oldRecord.startTime;

        if (newValid && !oldValid) {
            return true;
        }

        if (!newValid && oldValid) {
            return false;
        }

        /*
         * Prefer Terminated instances.
         */
        boolean newTerminated =
                newRecord.status.equalsIgnoreCase(
                        "Terminated"
                );

        boolean oldTerminated =
                oldRecord.status.equalsIgnoreCase(
                        "Terminated"
                );

        if (newTerminated && !oldTerminated) {
            return true;
        }

        if (!newTerminated && oldTerminated) {
            return false;
        }

        /*
         * Prefer smaller sequence number.
         */
        return newRecord.seqNo <
                oldRecord.seqNo;
    }


    // ============================================================
    // CREATE MAP KEY
    // ============================================================

    private String createKey(
            int jobId,
            int taskId) {

        return jobId + "_" + taskId;
    }


    // ============================================================
    // SAFE DOUBLE PARSER
    // ============================================================

    private double parseDouble(String value) {

        if (value == null ||
            value.trim().isEmpty()) {

            return 0.0;
        }

        try {

            return Double.parseDouble(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return 0.0;
        }
    }


    // ============================================================
    // PRIORITY
    // ============================================================

    private int calculatePriority(
            int executionTime) {

        if (executionTime <= 100) {
            return 1;
        }

        if (executionTime <= 1000) {
            return 2;
        }

        return 3;
    }


    // ============================================================
    // TASK RECORD
    // ============================================================

    private static class TaskRecord {

        long createTime;
        long modifyTime;

        int jobId;
        int taskId;
        int instanceNum;

        String status;

        double planCpu;
        double planMemory;

        TaskRecord(
                long createTime,
                long modifyTime,
                int jobId,
                int taskId,
                int instanceNum,
                String status,
                double planCpu,
                double planMemory) {

            this.createTime = createTime;
            this.modifyTime = modifyTime;

            this.jobId = jobId;
            this.taskId = taskId;
            this.instanceNum = instanceNum;

            this.status = status;

            this.planCpu = planCpu;
            this.planMemory = planMemory;
        }
    }


    // ============================================================
    // INSTANCE RECORD
    // ============================================================

    private static class InstanceRecord {

        long startTime;
        long endTime;

        int jobId;
        int taskId;
        int machineId;

        String status;

        int seqNo;
        int totalSeqNo;

        double realCpuMax;
        double realCpuAvg;

        double realMemoryMax;
        double realMemoryAvg;

        InstanceRecord(
                long startTime,
                long endTime,
                int jobId,
                int taskId,
                int machineId,
                String status,
                int seqNo,
                int totalSeqNo,
                double realCpuMax,
                double realCpuAvg,
                double realMemoryMax,
                double realMemoryAvg) {

            this.startTime = startTime;
            this.endTime = endTime;

            this.jobId = jobId;
            this.taskId = taskId;
            this.machineId = machineId;

            this.status = status;

            this.seqNo = seqNo;
            this.totalSeqNo = totalSeqNo;

            this.realCpuMax = realCpuMax;
            this.realCpuAvg = realCpuAvg;

            this.realMemoryMax = realMemoryMax;
            this.realMemoryAvg = realMemoryAvg;
        }
    }
}