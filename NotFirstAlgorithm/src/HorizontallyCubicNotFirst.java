import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class HorizontallyCubicNotFirst {
    private Task[] tasks;
    Integer[] tasks_indices_lct;
    Integer[] tasks_indices_est;
    Integer[] tasks_indices_ect;
    Profile tl;
    Integer makespan;

    private int[] estPrime;
    private int n;
    private int C;
    private Task[] tasksByLct;
    private Task[] tasksByEst;
    private Task[] tasksByEct;

    public HorizontallyCubicNotFirst(Task[] tasks, int C) {
        this.n = tasks.length;
        this.tasks = tasks;
        this.C = C;
        estPrime = new int[n];
        this.tl = new Profile();

        tasks_indices_lct = sortWithJavaLibrary(tasks, new Task.ComparatorByLctReverseEstByHeightByEct(tasks)); //Increasing LCT
        tasks_indices_est = sortWithJavaLibrary(tasks, new Task.ComparatorByEst(tasks)); //Increasing EST
        tasks_indices_ect = sortWithJavaLibrary(tasks, new Task.ComparatorByEct(tasks)); //Increasing ECT
        tasksByLct = new Task[n];
        tasksByEst = new Task[n];
        tasksByEct = new Task[n];
        for (int i = 0; i < n; i++) {
            tasksByLct[i] = tasks[tasks_indices_lct[i]];
            tasksByEst[i] = tasks[tasks_indices_est[i]];
            tasksByEct[i] = tasks[tasks_indices_ect[i]];
        }
        makespan = Integer.MIN_VALUE;
        InitializeTimeLine();
    }








    private int[] Filter() {
        for (int i = 0; i < n; i++)
            estPrime[i] = tasks[i].earliestStartingTime();

        for (int l = n-1; l >= 0; l--)
        {
            Task iTask = tasksByLct[l];
            int i = iTask.id();
            if(iTask.earliestCompletionTime() < iTask.latestCompletionTime() && iTask.inLambda == false)
            {
                boolean found = false; int lct = -1;
                InitializeIncrementsOfConflictingTask(i);
                ArrayList<Integer> indices = ScheduleConflictingTask(i);
                if(found == false && indices.size() != 0)
                {
                    int u = indices.size() - 1;
                    while(u >= 0 && found == false)
                    {
                        int k = indices.get(u);
                        int ECT = ExtendInitializeIncrements(k,i);
                        int ect = ScheduleTasks(tasksByLct[k].latestCompletionTime());
                        if(ect > tasksByLct[k].latestCompletionTime())
                        {
                            estPrime[i] = Math.max(estPrime[i], ECT);
                            found = true; lct = k;
                        }
                        u--;
                    }
                }
                if(found == true && lct > 0)
                {
                    for(int v = l-1; v >= 0; v--)
                    {
                        if(tasksByLct[v].earliestStartingTime() <= tasksByLct[l].earliestStartingTime()
                                && tasksByLct[v].height() >= tasksByLct[l].height()
                                && tasksByLct[v].earliestCompletionTime() >= tasksByLct[l].earliestCompletionTime())
                        {
                            int ECT = ExtendInitializeIncrements(lct,tasksByLct[v].id());
                            int ect = ScheduleTasks(tasksByLct[lct].latestCompletionTime());
                            if(ect > tasksByLct[lct].latestCompletionTime())
                            {
                                estPrime[tasksByLct[v].id()] = Math.max(estPrime[tasksByLct[v].id()], ECT);
                            }
                            tasksByLct[v].inLambda = true;
                        }
                    }
                }
                if(found == false)
                {
                    for(int v = l-1; v >= 0; v--)
                    {
                        if(tasksByLct[v].earliestStartingTime() == tasksByLct[l].earliestStartingTime()
                                && tasksByLct[v].height() <= tasksByLct[l].height()
                                && tasksByLct[v].earliestCompletionTime() <= tasksByLct[l].earliestCompletionTime())
                            tasksByLct[v].inLambda = true;
                    }
                }
            }
        }
        return estPrime;
    }




    public ArrayList<Integer> ScheduleConflictingTask(int i) {
        int hreq, hmaxInc, ov, ect, t0;
        ect = Integer.MIN_VALUE; t0 = -1;
        ov = hreq = hmaxInc = 0;
        ArrayList<Integer> indices = new ArrayList<>();
        Timepoint t = tl.first;

        while(t.next != null)
        {
            int l = t.next.time - t.time;

            hmaxInc += t.incrementMax;
            t.hMaxTotal = hmaxInc;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;
            t.hreal = hreq;
            t.overflow = ov;
            if(t.overflow > 0 && t0 == -1)
                t0 = t.previous.time;
            int hcons = Math.min(hreq + ov, hmax);
            //t.consumption = hcons;
            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;
            t.capacity = C - hcons;
            if(t.capacity < C)
                ect = t.next.time;

            t = t.next;
        }
        int h = 0;
        for(int k = 0; k < tasks.length; k++)
        {
            if(i != tasksByLct[k].id() && tasks[i].earliestStartingTime() < tasksByLct[k].earliestCompletionTime())
            {
                h += tasksByLct[k].height();
                t = tasksByLct[k].lct_to_timepoint;
                if(h + tasks[i].height() > C && t.overflow > 0)
                {
                    indices.add(k);
                    while(k+1 < tasks.length && tasksByLct[k].latestCompletionTime() == tasksByLct[k+1].latestCompletionTime())
                        k++;
                }
            }
        }
        return indices;
    }





    public void InitializeIncrementsOfConflictingTask(int l) {
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

            t = t.next;
        }
        //int minECT = Integer.MAX_VALUE;
        for(int i = 0; i < tasks.length; i++)
        {
            if(tasksByLct[i].id() != l && tasks[l].earliestStartingTime() < tasksByLct[i].earliestCompletionTime())
            {
                t = tasksByLct[i].est_to_timepoint;
                t.increment += tasksByLct[i].height();
                t.incrementMax += tasksByLct[i].height();

                t = tasksByLct[i].ect_to_timepoint;
                t.increment -= tasksByLct[i].height();


                t = tasksByLct[i].lct_to_timepoint;
                t.incrementMax -= tasksByLct[i].height();
                //minECT = Math.min(minECT, tasksByLct[i].earliestCompletionTime());
            }
        }
        t = tasksByEst[0].est_to_timepoint;
        t.increment += tasks[l].height();
        t.incrementMax += tasks[l].height();

        t = tasks[l].ect_to_timepoint;
        t.increment -= tasks[l].height();
        t.incrementMax -= tasks[l].height();

        //return minECT;
    }






    public int[] filterWithTimeline() {
        return Filter();
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

    private int ExtendInitializeIncrements(int k, int l)
    {
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

            t = t.next;
        }
        int minEct = Integer.MAX_VALUE;
        for(int i = 0; i < n; i++)
        {
            if(tasksByLct[i].id() != l && tasks[l].earliestStartingTime() < tasksByLct[i].earliestCompletionTime() &&
                    tasksByLct[i].latestCompletionTime() <= tasksByLct[k].latestCompletionTime())
            {
                minEct = Math.min(minEct, tasksByLct[i].earliestCompletionTime());
                t = tasksByLct[i].est_to_timepoint;
                t.increment += tasksByLct[i].height();
                t.incrementMax += tasksByLct[i].height();

                t = tasksByLct[i].ect_to_timepoint;
                t.increment -= tasksByLct[i].height();


                t = tasksByLct[i].lct_to_timepoint;
                t.incrementMax -= tasksByLct[i].height();
            }
        }
        t = tasksByEst[0].est_to_timepoint;
        t.increment += tasks[l].height();
        t.incrementMax += tasks[l].height();

        if(tasks[l].earliestCompletionTime() <= tasksByLct[k].latestCompletionTime() )
        {
            t = tasks[l].ect_to_timepoint;
            t.increment -= tasks[l].height();
            t.incrementMax -= tasks[l].height();
        }else{
            t = tasksByLct[k].lct_to_timepoint;
            t.increment -= tasks[l].height();
            t.incrementMax -= tasks[l].height();
        }

        return minEct;
    }

    private int ScheduleTasks(int maxLCT)
    {
        int hreq, hmaxInc, ov, ect;
        ect = Integer.MIN_VALUE;
        ov = hreq = hmaxInc = 0;
        Timepoint t = tl.first;

        while(t.time < maxLCT)
        {
            int l = t.next.time - t.time;

            hmaxInc += t.incrementMax;
            t.hMaxTotal = hmaxInc;
            int hmax = Math.min(hmaxInc, C);
            hreq += t.increment;

            int hcons = Math.min(hreq + ov, hmax);

            if(ov > 0 && ov < (hcons - hreq) * l)
            {
                l = Math.max(1, ov / (hcons-hreq));
                t.InsertAfter(new Timepoint(t.time + l, t.capacity));
            }
            ov += (hreq - hcons) * l;
            t.capacity = C - hcons;
            if(t.capacity < C)
                ect = t.next.time;

            t = t.next;
        }
        if(ov > 0)
            return Integer.MAX_VALUE;

        return ect;
    }

    private void InitializeIncrements(int maxIndex)
    {
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

            t = t.next;
        }
        for(int i = 0; i <= maxIndex; i++)
        {
            t = tasks[tasks_indices_lct[i]].est_to_timepoint;
            t.increment += tasks[tasks_indices_lct[i]].height();
            t.incrementMax += tasks[tasks_indices_lct[i]].height();

            t = tasks[tasks_indices_lct[i]].ect_to_timepoint;
            t.increment -= tasks[tasks_indices_lct[i]].height();

            t = tasks[tasks_indices_lct[i]].lct_to_timepoint;
            t.incrementMax -= tasks[tasks_indices_lct[i]].height();
        }
    }



    private void InitializeTimeLine()
    {
        int n = tasks.length;
        tl.Add(new Timepoint(tasksByEst[0].earliestStartingTime(), C));
        Timepoint t = tl.first;

        int p,i,j,k;
        p = i = j = k = 0;

        int maxLCT = Integer.MIN_VALUE;

        while(i < n || j < n || k < n)
        {
            if(i<n && (j==n || tasksByEst[i].earliestStartingTime() <=  tasksByEct[j].earliestCompletionTime()) &&
                    (k ==n || tasksByEst[i].earliestStartingTime() <= tasksByLct[k].latestCompletionTime()))
            {
                if(tasksByEst[i].earliestStartingTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasksByEst[i].earliestStartingTime(), C));
                    t = t.next;
                }
                tasksByEst[i].est_to_timepoint = t;
                p += tasksByEst[i].processingTime();
                maxLCT = Math.max(maxLCT, tasksByEst[i].latestCompletionTime());

                tasksByEst[i].inLambda = false;

                i++;
            }
            else if(j < n && (k==n || tasksByEct[j].earliestCompletionTime() <= tasksByLct[k].latestCompletionTime()))
            {
                if(tasksByEct[j].earliestCompletionTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasksByEct[j].earliestCompletionTime(), C));
                    t = t.next;
                }
                tasksByEct[j].ect_to_timepoint = t;
                j++;
            }
            else
            {
                if(tasksByLct[k].latestCompletionTime() > t.time)
                {
                    t.InsertAfter(new Timepoint(tasksByLct[k].latestCompletionTime(), C));
                    t = t.next;
                }
                tasksByLct[k].lct_to_timepoint = t;
                k++;
            }
        }
        t.InsertAfter(new Timepoint(maxLCT + p, 0));
    }
}
