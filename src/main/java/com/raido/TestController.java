package com.raido.raido;

import com.raidoengine.Raido;
import com.raidoengine.model.edge.Edge;
import com.raidoengine.model.provider.TimeProvider;
import com.raidoengine.model.route.TrainRoute;
import com.raidoengine.model.station.StationType;
import com.raidoengine.model.vehicle.Train;
import com.raidoengine.util.Triple;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/")
public class TestController {

    private static final String HELLO = "Hello, %s";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    String hello(@RequestParam(value = "name", required = false, defaultValue = "Rostik") String name) {
        Raido raido = new Raido("Львів", "Запоріжжя", TimeProvider.TODAY);
        return routes(raido);
    }

    private String routes(Raido raido) {
        StringBuilder builder = new StringBuilder();
        builder.append("Найкоротші маршрути у напрямку: ").append(raido.getStartVertex().getStation().getName())
                .append(" -> ").append(raido.getGoalVertex().getStation().getName()).append(". ")
                .append("Початковий час: ").append(TimeProvider.startTime.toLocalDate()).append(" ")
                .append(TimeProvider.startTime.toLocalTime()).append("<br />\n");
        List<List<Triple<Long, Edge, Float>>> routes = raido.getRoutes();
        routes.sort((o1, o2) -> {
            final Float[] cost1 = {0F};
            final Float[] cost2 = {0F};
            o1.forEach(longEdgeFloatTriple -> cost1[0] += longEdgeFloatTriple.getRight());
            o2.forEach(longEdgeFloatTriple -> cost2[0] += longEdgeFloatTriple.getRight());
            return Float.compare(cost1[0], cost2[0]);
        });

        routes.forEach(triples -> {
            builder.append("***************************************************************************************").append("<br />\n");
            Map<Long, TrainRoute> routeIdAndTrain = new HashMap<>();
            triples.forEach(routeIdEdgeCost -> {
                TrainRoute trainRoute = routeIdAndTrain.get(routeIdEdgeCost.getLeft());
                if (Objects.isNull(trainRoute)) {
                    TrainRoute tr = raido.getTrainRoutes().stream().filter(trr -> trr.getRouteId().equals(routeIdEdgeCost.getLeft())).findFirst().orElse(null);
                    routeIdAndTrain.put(routeIdEdgeCost.getLeft(), tr);
                }
                StationType from = routeIdEdgeCost.getMiddle().getFromVertex().getStation().getStationType();
                StationType to = routeIdEdgeCost.getMiddle().getToVertex().getStation().getStationType();
                if (!from.equals(to) && to.equals(StationType.VIRTUAL_STATION)) {
                    builder.append("ПОСАДКА. ").append(routeIdEdgeCost.getMiddle().getCost().getDeparture())
                            .append("<br />\n");
                } else if (!from.equals(to) && to.equals(StationType.RAILWAY_STATION)) {
                    builder.append("ВИСАДКА. ").append(routeIdEdgeCost.getMiddle().getCost().getArrival())
                            .append("<br />\n");
                } else {
                    builder.append(routeIdEdgeCost.getMiddle().getFromVertex().getStation().getRealStation().getName())
                            .append(" -> ")
                            .append("Поїзд№: ").append(trainRoute.getTrain().getTrainNumber()).append(". ").append(trainRoute.getTrainRouteName())
                            .append(" -> ")
                            .append(routeIdEdgeCost.getMiddle().getToVertex().getStation().getRealStation().getName())
                            .append("<br />\n");
                }
            });
            builder.append("***************************************************************************************").append("<br />\n");
        });

        return builder.toString();
    }
}
