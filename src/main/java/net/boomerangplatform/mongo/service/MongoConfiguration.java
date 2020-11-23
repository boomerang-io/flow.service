package net.boomerangplatform.mongo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MongoConfiguration {

  @Value("${workflow.mongo.collection.prefix}")
  private String workflowCollectionPrefix;
  
  public String fullCollectionName(String collectionName) {
    if (workflowCollectionPrefix == null || workflowCollectionPrefix.isBlank()) {
      return "" + collectionName;
    }
    return workflowCollectionPrefix + "_" + collectionName;
  }
}



