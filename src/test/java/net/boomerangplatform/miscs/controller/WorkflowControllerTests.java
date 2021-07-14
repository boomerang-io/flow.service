package net.boomerangplatform.miscs.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.boomerangplatform.controller.InternalController;
import net.boomerangplatform.controller.WorkflowController;
import net.boomerangplatform.misc.FlowTests;
import net.boomerangplatform.model.FlowWorkflowRevision;
import net.boomerangplatform.model.GenerateTokenResponse;
import net.boomerangplatform.model.RevisionResponse;
import net.boomerangplatform.model.WorkflowExport;
import net.boomerangplatform.model.WorkflowShortSummary;
import net.boomerangplatform.model.WorkflowSummary;
import net.boomerangplatform.model.projectstormv5.RestConfig;
import net.boomerangplatform.mongo.entity.RevisionEntity;
import net.boomerangplatform.mongo.entity.WorkflowEntity;
import net.boomerangplatform.mongo.model.FlowProperty;
import net.boomerangplatform.mongo.model.TaskConfigurationNode;
import net.boomerangplatform.mongo.model.TriggerEvent;
import net.boomerangplatform.mongo.model.TriggerScheduler;
import net.boomerangplatform.mongo.model.Triggers;
import net.boomerangplatform.mongo.model.WorkflowConfiguration;
import net.boomerangplatform.mongo.model.WorkflowScope;
import net.boomerangplatform.mongo.model.WorkflowStatus;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("local")
@WithMockUser(roles = {"admin"})
@WithUserDetails("mdroy@us.ibm.com")
public class WorkflowControllerTests extends FlowTests {

  @Autowired
  private WorkflowController controller;

  @Autowired
  private InternalController internalController;

  @Test
  public void testInternalWorkflowListing() {
    List<WorkflowShortSummary> summaryList = internalController.getAllWorkflows();

    assertNotNull(summaryList);
    assertEquals(18, summaryList.size());
  }

  @Test
  public void testGetWorkflowLatestVersion() {

    FlowWorkflowRevision entity = controller.getWorkflowLatestVersion("5d1a188af6ca2c00014c4314");

    assertEquals("5d1a188af6ca2c00014c4314", entity.getWorkFlowId());
  }

  @Test
  public void testGetWorkflowVersion() {
    FlowWorkflowRevision entity = controller.getWorkflowVersion("5d1a188af6ca2c00014c4314", 1L);
    assertEquals(1L, entity.getVersion());
    assertEquals("5d1a188af6ca2c00014c4314", entity.getWorkFlowId());
  }

  @Test
  public void testGetWorkflowWithId() {
    WorkflowSummary summary = controller.getWorkflowWithId("5d1a188af6ca2c00014c4314");
    assertEquals("5d1a188af6ca2c00014c4314", summary.getId());
  }

  @Test
  public void testInsertWorkflow() {
    WorkflowSummary entity = new WorkflowSummary();
    entity.setName("TestWorkflow");
    entity.setStatus(WorkflowStatus.deleted);
    WorkflowSummary summary = controller.insertWorkflow(entity);
    assertEquals("TestWorkflow", summary.getName());
    assertEquals(WorkflowStatus.active, summary.getStatus());

  }

  @Test
  public void testinsertWorkflow() throws IOException {

    File resource = new ClassPathResource("json/updated-model-v5.json").getFile();
    String json = new String(Files.readAllBytes(resource.toPath()));
    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    FlowWorkflowRevision revision = objectMapper.readValue(json, FlowWorkflowRevision.class);
    revision.setMarkdown("test");

    FlowWorkflowRevision revisionEntity =
        controller.insertWorkflow("5d1a188af6ca2c00014c4314", revision);
    assertEquals(2L, revisionEntity.getVersion());
    assertEquals("test", revisionEntity.getMarkdown());
  }

  @Test
  public void testUpdateWorkflow() {
    WorkflowSummary entity = new WorkflowSummary();
    entity.setId("5d1a188af6ca2c00014c4314");
    entity.setName("TestUpdateWorkflow");
    WorkflowSummary updatedEntity = controller.updateWorkflow(entity);
    assertEquals("5d1a188af6ca2c00014c4314", updatedEntity.getId());
    assertEquals("TestUpdateWorkflow", updatedEntity.getName());
  }

  @Test
  public void testUpdateWorkflowProperties() {

    FlowProperty property = new FlowProperty();
    property.setKey("testKey");
    property.setDescription("testDescription");
    property.setLabel("testLabel");
    property.setRequired(true);
    property.setType("testing");

    List<FlowProperty> properties = new ArrayList<>();
    properties.add(property);

    WorkflowEntity entity =
        controller.updateWorkflowProperties("5d1a188af6ca2c00014c4314", properties);

    assertNotNull(entity.getProperties());
    assertEquals(1, entity.getProperties().size());
    assertEquals("testDescription", entity.getProperties().get(0).getDescription());

  }

  @Test
  public void testExportWorkflow() {
    ResponseEntity<InputStreamResource> export =
        controller.exportWorkflow("5d1a188af6ca2c00014c4314");
    assertEquals(HttpStatus.OK, export.getStatusCode());
  }

  @Test
  public void testImportWorkflowUpdate() throws IOException {
    WorkflowExport export = new WorkflowExport();
    export.setDescription("testImportDescription");
    export.setName("testImportName");
    export.setId("5d7177af2c57250007e3d7a1");

    TriggerEvent manual = new TriggerEvent();
    manual.setEnable(true);

    Triggers triggers = new Triggers();
    triggers.setManual(manual);

    export.setTriggers(triggers);

    List<TaskConfigurationNode> nodes = new ArrayList<>();

    WorkflowConfiguration config = new WorkflowConfiguration();
    config.setNodes(nodes);

    File resource = new ClassPathResource("json/json-sample.json").getFile();
    String json = new String(Files.readAllBytes(resource.toPath()));
    ObjectMapper objectMapper = new ObjectMapper();
    RevisionEntity revision = objectMapper.readValue(json, RevisionEntity.class);

    export.setLatestRevision(revision);

    controller.importWorkflow(export, true, "", WorkflowScope.team);

    WorkflowSummary summary = controller.getWorkflowWithId("5d1a188af6ca2c00014c4314");
    assertEquals("test", summary.getDescription());
  }

  @Test
  public void testImportWorkflow() throws IOException {

    File resource = new ClassPathResource("scenarios/import/import-sample.json").getFile();
    String json = new String(Files.readAllBytes(resource.toPath()));
    ObjectMapper objectMapper = new ObjectMapper();
    WorkflowExport importedWorkflow = objectMapper.readValue(json, WorkflowExport.class);
    controller.importWorkflow(importedWorkflow, false, "", WorkflowScope.team);
    assertTrue(true);
  }

  @Test
  public void testGenerateWebhookToken() {

    GenerateTokenResponse response = controller.createToken("5d1a188af6ca2c00014c4314", "Token");
    assertNotEquals("", response.getToken());
  }

  @Test
  public void testDeleteWorkflow() {
    controller.deleteWorkflowWithId("5d1a188af6ca2c00014c4314");
    assertEquals(WorkflowStatus.deleted,
        controller.getWorkflowWithId("5d1a188af6ca2c00014c4314").getStatus());
  }

  @Test
  public void testViewChangeLog() {
    List<RevisionResponse> response =
        controller.viewChangelog(getOptionalString("5d1a188af6ca2c00014c4314"),
            getOptionalOrder(Direction.ASC), getOptionalString("sort"), 0, 2147483647);
    assertEquals(1, response.size());
    assertEquals(1, response.get(0).getVersion());
  }

  @Test
  @Ignore
  public void testUpdateWorkflowTriggers() {

    TriggerScheduler scheduler = new TriggerScheduler();
    scheduler.setEnable(true);
    scheduler.setSchedule("0 00 20 ? * TUE,WED,THU *");
    scheduler.setTimezone("timezone");

    TriggerEvent webhook = new TriggerEvent();
    webhook.setEnable(false);
    webhook.setToken("token");

    WorkflowSummary entity = controller.getWorkflowWithId("5d1a188af6ca2c00014c4314");
    assertNotNull(entity.getTriggers());
    assertNotNull(entity.getTriggers().getWebhook());
    assertEquals(false, entity.getTriggers().getScheduler().getEnable());
    assertEquals("", entity.getTriggers().getScheduler().getSchedule());
    assertEquals("", entity.getTriggers().getScheduler().getTimezone());
    assertEquals(true, entity.getTriggers().getWebhook().getEnable());
    assertEquals("A5DF2F840C0DFF496D516B4F75BD947C9BC44756A8AE8571FC45FCB064323641",
        entity.getTriggers().getWebhook().getToken());


    entity.getTriggers().setScheduler(scheduler);
    entity.getTriggers().setWebhook(webhook);

    WorkflowSummary updatedEntity = controller.updateWorkflow(entity);

    assertEquals("5d1a188af6ca2c00014c4314", updatedEntity.getId());

    assertEquals(true, updatedEntity.getTriggers().getScheduler().getEnable());
    assertEquals("0 00 20 ? * TUE,WED,THU *",
        updatedEntity.getTriggers().getScheduler().getSchedule());
    assertEquals("timezone", updatedEntity.getTriggers().getScheduler().getTimezone());
    assertEquals(false, updatedEntity.getTriggers().getWebhook().getEnable());
    assertEquals("A5DF2F840C0DFF496D516B4F75BD947C9BC44756A8AE8571FC45FCB064323641",
        updatedEntity.getTriggers().getWebhook().getToken());
  }

  @Test
  @Ignore
  public void testUpdateWorkflowTriggerNull() {

    WorkflowSummary entity = controller.getWorkflowWithId("5d1a188af6ca2c00014c4314");
    assertEquals(false, entity.getTriggers().getScheduler().getEnable());
    entity.setTriggers(null);
    assertNull(entity.getTriggers());

    WorkflowSummary updatedEntity = controller.updateWorkflow(entity);

    assertEquals("5d1a188af6ca2c00014c4314", updatedEntity.getId());
    assertEquals(false, updatedEntity.getTriggers().getScheduler().getEnable());

  }

  @Test
  @Ignore
  public void testUpdateWorkflowTriggerEvent() {

  }

  Optional<String> getOptionalString(String string) {
    return Optional.of(string);
  }

  Optional<Direction> getOptionalOrder(Direction direction) {
    return Optional.of(direction);
  }

  @Test
  public void testMissingTemplateVersionRevision() {

    FlowWorkflowRevision entity = controller.getWorkflowVersion("5d7177af2c57250007e3d7a1", 1l);
    assertNotNull(entity);
    verifyTemplateVersions(entity);
  }

  @Test
  public void testMissingTemplateVersionLatestRevision() {

    FlowWorkflowRevision entity = controller.getWorkflowLatestVersion("5d7177af2c57250007e3d7a1");
    assertNotNull(entity);
    verifyTemplateVersions(entity);
  }

  @Test
  public void testAvaliableParameters() {
    List<String> parameters = controller.getWorkflowParameters("5d1a188af6ca2c00014c4314");
    assertEquals(14, parameters.size());
    assertEquals("workflow.params.hello", parameters.get(0));
    assertEquals("params.hello", parameters.get(1));
    assertEquals("system.params.workflow-id", parameters.get(2));
    assertEquals("params.workflow-id", parameters.get(3));

  }

  private void verifyTemplateVersions(FlowWorkflowRevision entity) {
    RestConfig config = entity.getConfig();
    for (net.boomerangplatform.model.projectstormv5.ConfigNodes taskNode : config.getNodes()) {
      if (taskNode.getTaskId() != null) {
        assertNotNull(taskNode.getTaskVersion());
      }
    }
  }
}
