package net.boomerangplatform.tests.controller;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import net.boomerangplatform.controller.TeamController;
import net.boomerangplatform.misc.FlowTests;
import net.boomerangplatform.model.CreateFlowTeam;
import net.boomerangplatform.model.WorkflowQuotas;
import net.boomerangplatform.mongo.entity.FlowTeamConfiguration;
import net.boomerangplatform.mongo.entity.FlowTeamEntity;
import net.boomerangplatform.mongo.model.Quotas;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("local")
@WithMockUser(roles = {"admin"})
@WithUserDetails("mdroy@us.ibm.com")
public class TeamControllerTests extends FlowTests {

  @Autowired
  private TeamController controller;

  @Test
  public void testGetTeams() {
    
    controller.updateTeamFlagsandQuotas();
    assertEquals(3, controller.getTeams().size());

    assertEquals(Integer.valueOf(15),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowCount());
    assertEquals(Integer.valueOf(150),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(10),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowStorage());
    assertEquals(Integer.valueOf(60),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(2),
        controller.getTeams().get(0).getQuotas().getMaxConcurrentWorkflows());

//    assertEquals(Integer.valueOf(9),
//        controller.getTeams().get(0).getWorkflowQuotas().getCurrentWorkflowCount());
//    assertEquals(Integer.valueOf(3),
//        controller.getTeams().get(0).getWorkflowQuotas().getCurrentConcurrentWorkflows());
//    assertEquals(Integer.valueOf(0),
//        controller.getTeams().get(0).getWorkflowQuotas().getCurrentWorkflowExecutionMonthly());
//    assertEquals(Integer.valueOf(2),
//        controller.getTeams().get(0).getWorkflowQuotas().getCurrentWorkflowsPersistentStorage());
//    assertEquals(firstOfNextMonth(),
//        controller.getTeams().get(0).getWorkflowQuotas().getMonthlyResetDate());
  }

  @Test
  public void testCreateFlowTeam() {
    CreateFlowTeam request = new CreateFlowTeam();
    request.setName("WDC2 ISE Dev");
    request.setCreatedGroupId("5cedb53261a23a0001e4c1b6");

    controller.createCiTeam(request);

    assertEquals(4, controller.getTeams().size());
    
    controller.updateTeamFlagsandQuotas();
    
    assertEquals("WDC2 ISE Dev", controller.getTeams().get(3).getName());
    assertEquals(Integer.valueOf(10), controller.getTeams().get(3).getQuotas().getMaxWorkflowCount());
    assertEquals(Integer.valueOf(4), controller.getTeams().get(3).getQuotas().getMaxConcurrentWorkflows());
    assertEquals(Integer.valueOf(100), controller.getTeams().get(3).getQuotas().getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(5), controller.getTeams().get(3).getQuotas().getMaxWorkflowStorage());
    assertEquals(Integer.valueOf(30), controller.getTeams().get(3).getQuotas().getMaxWorkflowExecutionTime());
//    assertEquals(Integer.valueOf(0), controller.getTeams().get(3).getWorkflowQuotas().getCurrentConcurrentWorkflows());
//    assertEquals(Integer.valueOf(0), controller.getTeams().get(3).getWorkflowQuotas().getCurrentWorkflowCount());
//    assertEquals(Integer.valueOf(0), controller.getTeams().get(3).getWorkflowQuotas().getCurrentWorkflowExecutionMonthly());
//    assertEquals(Integer.valueOf(0), controller.getTeams().get(3).getWorkflowQuotas().getCurrentWorkflowsPersistentStorage());
//    assertEquals(firstOfNextMonth(), controller.getTeams().get(3).getWorkflowQuotas().getMonthlyResetDate());
  }

  @Test
  public void testGetAllTeamProperties() {
    List<FlowTeamConfiguration> configs =
        controller.getAllTeamProperties("5d1a1841f6ca2c00014c4309");
    assertEquals(0, configs.size());

    List<FlowTeamConfiguration> configs2 =
        controller.getAllTeamProperties("5d1a1841f6ca2c00014c4302");
    assertEquals(1, configs2.size());
  }

  @Test
  public void testDeleteTeamProperty() {
    controller.deleteTeamProperty("5d1a1841f6ca2c00014c4302",
        "df5f5749-4d30-41c3-803e-56b54b768407");
    assertEquals(0, controller.getAllTeamProperties("5d1a1841f6ca2c00014c4302").size());
  }

  @Test
  public void testUpdateTeamProperty() {
    assertEquals("Value",
        controller.getAllTeamProperties("5d1a1841f6ca2c00014c4302").get(0).getValue());

    FlowTeamConfiguration property = new FlowTeamConfiguration();
    property.setId("df5f5749-4d30-41c3-803e-56b54b768407");
    property.setValue("Updated Value");

    List<FlowTeamConfiguration> updatedConfigs = controller.updateTeamProperty(
        "5d1a1841f6ca2c00014c4302", property, "df5f5749-4d30-41c3-803e-56b54b768407");
    assertEquals("Updated Value", updatedConfigs.get(0).getValue());
  }

  @Test
  public void testCreateNewTeamProperty() {
    FlowTeamConfiguration property = new FlowTeamConfiguration();
    property.setKey("dylan.new.key");
    property.setValue("Dylan's New Value");

    FlowTeamConfiguration newConfig =
        controller.createNewTeamProperty("5d1a1841f6ca2c00014c4309", property);
    FlowTeamConfiguration newConfig2 =
        controller.createNewTeamProperty("5d1a1841f6ca2c00014c4302", property);

    assertEquals("dylan.new.key", newConfig.getKey());
    assertEquals("Dylan's New Value", newConfig.getValue());

    assertEquals("dylan.new.key", newConfig2.getKey());
    assertEquals("Dylan's New Value", newConfig2.getValue());
  }

  @Test
  public void testGetTeamQuotas() {
    WorkflowQuotas quotas = controller.getTeamQuotas("5d1a1841f6ca2c00014c4309");
    assertEquals(Integer.valueOf(15), quotas.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(150), quotas.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(10), quotas.getMaxWorkflowStorage());
    assertEquals(Integer.valueOf(60), quotas.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(2), quotas.getMaxConcurrentWorkflows());

    assertEquals(Integer.valueOf(9), quotas.getCurrentWorkflowCount());
    assertEquals(Integer.valueOf(3), quotas.getCurrentConcurrentWorkflows());
    assertEquals(Integer.valueOf(0), quotas.getCurrentWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(2), quotas.getCurrentWorkflowsPersistentStorage());
    assertEquals(firstOfNextMonth(), quotas.getMonthlyResetDate());

    assertEquals("5d1a1841f6ca2c00014c4309", controller.getTeams().get(0).getId());
    assertEquals(quotas.getMaxWorkflowCount(),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowCount());
    assertEquals(quotas.getMaxConcurrentWorkflows(),
        controller.getTeams().get(0).getQuotas().getMaxConcurrentWorkflows());
    assertEquals(quotas.getMaxWorkflowExecutionMonthly(),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowExecutionMonthly());
    assertEquals(quotas.getMaxWorkflowExecutionTime(),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowExecutionTime());
    assertEquals(quotas.getMaxWorkflowStorage(),
        controller.getTeams().get(0).getQuotas().getMaxWorkflowStorage());

  }

  @Test
  public void testResetTeamQuotas() {
    WorkflowQuotas previousQuotas = controller.getTeamQuotas("5d1a1841f6ca2c00014c4309");

    assertEquals(Integer.valueOf(15), previousQuotas.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(150), previousQuotas.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(10), previousQuotas.getMaxWorkflowStorage());
    assertEquals(Integer.valueOf(60), previousQuotas.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(2), previousQuotas.getMaxConcurrentWorkflows());

    controller.resetTeamQuotas("5d1a1841f6ca2c00014c4309");

    WorkflowQuotas updatedQuotas = controller.getTeamQuotas("5d1a1841f6ca2c00014c4309");

    assertEquals(Integer.valueOf(10), updatedQuotas.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(100), updatedQuotas.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(5), updatedQuotas.getMaxWorkflowStorage());
    assertEquals(Integer.valueOf(30), updatedQuotas.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(4), updatedQuotas.getMaxConcurrentWorkflows());

    assertEquals(Integer.valueOf(9), updatedQuotas.getCurrentWorkflowCount());
    assertEquals(Integer.valueOf(3), updatedQuotas.getCurrentConcurrentWorkflows());
    assertEquals(Integer.valueOf(0), updatedQuotas.getCurrentWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(2), updatedQuotas.getCurrentWorkflowsPersistentStorage());
    assertEquals(firstOfNextMonth(), updatedQuotas.getMonthlyResetDate());
  }

  @Test
  public void testUpdateQuotas() {
    WorkflowQuotas current = controller.getTeamQuotas("5d1a1841f6ca2c00014c4303"); // team3.json
    assertEquals(Integer.valueOf(10), current.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(4), current.getMaxConcurrentWorkflows());
    assertEquals(Integer.valueOf(100), current.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(30), current.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(5), current.getMaxWorkflowStorage());

    Quotas quotas = new Quotas();
    quotas.setMaxWorkflowCount(20);
    quotas.setMaxConcurrentWorkflows(8);
    quotas.setMaxWorkflowExecutionMonthly(200);
    quotas.setMaxWorkflowExecutionTime(60);
    quotas.setMaxWorkflowStorage(10);

    Quotas updateQuotas = controller.updateTeamQuotas("5d1a1841f6ca2c00014c4303", quotas);
    assertEquals(Integer.valueOf(20), updateQuotas.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(8), updateQuotas.getMaxConcurrentWorkflows());
    assertEquals(Integer.valueOf(200), updateQuotas.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(60), updateQuotas.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(10), updateQuotas.getMaxWorkflowStorage());

    WorkflowQuotas updated = controller.getTeamQuotas("5d1a1841f6ca2c00014c4303");
    assertEquals(updateQuotas.getMaxWorkflowCount(), updated.getMaxWorkflowCount());
    assertEquals(updateQuotas.getMaxConcurrentWorkflows(), updated.getMaxConcurrentWorkflows());
    assertEquals(updateQuotas.getMaxWorkflowExecutionMonthly(),
        updated.getMaxWorkflowExecutionMonthly());
    assertEquals(updateQuotas.getMaxWorkflowExecutionTime(), updated.getMaxWorkflowExecutionTime());
    assertEquals(updateQuotas.getMaxWorkflowStorage(), updated.getMaxWorkflowStorage());
  }

  @Test
  public void testUpdateQuotasForTeam() {
    Quotas quotas = new Quotas();
    quotas.setMaxWorkflowCount(25);
    quotas.setMaxConcurrentWorkflows(11);
    quotas.setMaxWorkflowExecutionMonthly(250);
    quotas.setMaxWorkflowExecutionTime(74);
    quotas.setMaxWorkflowStorage(8);

    Quotas newQuotas = controller.updateQuotasForTeam("5d1a1841f6ca2c00014c4302", quotas);

    assertEquals(Integer.valueOf(25), newQuotas.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(11), newQuotas.getMaxConcurrentWorkflows());
    assertEquals(Integer.valueOf(250), newQuotas.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(74), newQuotas.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(8), newQuotas.getMaxWorkflowStorage());

    WorkflowQuotas updated = controller.getTeamQuotas("5d1a1841f6ca2c00014c4302");
    assertEquals(newQuotas.getMaxWorkflowCount(), updated.getMaxWorkflowCount());
    assertEquals(newQuotas.getMaxConcurrentWorkflows(), updated.getMaxConcurrentWorkflows());
    assertEquals(newQuotas.getMaxWorkflowExecutionMonthly(),
        updated.getMaxWorkflowExecutionMonthly());
    assertEquals(newQuotas.getMaxWorkflowExecutionTime(), updated.getMaxWorkflowExecutionTime());
    assertEquals(newQuotas.getMaxWorkflowStorage(), updated.getMaxWorkflowStorage());
  }

  @Test
  public void testGetDefaultQuotas() {
    Quotas quota = controller.getDefaultQuotas();
    assertEquals(Integer.valueOf(4), quota.getMaxConcurrentWorkflows());
    assertEquals(Integer.valueOf(10), quota.getMaxWorkflowCount());
    assertEquals(Integer.valueOf(100), quota.getMaxWorkflowExecutionMonthly());
    assertEquals(Integer.valueOf(30), quota.getMaxWorkflowExecutionTime());
    assertEquals(Integer.valueOf(5), quota.getMaxWorkflowStorage());
  }

  @Test
  public void deactivateTeam() {
    FlowTeamEntity entity = controller.deactivateTeam("5d1a1841f6ca2c00014c4309");
    assertEquals(false, entity.getIsActive());
    assertEquals(2, controller.getTeams().size());
  }

  private Date firstOfNextMonth() {
    Calendar nextMonth = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    nextMonth.add(Calendar.MONTH, 1);
    nextMonth.set(Calendar.DAY_OF_MONTH, 1);
    nextMonth.set(Calendar.HOUR_OF_DAY, 0);
    nextMonth.set(Calendar.MINUTE, 0);
    nextMonth.set(Calendar.SECOND, 0);
    nextMonth.set(Calendar.MILLISECOND, 0);
    return nextMonth.getTime();
  }
}
