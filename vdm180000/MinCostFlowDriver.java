
// Test driver for mincost flow
package idsa;
import idsa.Graph.*;
import Timer;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;


public class MinCostFlowDriver {
    static int VERBOSE = 0;
    public static void main(String[] args) throws Exception {
	Scanner in = new Scanner(System.in);
	
	if(args.length > 0) { VERBOSE = Integer.parseInt(args[0]); }
	Graph g = Graph.readDirectedGraph(in);
	int s = in.nextInt();
	int t = in.nextInt();
	HashMap<Edge,Integer> capacity = new HashMap<>();
	HashMap<Edge,Integer> cost = new HashMap<>();
	int[] arr = new int[1 + g.edgeSize()];
	for(int i=1; i<=g.edgeSize(); i++) {
	    arr[i] = 1;   // default capacity
	}
	while(in.hasNextInt()) {
	    int i = in.nextInt();
	    int cap = in.nextInt();
	    arr[i] = cap;
	}

	Vertex src = g.getVertex(s);
	Vertex target = g.getVertex(t);

	for(Vertex u: g) {
	    for(Edge e: u) {
		capacity.put(e, arr[e.getName()]);
		cost.put(e, e.getWeight());
	    }
	}


	Timer timer = new Timer();
	MinCostFlow mcf = new MinCostFlow(g, src, target, capacity, cost);
	
	int result = mcf.costScalingMinCostFlow(d);
	
	System.out.println(result);

	if(VERBOSE > 0) {
	    for(Vertex u: g) {
		System.out.print(u + " : ");
		for(Edge e: u) {
		    if(mcf.flow(e) != 0) { System.out.print(e + ":" + mcf.flow(e) + "/" + mcf.capacity(e) + "@" + mcf.cost(e) + "| "); }
		}
		System.out.println();
	    }
	}

	System.out.println(timer.end());
    }
}
