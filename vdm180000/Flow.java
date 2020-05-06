// Starter code for max flow
package vdm180000;

import vdm180000.Graph.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Flow {
    Vertex src;
    Vertex sink;
    int excessFlow[];
    int height[];
    HashMap<Edge, Integer> maxflow;
    HashMap<Edge, Integer> capacity;
    Graph g;
    List<Vertex> listVertex;
    

    public Flow(Graph g, Vertex s, Vertex t, HashMap<Edge, Integer> capacity) {
        this.g = g;
        this.src = s;
        this.sink = t;
        this.capacity = capacity;
        maxflow = new HashMap<>();
        listVertex = new LinkedList<>();
        excessFlow = new int[g.n];
        height = new int[g.n];
    }


    private void initialize() {
        Vertex[] vertices = g.getVertexArray();
        for( Vertex u : vertices ) {
            if(!src.equals(u) && !sink.equals(u)) {
                listVertex.add(u);
            }

            for( Edge e : g.adj(u).outEdges ) {
                maxflow.put(e, 0);
            }

            excessFlow[u.getIndex()] = 0;
            height[u.getIndex()] = 0;
        }

        int src_idx = src.getIndex();
        height[src_idx] = g.n;
        for( Edge e : g.adj(src).outEdges ) {
            int curr_capacity = capacity(e);
            maxflow.put(e, curr_capacity);

            
            excessFlow[src_idx] -= curr_capacity;
            excessFlow[e.otherEnd(src).getIndex()] += curr_capacity;
        }
    }


    // Return max flow found. Use either relabel to front or FIFO.
    public int preflowPush() {
	    relabelToFront();
        return excessFlow[sink.getIndex()];
    }

    private void relabelToFront() {
        //Initializing graph for max flow
        initialize();

        Iterator<Vertex> itr;
        boolean iscomplete = false;

        while(!iscomplete) {
            iscomplete = true;

            //Creating the iterator
            itr = listVertex.iterator();
            while(itr.hasNext()) {
                Vertex current = itr.next();

                //checking if the excess flow is 0 or not
                //if not make it 0 by increasing the height of adjacent vertex and push the
                //excess flow
                if(excessFlow[current.getIndex()] == 0)
                    continue;

                int oldHeight = height[current.getIndex()];
                discharge(current);
                if(oldHeight != height[current.getIndex()]) {
                    iscomplete = false;
                    itr.remove();
                    listVertex.add(0, current);
                    break;
                }
            }
        }
    }


    private void discharge(Vertex u) {
        while(excessFlow[u.getIndex()] > 0) {

            // In residual graph looking at forward edges
            for(Edge e : g.adj(u).outEdges) {
                Vertex otherV = e.otherEnd(u);
                if(ResidualGraphForFlow(e, u) && height[otherV.getIndex()]+1 == height[u.getIndex()]) {
                    push(e,u,otherV);
                    if(excessFlow[u.getIndex()] == 0)
                        return;
                }//remove if excess flow from if doesnt work

            }

            // In residual graph look for reverse edges
            for(Edge e : g.adj(u).inEdges) {
                Vertex otherV = e.fromVertex();
                if(ResidualGraphForFlow(e,u) && height[otherV.getIndex()]+1 == height[u.getIndex()]) {
                    push(e,u,otherV);
                    if(excessFlow[u.getIndex()] == 0)
                        return;
                }

            }

            //Call Relabel if cannot push more flow
            relabel(u);
        }
    }


    private void relabel(Vertex u) {
        int minHeight = 3*g.n;
        //Checking heights of adjacent vertex and increase it to maxheight +1
        //so that excess flow can be sens through the vertex
        for(Edge e: g.adj(u).outEdges) {
            int mheight = height[e.toVertex().getIndex()];
            if(ResidualGraphForFlow(e,u) && mheight < minHeight){
                minHeight = mheight;
            }
        }


        for(Edge e: g.adj(u).inEdges) {
            int mheight = height[e.fromVertex().getIndex()];
            if(ResidualGraphForFlow(e,u) && mheight < minHeight) {
                minHeight = mheight;
            }
        }
        height[u.getIndex()] = minHeight+1;
    }


    private boolean ResidualGraphForFlow(Edge e, Vertex u) {
        if(e.fromVertex().equals(u)) {
            return flow(e) < capacity(e);
        }
        return flow(e) > 0;
    }


    private void push(Edge e, Vertex u, Vertex v) {
        int flowDelta = 0;
        if(e.fromVertex().equals(u)) {
            flowDelta = Math.min((capacity(e) - flow(e)), excessFlow[u.getIndex()]);
            maxflow.put(e, flow(e) + flowDelta);
        }
        else {
            flowDelta = Math.min(flow(e), excessFlow[u.getIndex()]);
            maxflow.put(e, flow(e) - flowDelta);
        }
        excessFlow[u.getIndex()] -= flowDelta;
        excessFlow[v.getIndex()] += flowDelta;
    }

    
    // flow going through edge e
    public int flow(Edge e) {
	    return maxflow.containsKey(e) ? maxflow.get(e) : 0;
    }

    // capacity of edge e
    public int capacity(Edge e) {
	    return capacity.containsKey(e) ? capacity.get(e): 0;
    }

    /* After maxflow has been computed, this method can be called to
       get the "S"-side of the min-cut found by the algorithm
    */
    public Set<Vertex> minCutS() {
	    return null;
    }

    /* After maxflow has been computed, this method can be called to
       get the "T"-side of the min-cut found by the algorithm
    */
    public Set<Vertex> minCutT() {
	    return null;
    }
}
