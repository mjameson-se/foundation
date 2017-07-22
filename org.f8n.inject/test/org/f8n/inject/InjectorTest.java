package org.f8n.inject;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import org.f8n.inject.example.ApiUserHandler;
import org.f8n.inject.example.ApiUserHandler.User;
import org.f8n.inject.example.ComponentServiceTest;
import org.f8n.inject.example.ConfidentialInformationHandler;
import org.f8n.inject.example.DatabaseImpl;
import org.f8n.inject.example.DeferralTest;
import org.f8n.inject.example.ExampleComponentConsumer;
import org.f8n.inject.example.ExampleComponentFactory;
import org.f8n.inject.example.ExampleComponentTwo;
import org.f8n.inject.example.HttpService;
import org.f8n.inject.example.OptionalDependencyTest;
import org.f8n.inject.example.OptionalNonexistentDependencyTest;
import org.f8n.inject.example.TargetTest;
import org.f8n.reflect.ClasspathSearch;
import org.f8n.reflect.TypeInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectorTest
{
  private static final Logger LOG = LoggerFactory.getLogger(InjectorTest.class);
  BasicServiceRegistry registry = new BasicServiceRegistry();
  Injector injector = new Injector(registry);

  @Before
  public void setUp() throws IOException
  {
    DeferralTest.setDefer(true);
    new ClasspathSearch().includePackage("org.f8n.inject.example").classStream().stream().forEach(injector::addClass);
  }

  @Test
  public void testOptional() throws Exception
  {
    getOne(OptionalDependencyTest.class).test();
    Assert.assertFalse(isRegistered(OptionalNonexistentDependencyTest.class));
    injector.resolveRemaining();
    getOne(OptionalNonexistentDependencyTest.class).test();
  }

  @Test
  public void testDeferral()
  {
    Assert.assertFalse(isRegistered(DeferralTest.class));
    DeferralTest.setDefer(false);
    LOG.info("Resolving remaining...");
    injector.resolveRemaining();
    injector.reportUnresolved(System.out);
    Assert.assertTrue(isRegistered(DeferralTest.class));
  }

  @Test
  public void testMultiple()
  {
    injector.resolveRemaining();
    HttpService http = getOne(HttpService.class);
    Assert.assertFalse(http.getLogger().isPresent());
    Assert.assertEquals(3, http.getHandlers().size());
  }

  @Test
  public void testBinding()
  {
    ApiUserHandler userHandler = getOne(ApiUserHandler.class);
    String userId = "user";
    String userPassword = "password";
    userHandler.createUser(new User(userId, userPassword));
    ConfidentialInformationHandler secretsHandler = getOne(ConfidentialInformationHandler.class);
    String secret = secretsHandler.getSecrets(userId, userPassword);
    DatabaseImpl d = getOne(DatabaseImpl.class);
    Assert.assertEquals(d.secret(), secret);
  }

  @Test
  public void testComponet()
  {
    Assert.assertFalse(isRegistered(ComponentServiceTest.class));
    Assert.assertTrue(isRegistered(Supplier.class));
  }

  @Test
  public void testTarget()
  {
    getOne(TargetTest.class).test();
  }

  @Test
  public void testComponentFactory()
  {
    getOne(ExampleComponentConsumer.class).test();
    getOne(ExampleComponentFactory.class).test();
    ExampleComponentTwo.test();
  }

  private <T> T getOne(Class<T> clazz)
  {
    Optional<T> t = registry.<T> getService(new TypeInfo(clazz)).stream().findAny();
    Assert.assertTrue(t.isPresent());
    return t.get();
  }

  private boolean isRegistered(Class<?> clazz)
  {
    return !registry.getService(new TypeInfo(clazz)).isEmpty();
  }
}
