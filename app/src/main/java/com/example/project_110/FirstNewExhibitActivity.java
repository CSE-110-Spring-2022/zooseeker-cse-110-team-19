package com.example.project_110;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirstNewExhibitActivity extends AppCompatActivity {
    private List<String> directionsList;
    private ArrayAdapter adapter;
    private List<VertexInfoStorable> shortestVertexOrder;
    private Map<String, ZooData.VertexInfo> vInfo;
    private Map<String, ZooData.EdgeInfo> eInfo;
    private Graph<String, IdentifiedWeightedEdge> g;
    private RouteProgressViewModel viewModel;
    private int counter;
    private boolean restarting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("OnCreate");
        restarting = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_new_exhibit);

        shortestVertexOrder = getIntent().getParcelableArrayListExtra("shortestVertexOrder");
        vInfo = ZooData.loadVertexInfoJSON(this, "sample_node_info.json");
        eInfo = ZooData.loadEdgeInfoJSON(this, "sample_edge_info.json");
        g = ZooData.loadZooGraphJSON(this, "sample_zoo_graph.json");
        viewModel = new ViewModelProvider(this)
                .get(RouteProgressViewModel.class);
        counter = 0;
        System.out.println("oncreate sets counter: " + this.counter);

        String start = shortestVertexOrder.get(counter).id;
        counter +=1;
        String next = shortestVertexOrder.get(counter).id;
        System.out.println("oncreate sets counter: " + this.counter);

        // generate shortest path from start to first exhibit
        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, next);
        int i = 1;
        directionsList = new ArrayList<>();
        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            String direction = String.format("  %d. Walk %.0f meters along %s from '%s' to '%s'.\n",
                    i,
                    g.getEdgeWeight(e),
                    eInfo.get(e.getId()).street,
                    vInfo.get(g.getEdgeSource(e).toString()).name,
                    vInfo.get(g.getEdgeTarget(e).toString()).name);
            i++;
            directionsList.add(direction);
        }
        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, directionsList);
        ListView listView = (ListView) findViewById(R.id.directions_list);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        System.out.println("Pause");
        super.onPause();
        // TODO: change third parameter to what the user is actually seeing once the previous is implemented
        if (!restarting)
            viewModel.storeRouteProgressItem(shortestVertexOrder, counter - 1, counter- 1);
    }

    @Override
    protected void onResume() {
        System.out.println("Resume");
        super.onResume();
        System.out.println(viewModel == null);
        RouteProgressItem item = viewModel.loadRouteProgressItem();
        System.out.println("a");
        if (item != null) {
            System.out.println("Item is not null");
            this.shortestVertexOrder = item.shortestVertexOrder;
            this.counter = item.currDestInd;
            System.out.println("Resume sets counter: " + this.counter);
            nextDirection();
        }
        System.out.println("b");
        // TODO: set the current view to the direction at index item.currViewInd
    }

    public void onNextBtnClick(View view) {
        nextDirection();
    }

    public void nextDirection() {
        directionsList.clear();
        if(counter == shortestVertexOrder.size()-2){
            Button disableNext = (Button) findViewById(R.id.next_button);
            disableNext.setClickable(false);
        }
        System.out.println("onNextBtnClick counter: " + this.counter);
        String start = shortestVertexOrder.get(counter).id;
        counter+=1;
        String next = shortestVertexOrder.get(counter).id;
        System.out.println("onNextBtnClick sets counter: " + this.counter);

        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, next);
        int i = 1;

        String currVertex = start;

        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            ZooData.VertexInfo source = vInfo.get(g.getEdgeSource(e).toString());
            ZooData.VertexInfo target = vInfo.get(g.getEdgeTarget(e).toString());
            if (currVertex.equals(target.id)) {
                //swap print statement
                ZooData.VertexInfo temp = target;
                target = source;
                source = temp;
            }

            String direction = String.format("  %d. Walk %.0f meters along %s from '%s' to '%s'.\n",
                    i,
                    g.getEdgeWeight(e),
                    eInfo.get(e.getId()).street,
                    source.name,
                    target.name);
            i++;
            directionsList.add(direction);
            currVertex = target.id;
        }
        adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, directionsList);
        ListView listView = (ListView) findViewById(R.id.directions_list);
        listView.setAdapter(adapter);
    }


    private void lastExhibit() {
    }

    public void onClearPlanBtnClick(View view) {
        restarting = true;
        System.out.println("onClearPlanBtnClick");
        viewModel.clearAllItems();
        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}