package net.boomerangplatform.tests;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class AbstractFlowTests {

  private static final Logger LOGGER = Logger.getLogger(BoomerangTestConfiguration.class.getName());


  @Mock
  MongoTemplate mockMongoTemplate;
  @Mock
  protected MongoDatabase mockDb;
//  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  MockRestServiceServer mockRestServiceServer;
  @Autowired
  protected RestTemplate mockRestTemplate;
  
  
  @Autowired
  private MongoTemplate mongoTemplate;

  protected abstract Map<String, List<String>> getData();

  protected abstract String[] getCollections();

  //TODO: REMOVE AND MOVE TO APPROVEEXECUTETESTS
  @Before
  public void setUp() throws IOException {
    Mockito.when(mockMongoTemplate.getDb()).thenReturn(mockDb);
    Mockito.when(MockRestServiceServer.bindTo(mockRestTemplate).
        ignoreExpectOrder(true).build()).thenReturn(mockRestServiceServer);

    init();
    clearAllCollections();
    setupDB();
  }

  @After
  public void tearDown() {

  }

  protected String parseToJson(final Object template) throws JsonProcessingException {
    return TestUtil.parseToJson(template);
  }

  protected String loadResourceAsString(String fileName) {
    return TestUtil.loadResourceAsString(fileName);
  }

  private void insertDataIntoCollection(String collectionName, String filePath) throws IOException {
    MongoDatabase db = mongoTemplate.getDb();

    final MongoCollection<Document> collection = db.getCollection(collectionName);
    final Document doc = Document.parse(getMockFile(filePath));
    collection.insertOne(doc);
  }

  protected static String getMockFile(String path) throws IOException {
    return TestUtil.getMockFile(path);
  }

  private void init() {
    MongoDatabase db = mongoTemplate.getDb();

    for (String collection : getCollections()) {

      if (db.getCollection(collection) == null) {
        db.createCollection(collection);
      }
    }
  }

  private void setupDB() {
    getData().entrySet().stream()
        .forEach(collection -> insertDataForEntity(collection.getKey(), collection.getValue()));
  }

  private void clearAllCollections() {
    for (String name : getCollections()) {
      clearColection(name);
    }
  }

  private void insertDataForEntity(String entity, List<String> values) {
    values.forEach(filePath -> {
      try {
        insertDataIntoCollection(entity, filePath);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Error insert data!", e);
      }
    });
  }

  protected void clearDB() {
    mockDb = mongoTemplate.getDb();
    for (String collection : getCollections()) {
      mockDb.getCollection(collection).drop();
    }
  }

  protected void clearColection(String collectionName) {
    MongoDatabase db = mongoTemplate.getDb();
    final MongoCollection<Document> collection = db.getCollection(collectionName);
    collection.deleteMany(new Document());
  }

}
