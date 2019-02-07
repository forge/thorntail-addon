package org.jboss.forge.addon.thorntail.ui;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SetupCommandTest
{
   private ProjectFactory projectFactory;
   private UITestHarness uiTestHarness;
   private ShellTest shellTest;

   private Project project;

   @Before
   public void setUp() throws Exception
   {
      AddonRegistry addonRegistry = Furnace.instance(getClass().getClassLoader()).getAddonRegistry();
      projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
      uiTestHarness = addonRegistry.getServices(UITestHarness.class).get();
      shellTest = addonRegistry.getServices(ShellTest.class).get();
      project = projectFactory.createTempProject();
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
      try (CommandController controller = uiTestHarness.createCommandController(SetupCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         // Checks the command metadata
         assertTrue(controller.getCommand() instanceof SetupCommand);
         UICommandMetadata metadata = controller.getMetadata();
         assertEquals("Thorntail: Setup", metadata.getName());
         assertEquals("Thorntail", metadata.getCategory().getName());
         assertNull(metadata.getCategory().getSubCategory());
         assertEquals(3, controller.getInputs().size());
         assertFalse(controller.hasInput("dummy"));
         assertTrue(controller.hasInput("httpPort"));
         assertTrue(controller.hasInput("contextPath"));
         assertTrue(controller.hasInput("portOffset"));
      }
   }

   @Test
   public void checkCommandShell() throws Exception
   {
      shellTest.getShell().setCurrentResource(project.getRoot());
      Result result = shellTest.execute(("thorntail-setup"), 10, TimeUnit.SECONDS);

      Assert.assertThat(result, not(instanceOf(Failed.class)));
      Assert.assertTrue(project.hasFacet(ThorntailFacet.class));
   }

   @Test
   public void testThorntailSetup() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(SetupCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         Assert.assertTrue(controller.isValid());
         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Thorntail is now set up! Enjoy!"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         Assert.assertTrue(flag.get());
         ThorntailFacet facet = project.getFacet(ThorntailFacet.class);
         Assert.assertTrue(facet.isInstalled());

         MavenPluginAdapter thorntailPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                  .getEffectivePlugin(ThorntailFacet.PLUGIN_COORDINATE);
         Assert.assertEquals("thorntail-maven-plugin", thorntailPlugin.getCoordinate().getArtifactId());
         Assert.assertEquals(1, thorntailPlugin.getExecutions().size());
         Assert.assertEquals(0, thorntailPlugin.getConfig().listConfigurationElements().size());
      }
   }

   @Test
   public void testThorntailSetupWithParameters() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(SetupCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("httpPort", 4242);
         controller.setValueFor("contextPath", "root");
         controller.setValueFor("portOffset", 42);
         Assert.assertTrue(controller.isValid());

         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Thorntail is now set up! Enjoy!"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         Assert.assertTrue(flag.get());
         ThorntailFacet facet = project.getFacet(ThorntailFacet.class);
         Assert.assertTrue(facet.isInstalled());

         MavenPluginAdapter thorntailPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                  .getEffectivePlugin(ThorntailFacet.PLUGIN_COORDINATE);
         Assert.assertEquals("thorntail-maven-plugin", thorntailPlugin.getCoordinate().getArtifactId());
         Assert.assertEquals(1, thorntailPlugin.getExecutions().size());
         Configuration config = thorntailPlugin.getConfig();
         ConfigurationElement configurationProps = config.getConfigurationElement("properties");
         Assert.assertEquals(3, configurationProps.getChildren().size());
         Assert.assertEquals("4242", configurationProps.getChildByName("swarm.http.port").getText());
         Assert.assertEquals("root", configurationProps.getChildByName("swarm.context.path").getText());
         Assert.assertEquals("42", configurationProps.getChildByName("swarm.port.offset").getText());
      }
   }

   @Test
   public void testThorntailSetupWithNullParameters() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(SetupCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("httpPort", null);
         controller.setValueFor("contextPath", null);
         controller.setValueFor("portOffset", null);
         Assert.assertTrue(controller.isValid());

         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Thorntail is now set up! Enjoy!"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         Assert.assertTrue(flag.get());
         ThorntailFacet facet = project.getFacet(ThorntailFacet.class);
         Assert.assertTrue(facet.isInstalled());

         MavenPluginAdapter thorntailPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                  .getEffectivePlugin(ThorntailFacet.PLUGIN_COORDINATE);
         Assert.assertEquals("thorntail-maven-plugin", thorntailPlugin.getCoordinate().getArtifactId());
         Assert.assertEquals(1, thorntailPlugin.getExecutions().size());
         Assert.assertEquals(0, thorntailPlugin.getConfig().listConfigurationElements().size());
      }
   }

   @Test
   public void testThorntailSetupWithZeroParameters() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(SetupCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("httpPort", 0);
         controller.setValueFor("portOffset", 0);
         Assert.assertTrue(controller.isValid());

         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Thorntial is now set up! Enjoy!"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         Assert.assertTrue(flag.get());
         ThorntailFacet facet = project.getFacet(ThorntailFacet.class);
         Assert.assertTrue(facet.isInstalled());

         MavenPluginAdapter thorntailPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                  .getEffectivePlugin(ThorntailFacet.PLUGIN_COORDINATE);
         Assert.assertEquals("thorntail-maven-plugin", thorntailPlugin.getCoordinate().getArtifactId());
         Assert.assertEquals(1, thorntailPlugin.getExecutions().size());
         Assert.assertEquals(0, thorntailPlugin.getConfig().listConfigurationElements().size());
      }
   }
}
