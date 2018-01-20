package com.raidoengine;

import com.raidoengine.data_structure.edge.Edge;
import com.raidoengine.data_structure.graph.RailwayRouteGraph;
import com.raidoengine.data_structure.route.TrainRoute;
import com.raidoengine.data_structure.vertex.Vertex;
import com.raidoengine.engine.RDijkstra;
import com.raidoengine.routeparsing.TrainRouteMapper;
import com.raidoengine.routeparsing.TrainRouteParser;
import com.raidoengine.util.Triple;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Raido {

    private final TrainRouteParser trainRouteParser = new TrainRouteParser("src/test/resources/trains");
    private final List<TrainRoute> trainRoutes = TrainRouteMapper.mapFromLocal(trainRouteParser.getTrainRouteLocals());

    private List<TrainRoute> trainRoutesWithoutTransfers = new ArrayList<>();

    private List<TrainRoute> trainRoutesFromA = new ArrayList<>();
    private List<TrainRoute> trainRoutesFromB = new ArrayList<>();

    private Set<Long> alreadyFoundIds = new HashSet<>();

    private final RDijkstra rDijkstra = new RDijkstra();

    private String start;
    private String goal;

    public Set<List<Triple<Long, Edge, Float>>> getSetOfRouteLists(String start, String goal) {
        this.start = start;
        this.goal = goal;

        findRoutesWithoutTransfers(start, goal);

        List<Pair<TrainRoute, TrainRoute>> routePairs = new ArrayList<>();

        trainRoutesWithoutTransfers.forEach(trainRoute -> routePairs.add(new Pair<>(trainRoute, null)));

        if (routePairs.size() < 10) {
            routePairs.addAll(getRoutePairs(start, goal));
        }

        routePairs.forEach(pair -> {
            RailwayRouteGraph railwayRouteGraph;
            if (pair.getValue() == null) {
                railwayRouteGraph = new RailwayRouteGraph(Collections.singletonList(pair.getKey()));
            } else {
                railwayRouteGraph = new RailwayRouteGraph(Arrays.asList(pair.getKey(), pair.getValue()));
            }

            rDijkstra.getShortestPath(railwayRouteGraph.getGraph(), railwayRouteGraph.getStationVertex(start), railwayRouteGraph.getStationVertex(goal));
        });

        List<List<Triple<Long, Edge, Float>>> routeIdEdgeCostListSet = rDijkstra.getRouteIdEdgeCostListSet();
        sortResultByCost(routeIdEdgeCostListSet);

        List<List<Triple<Long, Edge, Float>>> routeIdEdgeCostListSetCut = routeIdEdgeCostListSet
                .subList(0, routeIdEdgeCostListSet.size() > 50 ? 50 : routeIdEdgeCostListSet.size() == 0 ? 0 : routeIdEdgeCostListSet.size() - 1);

        return new HashSet<>(routeIdEdgeCostListSetCut);
    }



    private void findRoutesWithoutTransfers(String startName, String goalName) {
        List<TrainRoute> allTrainRoutesWithoutTransfers = trainRoutes.stream().filter(trainRoute -> trainRoute.getTrainRouteSchedule().stream()
                .anyMatch(sad -> sad.getLeft().getName().contains(startName)) && trainRoute.getTrainRouteSchedule().stream()
                .anyMatch(sad -> sad.getLeft().getName().contains(goalName))).collect(Collectors.toList());

        alreadyFoundIds.addAll(allTrainRoutesWithoutTransfers.stream().map(TrainRoute::getRouteId).collect(Collectors.toList()));

        trainRoutesWithoutTransfers.addAll(allTrainRoutesWithoutTransfers.stream().filter(trainRoute -> trainRoute.areInOrder(startName, goalName)).collect(Collectors.toList()));
    }

    private List<Pair<TrainRoute, TrainRoute>> getRoutePairs(String startName, String goalName) {

        trainRoutesFromA = getTrainRoutesFromStation(startName);
        trainRoutesFromB = getTrainRoutesFromStation(goalName);

        return getPairOfRoutesWithCommonStation(trainRoutesFromA, trainRoutesFromB);
    }

    private List<TrainRoute> getTrainRoutesFromStation(String stationName) {
        if (alreadyFoundIds == null) alreadyFoundIds = Collections.emptySet();

        final Set<Long> finalExceptionRoutes = alreadyFoundIds;
        return trainRoutes.stream().filter(trainRoute -> !finalExceptionRoutes.contains(trainRoute.getRouteId())
                && trainRoute.getTrainRouteSchedule().stream()
                .anyMatch(sad -> sad.getLeft().getName().contains(stationName))).collect(Collectors.toList());
    }

    // TODO MAKE WITH COMMON SETTLEMENT.
    private List<Pair<TrainRoute, TrainRoute>> getPairOfRoutesWithCommonStation(List<TrainRoute> trainRoutesFromA, List<TrainRoute> trainRoutesFromB) {
        List<Pair<TrainRoute, TrainRoute>> pairOfRoutesWithCommonStation = new ArrayList<>();

        trainRoutesFromA.forEach(trainRouteA -> trainRouteA.getTrainRouteSchedule().forEach(sad -> {
            final boolean[] addA = {false};
            trainRoutesFromB.forEach(trainRouteB -> {
                if (!trainRouteA.getRouteId().equals(trainRouteB.getRouteId())
                        && !isPresent(trainRouteA, trainRouteB, pairOfRoutesWithCommonStation)
                        && !trainRouteA.getTrain().equals(trainRouteB.getTrain())
                        && trainRouteB.hasStation(sad.getLeft())
                        && trainRouteA.areInOrder(start, sad.getLeft().getName())
                        && trainRouteB.areInOrder(sad.getLeft().getName(), goal)) {
                    pairOfRoutesWithCommonStation.add(new Pair<>(trainRouteA, trainRouteB));
                    alreadyFoundIds.add(trainRouteB.getRouteId());
                    addA[0] = true;
                }
            });
            if (addA[0]) alreadyFoundIds.add(trainRouteA.getRouteId());
        }));

        return pairOfRoutesWithCommonStation;
    }

    private boolean isPresent(TrainRoute a, TrainRoute b, List<Pair<TrainRoute, TrainRoute>> pairs) {
        return pairs.stream().anyMatch(pair -> (pair.getKey().getRouteId().equals(a.getRouteId()) && pair.getValue().getRouteId().equals(b.getRouteId()))
        || pair.getValue().getRouteId().equals(a.getRouteId()) && pair.getKey().getRouteId().equals(b.getRouteId()));
    }

    private void sortResultByCost(List<List<Triple<Long, Edge, Float>>> routeIdEdgeCostListSet) {
        routeIdEdgeCostListSet.sort((o1, o2) -> {
            final Float[] cost1 = {0F};
            final Float[] cost2 = {0F};
            o1.forEach(longEdgeFloatTriple -> cost1[0] += longEdgeFloatTriple.getRight());
            o2.forEach(longEdgeFloatTriple -> cost2[0] += longEdgeFloatTriple.getRight());
            return Float.compare(cost1[0], cost2[0]);
        });
    }

    private void printResult(List<List<Triple<Long, Edge, Float>>> routeIdEdgeCostListSet) {
        List<List<Triple<Long, Edge, Float>>> routeIdEdgeCostListSetCut = routeIdEdgeCostListSet.subList(0, routeIdEdgeCostListSet.size() > 50 ? 50 : routeIdEdgeCostListSet.size() - 1);
        routeIdEdgeCostListSetCut.forEach(this::printSingleResult);

        System.out.println("Found: " + routeIdEdgeCostListSet.size());
        System.out.println("Printed: " + routeIdEdgeCostListSetCut.size());
    }

    private void printSingleResult(List<Triple<Long, Edge, Float>> routeIdEdgeCostList) {
        final Float[] cost = {0F};
        StringBuilder route = new StringBuilder();
        routeIdEdgeCostList.forEach(routeIdEdgeCost -> {
            cost[0] += routeIdEdgeCost.getRight();
            TrainRoute tr = trainRoutes.stream().filter(trainRoute -> trainRoute.getRouteId().equals(routeIdEdgeCost.getLeft())).findFirst().orElse(null);
            if (Objects.isNull(tr)) {
                route.append("Train route is null. RouteIdEdgeCost: ").append(routeIdEdgeCost);
            }
            else {
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