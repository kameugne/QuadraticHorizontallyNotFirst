import java.util.Arrays;

/*public class ExtendedThetaTree {
	
	Task[] tasks;
	private int c;
	private int ci;
	
    private int env_of_nodes[];
    private int e_of_nodes[];
    private int c_env_of_nodes[];
    
    private int n;
    private int firstIndexOnTheLowestLevel;
    private int lastIndexOnTheLowestLevel;
    private int task_index_to_node_index[];
    
    private Integer[] task_indices;*/
    
    /*
    * The tree is initialized with Theta = T and Lambda = \empty, meaning that that no task is initially in Lambda.
    * Therefore, each leaf is initialized with the attributes of the corresponding task.
    */
    /*public ExtendedThetaTree(Task[] tasks, int C) {
        this.tasks = tasks;
        this.n = tasks.length;
        this.c = C;
        
        this.env_of_nodes = new int[2 * n - 1];
        this.e_of_nodes = new int[2 * n - 1];
        this.c_env_of_nodes = new int[2 * n - 1];
               
        this.task_index_to_node_index = new int [n];    
        this.firstIndexOnTheLowestLevel = nextPowerOfTwoMinusOne(n);       
        this.lastIndexOnTheLowestLevel = 2 * (n - 1);
       
        task_indices = new Integer[tasks.length];
        for (int q = 0; q < n; q++) {
           task_indices[q] = new Integer(q);
        }
        Arrays.sort(task_indices, new Task.ComparatorByEst(tasks));  
        
        for (int q = 0; q < n; q++) {
            task_index_to_node_index[task_indices[q]]  = q;          
            
            int b = getNodeIndexWithLeafIndex(q);
            env_of_nodes[b] = Integer.MIN_VALUE;
            e_of_nodes[b] = 0;
            c_env_of_nodes[b] = Integer.MIN_VALUE;
        }
      
        //The inner-nodes are updated
        for(int i = n-2; i>=0; i--)
        {  	
        	updateNode(i);
        }
    }
    
    public void init(int ci)
    {
    	//The current capacity is set.
    	this.ci = ci;
    	
    	//We reset the values of the nodes.
    	for (int i = 0; i < 2 * n - 1; i++) 
    	{
    		e_of_nodes[i] = 0;
    		env_of_nodes[i] = Integer.MIN_VALUE;
    		c_env_of_nodes[i] = Integer.MIN_VALUE;
    	}     
    }
    
    public int Env(int index)
    {
    	int i = task_index_to_node_index[index];
        int b = getNodeIndexWithLeafIndex(i);   
       
        //We add the task to Theta. Therefore, we update the corresponding node.
        e_of_nodes[b] = tasks[index].energy();
        env_of_nodes[b] = tasks[index].envelop(c);
        c_env_of_nodes[b] = tasks[index].envelop(c-ci);
        
        updateInnerNodes(b);
        
        int met=0;
        int e = 0;
        
        //We go down the tree to find the node corresponding to the minest.
        while(!isLeaf(met))
        {
        	if(plus(c_env_of_nodes[right(met)], e) >
        		(c-ci) * tasks[index].latestCompletionTime())
        	{
        		met = right(met);
        	}
        	else
        	{
        		e += e_of_nodes[right(met)];
        		met = left(met);
        	}
        }
        
        //We go up the tree to do the correct cut, based on the minest.
        int a_e = e_of_nodes[met];
        int a_env = env_of_nodes[met];
        int b_e = 0;
        
        while(met != 0)
        {
        	if(isLeft(met))
        	{
        		b_e += e_of_nodes[right(parent(met))];
        	}
        	else
        	{
        		a_env = Math.max(a_env, plus(env_of_nodes[left(parent(met))], a_e));
        		a_e += e_of_nodes[left(parent(met))];
        	}
        	met = parent(met);
        }
        return plus(a_env, b_e);
    }
    

    
//PRIVATE FUNCTIONS -------------------------------------------------------------
    
    private int left(int index)
    {
    	return 2*index + 1;
    }
    
    private int right(int index)
    {
    	return 2*index + 2;
    }
    
    private int parent(int index)
    {
    	return (index+1)/2 - 1;
    }
    
    private boolean isLeft(int index)
    {
    	return index % 2 == 1; //Left nodes have odd indices
    }
    
    private boolean isLeaf(int index)
    {
    	return index >= n-1; //We check wether or not it is an inned-node
    }
    
    private void updateInnerNodes(int leafIndex)
    {
    	int w = (leafIndex - 1) / 2;
    	int t = 0;
    	
    	if(leafIndex == 0)
			return;
    	
        do {	
        	updateNode(w);
        	
            w =  (int) Math.floor((w - 1)/ 2);
            if (w == 0) 
                t++;
            if (t > 1) 
                break;
            
        } while (w >= 0);
    }
    
    private void updateNode(int i)
    {
    	int left, right;
    	left = 2 * i + 1;
    	right = 2 * i + 2;
 	
    	env_of_nodes[i] = Math.max(env_of_nodes[right], plus(env_of_nodes[left], e_of_nodes[right]));
    	e_of_nodes[i] = plus(e_of_nodes[left], e_of_nodes[right]);
        c_env_of_nodes[i] = Math.max(plus(c_env_of_nodes[left], e_of_nodes[right]), c_env_of_nodes[right]);
    	
    }
    
    private int plus(int a, int b)
    {
    	if(a == Integer.MIN_VALUE || b == Integer.MIN_VALUE)
    		return Integer.MIN_VALUE;
    	else 
    		return a + b;
    }
     
    private int getNodeIndexWithLeafIndex(int index)
    {
    	int b;
    	if (index <= (lastIndexOnTheLowestLevel - firstIndexOnTheLowestLevel))   
            b = firstIndexOnTheLowestLevel + index;
        else 
            b = lastIndexOnTheLowestLevel / 2 + index - ((lastIndexOnTheLowestLevel - firstIndexOnTheLowestLevel) + 1);  
    	
    	return b;
    }
    
    // Returns 2^ceil(lg(n)) - 1
    private static int nextPowerOfTwoMinusOne(int n) {
        // If n is a power of two
        if ((n & (n - 1)) == 0)
            return n - 1;
        int shift = 1;
        int result = n;
        do {
            n = result;
            result = n | (n >> shift);
            shift += shift;
        }
        while (n != result);
        return result;
    }

}*/