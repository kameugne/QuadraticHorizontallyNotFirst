public final class EfficientExtendedThetaTree {
    private int C;
    private int n;
    private int ci;
    private Task[] tasks;

    private int[] envOfNodes;
    private int[] energyOfNodes;
    private int[] cEnvOfNodes;

    private int firstIndexOnLowestLevel;
    private int lastIndexOnLowestLevel;
    private int[] taskIndexToNodeIndex;

    private int[] taskIndices;

    public EfficientExtendedThetaTree(int n) {
        this.n = n;
        int arraySize = 2 * n - 1;
        envOfNodes = new int[arraySize];
        energyOfNodes = new int[arraySize];
        cEnvOfNodes = new int[arraySize];
        taskIndexToNodeIndex = new int[n];
    }

    public final void intialize(Task[] sortedTasks) {
        tasks = sortedTasks;
        ci = 0;

        firstIndexOnLowestLevel = nextPowerOfTwoMinusOne(n);
        lastIndexOnLowestLevel = 2 * (n-1);

        for (int i = 0; i < n; i++) {
            taskIndexToNodeIndex[tasks[i].id()] = i;

            int b = getNodeIndexWithLeafIndex(i);
            envOfNodes[b] = Integer.MIN_VALUE;
            energyOfNodes[b] = 0;
            cEnvOfNodes[b] = Integer.MIN_VALUE;
        }

        for (int i = n - 2; i >= 0; i--) {
            updateNode(i);
        }
    }

    public final void reset(int C, int ci) {
        this.C = C;
        this.ci = ci;
        for (int i = 0; i < 2 * n -1; i++) {
            energyOfNodes[i] = 0;
            envOfNodes[i] = Integer.MIN_VALUE;
            cEnvOfNodes[i] = Integer.MIN_VALUE;
        }
    }

    public final void addTask(int index) {
        int i = taskIndexToNodeIndex[index];
        int b = getNodeIndexWithLeafIndex(i);

        Task task = tasks[i];
        energyOfNodes[b] = task.energy();
        envOfNodes[b] = task.envelop(C);
        cEnvOfNodes[b] = task.envelop(C-ci);

        updateInnerNodes(b);
    }

    public final int getEnvC() {
        return cEnvOfNodes[0];
    }
    public final int getEnv() {
        return envOfNodes[0];
    }

    private final int left(int index) {
        return 2 * index + 1;
    }

    private final int right(int index) {
        return 2 * index + 2;
    }

    private final int parent(int index) {
        return (index+1) / 2 - 1;
    }

    private final boolean isLeaf(int index) {
        return index >= n -1;
    }

    private final boolean isLeft(int index) {
        return index % 2 == 1;
    }

    private final void updateInnerNodes(int leafIndex) {
        int w = (leafIndex - 1) / 2;
        int t = 0;

        if (leafIndex == 0)
            return;

        do {
            updateNode(w);
            w = (int) ((w - 1.0) / 2);
            if (w == 0)
                t++;
            if (t > 1)
                break;
        } while (w >= 0);
    }

    private final int getNodeIndexWithLeafIndex(int index) {
        int b;
        if (index <= lastIndexOnLowestLevel - firstIndexOnLowestLevel)
            b = firstIndexOnLowestLevel + index;
        else
            b= lastIndexOnLowestLevel / 2 + index - (lastIndexOnLowestLevel - firstIndexOnLowestLevel + 1);
        return b;
    }

    private final void updateNode(int i) {
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        envOfNodes[i] = max(envOfNodes[right], plus(envOfNodes[left], energyOfNodes[right]));
        energyOfNodes[i] = plus(energyOfNodes[left], energyOfNodes[right]);
        cEnvOfNodes[i] = max(plus(cEnvOfNodes[left], energyOfNodes[right]), cEnvOfNodes[right]);
    }

    private final int plus(int a, int b)
    {
        if(a == Integer.MIN_VALUE || b == Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        else
            return a + b;
    }

    private final int max(int a, int b) {
        return a > b ? a : b;
    }

    private final int nextPowerOfTwoMinusOne(int n) {
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n;
    }
}