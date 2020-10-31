import java.io.BufferedWriter;
import java.io.FileWriter;
public class Main {
    public static void main(String[] args) throws Exception {
        String fileName;
        RunRCPSP sample;
        String dir = "Data/BL/";
        for (int i = 1; i <= 20; i++) {
            fileName = dir + "bl20_" + i + ".rcp";
            String name = "bl20_" + i;
            System.out.print(name + ".rcp" + " | ");
            for (int prop = 0; prop < 10; prop++) {
                sample = new RunRCPSP(fileName, prop, 1);
                System.out.print(+sample.howMuchTime() + " | " + sample.howManyBacktracks() + " | " + sample.makeSpanSolution() + " | " + sample.howManyAdjustments() + " | ");
            }
            System.out.println("");
        }

    }
}
/*public class Main {
    public static void main (String[] args) throws Exception {
        String fileName;
        RunRCPSP sample;
        String dir =  "Data/Pack/";
        int NbSolveByAll = 0;

        int makespanTL = -1;
        int makespanH = -1;
        int makespanCH = -1;
        int makespanOH = -1;

        int NbSolveByTL = 0;
        int NbSolveByH = 0;
        int NbSolveByCH = 0;
        int NbSolveByOH = 0;

        long BacktTL = 0;
        long BacktH = 0;
        long BacktCH = 0;
        long BacktOH = 0;

        double timeTL = 0;
        double timeH = 0;
        double timeCH = 0;
        double timeOH = 0;

        long TotalBacktTL = 0;
        long TotalBacktH = 0;
        long TotalBacktCH = 0;
        long TotalBacktOH = 0;

        double TotalTimeTL = 0;
        double TotalTimeH = 0;
        double TotalTimeCH = 0;
        double TotalTimeOH = 0;
        BufferedWriter writer = new BufferedWriter(new FileWriter("Data/Results/FinalresultsPack.txt"));
        writer.write("Inst |" + "timeTL |" + "backtTL |" + "makespanTL |" + "propTL |"
                                  + "timeH |" + "backtH |" + "makespanH |" + "propH |"
                                  + "timeCTLH |" + "backtCTLH |" + "makespanCTLH |" + "propCTLH |"
                                  + "timeCH |" + "backtCH |" + "makespanCH |" + "propCH |");
        writer.newLine();
        //for (int i = 1; i <= 48; i++) {
            for(int j = 1; j<= 55; j++) {
                if (j < 10){
                    fileName = dir + "pack00" +j + ".rcp";
                    String name = "pack00" + j;
                    System.out.print(name + ".rcp" + " | ");
                    writer.write(name + ".rcp" + " | ");
                }else{
                    fileName = dir + "pack0" +j + ".rcp";
                    String name = "pack0" + j;
                    System.out.print(name + ".rcp" + " | ");
                    writer.write(name + ".rcp" + " | ");
                }
                for (int prop = 0; prop < 4; prop++) {
                    sample = new RunRCPSP(fileName, prop, 3);
                    if (prop == 0) {
                        makespanTL = sample.makeSpanSolution();
                        if (makespanTL != -1) {
                            NbSolveByTL += 1;
                            BacktTL = sample.howManyBacktracks();
                            timeTL = sample.howMuchTime();
                        }
                    }
                    if (prop == 1) {
                        makespanH = sample.makeSpanSolution();
                        if (makespanH != -1){
                            NbSolveByH += 1;
                            BacktH = sample.howManyBacktracks();
                            timeH = sample.howMuchTime();
                        }
                    }
                    if (prop == 2) {
                        makespanCH = sample.makeSpanSolution();
                        if (makespanCH != -1) {
                            NbSolveByCH += 1;
                            BacktCH = sample.howManyBacktracks();
                            timeCH = sample.howMuchTime();
                        }
                    }
                    if (prop == 3) {
                        makespanOH = sample.makeSpanSolution();
                        if (makespanOH != -1) {
                            NbSolveByOH += 1;
                            BacktOH = sample.howManyBacktracks();
                            timeOH = sample.howMuchTime();
                        }
                    }
                    writer.write(+sample.howMuchTime() + " | " + sample.howManyBacktracks() + " | " + sample.makeSpanSolution() + " | " + sample.howManyAdjustments() + " | ");
                    System.out.print(+sample.howMuchTime() + " | " + sample.howManyBacktracks() + " | " + sample.makeSpanSolution() + " | " + sample.howManyAdjustments() + " | ");
                }
                System.out.println("");
                writer.newLine();
                if (makespanTL != - 1 && makespanH != -1 && makespanCH != -1 && makespanOH != -1) {
                    TotalBacktTL += BacktTL;
                    TotalBacktH += BacktH;
                    TotalBacktCH += BacktCH;
                    TotalBacktOH += BacktOH;

                    TotalTimeTL += timeTL;
                    TotalTimeH += timeH;
                    TotalTimeCH += timeCH;
                    TotalTimeOH += timeOH;

                    NbSolveByAll += 1;
                }
            }
        //}

        System.out.println("Solve by All :" +NbSolveByAll);
        System.out.println("NbTL :"+ NbSolveByTL + ", NbH :"+NbSolveByH + ", NbCH :"+NbSolveByCH + ", NbOH :"+NbSolveByOH);
        System.out.println("TimeTL :"+ TotalTimeTL/NbSolveByAll + ", TimeH :"+TotalTimeH/NbSolveByAll + ", TimeCH :"+TotalTimeCH/NbSolveByAll + ", TimeOH :"+TotalTimeOH/NbSolveByAll);
        System.out.println("BackTL :"+ TotalBacktTL/NbSolveByAll + ", BackH :"+TotalBacktH/NbSolveByAll + ", BackCH :"+TotalBacktCH/NbSolveByAll + ", BackOH :"+TotalBacktOH/NbSolveByAll);

        writer.write("Solve by All :" +NbSolveByAll + "     ");
        writer.write("  NbTL :"+ NbSolveByTL + ", NbH :"+NbSolveByH + ", NbCH :"+NbSolveByCH + ", NbOH :"+NbSolveByOH);
        writer.write("   TimeTL :"+ TotalTimeTL/NbSolveByAll + ", TimeH :"+TotalTimeH/NbSolveByAll + ", TimeCH :"+TotalTimeCH/NbSolveByAll + ", TimeOH :"+TotalTimeOH/NbSolveByAll);
        writer.write("   BackTL :"+ TotalBacktTL/NbSolveByAll + ", BackH :"+TotalBacktH/NbSolveByAll + ", BackCH :"+TotalBacktCH/NbSolveByAll + ", BackOH :"+TotalBacktOH/NbSolveByAll);
        writer.close();
    }
}*/
