package org.jboss.forge.addon.thorntail.ui;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CreateTestClassCommandTest
{

   private ProjectFactory projectFactory;
   private UITestHarness uiTestHarness;
   private ShellTest shellTest;

   private Project project;

   @Before
   public void setUp()
   {
      AddonRegistry addonRegistry = Furnace.instance(getClass().getClassLoader()).getAddonRegistry();
      projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
      uiTestHarness = addonRegistry.getServices(UITestHarness.class).get();
      shellTest = addonRegistry.getServices(ShellTest.class).get();
      project = projectFactory.createTempProject(Arrays.asList(JavaSourceFacet.class, ThorntailFacet.class));
   }

   @After
   public void tearDown() throws Exception
   {
      if (shellTest != null)
      {
         shellTest.close();
      }
   }

   @Test
   public void checkCommandMetadata() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(CreateTestClassCommand.class,
               project.getRoot()))
      {
         controller.initialize();

         UICommandMetadata metadata = controller.getMetadata();
         assertThat(controller.getCommand(), is(instanceOf(CreateTestClassCommand.class)));
         assertThat(metadata.getName(), is("Thorntail: New Test"));
         assertThat(metadata.getCategory().getName(), is("Thorntail"));
         assertThat(metadata.getCategory().getSubCategory(), is(nullValue()));
      }
   }

   @Test
   public void should_create_incontainer_test() throws Exception
   {
      assertThat(project.hasFacet(ThorntailFacet.class), is(true));
      try (CommandController controller = uiTestHarness.createCommandController(CreateTestClassCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("targetPackage", "org.example");
         controller.setValueFor("named", "HelloWorldTest");

         assertThat(controller.isValid(), is(true));
         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Test Class org.example.HelloWorldTest was created"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         assertThat(flag.get(), is(true));
      }
      JavaResource javaResource = project.getFacet(JavaSourceFacet.class)
               .getTestJavaResource("org.example.HelloWorldTest");
      assertThat(javaResource.exists(), is(true));
      JavaClassSource testClass = Roaster.parse(JavaClassSource.class, javaResource.getContents());
      assertThat(testClass.getAnnotation(RunWith.class), is((notNullValue())));
      final AnnotationSource<JavaClassSource> defaultDeployment = testClass.getAnnotation("DefaultDeployment");
      assertThat(defaultDeployment, is((notNullValue())));
      assertThat(defaultDeployment.getValues().size(), is(0));

      final MethodSource<JavaClassSource> testMethod = testClass.getMethod("should_start_service");
      assertThat(testMethod, is(notNullValue()));
      assertThat(testMethod.getAnnotation(Test.class), is(notNullValue()));

   }

   @Test
   public void should_create_asclient_test() throws Exception
   {
      assertThat(project.hasFacet(ThorntailFacet.class), is(true));
      try (CommandController controller = uiTestHarness.createCommandController(CreateTestClassCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("targetPackage", "org.example");
         controller.setValueFor("named", "HelloWorldTest");
         controller.setValueFor("asClient", true);

         assertThat(controller.isValid(), is(true));
         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Test Class org.example.HelloWorldTest was created"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         assertThat(flag.get(), is(true));
      }

      JavaResource javaResource = project.getFacet(JavaSourceFacet.class)
               .getTestJavaResource("org.example.HelloWorldTest");
      assertThat(javaResource.exists(), is(true));
      JavaClassSource testClass = Roaster.parse(JavaClassSource.class, javaResource.getContents());
      assertThat(testClass.getAnnotation(RunWith.class), is((notNullValue())));

      final AnnotationSource<JavaClassSource> defaultDeployment = testClass.getAnnotation("DefaultDeployment");
      assertThat(defaultDeployment, is((notNullValue())));
      final String testable = defaultDeployment.getLiteralValue("testable");
      assertThat(testable, is("false"));

      final MethodSource<JavaClassSource> testMethod = testClass.getMethod("should_start_service");
      assertThat(testMethod, is(notNullValue()));
      assertThat(testMethod.getAnnotation(Test.class), is(notNullValue()));

   }

   @Test
   public void should_set_archivetype_test() throws Exception
   {
      assertThat(project.hasFacet(ThorntailFacet.class), is(true));
      try (CommandController controller = uiTestHarness.createCommandController(CreateTestClassCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("targetPackage", "org.example");
         controller.setValueFor("named", "HelloWorldTest");
         controller.setValueFor("archiveType", "WAR");

         assertThat(controller.isValid(), is(true));
         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Test Class org.example.HelloWorldTest was created"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         assertThat(flag.get(), is(true));
      }

      JavaResource javaResource = project.getFacet(JavaSourceFacet.class)
               .getTestJavaResource("org.example.HelloWorldTest");
      assertThat(javaResource.exists(), is(true));
      JavaClassSource testClass = Roaster.parse(JavaClassSource.class, javaResource.getContents());
      assertThat(testClass.getAnnotation(RunWith.class), is((notNullValue())));

      final AnnotationSource<JavaClassSource> defaultDeployment = testClass.getAnnotation("DefaultDeployment");
      assertThat(defaultDeployment, is((notNullValue())));
      final String testable = defaultDeployment.getLiteralValue("type");
      assertThat(testable, is("DefaultDeployment.Type.WAR"));

      final MethodSource<JavaClassSource> testMethod = testClass.getMethod("should_start_service");
      assertThat(testMethod, is(notNullValue()));
      assertThat(testMethod.getAnnotation(Test.class), is(notNullValue()));

   }


}
