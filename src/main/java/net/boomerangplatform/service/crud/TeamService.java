package net.boomerangplatform.service.crud;

import java.util.List;
import org.springframework.data.domain.Pageable;
import net.boomerangplatform.model.FlowTeam;
import net.boomerangplatform.model.TeamQueryResult;
import net.boomerangplatform.model.TeamWorkflowSummary;
import net.boomerangplatform.model.WorkflowQuotas;
import net.boomerangplatform.mongo.entity.FlowTeamConfiguration;
import net.boomerangplatform.mongo.entity.FlowTeamEntity;
import net.boomerangplatform.mongo.entity.FlowUserEntity;
import net.boomerangplatform.mongo.model.Quotas;

public interface TeamService {

  FlowTeam createStandaloneTeam(String name);

  TeamQueryResult getAllAdminTeams(Pageable pageable);

  List<TeamWorkflowSummary> getTeamListing(FlowUserEntity userEntity);
  
  List<TeamWorkflowSummary> getAllTeamListing();

  List<TeamWorkflowSummary> getAllTeams();

  void createFlowTeam(String higherLevelGroupId, String teamName);


  List<TeamWorkflowSummary> getUserTeams(FlowUserEntity userEntity);

  List<FlowTeamConfiguration> getAllTeamProperties(String teamId);

  void deleteTeamProperty(String teamId, String configurationId);

  List<FlowTeamConfiguration> updateTeamProperty(String teamId, FlowTeamConfiguration property);

  FlowTeamConfiguration createNewTeamProperty(String teamId, FlowTeamConfiguration property);

  void updateTeamMembers(String teamId, List<String> teamMembers);

  FlowTeam getTeamById(String teamId);
  
  FlowTeam getTeamByIdDetailed(String teamId);


  void updateTeam(String teamId, FlowTeam flow);

  WorkflowQuotas getTeamQuotas(String teamId);

  WorkflowQuotas resetTeamQuotas(String teamId);

  Quotas updateTeamQuotas(String teamId, Quotas quotas);

  Quotas updateQuotasForTeam(String teamId, Quotas quotas);

  Quotas getDefaultQuotas();

  FlowTeamEntity deactivateTeam(String teamId);

  void updateFlagsAndQuotas();
  

}
