package net.boomerangplatform.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import net.boomerangplatform.model.FlowWorkflowRevision;
import net.boomerangplatform.model.GenerateTokenResponse;
import net.boomerangplatform.model.RevisionResponse;
import net.boomerangplatform.model.WorkflowExport;
import net.boomerangplatform.model.WorkflowSummary;
import net.boomerangplatform.mongo.model.FlowProperty;
import net.boomerangplatform.mongo.model.WorkflowScope;
import net.boomerangplatform.mongo.model.WorkflowStatus;
import net.boomerangplatform.service.crud.WorkflowService;
import net.boomerangplatform.service.crud.WorkflowVersionService;

@RestController
@RequestMapping("/workflow/workflow")
public class WorkflowController {

  @Autowired
  private WorkflowService workflowService;

  @Autowired
  private WorkflowVersionService workflowVersionService;

  @DeleteMapping(value = "{id}")
  public void deleteWorkflowWithId(@PathVariable String id) {
    workflowService.deleteWorkflow(id);
  }

  @PostMapping(value = "{id}/token")
  public GenerateTokenResponse createToken(@PathVariable String id, @RequestParam String label) {
    return workflowService.generateTriggerToken(id, label);
  }
  
  @DeleteMapping(value = "{id}/token")
  public void deleteToken(@PathVariable String id, @RequestParam String label) {
    workflowService.deleteToken(id, label);
  }

  @GetMapping(value = "/{workFlowId}/revision")
  public FlowWorkflowRevision getWorkflowLatestVersion(@PathVariable String workFlowId) {
    return workflowVersionService.getLatestWorkflowVersion(workFlowId);
  }

  @GetMapping(value = "/{workFlowId}/revision/{version}")
  public FlowWorkflowRevision getWorkflowVersion(@PathVariable String workFlowId,
      @PathVariable Long version) {
    return workflowVersionService.getWorkflowVersion(workFlowId, version);
  }

  @GetMapping(value = "{id}/summary")
  public WorkflowSummary getWorkflowWithId(@PathVariable String id) {
    return workflowService.getWorkflow(id);
  }

  @PostMapping(value = "")
  public WorkflowSummary insertWorkflow(@RequestBody WorkflowSummary workflowSummaryEntity) {
    workflowSummaryEntity.setStatus(WorkflowStatus.active);
    return workflowService.saveWorkflow(workflowSummaryEntity);
  }

  @PostMapping(value = "/{workFlowId}/revision")
  public ResponseEntity<FlowWorkflowRevision> insertWorkflow(@PathVariable String workFlowId,
      @RequestBody FlowWorkflowRevision workflowSummaryEntity) {
    workflowSummaryEntity.setId(null);
    workflowSummaryEntity.setWorkFlowId(workFlowId);
    return workflowVersionService.insertWorkflowVersion(workflowSummaryEntity);
  }

  @PatchMapping(value = "")
  public WorkflowSummary updateWorkflow(@RequestBody WorkflowSummary workflowSummaryEntity) {
    return workflowService.updateWorkflow(workflowSummaryEntity);
  }

  @PatchMapping(value = "/{workFlowId}/properties")
  public WorkflowSummary updateWorkflowProperties(@PathVariable String workFlowId,
      @RequestBody List<FlowProperty> properties) {
    return workflowService.updateWorkflowProperties(workFlowId, properties);
  }

  @GetMapping(value = "/{workFlowId}/changelog")
  public List<RevisionResponse> viewChangelog(
      @PathVariable(value = "workFlowId", required = true) Optional<String> workFlowId,
      @RequestParam(defaultValue = "ASC") Optional<Direction> order,
      @RequestParam Optional<String> sort, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "2147483647") int size) {

    Sort pagingSort = Sort.by(new Order(Direction.DESC, "date"));

    if (sort.isPresent()) {
      Direction direction = Direction.ASC;
      final String sortByKey = sort.get();

      if (order.isPresent()) {
        direction = order.get();
      }

      pagingSort = Sort.by(new Order(direction, sortByKey));
    }

    final Pageable pageable = PageRequest.of(page, size, pagingSort);
    return workflowVersionService.viewChangelog(workFlowId, pageable);
  }

  @GetMapping(value = "/export/{workFlowId}", produces = "application/json")
  public ResponseEntity<InputStreamResource> exportWorkflow(@PathVariable String workFlowId) {
    return workflowService.exportWorkflow(workFlowId);
  }

  @PostMapping(value = "/import")
  public void importWorkflow(@RequestBody WorkflowExport export, @RequestParam Boolean update,
      @RequestParam(required=false) String flowTeamId, @RequestParam(required=true) WorkflowScope scope) {
    workflowService.importWorkflow(export, update, flowTeamId, scope);
  }
  
  @GetMapping(value= "/{workFlowId}/available-parameters")
  public List<String> getWorkflowParameters(@PathVariable String workFlowId){
    return workflowService.getWorkflowParameters(workFlowId);
  }

}
