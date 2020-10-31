public class Timeline {
    private DisjointSets set;
    private int e;
    private int[] timepoints;
    private int[] originalTimepoints;
    private int[] timepointCapcity;
    private int[] estToTimepointsMap;
    private int[] processingTime;
    private int n;

    //Legacy constructor
    @Deprecated
    public Timeline(Task[] tasks, Task[] tasksByEst) {
        this(tasks.length);
        initialize(tasks, tasksByEst, false);
    }

    public Timeline(int n) {
        this.n = n;
        processingTime = new int[n];
        timepoints = new int[n+1];
        originalTimepoints = new int[n+1];
        estToTimepointsMap = new int[n];
        set = new DisjointSets(n+1);
        timepointCapcity = new int[n];
    }

    public final void initialize(Task[] tasks, Task[] tasksByEst) {
        initialize(tasks, tasksByEst, false);
    }

    public final void initialize(Task[] tasks, Task[] tasksByEst, boolean takeEnergyAsProcessingTime) {
        // I made a little adjustement to the algoirthm. Instead of adding timepoints only when EST are differents,
        // I allow duplicated timepoints. It allow me to have a predetermined number of timepoints in the constructor
        // of the timeline and do all my "new" calls there, saving a lot of execution time. It does not change the
        // complexity of the algorithm because if there are duplicates timepoints, they will be automatically merged
        // in ScheduleTask. And since each duplicate timepoint will be merged only once and there is at most n-1
        // duplicates, it only add, in worst case, n-1 steps to the global execution. I could pre-merge them in the
        // initialization of the timeline, but as it is, duplicates are merge only if they need to be.
        for (int i = 0; i < n; i++) {
            processingTime[i] = takeEnergyAsProcessingTime ? tasks[i].energy() : tasks[i].processingTime();
            timepoints[i] = tasksByEst[i].earliestStartingTime();
            originalTimepoints[i] = tasksByEst[i].earliestStartingTime();
            estToTimepointsMap[tasksByEst[i].id()] = i;
        }

        timepoints[n] = Integer.MAX_VALUE;

        for (int k = 0; k < n; k++)
            timepointCapcity[k] = timepoints[k+1] - timepoints[k];
        e = -1;
    }

    public final void reinitialize(int newCapacity) {
        for (int i = 0; i < n; i++) {
            timepoints[i] = originalTimepoints[i] * newCapacity;
        }
        timepoints[n] = Integer.MAX_VALUE;

        set.reinitialize();

        for (int k = 0; k < n; k++)
            timepointCapcity[k] = timepoints[k+1] - timepoints[k];
        e = -1;
    }

    public final int earliestCompletionTime() {
        if (e == -1) return Integer.MIN_VALUE;
        return timepoints[e+1] - timepointCapcity[e];
    }

    //i \in [0,n[
    @SuppressWarnings("Duplicates")
    public final void scheduleTask(int i) {
        int rho = processingTime[i];
        int k = set.greatest(estToTimepointsMap[i]);
        while (rho > 0) {
            if (k == timepointCapcity.length)
                throw new RuntimeException();

            int delta = min(timepointCapcity[k], rho);
            rho -= delta;
            timepointCapcity[k] -= delta;
            if (timepointCapcity[k] == 0) {
                set.union(k, k+1);
                k = set.greatest(k);
            }
        }
        e = max(e, k);
    }

    public final int max(int a, int b) {
        return a > b ? a : b;
    }

    public final int min(int a, int b) {
        return a < b ? a : b;
    }

    public final int findSmallest() {
        return timepoints[set.smallest(e)];
    }
}
