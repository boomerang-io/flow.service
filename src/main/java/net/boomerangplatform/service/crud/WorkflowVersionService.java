package net.boomerangplatform.service.crud;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import net.boomerangplatform.model.FlowWorkflowRevision;
import net.boomerangplatform.model.RevisionResponse;

public interface WorkflowVersionService {

  void deleteWorkflowVersionWithId(String id);

  FlowWorkflowRevision getLatestWorkflowVersion(String workflowId);

  long getLatestWorkflowVersionCount(String workflowId);

  FlowWorkflowRevision getWorkflowVersion(String workflowId, long verison);

  FlowWorkflowRevision getWorkflowVersionWithId(String id);

  ResponseEntity<FlowWorkflowRevision> insertWorkflowVersion(FlowWorkflowRevision flowWorkflowEntity);

  List<RevisionResponse> viewChangelog(Optional<String> workFlowId, Pageable pageable);

}
