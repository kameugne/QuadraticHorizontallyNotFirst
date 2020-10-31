import java.util.Random;

public class TaskGenerator {
    public static Task[] generateCumulativeTaks(int n, int begin, int end, int C) {
        StringBuilder builder = new StringBuilder();

        Task[] tasks = new Task[n];
        for (int i = 0; i < n; i++) {
            int processing = nextInt(1, (end-begin)/2);
            int est = nextInt(0, end - processing);
            int lct = nextInt(est + processing, end);
            int c = nextInt(1, C);
            tasks[i] = new Task(i, est, lct, processing, c);
        }

        return tasks;
    }

    private static Random r;
    private static int nextInt(int min, int max) {
        if (r == null) r = new Random();
        if (max - min == 0) return min;
        if (max-min < 0)
            System.out.println("fuuu");
        return min + r.nextInt(max - min);
    }
}