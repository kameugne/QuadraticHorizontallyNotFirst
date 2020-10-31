public interface IThetaSet {
    void setTasks(Task[] tasks, Task[] sortedTasks);
    void initialize(int C, int ci);
    void add(int i);
    int envThetaCi();
}
