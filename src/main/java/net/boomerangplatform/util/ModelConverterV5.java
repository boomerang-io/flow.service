package net.boomerangplatform.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import net.boomerangplatform.model.controller.TaskResult;
import net.boomerangplatform.model.projectstormv5.ConfigNodes;
import net.boomerangplatform.model.projectstormv5.Extras;
import net.boomerangplatform.model.projectstormv5.ExtrasNode;
import net.boomerangplatform.model.projectstormv5.Link;
import net.boomerangplatform.model.projectstormv5.Point;
import net.boomerangplatform.model.projectstormv5.Port;
import net.boomerangplatform.model.projectstormv5.RestConfig;
import net.boomerangplatform.model.projectstormv5.RestDag;
import net.boomerangplatform.model.projectstormv5.TaskNode;
import net.boomerangplatform.model.projectstormv5.WorkflowRevision;
import net.boomerangplatform.mongo.entity.RevisionEntity;
import net.boomerangplatform.mongo.model.CoreProperty;
import net.boomerangplatform.mongo.model.Dag;
import net.boomerangplatform.mongo.model.TaskType;
import net.boomerangplatform.mongo.model.WorkflowExecutionCondition;
import net.boomerangplatform.mongo.model.next.DAGTask;
import net.boomerangplatform.mongo.model.next.Dependency;

public class ModelConverterV5 {

  private static final String POSITIONKEY = "position";
  private static final String TASKNAMEKEY = "taskName";
  private static final String POINTSKEY = "points";
  private static final String DECISIONKEY = "decision";
  private static final String RIGHTKEY = "right";
  private static final String TASKLINK = "task";

  private static final String CUSTOMTASKNAME = "customTask";
  private static final String TEMPLATETASKNAME = "templateTask";

  private static String getPropertyForKey(List<CoreProperty> properties, String key) {

    Optional<CoreProperty> property =
        properties.stream().filter(x -> x.getKey().equals(key)).findFirst();
    if (property.isPresent()) {
      return property.get().getValue();
    }

    return null;
  }

  private static void buildConfig(DAGTask dagTask, ConfigNodes config) {
    if (config != null) {

      dagTask.setTemplateVersion(config.getTaskVersion());

      /* Set up tas inputs */

      Map<String, String> inputs = config.getInputs();
      List<CoreProperty> coreProperties = new LinkedList<>();
      for (Entry<String, String> entry : inputs.entrySet()) {
        CoreProperty property = new CoreProperty();
        property.setKey(entry.getKey());
        property.setValue(entry.getValue());

        coreProperties.add(property);

      }

      dagTask.setProperties(coreProperties);

      /* Set up task results. */
      List<TaskResult> results = config.getOutputs();
      dagTask.setResults(results);

      if (getPropertyForKey(dagTask.getProperties(), TASKNAMEKEY) != null) {
        dagTask.setLabel(getPropertyForKey(dagTask.getProperties(), TASKNAMEKEY));
        Predicate<CoreProperty> isQualified = (item -> item.getKey().equals(TASKNAMEKEY));
        dagTask.getProperties().removeIf(isQualified);
      }

      if (dagTask.getType() == TaskType.decision) {
        String value = config.getInputs().get("value");
        dagTask.setDecisionValue(value);
      }
    }
  }

  private static void buildMetadataSection(TaskNode model, DAGTask task) {
    Map<String, Object> metadata = new HashMap<>();

    task.setMetadata(metadata);
    Map<String, Double> positionMap = new HashMap<>();
    positionMap.put("x", model.getX());
    positionMap.put("y", model.getY());
    Map<String, Map<String, Double>> metaDataMap = new HashMap<>();
    metaDataMap.put(POSITIONKEY, positionMap);
    metadata.put(POSITIONKEY, positionMap);
  }

  private static void caclulateDependncies(List<Link> links, List<Dependency> dependencies,
      List<Port> ports) {
    for (Port port : ports) {
      if ("left".equals(port.getPosition()) && !port.getLinks().isEmpty()) {
        for (String linkid : port.getLinks()) {
          Link link = links.stream().parallel().filter(l -> linkid.equals(l.getId())).findFirst()
              .orElse(null);
          if (link != null) {
            String source = link.getSource();
            Dependency dependency = createDependency(link, source);
            dependencies.add(dependency);
          }

        }
      }
    }
  }

  private static Dependency createDependency(Link link, String source) {
    Dependency dependency = new Dependency();
    dependency.setTaskId(source);
    dependency.setConditionalExecution(false);

    if (link.getExecutionCondition() != null) {
      dependency
          .setExecutionCondition(WorkflowExecutionCondition.valueOf(link.getExecutionCondition()));
    } else {
      dependency.setExecutionCondition(WorkflowExecutionCondition.always);
    }

    if (DECISIONKEY.equals(link.getType())) {
      dependency.setConditionalExecution(true);
    }

    Map<String, Object> metadata = new HashMap<>();

    dependency.setMetadata(metadata);
    dependency.setSwitchCondition(link.getSwitchCondition());
    List<Point> points = link.getPoints();
    metadata.put(POINTSKEY, points);
    return dependency;
  }

  public static RevisionEntity convertToEntityModel(WorkflowRevision revision) {
    RevisionEntity entity = new RevisionEntity();
    entity.setVersion(revision.getVersion());
    entity.setMarkdown(revision.getMarkdown());

    if (revision.getDag() == null) {
      return entity;
    }

    mapBaseData(revision, entity);
    List<Link> links = revision.getDag().getLinks();
    List<ConfigNodes> configNodes = revision.getConfig().getNodes();
    Dag dag = new Dag();
    entity.setDag(dag);
    List<DAGTask> tasks = new LinkedList<>();
    dag.setTasks(tasks);
    for (TaskNode node : revision.getDag().getNodes()) {
      DAGTask dagTask = new DAGTask();
      dagTask.setId(node.getNodeId());

      dagTask.setTemplateId(node.getTaskId());

      if ("startend".equals(node.getType())) {
        if ("Start".equals(node.getPassedName())) {
          dagTask.setType(TaskType.start);
        } else if ("End".equals(node.getPassedName())) {
          dagTask.setType(TaskType.end);
        }
      } else if (TEMPLATETASKNAME.equals(node.getType())) {
        dagTask.setType(TaskType.template);
      } else if (CUSTOMTASKNAME.equals(node.getType())) {
        dagTask.setType(TaskType.customtask);
      } else {
        dagTask.setType(TaskType.valueOf(node.getType()));
      }

      List<Dependency> dependencies = new LinkedList<>();
      dagTask.setDependencies(dependencies);
      List<Port> ports = node.getPorts();
      ConfigNodes config = configNodes.stream().parallel()
          .filter(c -> node.getNodeId().equals(c.getNodeId())).findFirst().orElse(null);
      dagTask.setLabel(node.getTaskName());


      buildConfig(dagTask, config);
      caclulateDependncies(links, dependencies, ports);
      buildMetadataSection(node, dagTask);
      tasks.add(dagTask);

    }

    return entity;
  }

  public static WorkflowRevision convertToRestModel(RevisionEntity convertedRevision) {

    WorkflowRevision revision = new WorkflowRevision();

    if (convertedRevision.getDag() == null) {
      return revision;
    }

    revision.setId(convertedRevision.getId());
    revision.setChangelog(convertedRevision.getChangelog());
    revision.setVersion(convertedRevision.getVersion());
    revision.setWorkFlowId(convertedRevision.getWorkFlowId());
    revision.setMarkdown(convertedRevision.getMarkdown());

    RestConfig restConfig = new RestConfig();
    revision.setConfig(restConfig);

    RestDag restDag = new RestDag();
    revision.setDag(restDag);
    restDag.setGridSize(0);
    restDag.setId(generateUniqueID());
    restDag.setOffsetX(0);
    restDag.setOffsetY(0);
    restDag.setZoom(100);

    List<Link> links = new LinkedList<>();

    List<DAGTask> tasks = convertedRevision.getDag().getTasks();
    List<TaskNode> taskNodes = new LinkedList<>();
    List<ConfigNodes> configNodeList = new LinkedList<>();
    restConfig.setNodes(configNodeList);
    restDag.setNodes(taskNodes);
    restDag.setLinks(links);

    List<ImmutablePair<String, Dependency>> pairs = new LinkedList<>();

    for (DAGTask dagTask : tasks) {

      createTaskNode(taskNodes, configNodeList, pairs, dagTask);
    }

    /* Create links. */
    for (ImmutablePair<String, Dependency> pair : pairs) {
      String key = pair.getLeft();
      Dependency dep = pair.getValue();

      String type = TASKLINK;

      if (dep.isConditionalExecution()) {
        type = DECISIONKEY;
      }

      Link link = createLink(key, dep, type);

      links.add(link);

      String source = key;
      String target = dep.getTaskId();

      createNodePortsForNode(taskNodes, link, source, target);
    }

    return revision;
  }

  private static Link createLink(String key, Dependency dep, String type) {
    String uniqueId = generateUniqueID();

    Link link = new Link();
    link.setId(uniqueId);
    link.setLinkId(uniqueId);
    link.setType(type);
    link.setSelected(false);
    link.setWidth(3);
    link.setColor("rgba(255,255,255,0.5)");
    link.setCurvyness(50);

    if (dep != null && dep.getExecutionCondition() != null) {
      link.setExecutionCondition(dep.getExecutionCondition().toString());
    }

    if (dep != null && dep.getSwitchCondition() != null) {
      link.setSwitchCondition(dep.getSwitchCondition());
    }

    link.setExtras(new Extras());
    link.setLabels(new LinkedList<>());

    if (dep != null) {
      link.setSource(dep.getTaskId());
    }

    link.setTarget(key);

    if (dep != null) {

      Map<String, Object> metadata = dep.getMetadata();
      if (metadata != null) {

        createMetadata(link, metadata);
      }
    }
    return link;
  }

  private static void createMetadata(Link link, Map<String, Object> metadata) { // NOSONAR
    List<Point> points = new LinkedList<>();

    Object pointsObject = metadata.get(POINTSKEY);

    if (pointsObject instanceof List) {
      List<Point> positionMap = (List<Point>) metadata.get(POINTSKEY);
      if (positionMap.get(0) instanceof Point) {
        for (Point newPoint : positionMap) {
          if (newPoint instanceof Point) {
            Point castPoint = newPoint;
            Point point = new Point();
            point.setX(castPoint.getX());
            point.setY(castPoint.getY());
            point.setSelected(false);
            point.setId(generateUniqueID());
            points.add(point);
          }
        }
      } else {
        @SuppressWarnings("unchecked")

        List<Map<String, Object>> pontsDictionary =
            (List<Map<String, Object>>) metadata.get(POINTSKEY);

        for (Map<String, Object> newPoint : pontsDictionary) {

          Point point = new Point();

          if (newPoint.get("x") instanceof Integer) {
            point.setX(((Integer) newPoint.get("x")).doubleValue());
          } else if (newPoint.get("x") instanceof Double) {
            point.setX(((Double) newPoint.get("x")));
          } else if (newPoint.get("x") instanceof String) {
            point.setX(Double.valueOf((String) newPoint.get("x")));
          }

          if (newPoint.get("y") instanceof Integer) {
            point.setY(((Integer) newPoint.get("y")).doubleValue());
          } else if (newPoint.get("y") instanceof Double) {
            point.setY(((Double) newPoint.get("y")));
          } else if (newPoint.get("y") instanceof String) {
            point.setY(Double.valueOf((String) newPoint.get("y")));
          }

          point.setSelected(false);
          point.setId(generateUniqueID());
          points.add(point);

        }
      }
    }

    link.setPoints(points);
  }

  private static void createNodePortsForNode(List<TaskNode> taskNodes, Link link, String source,
      String target) {
    /* Create port information. */
    TaskNode leftNode = taskNodes.stream().parallel().filter(c -> c.getNodeId().equals(source))
        .findFirst().orElse(null);
    if (leftNode != null) {

      Port leftNodePort = leftNode.getPorts().stream().parallel()
          .filter(c -> c.getPosition().equals("left")).findFirst().orElse(null);
      if (leftNodePort != null) {
        leftNodePort.getLinks().add(link.getId());
        link.setTargetPort(leftNodePort.getNodePortId());
      }
    }

    TaskNode rightNode = taskNodes.stream().parallel().filter(c -> c.getNodeId().equals(target))
        .findFirst().orElse(null);
    if (rightNode != null) {

      Port rightNodePort = rightNode.getPorts().stream().parallel()
          .filter(c -> c.getPosition().equals(RIGHTKEY)).findFirst().orElse(null);
      if (rightNodePort != null) {
        rightNodePort.getLinks().add(link.getId());
        link.setSourcePort(rightNodePort.getNodePortId());
      }
    }
  }

  private static Port createPort(String position, String taskId, String type) {

    String uniqueId = generateUniqueID();

    Port port = new Port();
    port.setPosition(position);
    port.setNodePortId(uniqueId);
    port.setId(uniqueId);
    port.setSelected(false);
    port.setParentNode(taskId);

    if (CUSTOMTASKNAME.equals(type) || TEMPLATETASKNAME.equals(type) || "approval".equals(type)
        || "manual".equals(type) || "setwfproperty".equals(type) || "eventwait".equals(type)
        || "releaselock".equals(type) || "script".equals(type) || "acquirelock".equals(type)
        || "runworkflow".equals(type)) {
      port.setType("task");
    } else {
      port.setType(type);
    }

    List<String> linksList = new LinkedList<>();

    port.setLinks(linksList);
    port.setName(position);

    port.setLinks(linksList);

    return port;
  }

  private static void createTaskNode(List<TaskNode> taskNodes, List<ConfigNodes> configNodeList,
      List<ImmutablePair<String, Dependency>> pairs, DAGTask dagTask) {
    TaskNode taskNode = new TaskNode();
    taskNode.setPorts(new LinkedList<>());
    taskNode.setNodeId(dagTask.getTaskId());
    taskNode.setSelected(false);
    taskNode.setTaskId(dagTask.getTemplateId());

    taskNode.setPrimaryId(generateUniqueID());
    taskNode.setPassedName(dagTask.getLabel());
    taskNode.setTaskName(dagTask.getLabel());
    taskNodes.add(taskNode);

    String type = getTaskType(dagTask);
    taskNode.setType(type);
    taskNode.setExtras(new ExtrasNode());

    taskNode.setPorts(new LinkedList<>());

    if (dagTask.getType() == TaskType.start) {
      taskNode.setPassedName("Start");
      taskNode.getPorts().add(createPort(RIGHTKEY, taskNode.getTaskId(), taskNode.getType()));
    } else if (dagTask.getType() == TaskType.end) {
      taskNode.setPassedName("End");
      taskNode.getPorts().add(createPort("left", taskNode.getTaskId(), taskNode.getType()));
    } else {
      taskNode.getPorts().add(createPort("left", taskNode.getTaskId(), taskNode.getType()));
      taskNode.getPorts().add(createPort(RIGHTKEY, taskNode.getTaskId(), taskNode.getType()));
    }

    if (dagTask.getMetadata() != null) {
      Map<String, Object> metadata = dagTask.getMetadata();

      Map<String, Number> position = (Map<String, Number>) metadata.get(POSITIONKEY);
      setupPosition(taskNode, position);
    }

    List<CoreProperty> inputs = dagTask.getProperties();
    ConfigNodes config = new ConfigNodes();
    config.setNodeId(dagTask.getTaskId());
    config.setTaskId(dagTask.getTemplateId());
    config.setTaskVersion(dagTask.getTemplateVersion());
    config.setType(taskNode.getType());
    config.setTaskVersion(dagTask.getTemplateVersion());

    config.setOutputs(dagTask.getResults());
    if (inputs != null && !inputs.isEmpty()) {

      Map<String, String> map = new HashMap<>();
      for (CoreProperty coreProperty : inputs) {
        map.put(coreProperty.getKey(), coreProperty.getValue());
      }
      config.setInputs(map);
      config.getInputs().put(TASKNAMEKEY, dagTask.getLabel());
      configNodeList.add(config);
    }

    for (Dependency dependency : dagTask.getDependencies()) {
      pairs.add(new ImmutablePair<>(taskNode.getNodeId(), dependency));
    }
  }

  private static void setupPosition(TaskNode taskNode, Map<String, Number> position) {
    if (position != null) {
      if (position.get("x") instanceof Integer) {
        taskNode.setX(position.get("x").doubleValue());
      } else if (position.get("x") instanceof Double) {
        taskNode.setX((Double) position.get("x"));
      }

      if (position.get("y") instanceof Integer) {
        taskNode.setY(position.get("y").doubleValue());
      } else if (position.get("y") instanceof Double) {
        taskNode.setY((Double) position.get("y"));
      }
    }
  }

  private static String generateUniqueID() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();
  }

  private static String getTaskType(DAGTask dagTask) {
    String type = null;
    TaskType dagType = dagTask.getType();

    if ("5c3907a1352b1b51412ed079".equals(dagTask.getTemplateId())) {
      return "script";
    }

    if (dagType == TaskType.end || dagType == TaskType.start) {
      type = "startend";
    } else if (dagType == TaskType.template) {
      type = TEMPLATETASKNAME;
    } else if (dagType == TaskType.customtask) {
      type = CUSTOMTASKNAME;
    } else if (dagType == TaskType.decision) {
      type = DECISIONKEY;
    } else if (dagType == TaskType.setwfproperty) {
      type = "setwfproperty";
    } else if (dagType == TaskType.manual) {
      type = "manual";
    } else if (dagType == TaskType.approval) {
      type = "approval";
    } else if (dagType == TaskType.eventwait) {
      type = "eventwait";
    } else if (dagType == TaskType.acquirelock) {
      type = "acquirelock";
    } else if (dagType == TaskType.releaselock) {
      type = "releaselock";
    } else if (dagType == TaskType.runworkflow) {
      type = "runworkflow";
    } else if (dagType == TaskType.script) {
      type = "script";
    }
    return type;
  }

  private static void mapBaseData(WorkflowRevision revision, RevisionEntity entity) {
    entity.setId(revision.getId());
    entity.setChangelog(revision.getChangelog());
    entity.setVersion(revision.getVersion());
    entity.setWorkFlowId(revision.getWorkFlowId());
  }

  private ModelConverterV5() {

  }

}
