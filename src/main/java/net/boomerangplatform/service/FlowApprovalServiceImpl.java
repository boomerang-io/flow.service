package net.boomerangplatform.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.boomerangplatform.model.Approval;
import net.boomerangplatform.model.FlowTeam;
import net.boomerangplatform.model.TeamWorkflowSummary;
import net.boomerangplatform.model.WorkflowSummary;
import net.boomerangplatform.mongo.entity.ApprovalEntity;
import net.boomerangplatform.mongo.entity.FlowUserEntity;
import net.boomerangplatform.mongo.entity.TaskExecutionEntity;
import net.boomerangplatform.mongo.model.Audit;
import net.boomerangplatform.mongo.model.TaskStatus;
import net.boomerangplatform.mongo.model.internal.InternalTaskResponse;
import net.boomerangplatform.mongo.service.ActivityTaskService;
import net.boomerangplatform.mongo.service.ApprovalService;
import net.boomerangplatform.service.refactor.TaskClient;
import net.boomerangplatform.service.crud.TeamService;
import net.boomerangplatform.service.crud.WorkflowService;

@Service
public class FlowApprovalServiceImpl implements FlowApprovalService {

  @Autowired
  private ApprovalService approvalService;
  
  @Autowired
  private TaskClient taskClient;
  
  @Autowired
  private UserIdentityService userIdentityService;
  
  @Autowired
  private TeamService teamService;
  
  @Autowired
  private WorkflowService workflowService;
  
  @Autowired
  private ActivityTaskService activityTaskService;
  
  @Override
  public void actionApproval(String id, Boolean approved) {
    
    FlowUserEntity flowUser = userIdentityService.getCurrentUser();
    ApprovalEntity approvalEntity = approvalService.findById(id);
   
    if (approvalEntity != null && flowUser != null) {
      
      Audit audit = new Audit();
      audit.setActionDate(new Date());
      audit.setApproverId(flowUser.getId());
      audit.setResult(approved);
      
      approvalEntity.setAudit(audit);
      
      
      approvalEntity = approvalService.save(approvalEntity);
      
      InternalTaskResponse actionApprovalResponse = new InternalTaskResponse();
      actionApprovalResponse.setActivityId(approvalEntity.getTaskActivityId());
      if (approved.booleanValue()) {
        actionApprovalResponse.setStatus(TaskStatus.completed);
      }
      else {
        actionApprovalResponse.setStatus(TaskStatus.failure);
      }
      
      taskClient.endTask(actionApprovalResponse);
    }
  }

  @Override
  public List<Approval> getApprovalsForUser() {
    FlowUserEntity flowUser = userIdentityService.getCurrentUser();
    List<Approval> approvals = new LinkedList<>();
    List<TeamWorkflowSummary> teams = teamService.getUserTeams(flowUser);
    for (TeamWorkflowSummary team : teams) {
      String flowId = team.getId();
      List<Approval> teamApprovals = this.getApprovalsForTeam(flowId);
      approvals.addAll(teamApprovals);
    }
    return approvals;
  }

  @Override
  public List<Approval> getApprovalsForTeam(String teamId) {
    List<ApprovalEntity> approvalsList = approvalService.getActiivtyForTeam(teamId);
    List<Approval> approvals = new LinkedList<>();
    for (ApprovalEntity approvalEntity :approvalsList ) {
      Approval approval = convertToApproval(approvalEntity);
      approvals.add(approval);
    }
    return approvals;
  }

  private Approval convertToApproval(ApprovalEntity approvalEntity) {
    Approval approval = new Approval();
    approval.setId(approvalEntity.getId());
    approval.setAudit(approvalEntity.getAudit());
    approval.setActivityId(approvalEntity.getActivityId());
    approval.setTaskActivityId(approvalEntity.getTaskActivityId());
    approval.setWorkflowId(approvalEntity.getWorkflowId());
    approval.setTeamId(approvalEntity.getTeamId());
 
    
    WorkflowSummary workflowSummary = workflowService.getWorkflow(approval.getWorkflowId());
    approval.setWorkflowName(workflowSummary.getName());
    
    FlowTeam flowTeam = teamService.getTeamById(approval.getTeamId());
    approval.setTeamName(flowTeam.getName());
    approval.setTaskName("");
    
    TaskExecutionEntity taskExecution = activityTaskService.findById(approval.getTaskActivityId());
    approval.setTaskName(taskExecution.getTaskName());
    return approval;
  }

  @Override
  public Approval getApprovalById(String id) {
    ApprovalEntity approvalEntity = this.approvalService.findById(id);
    return this.convertToApproval(approvalEntity);
  }

  @Override
  public ApprovalEntity getApprovalByTaskActivityId(String id) {
    ApprovalEntity approvalEntity = this.approvalService.findByTaskActivityId(id);
    if (approvalEntity == null) {
      return null;
    }
    return approvalEntity;
  }
}
