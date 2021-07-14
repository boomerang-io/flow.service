
package net.boomerangplatform.model.projectstormv5;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.boomerangplatform.mongo.model.ChangeLog;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"config", "dag", "id", "version", "workFlowId", "changelog", "markdown"})
public class WorkflowRevision {

  @JsonProperty("config")
  private RestConfig config;
  @JsonProperty("dag")
  private RestDag dag;
  @JsonProperty("id")
  private String id;
  @JsonProperty("version")
  private long version;
  @JsonProperty("workFlowId")
  private String workFlowId;
  @JsonProperty("changelog")
  private ChangeLog changelog;
  @JsonProperty("markdown")
  private String markdown;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  private boolean templateUpgradesAvailable;

  @JsonProperty("config")
  public RestConfig getConfig() {
    return config;
  }

  @JsonProperty("config")
  public void setConfig(RestConfig config) {
    this.config = config;
  }

  @JsonProperty("dag")
  public RestDag getDag() {
    return dag;
  }

  @JsonProperty("dag")
  public void setDag(RestDag dag) {
    this.dag = dag;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("version")
  public long getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(long version) {
    this.version = version;
  }

  @JsonProperty("workFlowId")
  public String getWorkFlowId() {
    return workFlowId;
  }

  @JsonProperty("workFlowId")
  public void setWorkFlowId(String workFlowId) {
    this.workFlowId = workFlowId;
  }

  @JsonProperty("changelog")
  public ChangeLog getChangelog() {
    return changelog;
  }

  @JsonProperty("changelog")
  public void setChangelog(ChangeLog changelog) {
    this.changelog = changelog;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public boolean isTemplateUpgradesAvailable() {
    return templateUpgradesAvailable;
  }

  public void setTemplateUpgradesAvailable(boolean templateUpgradesAvailable) {
    this.templateUpgradesAvailable = templateUpgradesAvailable;
  }

  public String getMarkdown() {
    return markdown;
  }

  public void setMarkdown(String markdown) {
    this.markdown = markdown;
  }

}
