package org.f8n.generation.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.f8n.generation.TemplatePlugin;
import org.f8n.generation.VariableResolver;
import org.f8n.generation.base.ExtensionMethod.ExtensionPoint;
import org.f8n.generation.base.FeatureTags.Feature;
import org.f8n.cornerstone.reflection.ClassStream;
import org.f8n.cornerstone.reflection.InterfaceWrapper;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class ExtensionRegistry implements TemplatePlugin
{
  private static final Pattern extensionMatcher = Pattern.compile("^#! \\{(\\S*)\\}\\R{0,1}", Pattern.MULTILINE);

  private ListMultimap<String, InterfaceWrapper<ExtensionMethod>> extensionPoints = ArrayListMultimap.create();

  @Override
  public void load(ClassStream cs)
  {
    cs.mapMethods()
      .publicOnly()
      .withAnnotation(ExtensionPoint.class)
      .withReturnType(String.class)
      .withParameterTypes(VariableResolver.class)
      .sorted()
      .<String, ExtensionMethod> asInterface((b) -> b::invoke)
      .forEach((iw) ->
      {
        for (String extPt : iw.getMethod().getAnnotation(ExtensionPoint.class).value())
        {
          extensionPoints.put(extPt, iw);
        }
      });
  }

  @Override
  public String process(Matcher matcher, VariableResolver variableLookup)
  {
    Collection<String> builder = new ArrayList<>();
    String extension = matcher.group(1);
    for (InterfaceWrapper<ExtensionMethod> extMethod : extensionPoints.get(extension))
    {
      Optional<Feature> featureOpt = extMethod.getAnnotation(Feature.class);
      if (featureOpt.isPresent() && !FeatureTags.hasTag(featureOpt.get(), variableLookup))
      {
        continue;
      }

      String output = extMethod.getInterface().process(variableLookup);
      if (output != null)
      {
        builder.add(output);
      }
    }
    return builder.isEmpty() ? null : Joiner.on(WhitespaceHelper.lineEnding()).join(builder) + WhitespaceHelper.lineEnding();
  }

  @Override
  public Pattern pattern()
  {
    return extensionMatcher;
  }
}
