package net.boomerangplatform.tests.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.expression.AccessException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;
import org.springframework.test.util.MetaAnnotationUtils;
import org.springframework.test.util.MetaAnnotationUtils.AnnotationDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import net.boomerangplatform.mongo.service.MongoConfiguration;

public class ActiveProfilesResolver1 implements ActiveProfilesResolver {
  private static final Log logger = LogFactory.getLog(ActiveProfilesResolver1.class);
  private static String resolvedMongoClass = "";
  private Class<?> testClazz = null;
  Collection<String> primaryInheritedProfiles = null;
  
  public ActiveProfilesResolver1() {
  }
  
  public static int HIT_COUNT = 0;

  @SuppressWarnings("unchecked")
  @Override
  public String[] resolve(Class<?> testClass) {
    testClazz = testClass;
    if (HIT_COUNT == 0) {
      HIT_COUNT++;
      primaryInheritedProfiles = ActiveProfilesResolver1.resolveActiveProfiles(testClass);
      resolvedMongoClass = resolveMongoClass(testClass);
      primaryInheritedProfiles.add(resolvedMongoClass);
    }
    return new String[] {resolveMongoClass(testClass)};
  }

  public String resolveMongoClass(Class<?> testClass) {
    ClassLoader testClassClassLoader = null;
    ClassLoader mongoClassLoader = null;
    MongoConfiguration mc = null;
    MongoConfiguration mongoConfiguration = null;
    String exportMe = "";
    String returnVal = "";
    try {


      // throw new RuntimeException("AHA"); // Gets here
      String name = "net.boomerangplatform.mongo.service.MongoConfiguration";
      boolean initialize = true;
      testClassClassLoader = testClass.getClassLoader();
      Class<?> mongoConfigurationClass = Class.forName(name, initialize, testClassClassLoader);
      Class<?>[] classTypeParams = {};
      Object constructor = mongoConfigurationClass.getDeclaredConstructor(classTypeParams);
      Object instance =
          ((java.lang.reflect.Constructor<MongoConfiguration>) constructor).newInstance();
      // throw new RuntimeException("Gets here");
      // invoke constructor with instance

      mc = (MongoConfiguration) mongoConfigurationClass.cast(instance);
      mongoClassLoader = mc.getClass().getClassLoader();


      mongoConfigurationClass = mongoClassLoader.loadClass(instance.toString());
      SpelExpressionParser parser = new SpelExpressionParser();
      // parser.

      SimpleEvaluationContext ec = SimpleEvaluationContext.forReadWriteDataBinding().build();
      if (ec == null)
        throw new RuntimeException("SimpleEvaluationContext is null");
      if (ec.getBeanResolver() == null) {
        throw new RuntimeException("SimpleEvaluationContext.beanResolver is null");
      }
      mongoConfiguration =
          (MongoConfiguration) ec.getBeanResolver().resolve(ec, "mongoConfiguration");
      BeanDefinitionBuilder mongoBeanBuilder =
          BeanDefinitionBuilder.rootBeanDefinition(mongoConfigurationClass);
      AbstractBeanDefinition exportBeanDefinition = mongoBeanBuilder.getBeanDefinition();
      exportMe = exportBeanDefinition.toString();
      returnVal = "exportMe";
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
        | InvocationTargetException | ClassNotFoundException | AccessException e) {
      e.getMessage();
    } finally {
      // throw new RuntimeException("DUNNO");
    }

    return returnVal;

  }

  /**
   * Resolve <em>active bean definition profiles</em> for the supplied {@link Class}.
   * <p>
   * Note that the {@link ActiveProfiles#inheritProfiles inheritProfiles} flag of
   * {@link ActiveProfiles @ActiveProfiles} will be taken into consideration. Specifically, if the
   * {@code inheritProfiles} flag is set to {@code true}, profiles defined in the test class will be
   * merged with those defined in superclasses.
   * 
   * @param testClass the class for which to resolve the active profiles (must not be {@code null})
   * @return the set of active profiles for the specified class, including active profiles from
   *         superclasses if appropriate (never {@code null})
   * @see ActiveProfiles
   * @see ActiveProfilesResolver
   * @see org.springframework.context.annotation.Profile
   */
  static Collection<String> resolveActiveProfiles(Class<?> testClass) {
    Assert.notNull(testClass, "Class must not be null");

    final List<String[]> profileArrays = new ArrayList<>();

    Class<ActiveProfiles> annotationType = ActiveProfiles.class;
    AnnotationDescriptor<ActiveProfiles> descriptor =
        MetaAnnotationUtils.findAnnotationDescriptor(testClass, annotationType);
    if (descriptor == null && logger.isDebugEnabled()) {
      logger.debug(String.format(
          "Could not find an 'annotation declaring class' for annotation type [%s] and class [%s]",
          annotationType.getName(), testClass.getName()));
    }

    while (descriptor != null) {
      Class<?> rootDeclaringClass = descriptor.getRootDeclaringClass();
      Class<?> declaringClass = descriptor.getDeclaringClass();
      ActiveProfiles annotation = descriptor.synthesizeAnnotation();

      if (logger.isTraceEnabled()) {
        logger.trace(String.format("Retrieved @ActiveProfiles [%s] for declaring class [%s]",
            annotation, declaringClass.getName()));
      }

      Class<? extends ActiveProfilesResolver> resolverClass = annotation.resolver();
      if (ActiveProfilesResolver.class == resolverClass) {
        resolverClass = DefaultActiveProfilesResolver.class;
      }

      ActiveProfilesResolver resolver;
      try {
        resolver = BeanUtils.instantiateClass(resolverClass, ActiveProfilesResolver.class);
      } catch (Exception ex) {
        String msg = String.format(
            "Could not instantiate ActiveProfilesResolver of type [%s] " + "for test class [%s]",
            resolverClass.getName(), rootDeclaringClass.getName());
        logger.error(msg);
        throw new IllegalStateException(msg, ex);
      }

      String[] profiles = resolver.resolve(rootDeclaringClass);
      if (!ObjectUtils.isEmpty(profiles)) {
        profileArrays.add(profiles);
      }

      /*******
       * 
       * ********/
      profileArrays.add(new String[] {resolvedMongoClass});

      descriptor = (annotation.inheritProfiles()
          ? MetaAnnotationUtils.findAnnotationDescriptor(rootDeclaringClass.getSuperclass(),
              annotationType)
          : null);
    }

    // Reverse the list so that we can traverse "down" the hierarchy.
    Collections.reverse(profileArrays);

    final Set<String> activeProfiles = new LinkedHashSet<>();
    for (String[] profiles : profileArrays) {
      for (String profile : profiles) {
        if (StringUtils.hasText(profile)) {
          activeProfiles.add(profile.trim());
        }
      }
    }

    return activeProfiles;
  }
}
