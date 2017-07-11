package org.f8n.inject;

import java.util.Optional;
import java.util.function.Supplier;

import org.f8n.inject.example.ApiUserHandler;
import org.f8n.inject.example.ApiUserHandler.User;
import org.f8n.inject.example.ComponentServiceTest;
import org.f8n.inject.example.ConfidentialInformationHandler;
import org.f8n.inject.example.DatabaseImpl;
import org.f8n.inject.example.DeferralTest;
import org.f8n.inject.example.HttpService;
import org.f8n.inject.example.OptionalDependencyTest;
import org.f8n.inject.example.OptionalNonexistentDependencyTest;
import org.f8n.reflect.ClasspathSearch;
import org.f8n.reflect.TypeInfo;
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
    new ClasspathSearch().includePackage("org.f8n.inject.example").classStream().stream().forEach(injector::addClass);
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

    Assert.assertFalse(isRegistered(ComponentServiceTest.class));
    Assert.assertTrue(isRegistered(Supplier.class));
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
