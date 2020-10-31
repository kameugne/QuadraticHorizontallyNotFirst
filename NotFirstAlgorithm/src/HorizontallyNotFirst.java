import java.util.*;

public class HorizontallyNotFirst {
    private Integer C;
    private Integer n;
    Task[] tasks;
    Profile tl;

    private Integer[] tasks_indices_lct;
    private Integer[] tasks_indices_est;
    private Integer[] tasks_indices_ect;
    private int[] estPrime;

    Integer makespan;
    public HorizontallyNotFirst(Task[] tasks, int C) {
        this.C = C;
        this.n = tasks.length;
        this.tasks = tasks;
        this.tl = new Profile();
        estPrime = new int[tasks.length];

        tasks_indices_lct = sortWithJavaLibrary(tasks, new Task.ComparatorByLctByEstReverseHeightReverseEct(tasks)); //Increasing LCT
        tasks_indices_est = sortWithJavaLibrary(tasks, new Task.ComparatorByEst(tasks)); //Increasing EST
        tasks_indices_ect = sortWithJavaLibrary(tasks, new Task.ComparatorByEct(tasks)); //Increasing ECT
        makespan = Integer.MIN_VALUE;
        InitializeTimeLine();
    }

    public int[] Filter() {
        for (int i = 0; i < n; i++)
            estPrime[i] = tasks[i].earliestStartingTime();
        for (int i = n-1; i >= 0; i--) {
            if (tasks[tasks_indices_lct[i]].inLambda && tasks[tasks_indices_lct[i]].earliestCompletionTime() < tasks[tasks_indices_lct[i]].latestCompletionTime()) {
                int MaxLct = InitializeIncrements(i);
                ScheduleTasks(MaxLct, i);
                int Energy = 0;
                int ect = Integer.MAX_VALUE;
                int est = Integer.MAX_VALUE;
                int estEnergyOverflow = 0;
                boolean isNotFirst = false;
                int j = 0;
                while (j < n && !isNotFirst){
                    if (j != i && tasks[tasks_indices_lct[i]].earliestStartingTime() < tasks[tasks_indices_lct[j]].earliestCompletionTime()) {
                        Energy += tasks[tasks_indices_lct[j]].energy();
                        ect = Math.min(ect, tasks[tasks_indices_lct[j]].earliestCompletionTime());
                        if (tasks[tasks_indices_lct[j]].earliestStartingTime() <= est) {
                            est = Math.min(est, tasks[tasks_indices_lct[j]].earliestStartingTime());
                            estEnergyOverflow = tasks[tasks_indices_lct[j]].est_to_timepoint.energy - tasks[tasks_indices_lct[j]].est_to_timepoint.overflow;
                        }
                        Timepoint t = tasks[tasks_indices_lct[j]].lct_to_timepoint;
                        if (t.overflow > t.energy - estEnergyOverflow - Energy){
                            estPrime[tasks_indices_lct[i]] = Math.max(ect, estPrime[tasks_indices_lct[i]]);
                            tasks[tasks_indices_lct[i]].inLambda = false;
                            isNotFirst = true;
                        }
                    }
                    j++;
                }
                if (isNotFirst) {
                    int u = 0;
                    while (u < i) {
                        if (tasks[tasks_indices_lct[u]].earliestStartingTime() <= tasks[tasks_indices_lct[i]].earliestStartingTime() &&
                                tasks[tasks_indices_lct[u]].height() >= tasks[tasks_indices_lct[i]].height() &&
                                tasks[tasks_indices_lct[u]].earliestCompletionTime() >= tasks[tasks_indices_lct[i]].earliestCompletionTime()) {
                            if (tasks[tasks_indices_lct[u]].inLambda && tasks[tasks_indices_lct[i]].earliestStartingTime() >= tasks[tasks_indices_lct[u]].earliestCompletionTime() ||
                                    tasks[tasks_indices_lct[u]].latestCompletionTime() > tasks[tasks_indices_lct[j-1]].latestCompletionTime()) {
                                estPrime[tasks_indices_lct[u]] = Math.max(ect, estPrime[tasks_indices_lct[u]]);
                                tasks[tasks_indices_lct[u]].inLambda = false;
                            }
                        }
                        u++;
                    }
                }else {
                    int u = 0;
                    while (u < i) {
                        if (tasks[tasks_indices_lct[u]].earliestStartingTime() == tasks[tasks_indices_lct[i]].earliestStartingTime() &&
                            tasks[tasks_indices_lct[u]].height() <= tasks[tasks_indices_lct[i]].height() &&
                            tasks[tasks_indices_lct[u]].earliestCompletionTime() <= tasks[tasks_indices_lct[i]].earliestCompletionTime()) {
                            tasks[tasks_indices_lct[u]].inLambda = false;
                        }
                        u++;
                    }
                }
            }
        }
        return estPrime;
    }





    private void ScheduleTasks(int maxLCT, int i) {
        int hreq, hmaxInc, ov, ect, energy;
        ect = Integer.MIN_VALUE;
        ov = hreq = hmaxInc = energy = 0;
        Timepoint t = tl.first;
        while(t.time < maxLCT) {
            int l = t.next.time - t.time;
            hmaxInc += t.incrementMax;
            t.hMaxTotal = hmaxInc;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;
            t.hreal = hreq;
            t.energy = energy;
            t.overflow = ov;

            int hcons = Math.min(hreq + ov, hmax);

            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;
            if (t.time < tasks[tasks_indices_lct[i]].earliestCompletionTime())
                energy += (hreq - tasks[tasks_indices_lct[i]].height()) * l;
            else
                energy += hreq * l;

            t.capacity = C - hcons;

            if(t.capacity < C)
                ect = t.next.time;
            t = t.next;
        }
        t.overflow = ov;
        t.energy = energy;
    }
    private int InitializeIncrements(int ind) {
        Timepoint t = tl.first;
        while(t != null)
        {
            t.increment = 0;
            t.incrementMax = 0;
            t.hMaxTotal = 0;
            t.hreal = 0;
            t.minimumOverflow = 0;
            t.overflow = 0;
            t.capacity = C;
            t.energy = 0;
            t.contact = null;

            t = t.next;
        }
        int index = -1;
        for(int i = 0; i < n; i++) {
            if (i != ind && tasks[tasks_indices_lct[ind]].earliestStartingTime() < tasks[tasks_indices_lct[i]].earliestCompletionTime()) {
                t = tasks[tasks_indices_lct[i]].est_to_timepoint;
                t.increment += tasks[tasks_indices_lct[i]].height();
                t.incrementMax += tasks[tasks_indices_lct[i]].height();

                t = tasks[tasks_indices_lct[i]].ect_to_timepoint;
                t.increment -= tasks[tasks_indices_lct[i]].height();

                t = tasks[tasks_indices_lct[i]].lct_to_timepoint;
                t.incrementMax -= tasks[tasks_indices_lct[i]].height();
                index = i;
            }
        }
        if (index != -1) {
            t = tl.first;
            t.increment += tasks[tasks_indices_lct[ind]].height();
            t.incrementMax += tasks[tasks_indices_lct[ind]].height();
            if (tasks[tasks_indices_lct[ind]].earliestCompletionTime() < tasks[tasks_indices_lct[index]].latestCompletionTime()) {
                t = tasks[tasks_indices_lct[ind]].ect_to_timepoint;
                t.increment -= tasks[tasks_indices_lct[ind]].height();
                t.incrementMax -= tasks[tasks_indices_lct[ind]].height();
            }else{
                t = tasks[tasks_indices_lct[index]].lct_to_timepoint;
                t.increment -= tasks[tasks_indices_lct[ind]].height();
                t.incrementMax -= tasks[tasks_indices_lct[ind]].height();
            }
            return tasks[tasks_indices_lct[index]].latestCompletionTime();
        }
        return Integer.MIN_VALUE;
    }





    /* ------------ Initialization Functions --------------*/
    /*
     * Profile Initialization, as described in chapter 5 of Generalizing the Edge-Finder Rule for the Cumulative Constraint.
     */
    private void InitializeTimeLine() {
        int n = tasks.length;
        tl.Add(new Timepoint(tasks[tasks_indices_est[0]].earliestStartingTime(), C));
        Timepoint t = tl.first;

        int p,i,j,k;
        p = i = j = k = 0;

        int maxLCT = Integer.MIN_VALUE;

        while(i < n || j < n || k < n)
        {
            if(i<n && (j == n || tasks[tasks_indices_est[i]].earliestStartingTime() <= tasks[tasks_indices_ect[j]].earliestCompletionTime()) &&
                    (k == n || tasks[tasks_indices_est[i]].earliestStartingTime() <= tasks[tasks_indices_lct[k]].latestCompletionTime()))
            {
                if(tasks[tasks_indices_est[i]].earliestStartingTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_est[i]].earliestStartingTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_est[i]].est_to_timepoint = t;
                p += tasks[tasks_indices_est[i]].processingTime();
                maxLCT = Math.max(maxLCT, tasks[tasks_indices_est[i]].latestCompletionTime());
                tasks[tasks_indices_est[i]].inLambda = true;
                i++;
            }
            else if(j < n && (k==n || tasks[tasks_indices_ect[j]].earliestCompletionTime() <= tasks[tasks_indices_lct[k]].latestCompletionTime()))
            {
                if(tasks[tasks_indices_ect[j]].earliestCompletionTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_ect[j]].earliestCompletionTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_ect[j]].ect_to_timepoint = t;
                j++;
            }
            else
            {
                if(tasks[tasks_indices_lct[k]].latestCompletionTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasks[tasks_indices_lct[k]].latestCompletionTime(), C));
                    t = t.next;
                }
                tasks[tasks_indices_lct[k]].lct_to_timepoint = t;
                k++;
            }

        }
        t.InsertAfter(new Timepoint(maxLCT + p, 0));
    }



    /* ------------ Utility Functions --------------*/
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
