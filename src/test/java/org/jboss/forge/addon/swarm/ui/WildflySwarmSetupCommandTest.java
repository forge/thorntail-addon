package org.jboss.forge.addon.swarm.ui;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.swarm.facet.WildflySwarmFacet;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class WildflySwarmSetupCommandTest {

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private ShellTest shellTest;

    private Project project;

    @Before
    public void setUp() {
        project = projectFactory.createTempProject();
    }

    @Test
    public void checkCommandMetadata() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(WildflySwarmSetupCommand.class,
                project.getRoot())) {
            controller.initialize();
            // Checks the command metadata
            assertTrue(controller.getCommand() instanceof WildflySwarmSetupCommand);
            UICommandMetadata metadata = controller.getMetadata();
            assertEquals("Wildfly-Swarm: Setup", metadata.getName());
            assertEquals("Uncategorized", metadata.getCategory().getName());
            assertNull("Bean Validation", metadata.getCategory().getSubCategory());
            assertEquals(0, controller.getInputs().size());
            assertFalse(controller.hasInput("dummy"));
        }
    }

    @Test
    public void checkCommandShell() throws Exception {
        shellTest.getShell().setCurrentResource(project.getRoot());
        Result result = shellTest.execute(("wildfly-swarm-setup"), 10, TimeUnit.SECONDS);

        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertTrue(project.hasFacet(WildflySwarmFacet.class));
    }

   @Test
   public void testWildflySwarmSetup() throws Exception
   {
      try (CommandController tester = uiTestHarness.createCommandController(WildflySwarmSetupCommand.class,
               project.getRoot()))
      {
         tester.initialize();

         Assert.assertTrue(tester.isValid());

         final AtomicBoolean flag = new AtomicBoolean();
         tester.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Wildfly Swarm is now set up! Enjoy!"))
               {
                  flag.set(true);
               }
            }
         });
         tester.execute();
         Assert.assertEquals(true, flag.get());
      }
   }
}
