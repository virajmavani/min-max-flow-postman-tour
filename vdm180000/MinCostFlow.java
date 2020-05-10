/**
* @authors - 
* Ayesha Gurnani : ang170003
* Viraj Mavani : vdm180000
* Rutali Bandivadekar : rdb170002
*/

// Starter code for mincost flow
package vdm180000;
import vdm180000.Graph.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

// Initial flow should be max flow
public class MinCostFlow {
    Graph g; //graph object
	Vertex s; //source vertex
	Vertex t; //sink vertex
    HashMap<Edge, Integer> edge_cost; //cost associated with an edge
    HashMap<Edge, Integer> edge_flow; //flow associated with an edge
    HashMap<Edge, Integer> edge_capacity; //capacity associated with an edge
    List<Vertex> activeList;
    int epsilon;
    int maximum_flow; //maximum flow of a graph
    int minimum_cost; //minimum cost of a graph
    int flow_excess[];
    int p[]; //price function
    int graph_size;
	
    /**
     * Class constructor for initializing class attributes
     * @param g - Graph
     * @param s - Source node
     * @param t - Sink node
     * @param capacity
     * @param cost
     */
    public MinCostFlow(Graph g, Vertex s, Vertex t, HashMap<Edge, Integer> capacity, HashMap<Edge, Integer> cost) {
    	this.g = g;
    	graph_size = g.n;
        this.s = s;
        this.t = t;
        this.edge_capacity = capacity;
        this.edge_cost = cost;
        edge_flow = new HashMap<>();
        activeList = new LinkedList<>();
        flow_excess = new int[graph_size];
        p = new int[graph_size];
        epsilon = Integer.MIN_VALUE;
        maximum_flow = 0;
        minimum_cost = 0;
    }
    
    /**
     * Returns minimum cost flow using Cost Scaling Algorithm
     * @param int d -> scaling factor
     */
    int costScalingMinCostFlow(int d) {
    	Vertex[] vertices = g.getVertexArray();
    	for(Vertex u : vertices) {
            activeList.add(u);
            flow_excess[u.getIndex()] = 0;
            p[u.getIndex()] = 0;
            
            for(Edge e: g.adj(u).outEdges)
                edge_flow.put(e, 0);
        }

        Flow flow = new Flow(g, s, t, edge_capacity);
        maximum_flow = flow.preflowPush(); //value of max flow

        flow_excess[s.getIndex()] = maximum_flow;
        flow_excess[t.getIndex()] = -maximum_flow;

        for(Integer cost : edge_cost.values())
            if (cost > epsilon) epsilon = cost;        
    	
        while(epsilon > 0) {
            refine();
            epsilon = epsilon/2;
        }
        
        for(Entry<Edge, Integer> node: edge_flow.entrySet())
        	minimum_cost = minimum_cost + cost(node.getKey()) * node.getValue();
        
        return minimum_cost;
    }
    
    /** 
     * Refine method implemented
     * while u.excess > 0, call discharge method 
     */
    private void refine() {
        Iterator<Vertex> iterator;
        boolean isPresent = false; 
        
    	Vertex[] vertices = g.getVertexArray();
    	
        for(Vertex u : vertices){
        	List<Edge> out_edges = g.adj(u).outEdges;
            for(Edge e : out_edges) {
                Vertex v = e.toVertex();
                
                if(reducedCost(e, u, v) < 0) {
                    int excess_flow = capacity(e) - flow(e);
                    flow_excess[u.getIndex()] = flow_excess[u.getIndex()] - excess_flow;
                    flow_excess[v.getIndex()] = flow_excess[v.getIndex()] + excess_flow;
                    edge_flow.put(e, capacity(e));
                }
                else {
                    flow_excess[u.getIndex()] = flow_excess[u.getIndex()] + flow(e);
                    flow_excess[v.getIndex()] = flow_excess[v.getIndex()] - flow(e);
                    edge_flow.put(e, 0);
                }
            }
        }
        
        while(!isPresent) {
        	isPresent = true;
            iterator = activeList.iterator();
            
            while(iterator.hasNext()) {
                Vertex u = iterator.next();
                if(flow_excess[u.getIndex()] > 0){
	                int previous_price = p[u.getIndex()];
	                discharge(u);
	                if(previous_price != p[u.getIndex()]) {
	                    iterator.remove();
	                	isPresent = false;
	                    activeList.add(0, u);
	                    break;
	                }
                }
            }
        }
    }
    
    /**
     * Discharge method implemented to drain the excess at Vertex u
     * @param - Vertex u
     */
    private void discharge(Vertex u) {
        while(flow_excess[u.getIndex()] > 0){
        	
            List<Edge> out_edges = g.adj(u).outEdges; //forward edges
            for(Edge e : out_edges) {
                Vertex v = e.otherEnd(u);
                if(flow_ResidualGraph(u, e) && reducedCost(e, u, v) < 0)
                    push(e, u, v);
                
                if(flow_excess[u.getIndex()] == 0)
                    return;
            }

        	List<Edge> in_edges = g.adj(u).inEdges; //reverse edges
            for(Edge e : in_edges) {
                Vertex v = e.fromVertex();
                if(flow_ResidualGraph(u, e) && reducedCost(e, u, v) < 0)
                    push(e, u, v);                
                
                if(flow_excess[u.getIndex()] == 0)
                    return;
            }
            
            relabel(u);
        }
    }
    
    /**
     * This method checks whether Edge e exists out of u in Gf
     * @param Vertex u - used to push the excess
     * @param Edge e
     */
    private boolean flow_ResidualGraph(Vertex u, Edge e) {
        if(e.fromVertex().equals(u)){
            return flow(e) < capacity(e);
        }
        else{
        	return flow(e) > 0;
        }
    }
   
    /**
     * reducedCost method gets the reduced flow cost of an Edge e
     * @param Edge e
     * @param Vertex u
     * @param Vertex v
    */
    private int reducedCost(Edge e, Vertex u, Vertex v) {
        if(e.fromVertex().equals(u)){
            return cost(e) + p[u.getIndex()] - p[v.getIndex()];
        }
        else{
            return -cost(e) + p[u.getIndex()] - p[v.getIndex()];
        }
    }
    
    
    /**
    * Push method implemented pushes the excess flow out from vertex
    * @param Edge e
    * @param Vertex u
    * @param Vertex v
    */
    private void push(Edge e, Vertex u, Vertex v) {
        int delta = 0;
        if(e.fromVertex().equals(u)) {
            delta = Math.min(flow_excess[u.getIndex()], (capacity(e) - flow(e)));
            int t = flow(e) + delta;
            edge_flow.put(e, t);
        }
        else{
            delta = Math.min(flow_excess[u.getIndex()], flow(e));
            int t = flow(e) - delta;
            edge_flow.put(e, t);
        }
        
        //Adjust u.excess and v.excess
        flow_excess[v.getIndex()] = flow_excess[v.getIndex()] + delta;
        flow_excess[u.getIndex()] = flow_excess[u.getIndex()] - delta;
    }
    
    /**
     * relabel function reduces the price function at Vertex u
     * @param Vertex u
     */
    private void relabel(Vertex u) {
        p[u.getIndex()] = p[u.getIndex()] - epsilon;
    }
    
    // flow going through edge e
    public int flow(Edge e) {
    	return edge_flow.containsKey(e) ? edge_flow.get(e) : 0;
    }

    // capacity of edge e
    public int capacity(Edge e) {
    	return edge_capacity.get(e);
    }

    // cost of edge e
    public int cost(Edge e) {
    	return edge_cost.get(e);
    }
}
