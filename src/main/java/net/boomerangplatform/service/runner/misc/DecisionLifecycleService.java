package net.boomerangplatform.service.runner.misc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Service;
import net.boomerangplatform.model.Task;
import net.boomerangplatform.mongo.model.next.Dependency;

@Service
public class DecisionLifecycleService {

  public void processDecision(Graph<String, DefaultEdge> graph, List<Task> tasksToRun,
      String activityId, String value, final String currentVertex,
      Task currentTask) {
    List<String> removeList = calculateNodesToRemove(graph, tasksToRun, activityId,
        value, currentVertex, currentTask);
    Iterator<DefaultEdge> itrerator = graph.edgesOf(currentVertex).iterator();
    while (itrerator.hasNext()) {
      DefaultEdge e = itrerator.next();
      String destination = graph.getEdgeTarget(e);
      String source = graph.getEdgeSource(e);

      if (source.equals(currentVertex)
          && removeList.stream().noneMatch(str -> str.trim().equals(destination))) {
        graph.removeEdge(e);
      }
    }
  }

  public List<String> calculateNodesToRemove(Graph<String, DefaultEdge> graph,
      List<Task> tasksToRun, String activityId, String value, final String currentVert,
      Task currentTask) {
    Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentVert);

    List<String> matchedNodes = new LinkedList<>();
    List<String> defaultNodes = new LinkedList<>();

    for (DefaultEdge edge : outgoingEdges) {
      String destination = graph.getEdgeTarget(edge);
      Task destTask = tasksToRun.stream().filter(t -> t.getTaskId().equals(destination)).findFirst()
          .orElse(null);
      if (destTask != null) {
        determineNodeMatching(currentVert, matchedNodes, defaultNodes, value, destTask);
      }
    }
    List<String> removeList = matchedNodes;
    if (matchedNodes.isEmpty()) {
      removeList = defaultNodes;
    }
    return removeList;
  }

  private void determineNodeMatching(final String currentVert, List<String> matchedNodes,
      List<String> defaultNodes, String value, Task destTask) {
    Optional<Dependency> optionalDependency = destTask.getDetailedDepednacies().stream()
        .filter(d -> d.getTaskId().equals(currentVert)).findAny();
    if (optionalDependency.isPresent()) {
      Dependency dependency = optionalDependency.get();
      String linkValue = dependency.getSwitchCondition();
      String node = destTask.getTaskId();
      boolean matched = false;
      if (linkValue != null) {
        String[] lines = linkValue.split("\\r?\\n");
        for (String line : lines) {
          String patternString = line;
          Pattern pattern = Pattern.compile(patternString);
          Matcher matcher = pattern.matcher(value);
          if (matcher.matches()) {
            matched = true;
          }
        }
        if (matched) {
          matchedNodes.add(node);
        }
      } else {
        defaultNodes.add(node);
      }
    }
  }
}
