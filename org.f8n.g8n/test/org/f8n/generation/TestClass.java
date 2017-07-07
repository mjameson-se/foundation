package org.f8n.generation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.f8n.g8n.TemplateProcessor;
import org.f8n.g8n.VariableResolver;
import org.f8n.g8n.base.ExtensionRegistry;
import org.f8n.g8n.base.FunctionsPlugin;
import org.f8n.g8n.base.IfDefPlugin;
import org.f8n.g8n.base.SubstitutionPlugin;
import org.f8n.g8n.java.AnnotationPlugin;
import org.f8n.g8n.java.AnnotationPlugin.AnnotationRegistration;
import org.f8n.r8n.ClassStream;
import org.f8n.r8n.ClasspathSearch;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.common.collect.ImmutableMap;

public class TestClass
{
  @Test
  public void test() throws IOException
  {
    System.setProperty("file.comment", "resources/test/Test.comment");
    InputStream is = Files.newInputStream(Paths.get("resources/templates/ktisis/java/FileBase.template"), StandardOpenOption.READ);
    InputStream fis = Files.newInputStream(Paths.get("resources/test/TestClass.json"), StandardOpenOption.READ);

    TemplateProcessor.registerPlugin(new ExtensionRegistry());
    TemplateProcessor.registerPlugin(new IfDefPlugin());
    TemplateProcessor.registerPlugin(new AnnotationPlugin());
    TemplateProcessor.registerPlugin(new SubstitutionPlugin());
    TemplateProcessor.registerPlugin(new FunctionsPlugin());
    TemplateProcessor.loadAll(new ClasspathSearch().includePackage("org.yesod.ktisis.java").classStream());
    TemplateProcessor.loadAll(new ClassStream(AnnotationStuff.class));
    VariableResolver parent = ImmutableMap.of("parent",
                                              ImmutableMap.of("features", ImmutableMap.of("setters", Boolean.TRUE, "builder", Boolean.TRUE)))::get;
    System.out.println(TemplateProcessor.processTemplate(is, VariableResolver.merge(JSON.std.mapFrom(fis)::get, parent)));
  }

  public static class AnnotationStuff
  {
    @AnnotationRegistration("field")
    public String field(VariableResolver ctx)
    {
      return TemplateProcessor.processTemplate("@Field(\"${type}\")", ctx);
    }

    @AnnotationRegistration("field")
    public String field2(VariableResolver ctx)
    {
      return TemplateProcessor.processTemplate("@Field2(\"${name}\")", ctx);
    }

    @AnnotationRegistration("ctor_arg")
    public String ctorArg(VariableResolver ctx)
    {
      return TemplateProcessor.processTemplate("@Arg(\"${name}\")", ctx);
    }
  }

  @Test
  @Ignore("Missing WhitespaceHelper.json")
  public void generateWhitespaceHelper() throws Exception
  {
    System.setProperty("file.comment", "resources/test/Test.comment");

    InputStream is = Files.newInputStream(Paths.get("resources/templates/ktisis/java/FileBase.template"), StandardOpenOption.READ);
    InputStream fis = Files.newInputStream(Paths.get("resources/test/WhitespaceHelper.json"), StandardOpenOption.READ);

    TemplateProcessor.registerPlugin(new AnnotationPlugin());
    TemplateProcessor.registerPlugin(new ExtensionRegistry());
    TemplateProcessor.registerPlugin(new SubstitutionPlugin());
    TemplateProcessor.registerPlugin(new FunctionsPlugin());
    TemplateProcessor.loadAll(new ClasspathSearch().includePackage("org.yesod.ktisis.java").classStream());

    System.out.println(TemplateProcessor.processTemplate(is, JSON.std.mapFrom(fis)::get));

  }

  @Test
  public void testIfDef() throws Exception
  {
    System.setProperty("file.comment", "resources/test/Test.comment");

    TemplateProcessor.registerPlugin(new AnnotationPlugin());
    TemplateProcessor.registerPlugin(new IfDefPlugin());
    TemplateProcessor.registerPlugin(new ExtensionRegistry());
    TemplateProcessor.registerPlugin(new SubstitutionPlugin());
    TemplateProcessor.registerPlugin(new FunctionsPlugin());
    TemplateProcessor.loadAll(new ClasspathSearch().includePackage("org.yesod.ktisis.java").classStream());

    try (InputStream is = Files.newInputStream(Paths.get("resources/test/ifdeftest.template"), StandardOpenOption.READ))
    {
      System.out.println(TemplateProcessor.processTemplate(is, ImmutableMap.of("not", "defined")::get));
    }
    System.out.println();
    try (InputStream is = Files.newInputStream(Paths.get("resources/test/ifdeftest.template"), StandardOpenOption.READ))
    {
      System.out.println(TemplateProcessor.processTemplate(is, ImmutableMap.of("define_me", "defined")::get));
    }
    System.out.println();
    try (InputStream is = Files.newInputStream(Paths.get("resources/test/ifdeftest.template"), StandardOpenOption.READ))
    {
      System.out.println(TemplateProcessor.processTemplate(is, ImmutableMap.of("second_def", "defined")::get));
    }
  }
}
