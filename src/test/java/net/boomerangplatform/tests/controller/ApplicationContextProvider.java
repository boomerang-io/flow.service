/**
 * 
 */
package net.boomerangplatform.tests.controller;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author paulvernonhayesii
 *
 */
@Configuration
public class ApplicationContextProvider implements ApplicationContextAware {

  private static ApplicationContext ctx = null;

  public static ApplicationContext getApplicationContext() {
    return ctx;
  }

  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    // Assign the ApplicationContext into a static variable
    ApplicationContextProvider.ctx = ctx;
  }
}
