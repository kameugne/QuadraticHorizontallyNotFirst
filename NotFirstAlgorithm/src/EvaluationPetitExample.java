import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by vincent on 2017-01-30.
 */
public class EvaluationPetitExample {
	
    public static void main(String[] args) throws Exception
    {
        boolean found = false;
        int C = 3;
        int n = 5;
        
        for (int i=0; i<900000000; i++)
        {
            Task[] tasks = TaskGenerator.generateCumulativeTaks(n, 0, 15, C);
            EdgeFinder ef = new EdgeFinder(tasks, C);
            ThetaTreeNotFirst tt = new ThetaTreeNotFirst(tasks, C);
            TimeLineNotFirst tl = new TimeLineNotFirst(tasks, C);
            HorizontallyNotFirst he = new HorizontallyNotFirst(tasks, C);

            
            int[] estTT = tt.filterWithTimeline();
            int[] estTL = tl.filterWithTimeline();
            int[] estHE = he.Filter();
            boolean validInstance = true;
            if(!ef.OverloadCheck())
            {
                validInstance = false;
            }
            else
            {
                for(int l=0; l < tasks.length; l++){
                    if( /*estTT[l] > tasks[l].earliestStartingTime() ||*/ estTT[l] + tasks[l].processingTime() > tasks[l].latestCompletionTime()){
                    	validInstance = false;
                        break;
                    }
                    if( estTL[l] + tasks[l].processingTime() > tasks[l].latestCompletionTime()){
                    	validInstance = false;
                        break;
                    }
                    if(estHE[l] > tasks[l].earliestStartingTime() || estHE[l] + tasks[l].processingTime() > tasks[l].latestCompletionTime()){
                    	validInstance = false;
                        break;
                    }
                    /*if(estTT[l] > tasks[l].earliestStartingTime() || estTT[l] + tasks[l].processingTime() > tasks[l].latestCompletionTime()){
                    	validInstance = false;
                        break;
                    }*/
                }

                for(int t=0; t<=15; t++)
                {
                    int h = 0;
                    for(int l=0; l < tasks.length; l++){
                        if(t >= tasks[l].latestStartingTime() && t < tasks[l].earliestCompletionTime())
                            h+=tasks[l].height();
                    }
                    if(h > C)
                    {
                        validInstance = false;
                        break;
                    }
                    
                    for(int k = 0; k < tasks.length; k++){
                    	int TT  = 0;
                    	for(int l = 0; l < tasks.length; l++){
                    		if(k != l && t >= tasks[l].latestStartingTime() && t < tasks[l].earliestCompletionTime())
                    			TT += tasks[l].height();
                    	}
                    	if(t < tasks[k].earliestCompletionTime() && tasks[k].height() + TT > C)
                    	{
                    		validInstance = false;
                            break;
                    	}
                    }
                }
            }

            for(int j=0; j < n; j++)
            {
                if(validInstance && estTT[j] > estHE[j])
                {
                    for(int k=0; k<tasks.length; k++)
                    {
                        System.out.println("k = " + k + "  : " + tasks[k].toString());
                    }
                    System.out.println("Theta tree not-first : " + Arrays.toString(estTT));
                    System.out.println("Horizontally Elastic not-first : " + Arrays.toString(estHE));
                    System.out.println("C = " + C);
                    System.out.println("i = " + j);
                    System.out.println("Theta tree not-first : " + estTT[j]);
                    System.out.println("Horizontally Elastic not-first : " + estHE[j]);
                    found = true;
                    break;
                }
            }
            if(found)
                break;
        }
    }

	

	private static void ScheduleConflictingTask(Task[] tasks, int C, int j) {
		// TODO Auto-generated method stub
		int ov = 0;  
		for(int t=0; t<=15; t++)
        {
			int hmax = 0; int hreq = 0;
			for(int k=0; k<tasks.length; k++)
            {
				if(k != j && tasks[k].earliestStartingTime() <= t && t < tasks[k].latestCompletionTime() && tasks[j].earliestStartingTime() < tasks[k].earliestCompletionTime())
					hmax += tasks[k].height();
				if(k != j && tasks[k].earliestStartingTime() <= t && t < tasks[k].earliestCompletionTime() && tasks[j].earliestStartingTime() < tasks[k].earliestCompletionTime())
					hreq += tasks[k].height();
            }
			if(t <= tasks[j].earliestCompletionTime())
			{
				hmax += tasks[j].height();
				hreq += tasks[j].height();
			}
			int Hmax = Math.min(hmax, C);
			int hcons = Math.min(hreq + ov, Hmax);
			ov = ov + hreq - hcons;
			for(int k=0; k<tasks.length; k++)
			{
				if(t == tasks[k].latestCompletionTime())
					System.out.println(tasks[k].toString() + ", overflow = " + ov + ", hreq = "+ hreq + ", hmax = "+ Hmax + ", hcons = "+ hcons);
				//if(t == tasks[k].earliestStartingTime())
					//System.out.println(tasks[k].toString() + ", est.overflow = " + ov + ", hreq = "+ hreq + ", hmax = "+ Hmax + ", hcons = "+ hcons);
			}
        }
	}

	
}
