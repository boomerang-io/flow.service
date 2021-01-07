package net.boomerangplatform.controller;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import net.boomerangplatform.model.CreateFlowTeam;
import net.boomerangplatform.model.FlowTeam;
import net.boomerangplatform.model.TeamQueryResult;
import net.boomerangplatform.model.TeamWorkflowSummary;
import net.boomerangplatform.model.WorkflowQuotas;
import net.boomerangplatform.model.profile.SortSummary;
import net.boomerangplatform.mongo.entity.FlowTeamConfiguration;
import net.boomerangplatform.mongo.entity.FlowTeamEntity;
import net.boomerangplatform.mongo.entity.FlowUserEntity;
import net.boomerangplatform.mongo.model.Quotas;
import net.boomerangplatform.mongo.model.UserType;
import net.boomerangplatform.service.UserIdentityService;
import net.boomerangplatform.service.crud.TeamService;

@RestController
@RequestMapping("/workflow")
public class TeamController {

  @Autowired
  private TeamService flowTeamService;

  @Autowired
  private UserIdentityService userIdentityService;

  @PostMapping(value = "/teams")
  public void createCiTeam(@RequestBody CreateFlowTeam createCiTeamRequest) {
    flowTeamService.createFlowTeam(createCiTeamRequest.getCreatedGroupId(),
        createCiTeamRequest.getName());
  }

  @GetMapping(value = "/teams")
  public List<TeamWorkflowSummary> getTeams() {
    final FlowUserEntity user = userIdentityService.getCurrentUser();
    if (user == null) {
      return new LinkedList<>();
    } else if (user.getType() == UserType.admin) {
      return flowTeamService.getAllTeams();
    } else {
      return flowTeamService.getUserTeams(user);
    }
  }

  @GetMapping(value = "/teams/{teamId}/properties")
  public List<FlowTeamConfiguration> getAllTeamProperties(@PathVariable String teamId) {
    return flowTeamService.getAllTeamProperties(teamId);
  }

  @DeleteMapping(value = "/teams/{teamId}/properties/{configurationId}")
  public void deleteTeamProperty(@PathVariable String teamId,
      @PathVariable String configurationId) {
    flowTeamService.deleteTeamProperty(teamId, configurationId);
  }

  @PatchMapping(value = "/teams/{teamId}/properties/{configurationId}")
  public List<FlowTeamConfiguration> updateTeamProperty(@PathVariable String teamId,
      @RequestBody FlowTeamConfiguration property, @PathVariable String configurationId) {
    return flowTeamService.updateTeamProperty(teamId, property);
  }

  @PostMapping(value = "/teams/{teamId}/properties")
  public FlowTeamConfiguration createNewTeamProperty(@PathVariable String teamId,
      @RequestBody FlowTeamConfiguration property) {
    return flowTeamService.createNewTeamProperty(teamId, property);
  }

  @GetMapping(value = "/teams/{teamId}/quotas")
  public WorkflowQuotas getTeamQuotas(@PathVariable String teamId) {
    return flowTeamService.getTeamQuotas(teamId);
  }

  @PutMapping(value = "/teams/{teamId}/quotas/default")
  public WorkflowQuotas resetTeamQuotas(@PathVariable String teamId) {
    return flowTeamService.resetTeamQuotas(teamId);
  }

  @PatchMapping(value = "/teams/{teamId}/quotas")
  public Quotas updateTeamQuotas(@PathVariable String teamId, @RequestBody Quotas quotas) {
    return flowTeamService.updateTeamQuotas(teamId, quotas);
  }

  @PutMapping(value = "/teams/{teamId}/quotas")
  public Quotas updateQuotasForTeam(@PathVariable String teamId, @RequestBody Quotas quotas) {
    return flowTeamService.updateQuotasForTeam(teamId, quotas);
  }

  @GetMapping(value = "/quotas/default")
  public Quotas getDefaultQuotas() {
    return flowTeamService.getDefaultQuotas();
  }

  @GetMapping(value = "/manage/teams/{teamId}")
  public FlowTeam getTeam(@PathVariable String teamId) {
    return flowTeamService.getTeamByIdDetailed(teamId);
  }

  @GetMapping(value = "/manage/teams")
  public TeamQueryResult getTeams(@RequestParam(required = false) String query,
      @RequestParam(defaultValue = "ASC") Direction order,
      @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size) {

    SortSummary sortSummary = new SortSummary();
    sortSummary.setProperty("name");
    sortSummary.setDirection(Direction.ASC.toString());

    Sort pagingSort = Sort.by(Direction.ASC, "name");

    if (StringUtils.isNotBlank(sort)) {
      Direction direction = order == null ? Direction.ASC : order;
      sortSummary.setDirection(direction.toString());
      sortSummary.setProperty(sort);

      pagingSort = Sort.by(direction, sort);
    }

    final Pageable pageable = PageRequest.of(page, size, pagingSort);

    TeamQueryResult response = flowTeamService.getAllAdminTeams(pageable);
    response.setupSortSummary(sortSummary);
    return response;
  }

  @DeleteMapping(value = "/teams/{teamId}")
  public FlowTeamEntity deactivateTeam(@PathVariable String teamId) {
    return flowTeamService.deactivateTeam(teamId);
  }

  @PatchMapping(value = "/teams/update")
  public void updateTeamFlagsandQuotas() {
flowTeamService.updateFlagsAndQuotas();

  }

}
