package net.boomerangplatform.tests.controller;

import static org.springframework.test.util.MetaAnnotationUtils.findAnnotationDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;
import org.springframework.test.util.MetaAnnotationUtils;
import org.springframework.test.util.MetaAnnotationUtils.AnnotationDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import net.boomerangplatform.mongo.entity.ActivityEntity;
import net.boomerangplatform.mongo.service.MongoConfiguration;

public class ActiveProfilesResolver1 extends DefaultActiveProfilesResolver {
  private static final Log logger = LogFactory.getLog(ActiveProfilesResolver1.class);
  private static Collection<String> listContainingResolvedMongoClass = new ArrayList<String>();
  private Document mongoConfiguration;
  Collection<String> primaryInheritedProfiles = new ArrayList<String>();
  static String[] profiles;
  private StandardEvaluationContext _evaluationContext = null;
  public int HIT_COUNT = 0;
  private static Collection<String> profileCollection = new ArrayList<String>();
  private ApplicationContext theContext; 
  
  
  public ActiveProfilesResolver1() {
    theContext = ApplicationContextProvider.getApplicationContext();
  }
  @SuppressWarnings("unchecked")
  @Override
  public String[] resolve(Class<?> testClass) {
    if (false) {
    throw new RuntimeException("HI");
    }
//    Assert.notNull(testClass., "Class must not be null");
    if (false) {
      /** OUTPUT OF BELOW = 
       * java.lang.RuntimeException: testClass (canonical name) = 
       * net.boomerangplatform.tests.controller.TeamControllerTeststestClass.getModule() = 
       * unnamed module @38b27cdctestClass.getName() = 
       * net.boomerangplatform.tests.controller.TeamControllerTests
       */
      throw new RuntimeException("\ntestClass (canonical name) = " + testClass.getCanonicalName() +
      "testClass.getModule() = " + testClass.getModule() + 
      "\ntestClass.getName() = " + testClass.getName() +"\n");   
    }
//    Assert.isInstanceOf(TeamControllerTests.class, testClass);
    // throw new RuntimeException("testClazz = testClass = " + testClazz);
    Collection<String> allProfiles = null;
    if (HIT_COUNT++ == 0) {
      primaryInheritedProfiles = ActiveProfilesResolver1.resolveActiveProfiles(testClass);
      
      if (logger.isInfoEnabled()) {
        logger.info("primaryInheritedProfiles(size) = " + primaryInheritedProfiles.size());
      }
//      listContainingResolvedMongoClass = resolveMongoClass(Document.class);
//       was:   ActiveProfilesResolver1.resolveActiveProfiles(MongoConfiguration.class);
      listContainingResolvedMongoClass = defineMongoConfiguration(TeamControllerTests.class);

      primaryInheritedProfiles.addAll(listContainingResolvedMongoClass);
      allProfiles = primaryInheritedProfiles;
      return ArrayUtils.toStringArray(allProfiles.toArray());// new String[]
                                                             // CollectionUtils//primaryInheritedProfiles;
    }
    // TODO: RE-ADD if needed resolvedMongoClass = resolveMongoClass(testClazz);
    return null;
  }

  public Collection<String> defineMongoConfiguration(Class<?> testClass) {
    ClassLoader testClassClassLoader = null;
    ClassLoader mongoClassLoader = null;
//    MongoConfiguration mc = null;
    MongoConfiguration mongoConfiguration = null;
    ActivityEntity ae = null;
    String exportMe = "";
    String returnVal = "";
    try {

      String name = "org.springframework.data.mongodb.core.mapping.Document";
      boolean initialize = true;
      testClassClassLoader = testClass.getClassLoader();
//      testClassClassLoader.
      Class<?> documentClass = Class.forName(name, initialize, testClassClassLoader);
       MongoConfiguration mc = new MongoConfiguration();

       final ExpressionParser parser = new SpelExpressionParser();
//       AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MongoConfig.class);
     
       throw new RuntimeException("applicationContext is : " + theContext);
//TODO: DELETE TRY/CATCH
    } catch (Exception e) { e.printStackTrace();  } return null;
//       if (applicationContext instanceof ApplicationContext) {
//           throw new RuntimeException("Context is instance of ApplicationContext");
//           if (context.getAutowireCapableBeanFactory() instanceof ConfigurableBeanFactory) {
//             throw new RuntimeException("Context.getAutowireCapableBeanFactory() is instance of ApplicationContext");
//           }
//         if (context instanceof AnnotationConfigApplicationContext) {
//           throw new RuntimeException("Context is instance of AnnotationConfigApplicationContext");
//           if (context.getAutowireCapableBeanFactory() instanceof ConfigurableBeanFactory) {
//             throw new RuntimeException("Context.getAutowireCapableBeanFactory() is instance of ApplicationContext");
//           }
//         if (false) {
//           throw new RuntimeException("Context is instance of ApplicationContext");
//         }
//         if (false) {
//           throw new RuntimeException("Context is instance of ApplicationContext");
//         }
//       }
/*null* DefaultListableBeanFactory beanFactory=(DefaultListableBeanFactory) context.getBeanFactory();
       

       Expression spelExpression = parser.parseExpression("#mongoConfiguration.fullCollectionName('flow_workflows_activity')");

       String value = spelExpression.getValue(context, null, String.class);

      if (context == null)
        throw new RuntimeException("StandardEvaluationContext is null");
      
//       ((EvaluationContext) context).setVariable("mongoConfiguration", mc);
 
      mongoConfiguration =
          (MongoConfiguration) ((EvaluationContext) context).getBeanResolver().
          resolve((EvaluationContext) context, "mongoConfiguration");
      
      
      spelExpression.getValue(context);
      
      
      BeanDefinitionBuilder mongoBeanBuilder =
          BeanDefinitionBuilder.rootBeanDefinition(documentClass);
      AbstractBeanDefinition exportBeanDefinition = mongoBeanBuilder.getBeanDefinition();
      exportMe = exportBeanDefinition.toString();
      returnVal = exportMe;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (AccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      // throw new RuntimeException("DUNNO");
    }
    
    Collection<String> collection = new ArrayList<String>();
    collection.add(returnVal);
    return collection;
   
    */
  
  }


//  

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
    /* TODO: MAY NEED TO DO THE Same for:
     *     Class<ActiveProfiles> annotationType = MongoConfiguration.class;

     */
    Class<ActiveProfiles> annotationType = ActiveProfiles.class;
    AnnotationDescriptor<ActiveProfiles> descriptor =
        MetaAnnotationUtils.findAnnotationDescriptor(testClass, annotationType);
    if (descriptor == null && logger.isDebugEnabled()) {
      logger.debug(String.format(
          "Could not find an 'annotation declaring class' for annotation type [%s] and class [%s]",
          annotationType.getName(), testClass.getName()));
    }
    Set<String> activeProfiles = null;
    do {  
          Class<?> rootDeclaringClass = descriptor.getRootDeclaringClass();
          Class<?> declaringClass = descriptor.getDeclaringClass();
          ActiveProfiles annotation = descriptor.synthesizeAnnotation();
          
          if (logger.isTraceEnabled()) {
            logger.trace(String.format("Retrieved @ActiveProfiles [%s] for declaring class [%s]",
                annotation, declaringClass.getName()));
          }
    
          ///The type of ActiveProfilesResolver to use for resolving the active bean definition profiles programmatically.
          Class<? extends ActiveProfilesResolver> resolverClass = annotation.resolver();
          if (ActiveProfilesResolver1.class == resolverClass) {
            resolverClass = ActiveProfilesResolver1.class;
          }
    
          ActiveProfilesResolver resolver;
          try {
            resolver = BeanUtils.instantiateClass(resolverClass, ActiveProfilesResolver1.class);
          } catch (Exception ex) {
            String msg = String.format(
                "Could not instantiate ActiveProfilesResolver of type [%s] " + "for test class [%s]",
                resolverClass.getName(), rootDeclaringClass.getName());
            logger.error(msg);
            throw new IllegalStateException(msg, ex);
          }
    // TODO: GETS HERE
          profiles = ((ActiveProfilesResolver1) resolver).resolves(rootDeclaringClass);
          if (!ObjectUtils.isEmpty(profiles)) {
            profileArrays.add(profiles);
          }
    
          // throw new RuntimeException("listContainingResolvedMongoClass =" +
          // listContainingResolvedMongoClass);
          /*******
           * TODO: OFFENDING CODE
           * ********/
          Object objs[] = listContainingResolvedMongoClass.toArray();
          String s = "";
          String addUs[] = new String[1];
          int i = 0;
          for (Object o : objs) {
            if (o instanceof Object) {
              s = (String) o;
              if (true) {
                throw new RuntimeException("s = " + s);
              }
              addUs[i++] = s;
            }
            profileArrays.add(addUs);
    
            descriptor = (annotation.inheritProfiles()
                ? MetaAnnotationUtils.findAnnotationDescriptor(rootDeclaringClass.getSuperclass(),
                    annotationType)
                : null);
          }
    
          // Reverse the list so that we can traverse "down" the hierarchy.
          Collections.reverse(profileArrays);
          if (profileArrays == null) {
            throw new RuntimeException("ProfileArrays is null");
          }
          activeProfiles = new LinkedHashSet<>();
          for (String[] theProfiles : profileArrays) {
            for (String profile : theProfiles) {
              if (StringUtils.hasText(profile) && !profile.contains("null")) {
                activeProfiles.add(profile.trim());
              }
            }
          }
          descriptor = null;
        } while (descriptor != null); 

//    ArrayList<String> suSppliedArray = new ArrayList<String>(); //activeProfiles.size()];
    // TODO: Verify activeProfiles is a list?
//    String[] supply = activeofiles.toArray(suppliedArray.toArray(new String[1]));
    // TODO: ERRORS OUT HERE BC supply is null
    for (String str : activeProfiles) {
      profileCollection.add(str);
    }
    return profileCollection;
  }
  
  /**
   * Resolve the <em>bean definition profiles</em> for the given {@linkplain Class test class} based
   * on profiles configured declaratively via {@link ActiveProfiles#profiles} or
   * {@link ActiveProfiles#value}.
   * 
   * @param testClass the test class for which the profiles should be resolved; never {@code null}
   * @return the list of bean definition profiles to use when loading the
   *         {@code ApplicationContext}; never {@code null}
   */
  public String[] resolves(Class<?> testClass) {
    Assert.notNull(testClass, "Class must not be null");

    final Set<String> activeProfiles = new LinkedHashSet<>();

    Class<ActiveProfiles> annotationType = ActiveProfiles.class;
    AnnotationDescriptor<ActiveProfiles> descriptor =
        findAnnotationDescriptor(testClass, annotationType);

    if (descriptor == null) {
      if (logger.isDebugEnabled()) {
        logger.debug(String.format(
            "Could not find an 'annotation declaring class' for annotation type [%s] and class [%s]",
            annotationType.getName(), testClass.getName()));
      }
    } else {
      Class<?> declaringClass = descriptor.getDeclaringClass();
      ActiveProfiles annotation = descriptor.synthesizeAnnotation();

      if (logger.isTraceEnabled()) {
        logger.trace(String.format("Retrieved @ActiveProfiles [%s] for declaring class [%s].",
            annotation, declaringClass.getName()));
      }

      for (String profile : annotation.profiles()) {
        if (StringUtils.hasText(profile)) {
          activeProfiles.add(profile.trim());
        }
      }
    }

    return StringUtils.toStringArray(activeProfiles);
  }
  
  /** 
   * Lazily creates a StandardEvaluationContext. The code has been inspired by {@link StandardBeanExpressionResolver#evaluate(String,BeanExpressionContext)}
   */
  public EvaluationContext createEvaluationContext(){
    if (_evaluationContext == null) {
      ConfigurableBeanFactory beanFactory=null;
//      if (applicationContext instanceof ConfigurableBeanFactory) {
        beanFactory=(DefaultListableBeanFactory) theContext;
//      }
      if (beanFactory == null && theContext != null && theContext.getAutowireCapableBeanFactory() instanceof ConfigurableBeanFactory) {
        beanFactory=(ConfigurableBeanFactory) theContext.getAutowireCapableBeanFactory();
      }
      if (beanFactory == null) {
        throw new IllegalStateException("Unable to find a ConfigurableBeanFactory");
      }
      BeanExpressionContext beanEvaluationContext=new BeanExpressionContext(beanFactory, null);
      StandardEvaluationContext sec=new StandardEvaluationContext();
      sec.setRootObject(beanEvaluationContext);
      sec.addPropertyAccessor(new BeanExpressionContextAccessor());
      sec.addPropertyAccessor(new BeanFactoryAccessor());
      sec.addPropertyAccessor(new MapAccessor());
      sec.setBeanResolver(new BeanFactoryResolver(beanEvaluationContext.getBeanFactory()));
      sec.setTypeLocator(new StandardTypeLocator(beanEvaluationContext.getBeanFactory().getBeanClassLoader()));
      ConversionService conversionService=beanEvaluationContext.getBeanFactory().getConversionService();
      if (conversionService != null) {
        sec.setTypeConverter(new StandardTypeConverter(conversionService));
      }
      _evaluationContext=sec;
    }
    return _evaluationContext;
  }
}