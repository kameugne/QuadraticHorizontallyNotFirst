import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;

public class RunRCPSP {
    // not-first/not-last propagator used
    private static final int TimeLine = 0;
    private static final int Horizontally = 1;
    private static final int CombQuadHoriz = 2;
    private static final int OldHorizontally = 3;

    // edge finder propagator used
    private static final int GingrasEF = 4;
    private static final int QuadSeverineEF = 5;
    private static final int CubicSeverineEF = 6;

    // combinaison of edge finder and not-first/not-last propagator used
    private static final int GingrasEF_TimeLine = 7;
    private static final int GingrasEF_Horizontally = 8;
    private static final int QuadSeverineEF_Horizontally = 9;
    private static final int CubicSeverineEF_OldHorizontally = 10;

    // search strategy used
    private static final int staticSearch = 0;
    private static final int dynamicSearchDOWS = 1;
    private static final int dynamicSearchCOSDOWS = 2;
    private static final int dynamicSearchMDLB = 3;
    private static final int dynamicSearchCOSMDLB = 4;

    private int m_numberOfTasks;
    private int m_numberOfResources;
    private int m_solution[];
    private float m_elapsedTime;
    private long m_backtracksNum;
    private long m_visitedNodes;
    private int m_makespan;
    private int m_adjustements;
    public RunRCPSP(String fileName, int propagator, int search) throws Exception {
        // new model creation
        Model model = new Model("RCPSP Solver");
        // read data from a file
        RCPSPInstance data = new RCPSPInstance(fileName);

        this.m_numberOfTasks = data.numberOfTasks;
        this.m_numberOfResources = data.numberOfResources;
        // variables of the problem
        IntVar[] startingTimes = new IntVar[m_numberOfTasks];
        IntVar[] processingTimes = new IntVar[m_numberOfTasks];
        IntVar[] endingTimes = new IntVar[m_numberOfTasks];

        for (int i = 0; i < m_numberOfTasks; i++) {
            startingTimes[i] = model.intVar("s[" + i + "]", 0, data.horizon(), true);
            endingTimes[i] = model.intVar("e[" + i + "]", data.processingTimes[i], data.horizon(), true);
            processingTimes[i] = model.intVar("p[" + i + "]", data.processingTimes[i]);
        }
        // the dummy task 0 starts at time 0
        model.arithm(startingTimes[0], "=", 0).post();
        // Makespan
        IntVar makespan = model.intVar("makespan", 0, data.horizon(), true);
        for (int i = 0; i < m_numberOfTasks; i++) {
            model.arithm(endingTimes[i], "<=", makespan).post();
        }
        model.arithm(makespan, "=", endingTimes[m_numberOfTasks-1]).post();
        // propagation of precedence constraints
        for(int i = 0; i< m_numberOfTasks; i++)
        {
            for(int j = i+1; j< m_numberOfTasks; j++)
            {
                if(data.precedences[i][j] == 1)
                {
                    model.arithm(startingTimes[i], "+", processingTimes[i], "<=", startingTimes[j]).post();
                }
                else if(data.precedences[i][j] == 0)
                {
                    model.arithm(startingTimes[j], "+", processingTimes[j], "<=", startingTimes[i]).post();
                }
            }
        }
        // new auxillary variable extended startingTime variable with makespan
        IntVar[] startingTimes_and_makespan = new IntVar[m_numberOfTasks+1];
        System.arraycopy(startingTimes, 0, startingTimes_and_makespan, 0, m_numberOfTasks);
        startingTimes_and_makespan[m_numberOfTasks] = makespan;

        // new class to count the number of propagations of each propagator
        AdjustmentsPropagator[] propagators = new AdjustmentsPropagator[m_numberOfResources];
        // propagate resource constraint
        for(int i = 0; i< m_numberOfResources; i++) {
            IntVar[] heights = new IntVar[m_numberOfTasks];
            for (int j = 0; j < m_numberOfTasks; j++) {
                heights[j] = model.intVar("h[" + i + "][" + j + "]", data.heights[i][j]);
            }
            // only consider tasks with positive heigth
            ArrayList<Integer> indices = new ArrayList<>();
            for (int j = 0; j < data.heights[i].length; j++) {
                if (data.heights[i][j] > 0) {
                    indices.add(j);
                }
            }
            // filtering variable by considering only those with positive heigth
            if (indices.size() != 0) {
                IntVar[] filtered_startingTimes_makespan = new IntVar[indices.size() + 1];
                IntVar[] filtered_endingTimes = new IntVar[indices.size()];
                Integer[] filtered_heights = new Integer[indices.size()];
                Integer[] filtered_processingTimes = new Integer[indices.size()];
                Task[] filtered_tasks = new Task[indices.size()];
                IntVar[] filtered_heights_var = new IntVar[indices.size()];

                for (int j = 0; j < indices.size(); j++) {
                    int index = indices.get(j);
                    // auxillary variable extraction
                    filtered_startingTimes_makespan[j] = startingTimes[index];
                    filtered_endingTimes[j] = endingTimes[index];
                    filtered_heights[j] = data.heights[i][index];
                    filtered_processingTimes[j] = data.processingTimes[index];
                    // convert variable to format requiere by choco
                    filtered_tasks[j] = new Task(startingTimes[index], processingTimes[index], endingTimes[index]);
                    filtered_heights_var[j] = heights[index];
                }
                // add makespan to the current startTime variable
                filtered_startingTimes_makespan[indices.size()] = makespan;
                // switch to differents propagators
                switch(propagator){
                    case TimeLine:
                        Constraint TimeLineNotFirst = new Constraint("Quadratic Not-First With TimeLine",
                                propagators[i] = new TimeLineNotFirstConstraint(
                                filtered_startingTimes_makespan,
                                filtered_endingTimes,
                                filtered_heights,
                                filtered_processingTimes,
                                data.capacities[i]));
                        model.post(TimeLineNotFirst);
                        break;
                    case Horizontally:
                        Constraint HorizontallyNotFirst = new Constraint("Quadratic Not-First With Profile",
                                propagators[i] = new HorizontallyNotFirstConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(HorizontallyNotFirst);
                        break;
                    case OldHorizontally:
                        Constraint OldHorizontallyNotFirst = new Constraint("Relaxed Cubic Not-First With Profile",
                                propagators[i] = new HorizontallyCubicNotFirstConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(OldHorizontallyNotFirst);
                        break;
                    case CombQuadHoriz:
                        Constraint CombQuadHorizontallyNotFirst = new Constraint("Combine Quad Not-First With Profile and TimeLine",
                                propagators[i] = new CombQuadHorizNotFirstConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(CombQuadHorizontallyNotFirst);
                        break;
                    case GingrasEF:
                        Constraint GingrasEF = new Constraint("Gingras and Quimper Horizontally elastic Edge Finder",
                                propagators[i] = new EdgeFinderConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(GingrasEF);
                        break;
                    case QuadSeverineEF:
                        Constraint QuadSeverineEF = new Constraint("Fetgo et al. Quad Horizontally elastic Edge Finder",
                                propagators[i] = new QuadEdgeFinderConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(QuadSeverineEF);
                        break;
                    case CubicSeverineEF:
                        Constraint CubicSeverineEF = new Constraint("Fetgo and Tayou Cubic Horizontally elastic Edge Finder",
                                propagators[i] = new CubicEdgeFinderConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(CubicSeverineEF);
                        break;
                    case GingrasEF_TimeLine:
                        Constraint GingrasEFC = new Constraint("Gingras and Quimper Horizontally elastic Edge Finder",
                                propagators[i] = new EdgeFinderConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        Constraint TimeLineNotFirstC = new Constraint("Quadratic Not-First With TimeLine",
                                propagators[i] = new TimeLineNotFirstConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(TimeLineNotFirstC);
                        model.post(GingrasEFC);
                        break;
                    case GingrasEF_Horizontally:
                        Constraint GingrasEFCC = new Constraint("Gingras and Quimper Horizontally elastic Edge Finder",
                                propagators[i] = new EdgeFinderConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        Constraint HorizontallyNotFirstC = new Constraint("Quadratic Not-First With Profile",
                                propagators[i] = new HorizontallyNotFirstConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(HorizontallyNotFirstC);
                        model.post(GingrasEFCC);
                        break;
                    case QuadSeverineEF_Horizontally:
                        Constraint QuadSeverineEFC = new Constraint("Fetgo et al. Quad Horizontally elastic Edge Finder",
                                propagators[i] = new QuadEdgeFinderConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        Constraint HorizontallyNotFirstCC = new Constraint("Quadratic Not-First With Profile",
                                propagators[i] = new HorizontallyNotFirstConstraint(
                                        filtered_startingTimes_makespan,
                                        filtered_endingTimes,
                                        filtered_heights,
                                        filtered_processingTimes,
                                        data.capacities[i]));
                        model.post(HorizontallyNotFirstCC);
                        model.post(QuadSeverineEFC);
                        break;
                    default:
                        model.cumulative(filtered_tasks, filtered_heights_var, model.intVar("capacity", data.capacities[i]), false, Cumulative.Filter.TIME).post();
                }
                model.cumulative(filtered_tasks, filtered_heights_var, model.intVar("capacity", data.capacities[i]), false, Cumulative.Filter.TIME).post();
            }
        }
        model.setObjective(false, makespan);
        Solver solver = model.getSolver();

        // switch to different search
        switch(search) {
            case staticSearch:
                solver.setSearch(Search.intVarSearch(new StaticVarOrder(model), new IntDomainMin(), startingTimes_and_makespan));
                break;
            case dynamicSearchDOWS:
                solver.setSearch(Search.domOverWDegSearch(startingTimes_and_makespan));
                break;
            case dynamicSearchCOSDOWS:
                solver.setSearch(Search.conflictOrderingSearch(Search.domOverWDegSearch(startingTimes_and_makespan)));
                break;
            case dynamicSearchMDLB:
                solver.setSearch(Search.minDomLBSearch(startingTimes_and_makespan));
                break;
            case dynamicSearchCOSMDLB:
                solver.setSearch(Search.conflictOrderingSearch(Search.minDomLBSearch(startingTimes_and_makespan)));
                break;
            default:
                solver.setSearch(Search.conflictOrderingSearch(Search.intVarSearch(new SmallestVarOrder(model), new IntDomainMin(), startingTimes_and_makespan)));
        }
        solver.setRestartOnSolutions();
        solver.limitTime(1*60*1000);
        // solution of the problem
        Solution best = solver.findOptimalSolution(makespan, false);
        m_solution = new int[m_numberOfTasks];
        if (best == null) {
            m_makespan = -1;
        } else if(solver.isObjectiveOptimal()) {
            m_makespan = best.getIntVal(makespan);
            for (int i = 0; i < m_numberOfTasks; i++){
                m_solution[i] = best.getIntVal(startingTimes[i]);
            }
        } else {
            m_makespan = -1;
        }
        //m_makespan = best.getIntVal(makespan);
        m_elapsedTime =  solver.getTimeCount();
        m_backtracksNum = solver.getBackTrackCount();
        m_visitedNodes = solver.getNodeCount(); //Retourne le nombre de noeuds visit�s dans l'arbre.
        m_adjustements = 0;
        for (int i = 0; i < m_numberOfResources; i++) {
            //On filtre les variables qui on un height null
            ArrayList<Integer> indices = new ArrayList<>();
            for(int j=0; j<data.heights[i].length; j++) {
                if(data.heights[i][j] > 0) {
                    indices.add(j);
                }
            }
            if(indices.size() != 0)
                m_adjustements += propagators[i].getNbAdjustments();
        }
    }
    public float howMuchTime() {
        return m_elapsedTime;
    }
    public long howManyBacktracks() {
        return m_backtracksNum;
    }
    public long howManyVisitedNodes() {
        return m_visitedNodes;
    }
    public int howManyAdjustments() {
        return m_adjustements;
    }
    public int makeSpanSolution() {
        return m_makespan;
    }
    public void printResults() {
        System.out.print(m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + " | " + m_adjustements + "\t \t");
    }
    public String getResults() {
        return m_makespan + " | " + m_elapsedTime + " | " + m_backtracksNum + " | " + m_adjustements + "\t \t";
    }
    public void printSolution() {
        for (int i = 0; i < m_numberOfTasks; i++){
            System.out.print("s["+ i + "] = "+ m_solution[i] + " , ");
        }
        System.out.println();
    }
}
