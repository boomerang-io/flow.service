package net.boomerangplatform.miscs.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import net.boomerangplatform.Application;
import net.boomerangplatform.client.BoomerangTeamService;
import net.boomerangplatform.client.model.Team;
import net.boomerangplatform.misc.FlowTests;
import net.boomerangplatform.tests.MongoConfig;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class, MongoConfig.class})
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("local")
@WithMockUser(roles = {"admin"})
@WithUserDetails("mdroy@us.ibm.com")
public class BoomerangTeamServiceTests extends FlowTests {

  @Autowired
  private BoomerangTeamService teamService;


  @Test
  public void testGetBoomerangTeam() {
    Team team = teamService.getTeam("5e73782ee04d700001a00249");
    assertEquals("WDC2 ISE Dev", team.getName());
  }

  @Override
  @Before
  public void setUp() throws IOException {

    super.setUp();

    mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    mockServer
        .expect(manyTimes(),
            requestTo(
                containsString("http://localhost:8085/admin/teams/team/5e73782ee04d700001a00249")))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(getMockFile("mock/admin/teams.json"), MediaType.APPLICATION_JSON));
  }
}
