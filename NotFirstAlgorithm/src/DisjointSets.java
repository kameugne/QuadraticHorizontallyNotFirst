public final class DisjointSets {
    private int[] R;
    private int[] max;
    private int[] min;
    private int n;

    public DisjointSets(int n) {
        this.n = n;
        R = new int[n];
        max = new int[n];
        min = new int[n];

        for (int i = 0; i < n; i++) {
            max[i] = i;
            min[i] = i;
            R[i] = -1;
        }
    }

    public final void reinitialize() {
        for (int i = 0; i < n; i++) {
            max[i] = i;
            min[i] = i;
            R[i] = -1;
        }
    }

    public final int find(int x) {
        int r = x;
        while (R[r] >= 0)
            r = R[r];

        int t;
        while (x != r) {
            t = R[x];
            R[x] = r;
            x = t;
        }

        return r;
    }

    public final void union(int v1, int v2) {
        int x = find(v1);
        int y = find(v2);
        if (x == y) throw new IllegalArgumentException("Sets are already united");

        int p = link(x,y);
        max[p] = max(max[x], max[y]);
        min[p] = min(min[x], min[y]);
    }

    public final int greatest(int v) {
        int x = find(v);
        return max[x];
    }

    public final int smallest(int v) {
        int x = find(v);
        return min[x];
    }

    private final int link(int x, int y) {
        assert(R[x] < 0 && R[y] < 0);
        if (R[x] < R[y]) {
            R[y] = x;
            return x;
        } else if (R[x] > R[y]) {
            R[x] = y;
            return y;
        } else {
            R[x] = y;
            R[y] = R[y] - 1;
            return y;
        }
    }

    private final int max(int a, int b) {
        return a > b ? a : b;
    }

    private final int min(int a, int b) {
        return a < b ? a : b;
    }
}
