public class TimelineWrapper implements IThetaSet {
    private Timeline timeline;

    public TimelineWrapper(int n) {
        timeline = new Timeline(n);
    }

    @Override
    public void setTasks(Task[] tasks, Task[] sortedTasks) {
        // We only need to adapt the processing time, which become the energy of the task. It does not change between
        // execution of the algo. For the sorted task, the processing time is not relevant in timeline initialisation
        timeline.initialize(tasks, sortedTasks, true);
    }

    @Override
    public void initialize(int C, int ci) {
        int c = C - ci;

        timeline.reinitialize(c);
    }

    @Override
    public void add(int i) {
        timeline.scheduleTask(i);
    }

    @Override
    public int envThetaCi() {
        return timeline.earliestCompletionTime();
    }
}
