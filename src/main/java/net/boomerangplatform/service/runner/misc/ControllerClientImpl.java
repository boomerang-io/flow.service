package net.boomerangplatform.service.runner.misc;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.boomerangplatform.model.Task;
import net.boomerangplatform.model.TaskResult;
import net.boomerangplatform.model.controller.TaskConfiguration;
import net.boomerangplatform.model.controller.TaskCustom;
import net.boomerangplatform.model.controller.TaskDeletion;
import net.boomerangplatform.model.controller.TaskResponse;
import net.boomerangplatform.model.controller.TaskTemplate;
import net.boomerangplatform.model.controller.Workflow;
import net.boomerangplatform.model.controller.WorkflowStorage;
import net.boomerangplatform.mongo.entity.ActivityEntity;
import net.boomerangplatform.mongo.entity.TaskExecutionEntity;
import net.boomerangplatform.mongo.model.CoreProperty;
import net.boomerangplatform.mongo.model.Revision;
import net.boomerangplatform.mongo.model.TaskStatus;
import net.boomerangplatform.mongo.model.internal.InternalTaskResponse;
import net.boomerangplatform.mongo.service.ActivityTaskService;
import net.boomerangplatform.mongo.service.FlowSettingsService;
import net.boomerangplatform.service.PropertyManager;
import net.boomerangplatform.service.crud.FlowActivityService;
import net.boomerangplatform.service.refactor.ControllerRequestProperties;
import net.boomerangplatform.service.refactor.TaskClient;

@Service
@Primary
public class ControllerClientImpl implements ControllerClient {

  private static final Logger LOGGER = LogManager.getLogger();

  @Value("${controller.createtask.url}")
  public String createTaskURL;

  @Value("${controller.createworkflow.url}")
  private String createWorkflowURL;

  @Autowired
  private FlowSettingsService flowSettinigs;

  @Autowired
  private TaskClient flowTaskClient;

  @Autowired
  private PropertyManager propertyManager;

  @Autowired
  @Qualifier("internalRestTemplate")
  public RestTemplate restTemplate;

  @Autowired
  public ActivityTaskService taskService;

  @Autowired
  public FlowActivityService activityService;

  @Value("${controller.terminateworkflow.url}")
  private String terminateWorkflowURL;

  private static final String CREATEWORKFLOWREQUEST = "Create Workflow Request";
  private static final String TERMINATEWORKFLOWREQUEST = "Terminate Workflow Request";
  private static final String CREATETEMPLATETASKREQUEST = "Create Template Task Request";
  private static final String CREATECUSTOMTASKREQUEST = "Create Custom Task Request";
  private static final String ERRORLOGPRFIX = "Error for: {}";

  @Override
  public boolean createFlow(String workflowId, String workflowName, String activityId,
      boolean enableStorage, List<CoreProperty> labels, Map<String, String> properties) {


    final Workflow request = new Workflow();
    request.setWorkflowActivityId(activityId);
    request.setWorkflowName(workflowName);
    request.setWorkflowId(workflowId);
    request.setParameters(properties);


    final WorkflowStorage storage = new WorkflowStorage();
    storage.setEnable(enableStorage);

    String storageClassName =
        this.flowSettinigs.getConfiguration("workflow", "storage.class").getValue();
    if (storageClassName != null && !storageClassName.isBlank()) {
      storage.setClassName(storageClassName);
    }

    String storageAccessMode =
        this.flowSettinigs.getConfiguration("workflow", "storage.accessMode").getValue();
    if (storageAccessMode != null && !storageAccessMode.isBlank()) {
      storage.setAccessMode(storageAccessMode);
    }

    String storageDefaultSize =
        this.flowSettinigs.getConfiguration("workflow", "storage.size").getValue();
    if (storageDefaultSize != null && !storageDefaultSize.isBlank()) {
      storage.setSize(storageDefaultSize);
    }

    request.setWorkflowStorage(storage);
    request.setLabels(this.convertToMap(labels));

    logPayload(CREATEWORKFLOWREQUEST, request);
    Date startTime = new Date();

    try {
      restTemplate.postForObject(createWorkflowURL, request, String.class);
    } catch (RestClientException ex) {
      LOGGER.error(ERRORLOGPRFIX, CREATEWORKFLOWREQUEST);
      LOGGER.error(ExceptionUtils.getStackTrace(ex));
      return false;
    }

    Date endTime = new Date();
    logRequestTime(CREATEWORKFLOWREQUEST, startTime, endTime);
    return true;
  }

  @Override
  public boolean terminateFlow(String workflowId, String workflowName, String activityId) {
    final Workflow request = new Workflow();
    request.setWorkflowActivityId(activityId);
    request.setWorkflowName(workflowName);
    request.setWorkflowId(workflowId);
    final WorkflowStorage storage = new WorkflowStorage();
    storage.setEnable(true);
    request.setWorkflowStorage(storage);
    logPayload(TERMINATEWORKFLOWREQUEST, request);

    Date startTime = new Date();
    try {
      restTemplate.postForObject(terminateWorkflowURL, request, String.class);
    } catch (RestClientException ex) {
      LOGGER.error(ERRORLOGPRFIX, TERMINATEWORKFLOWREQUEST);
      LOGGER.error(ExceptionUtils.getStackTrace(ex));
      return false;
    }
    Date endTime = new Date();
    logRequestTime(TERMINATEWORKFLOWREQUEST, startTime, endTime);
    return true;
  }

  @Override
  @Async("flowAsyncExecutor")
  public void submitCustomTask(Task task, String activityId, String workflowName,
      List<CoreProperty> labels) {


    TaskResult taskResult = new TaskResult();
    TaskExecutionEntity taskExecution =
        taskService.findByTaskIdAndActivityId(task.getTaskId(), activityId);

    ActivityEntity activity = this.activityService.findWorkflowActivity(activityId);


    taskResult.setNode(task.getTaskId());
    final TaskCustom request = new TaskCustom();
    request.setTaskId(task.getTaskId());
    request.setWorkflowId(task.getWorkflowId());
    request.setWorkflowName(workflowName);
    request.setWorkflowActivityId(activityId);
    request.setTaskName(task.getTaskName());
    request.setTaskActivityId(task.getTaskActivityId());
    request.setLabels(this.convertToMap(labels));

    ControllerRequestProperties applicationProperties =
        propertyManager.buildRequestPropertyLayering(task, activityId, task.getWorkflowId());

    Map<String, String> map = applicationProperties.getMap(true);
    String image = applicationProperties.getLayeredProperty("image");
    image = propertyManager.replaceValueWithProperty(image, activityId, applicationProperties);
    request.setImage(image);

    String command = applicationProperties.getLayeredProperty("command");
    command = propertyManager.replaceValueWithProperty(command, activityId, applicationProperties);
    request.setCommand(command);


    List<String> args = prepareCustomTaskArguments(activityId, applicationProperties, map);
    request.setArguments(args);
    final Date startDate = new Date();
    taskExecution.setStartTime(startDate);
    taskExecution.setFlowTaskStatus(TaskStatus.inProgress);
    taskExecution = taskService.save(taskExecution);

    boolean enableLifecycle = task.getEnableLifecycle();
    TaskConfiguration taskConfiguration = buildTaskConfiguration(enableLifecycle);
    request.setConfiguration(taskConfiguration);
    request.setWorkspaces(activity.getTaskWorkspaces());


    logPayload("Create Task Request", request);

    Map<String, String> outputProperties = new HashMap<>();

    try {

      Date startTime = new Date();
      TaskResponse response =
          restTemplate.postForObject(createTaskURL, request, TaskResponse.class);

      Date endTime = new Date();

      logRequestTime(CREATECUSTOMTASKREQUEST, startTime, endTime);
      if (response != null) {
        this.logPayload(CREATECUSTOMTASKREQUEST, response);
        if (response.getResults() != null && !response.getResults().isEmpty()) {
          outputProperties = response.getResults();
        }
      }
      final Date finishDate = new Date();
      final long duration = finishDate.getTime() - startDate.getTime();
      taskExecution.setDuration(duration);
      taskExecution.setFlowTaskStatus(TaskStatus.completed);

      if (response != null && !"0".equals(response.getCode())) {
        taskExecution.setFlowTaskStatus(TaskStatus.failure);
      } else {
        taskResult.setStatus(taskExecution.getFlowTaskStatus());
      }
    } catch (RestClientException ex) {
      taskExecution.setFlowTaskStatus(TaskStatus.failure);
      taskResult.setStatus(TaskStatus.failure);
      LOGGER.error(ERRORLOGPRFIX, CREATECUSTOMTASKREQUEST);
      LOGGER.error(ExceptionUtils.getStackTrace(ex));
    }

    taskService.save(taskExecution);
    InternalTaskResponse response = new InternalTaskResponse();
    response.setActivityId(task.getTaskActivityId());
    response.setStatus(taskExecution.getFlowTaskStatus());
    response.setOutputProperties(outputProperties);

    flowTaskClient.endTask(response);
  }

  private List<String> prepareCustomTaskArguments(String activityId,
      ControllerRequestProperties applicationProperties, Map<String, String> map) {
    List<String> args = new LinkedList<>();
    if (map.get("arguments") != null) {
      String arguments = applicationProperties.getLayeredProperty("arguments");
      if (!arguments.isBlank()) {
        String[] lines = arguments.split("\\r?\\n");
        args = new LinkedList<>();
        for (String line : lines) {
          String newValue =
              propertyManager.replaceValueWithProperty(line, activityId, applicationProperties);
          args.add(newValue);
        }
      }
    }
    return args;
  }


  @Override
  @Async("flowAsyncExecutor")
  public void submitTemplateTask(Task task, String activityId, String workflowName,
      List<CoreProperty> labels) {

    ActivityEntity activity = this.activityService.findWorkflowActivity(activityId);

    TaskResult taskResult = new TaskResult();
    TaskExecutionEntity taskExecution =
        taskService.findByTaskIdAndActivityId(task.getTaskId(), activityId);

    taskResult.setNode(task.getTaskId());

    final TaskTemplate request = new TaskTemplate();
    request.setTaskId(task.getTaskId());
    request.setWorkflowId(task.getWorkflowId());
    request.setWorkflowName(workflowName);
    request.setWorkflowActivityId(activityId);
    request.setTaskName(task.getTaskName());
    request.setTaskActivityId(task.getTaskActivityId());
    request.setLabels(this.convertToMap(labels));

    ControllerRequestProperties applicationProperties =
        propertyManager.buildRequestPropertyLayering(task, activityId, task.getWorkflowId());

    Map<String, String> map = applicationProperties.getTaskInputProperties();

    request.setParameters(map);

    List<String> args = prepareCustomTaskArguments(activityId, applicationProperties, map);
    request.setArguments(args);

    boolean enableLifecycle = task.getEnableLifecycle();
    TaskConfiguration taskConfiguration = buildTaskConfiguration(enableLifecycle);
    request.setConfiguration(taskConfiguration);

    prepareTemplateImageRequest(task, taskResult, request);

    final Date startDate = new Date();

    taskExecution.setStartTime(startDate);
    taskExecution.setFlowTaskStatus(TaskStatus.inProgress);
    taskExecution = taskService.save(taskExecution);

    request.setWorkspaces(activity.getTaskWorkspaces());
    Map<String, String> outputProperties = new HashMap<>();

    logPayload(CREATETEMPLATETASKREQUEST, request);
    try {

      Date startTime = new Date();

      TaskResponse response =
          restTemplate.postForObject(createTaskURL, request, TaskResponse.class);

      Date endTime = new Date();

      logRequestTime(CREATETEMPLATETASKREQUEST, startTime, endTime);

      if (response != null) {
        this.logPayload(CREATETEMPLATETASKREQUEST, response);
        if (response.getResults() != null && !response.getResults().isEmpty()) {
          outputProperties = response.getResults();
        }
      }

      final Date finishDate = new Date();
      final long duration = finishDate.getTime() - startDate.getTime();

      taskExecution.setDuration(duration);
      taskExecution.setFlowTaskStatus(TaskStatus.completed);
      if (response != null && !"0".equals(response.getCode())) {
        taskExecution.setFlowTaskStatus(TaskStatus.failure);
        taskResult.setStatus(TaskStatus.failure);
      } else {
        taskResult.setStatus(taskExecution.getFlowTaskStatus());
      }
      LOGGER.info("Task result: {}", taskResult.getStatus());
    } catch (RestClientException ex) {
      taskExecution.setFlowTaskStatus(TaskStatus.failure);
      taskResult.setStatus(TaskStatus.failure);

      LOGGER.error(ERRORLOGPRFIX, CREATETEMPLATETASKREQUEST);
      LOGGER.error(ExceptionUtils.getStackTrace(ex));

    }
    taskService.save(taskExecution);

    InternalTaskResponse response = new InternalTaskResponse();
    response.setActivityId(task.getTaskActivityId());
    response.setStatus(taskExecution.getFlowTaskStatus());
    response.setOutputProperties(outputProperties);

    flowTaskClient.endTask(response);
  }

  private void prepareTemplateImageRequest(Task task, TaskResult taskResult,
      final TaskTemplate request) {
    if (task.getRevision() != null) {
      Revision revision = task.getRevision();
      request.setArguments(revision.getArguments());

      if (revision.getImage() != null && !revision.getImage().isBlank()) {
        request.setImage(revision.getImage());
      } else {
        String workerImage =
            this.flowSettinigs.getConfiguration("controller", "worker.image").getValue();
        request.setImage(workerImage);
      }

      if (revision.getCommand() != null && !revision.getCommand().isBlank()) {
        request.setCommand(revision.getCommand());
      }
    } else {
      taskResult.setStatus(TaskStatus.invalid);
    }
  }

  private TaskConfiguration buildTaskConfiguration(boolean enableLifecycle) {
    TaskConfiguration taskConfiguration = new TaskConfiguration();
    TaskDeletion taskDeletion = TaskDeletion.Never;
    String settingsPolicy =
        this.flowSettinigs.getConfiguration("controller", "job.deletion.policy").getValue();
    if (settingsPolicy != null) {
      taskDeletion = TaskDeletion.valueOf(settingsPolicy);
    }
    boolean enableDebug = false;
    String enableDebugFlag =
        this.flowSettinigs.getConfiguration("controller", "enable.debug").getValue();
    if (settingsPolicy != null) {
      enableDebug = Boolean.parseBoolean(enableDebugFlag);
    }

    taskConfiguration.setDeletion(taskDeletion);
    taskConfiguration.setDebug(Boolean.valueOf(enableDebug));
    taskConfiguration.setLifecycle(enableLifecycle);

    return taskConfiguration;
  }

  private void logPayload(String payloadName, Object request) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
      LOGGER.debug("Creating For Palyoad Type: {}", payloadName);
      LOGGER.debug(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error(ExceptionUtils.getStackTrace(e));
    }
  }

  private void logRequestTime(String payloadName, Date start, Date end) {
    long diff = end.getTime() - start.getTime();
    LOGGER.debug("Benchmark [Request Type]: {} - {} ms", payloadName, diff);
  }

  private Map<String, String> convertToMap(List<CoreProperty> labelList) {
    if (labelList == null) {
      return null;
    }

    Map<String, String> labels = new HashMap<>();
    for (CoreProperty property : labelList) {
      labels.put(property.getKey(), property.getValue());
    }
    return labels;
  }
}
