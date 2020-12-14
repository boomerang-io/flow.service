package net.boomerangplatform.tests;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.StringUtils;
import com.mongodb.ConnectionString;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import net.boomerangplatform.mongo.service.MongoConfiguration;

@Configuration
public class MongoConfig implements InitializingBean, DisposableBean  {

  private static final String MONGO_IP = "localhost";

  private static final String MONGO_DB = "test_db";

  @Value("${test.mongo.log.enable:false}")
  private boolean enableMongoLogs;

  private MongodExecutable executable;

  private int mongoPort;

  @Override
  public void afterPropertiesSet() throws Exception {
    String proxyHost = System.getProperty("http.proxyHost");
    String proxyPort = System.getProperty("http.proxyPort");

    MongodStarter starter;
    ProcessOutput processOutput = enableMongoLogs ? ProcessOutput.getDefaultInstance("mongo")
        : ProcessOutput.getDefaultInstanceSilent();
    Command command = Command.MongoD;
    if (!StringUtils.isEmpty(proxyHost) && !StringUtils.isEmpty(proxyPort)) {

      final IDownloadConfig downloadConfig = new DownloadConfigBuilder().defaultsForCommand(command)
          .proxyFactory(new HttpProxyFactory(proxyHost, Integer.parseInt(proxyPort))).build();

      IRuntimeConfig runtimeConfig =
          new RuntimeConfigBuilder().defaults(command).processOutput(processOutput)
              .artifactStore(
                  new ExtractedArtifactStoreBuilder().defaults(command).download(downloadConfig))
              .build();
      starter = MongodStarter.getInstance(runtimeConfig);
    } else {
      IRuntimeConfig runtimeConfig =
          new RuntimeConfigBuilder().defaults(command).processOutput(processOutput).build();
      starter = MongodStarter.getInstance(runtimeConfig);
    }

    IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION).build();
    executable = starter.prepare(mongodConfig);
    MongodProcess mongodProcess = executable.start();

    this.mongoPort = mongodProcess.getConfig().net().getPort();
  }


  @Bean
  public MongoDatabaseFactory factory() {
    // also possible to connect to a remote or real MongoDB instance
    return new SimpleMongoClientDatabaseFactory(
        new ConnectionString("mongodb://" + MONGO_IP + ":" + mongoPort + "/" + MONGO_DB));
  }


  @Bean
  public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
    MongoTemplate template = new MongoTemplate(mongoDbFactory);
    template.setWriteConcern(WriteConcern.ACKNOWLEDGED);
    return template;
  }
  
  @Bean
  public MongoConfiguration mongoConfiguration() {
      return new MongoConfiguration();
  }

  @Override
  public void destroy() throws Exception {
    if (executable != null) {
      executable.stop();
    }
  }
}
