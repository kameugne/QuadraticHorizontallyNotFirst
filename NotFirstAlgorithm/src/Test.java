import java.util.Arrays;

public class Test {
    public static void main (String[] args) {
        int n = 3;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 6, 13, 6, 2);
        tasks[1] = new Task(1, 0, 8, 3, 2);
        tasks[2] = new Task(2, 1, 10, 4, 1);
        /*int n = 3;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 2, 6, 3, 2);
        tasks[1] = new Task(1, 4, 12, 6, 1);
        tasks[2] = new Task(2, 4, 11, 6, 1);*/

        /*int n = 3;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 5, 13, 6, 1);
        tasks[1] = new Task(1, 1, 13, 4, 1);
        tasks[2] = new Task(2, 2, 9, 4, 2);*/

        /*int n = 3;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 0, 2, 1, 1);
        tasks[1] = new Task(1, 0, 4, 2, 1);
        tasks[2] = new Task(2, 0, 2, 2, 2);*/
        /*int n = 4;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 2, 11, 3, 1);
        tasks[1] = new Task(1, 4, 9, 4, 1);
        tasks[2] = new Task(2, 5, 9, 2, 2);
        tasks[3] = new Task(3, 4, 11, 4, 2);*/

        /*int n = 4;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 6, 13, 5, 2);
        tasks[1] = new Task(1, 4, 8, 3, 1);
        tasks[2] = new Task(2, 5, 10, 2, 2);
        tasks[3] = new Task(3, 1, 14, 6, 1);*/
        /*int n = 4;
        int C = 3;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 1, 12, 5, 2);
        tasks[1] = new Task(1, 2, 14, 4, 1);
        tasks[2] = new Task(2, 2, 6, 2, 2);
        tasks[3] = new Task(3, 0, 13, 3, 2);*/

        /*int n = 4;
        int C = 2;
        Task[] tasks = new Task[n];
        tasks[0] = new Task(0, 0, 20, 6, 1);
        tasks[1] = new Task(1, 5, 6, 1, 2);
        tasks[2] = new Task(2, 1, 5, 2, 1);
        tasks[3] = new Task(3, 3, 10, 1, 1);*/


        QuadHorizontallyNotFirst exp = new QuadHorizontallyNotFirst(tasks, C);
        System.out.println("Horizontally : " + Arrays.toString(exp.filterWithTimeline()));
        ThetaTreeNotFirst tt = new ThetaTreeNotFirst(tasks, C);
        System.out.println("Theta tree : " + Arrays.toString(tt.filterWithTimeline()));

    }
}
