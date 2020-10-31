import java.util.Arrays;
import java.util.Comparator;

public class ThetaTreeNotFirst {
    private Task[] tasks;
    private Task[] tasksByLct;
    private Task[] tasksByEst;
    private int[] estPrime;
    private int n;
    private int C;

    Integer[] tasks_indices_lct;
    Integer[] tasks_indices_est;
    Integer makespan;
    public ThetaTreeNotFirst(Task[] tasks, int C) {
        n = tasks.length;
        this.tasks = tasks;
        this.C = C;
        estPrime = new int[n];
        tasksByLct = new Task[n];
        tasksByEst = new Task[n];
        tasks_indices_lct = sortWithJavaLibrary(tasks, new Task.ComparatorByLct(tasks)); //Increasing LCT
        tasks_indices_est = sortWithJavaLibrary(tasks, new Task.ComparatorByEst(tasks)); //Increasing EST
        for (int i = 0; i < n; i++) {
            tasksByEst[i] = tasks[tasks_indices_est[i]];
            tasksByLct[i] = tasks[tasks_indices_lct[i]];
        }
        makespan = Integer.MIN_VALUE;
    }
    public int[] filter() {
        for (int i = 0; i < n; i++)
            estPrime[i] = tasks[i].earliestStartingTime();

        /*EfficientExtendedThetaTree theta = new EfficientExtendedThetaTree(n);
        theta.intialize(tasksByEst);
        theta.reset(C, 0);
        for (int k = 0; k < n; k++) {
        	theta.addTask(tasksByLct[k].id());
        }
        makespan = (int)Math.ceil((double)theta.getEnvC() / (double)C);
        if (makespan > tasks[tasks_indices_lct[n-1]].latestCompletionTime())
            return estPrime;*/

        EfficientExtendedThetaTree thetaSet = new EfficientExtendedThetaTree(n);
        thetaSet.intialize(tasksByEst);
        for (int k = 0; k < n; k++) {
            Task iTask = tasksByLct[k];
            int i = iTask.id();
            int ci = iTask.height();
            int minEct = Integer.MAX_VALUE;
            thetaSet.reset(C, ci);
            if (iTask.earliestCompletionTime() < iTask.latestCompletionTime()){
                for (int l = 0; l < n; l++) {
                    Task jTask = tasksByLct[l];
                    int j = jTask.id();
                    if (iTask.earliestStartingTime() < jTask.earliestCompletionTime() && j != i) {
                        thetaSet.addTask(j);
                        minEct = Math.min(minEct, jTask.earliestCompletionTime());
                        int Clctj = C * jTask.latestCompletionTime();
                        int ciMin = ci * Math.min(iTask.earliestCompletionTime(), jTask.latestCompletionTime());
                        if (thetaSet.getEnvC() > Clctj - ciMin){
                            estPrime[i] = Math.max(estPrime[i], minEct);
                            break;
                        }
                    }
                }
            }
        }

        return estPrime;
    }
    public int[] filterWithTimeline() {
        return filter();
    }
    private static Integer[] sortWithJavaLibrary(Task[] tasks, Comparator<Integer> comparator) {

        int n = tasks.length;
        Integer[] tasks_indices = new Integer[n];
        for (int q = 0; q < n; q++) {
            tasks_indices[q] = new Integer(q);
        }
        Arrays.sort(tasks_indices, comparator);
        return tasks_indices;
    }
}
