package net.boomerangplatform.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import net.boomerangplatform.client.model.UserProfile;
import net.boomerangplatform.model.FlowUser;
import net.boomerangplatform.model.OneTimeCode;
import net.boomerangplatform.model.UserQueryResult;
import net.boomerangplatform.mongo.entity.FlowUserEntity;

public interface UserIdentityService {

  public UserProfile getOrRegisterCurrentUser();

  public FlowUserEntity getCurrentUser();

  public FlowUserEntity getUserByID(String userId);

  UserQueryResult getUserViaSearchTerm(String searchTerm, Pageable pageable);

  UserQueryResult getUsers(Pageable pageable);

  List<FlowUserEntity> getUsersForTeams(List<String> teamIds);

  public void updateFlowUser(String userId, FlowUser flowUser);

  public ResponseEntity<Boolean> activateSetup(OneTimeCode otc);

  public void deleteFlowUser(String userId);

  public FlowUserEntity addFlowUser(FlowUser flowUser);

}
