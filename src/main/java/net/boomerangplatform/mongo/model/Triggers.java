package net.boomerangplatform.mongo.model;

public class Triggers {

  private Trigger manual;
  private TriggerScheduler scheduler;
  private TriggerEvent webhook;
  private TriggerEvent dockerhub;
  private TriggerEvent slack;
  private TriggerEvent custom;
  
  public Trigger getManual() {
    return manual;
  }
  public void setManual(Trigger manual) {
    this.manual = manual;
  }
  public TriggerScheduler getScheduler() {
    return scheduler;
  }
  public void setScheduler(TriggerScheduler scheduler) {
    this.scheduler = scheduler;
  }
  public TriggerEvent getWebhook() {
    return webhook;
  }
  public void setWebhook(TriggerEvent webhook) {
    this.webhook = webhook;
  }
  public TriggerEvent getDockerhub() {
    return dockerhub;
  }
  public void setDockerhub(TriggerEvent dockerhub) {
    this.dockerhub = dockerhub;
  }
  public TriggerEvent getSlack() {
    return slack;
  }
  public void setSlack(TriggerEvent slack) {
    this.slack = slack;
  }
  public TriggerEvent getCustom() {
    return custom;
  }
  public void setCustom(TriggerEvent custom) {
    this.custom = custom;
  }
}
