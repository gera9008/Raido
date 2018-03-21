package com.raidoengine;

import com.raidoengine.algorithm.RDijkstra;
import com.raidoengine.model.constant.Constants;
import com.raidoengine.model.edge.Edge;
import com.raidoengine.model.graph.MapGraph;
import com.raidoengine.model.graph.RailwayRouteGraph;
import com.raidoengine.model.provider.TimeProvider;
import com.raidoengine.model.route.TrainRoute;
import com.raidoengine.model.vertex.Vertex;
import com.raidoengine.routeparsing.TrainRouteMapper;
import com.raidoengine.routeparsing.TrainRouteParser;
import com.raidoengine.util.Triple;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author ace. 21-Mar-18.
 */
public class Raido {
    private final TrainRouteParser trainRouteParser = new TrainRouteParser("raido-engine/src/test/resources/trains");
    private final List<TrainRoute> trainRoutes = TrainRouteMapper.mapFromLocal(trainRouteParser.getTrainRouteLocals());
    private final RailwayRouteGraph railwayRouteGraph;

    private Vertex startVertex;
    private Vertex goalVertex;

    public Raido(String start, String goal, LocalDateTime startTime) {
        TimeProvider.startTime = Objects.nonNull(startTime) ? startTime : TimeProvider.TODAY;
        railwayRouteGraph = new RailwayRouteGraph(trainRoutes);
        startVertex = railwayRouteGraph.getVertexByStationName(start);
        goalVertex = railwayRouteGraph.getVertexByStationName(goal);
    }

    public static void main(String[] args) {
        String start = "Берегове";
        String goal = "Запоріжжя-1";
        LocalDateTime startTime = TimeProvider.TODAY.plusHours(0);
        Raido raido = new Raido(start, goal, startTime);
        if (Objects.isNull(raido.startVertex)) {
            System.out.println("Not such start station");
        } else if (Objects.isNull(raido.goalVertex)) {
            System.out.println("Not such goal station");
        } else {
            List<List<Triple<Long, Edge, Float>>> routes = raido.rRGDijkstraPaths();
            raido.printResult(routes);
        }
    }

    public List<List<Triple<Long, Edge, Float>>> getRoutes() {
        if (Objects.isNull(this.startVertex) || Objects.isNull(this.goalVertex)) {
            return null;
        } else {
            return rRGDijkstraPaths();
        }
    }

    public Vertex getStartVertex() {
        return startVertex;
    }

    public Vertex getGoalVertex() {
        return goalVertex;
    }

    public List<TrainRoute> getTrainRoutes() {
        return trainRoutes;
    }

    private List<List<Triple<Long, Edge, Float>>> rRGDijkstraPaths() {
        final RDijkstra rDijkstra = new RDijkstra(railwayRouteGraph.getGraph(), startVertex, goalVertex);

        trainRoutes.addAll(railwayRouteGraph.getGeneratedRoutes());
        List<List<Triple<Long, Edge, Float>>> routes = rDijkstra.getRouteIdEdgeCostListSet();
        rDijkstra.calculateShortestPath();

        Set<Long> routeIdsToDisable = new HashSet<>(rDijkstra.getRouteIdsToDisable());
        routeIdsToDisable.forEach(routeId -> {
            rDijkstra.getCurrentlyDisabledRouteIds().clear();
            rDijkstra.getCurrentlyDisabledRouteIds().add(routeId);
            do {
                rDijkstra.calculateShortestPath();
            }
            while ((!routes.isEmpty() && !routes.get(routes.size() - 1).isEmpty()) && routes.size() < Constants.MAX_AMOUNT_OF_ALGORITHM_PASSES);

            if (routes.get(routes.size() - 1).isEmpty()) {
                routes.remove(routes.size() - 1);
            }
        });

        routes.sort((o1, o2) -> {
            final Float[] cost1 = {0F};
            final Float[] cost2 = {0F};

            o1.forEach(longEdgeFloatTriple -> cost1[0] += longEdgeFloatTriple.getRight());
            o2.forEach(longEdgeFloatTriple -> cost2[0] += longEdgeFloatTriple.getRight());

            return Float.compare(cost1[0], cost2[0]);
        });

        cleanMapGraph(railwayRouteGraph);

        return routes;
    }

    private void cleanMapGraph(MapGraph mapGraph) {
        mapGraph.cleanGraph();
    }

    private void printResult(List<List<Triple<Long, Edge, Float>>> routeIdEdgeCostListSet) {
        System.out.println("Start time: " + TimeProvider.startTime);

        routeIdEdgeCostListSet.forEach(this::printSingleResult);

        System.out.println(routeIdEdgeCostListSet.size() + " routes found.");
    }

    private void printSingleResult(List<Triple<Long, Edge, Float>> routeIdEdgeCostList) {
        final Float[] cost = {0F};
        StringBuilder route = new StringBuilder();
        routeIdEdgeCostList.forEach(routeIdEdgeCost -> {
            cost[0] += routeIdEdgeCost.getRight();
            TrainRoute tr = trainRoutes.stream().filter(trainRoute -> trainRoute.getRouteId().equals(routeIdEdgeCost.getLeft())).findFirst().orElse(null);
            if (Objects.isNull(tr)) {
                route.append("Train route is null. RouteIdEdgeCost: ").append(routeIdEdgeCost);
            } else {
                route.append("Поїзд№: ")
                        .append(tr.getTrain().getTrainNumber())
                        .append(". ")
                        .append(tr.getTrainRouteName())
                        .append(". ")
                        .append(routeIdEdgeCost.getMiddle());
            }
        });
        System.out.println("Вартість: " + Arrays.toString(cost));
        System.out.println(route);
    }
}
