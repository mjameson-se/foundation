package org.f8n.injector;

import java.util.Optional;

import org.f8n.cornerstone.reflection.ClasspathSearch;
import org.f8n.cornerstone.reflection.TypeInfo;
import org.f8n.injector.example.ApiUserHandler;
import org.f8n.injector.example.ApiUserHandler.User;
import org.f8n.injector.example.ConfidentialInformationHandler;
import org.f8n.injector.example.DatabaseImpl;
import org.f8n.injector.example.DeferralTest;
import org.f8n.injector.example.HttpService;
import org.f8n.injector.example.OptionalDependencyTest;
import org.f8n.injector.example.OptionalNonexistentDependencyTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectorTest
{
  private static final Logger LOG = LoggerFactory.getLogger(InjectorTest.class);
  BasicServiceRegistry registry = new BasicServiceRegistry();
  Injector injector = new Injector(registry);

  @Test
  public void testInjector() throws Exception
  {
    DeferralTest.setDefer(true);
    new ClasspathSearch().includePackage("org.f8n.injector.example").classStream().stream().forEach(injector::addClass);
    injector.reportUnresolved(System.out);
    getOne(OptionalDependencyTest.class).test();
    Assert.assertFalse(isRegistered(DeferralTest.class));
    DeferralTest.setDefer(false);
    LOG.info("Resolving remaining...");
    injector.resolveRemaining();
    injector.reportUnresolved(System.out);
    Assert.assertTrue(isRegistered(DeferralTest.class));
    getOne(OptionalNonexistentDependencyTest.class).test();
    HttpService http = getOne(HttpService.class);
    Assert.assertFalse(http.getLogger().isPresent());
    Assert.assertEquals(2, http.getHandlers().size());
    ApiUserHandler userHandler = getOne(ApiUserHandler.class);
    String userId = "user";
    String userPassword = "password";
    userHandler.createUser(new User(userId, userPassword));
    ConfidentialInformationHandler secretsHandler = getOne(ConfidentialInformationHandler.class);
    String secret = secretsHandler.getSecrets(userId, userPassword);
    DatabaseImpl d = getOne(DatabaseImpl.class);
    Assert.assertEquals(d.secret(), secret);
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
