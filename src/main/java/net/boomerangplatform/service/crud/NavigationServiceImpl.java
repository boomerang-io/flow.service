package net.boomerangplatform.service.crud;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import net.boomerangplatform.model.FlowFeatures;
import net.boomerangplatform.model.Navigation;
import net.boomerangplatform.model.NavigationType;
import net.boomerangplatform.security.service.ApiTokenService;
import net.boomerangplatform.service.FeatureService;

@Service
public class NavigationServiceImpl implements NavigationService {

  @Value("${flow.externalUrl.navigation}")
  private String flowExternalUrlNavigation;

  @Autowired
  private ApiTokenService apiTokenService;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  @Autowired
  @Qualifier("internalRestTemplate")
  private RestTemplate restTemplate;

  @Autowired
  private FeatureService featureService;
  
  @Value("${flow.apps.flow.url}")
  private String flowAppsUrl;

  @Override
  public List<Navigation> getNavigation(boolean isUserAdmin, String teamId) {

    FlowFeatures features = featureService.getFlowFeatures();

    if (flowExternalUrlNavigation.isBlank()) {
      List<Navigation> response = new ArrayList<>();
      Navigation workflows = new Navigation();
      workflows.setName("Workflows");
      workflows.setType(NavigationType.link);
      workflows.setIcon("FlowData16");
      workflows.setLink(flowAppsUrl + "/workflows");
      response.add(workflows);

      Navigation activity = new Navigation();
      activity.setName("Activity");
      activity.setType(NavigationType.link);
      activity.setIcon("Activity16");
      activity.setLink(flowAppsUrl + "/activity");
      response.add(activity);

      Navigation insights = new Navigation();
      insights.setName("Insights");
      insights.setType(NavigationType.link);
      insights.setIcon("ChartScatter16");
      insights.setLink(flowAppsUrl + "/insights");
      response.add(insights);

      Navigation management = new Navigation();
      management.setName("Management");
      management.setIcon("SettingsAdjust16");
      management.setChildLinks(new ArrayList<>());
      management.setType(NavigationType.category);

      Navigation teamProperties = new Navigation();
      teamProperties.setName("Team Parameters");
      teamProperties.setLink(flowAppsUrl + "/team-parameters");
      teamProperties.setType(NavigationType.link);
      management.getChildLinks().add(teamProperties);
      response.add(management);

      if (isUserAdmin) {
        Navigation admin = new Navigation();
        admin.setName("Admin");
        admin.setType(NavigationType.category);
        admin.setIcon("Settings16");
        admin.setChildLinks(new ArrayList<>());

        if (((Boolean) features.getFeatures().get("team.management"))) {
          Navigation teams = new Navigation();
          teams.setName("Teams");
          teams.setLink( flowAppsUrl + "/admin/teams");
          teams.setType(NavigationType.link);
          admin.getChildLinks().add(teams);
        }

        if (((Boolean) features.getFeatures().get("user.management"))) {
          Navigation users = new Navigation();
          users.setName("Users");
          users.setLink(flowAppsUrl + "/admin/users");
          users.setType(NavigationType.link);
          admin.getChildLinks().add(users);
        }
        
        Navigation properties = new Navigation();
        properties.setName("Global Parameters");
        properties.setLink(flowAppsUrl + "/admin/parameters");
        properties.setType(NavigationType.link);
        admin.getChildLinks().add(properties);

        Navigation quotas = new Navigation();
        quotas.setName("Quotas");
        quotas.setLink(flowAppsUrl + "/admin/quotas");
        quotas.setType(NavigationType.link);
        admin.getChildLinks().add(quotas);

        Navigation settings = new Navigation();
        settings.setName("Settings");
        settings.setLink(flowAppsUrl + "/admin/settings");
        settings.setType(NavigationType.link);
        admin.getChildLinks().add(settings);

        Navigation taskManager = new Navigation();
        taskManager.setName("Task Manager");
        taskManager.setLink(flowAppsUrl + "/admin/task-templates");
        taskManager.setType(NavigationType.link);
        admin.getChildLinks().add(taskManager);

        Navigation systemWorkflows = new Navigation();
        systemWorkflows.setName("System Workflows");
        systemWorkflows.setLink(flowAppsUrl + "/admin/system-workflows");
        systemWorkflows.setType(NavigationType.link);

        admin.getChildLinks().add(systemWorkflows);

        response.add(admin);

      }

      return response;
    }

    else {

      UriComponentsBuilder uriComponentsBuilder =
          UriComponentsBuilder.fromHttpUrl(flowExternalUrlNavigation);
      UriComponents uriComponents = null;

      if (teamId == null || teamId.isBlank()) {
        uriComponents = uriComponentsBuilder.build();
      } else {
        uriComponents = uriComponentsBuilder.queryParam("teamId", teamId).build();
      }

      HttpHeaders headers = new HttpHeaders();
      headers.add(AUTHORIZATION_HEADER, TOKEN_PREFIX + apiTokenService.getUserToken());
      HttpEntity<String> request = new HttpEntity<>(headers);
      ResponseEntity<List<Navigation>> response = restTemplate.exchange(uriComponents.toUriString(),
          HttpMethod.GET, request, new ParameterizedTypeReference<List<Navigation>>() {});
      return response.getBody();
    }
  }
}
