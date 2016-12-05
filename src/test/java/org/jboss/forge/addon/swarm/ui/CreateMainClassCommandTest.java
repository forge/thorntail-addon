/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.swarm.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.swarm.facet.WildFlySwarmFacet;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class CreateMainClassCommandTest
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
      project = projectFactory.createTempProject(Arrays.asList(JavaSourceFacet.class, WildFlySwarmFacet.class));
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
      try (CommandController controller = uiTestHarness.createCommandController(CreateMainClassCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         // Checks the command metadata
         assertTrue(controller.getCommand() instanceof CreateMainClassCommand);
         UICommandMetadata metadata = controller.getMetadata();
         assertEquals("WildFly Swarm: New Main Class", metadata.getName());
         assertEquals("WildFly Swarm", metadata.getCategory().getName());
         assertNull(metadata.getCategory().getSubCategory());
      }
   }

   @Test
   public void testExecution() throws Exception
   {
      Assert.assertTrue(project.hasFacet(WildFlySwarmFacet.class));
      try (CommandController controller = uiTestHarness.createCommandController(CreateMainClassCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("targetPackage", "org.example");
         Assert.assertTrue(controller.isValid());
         final AtomicBoolean flag = new AtomicBoolean();
         controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener()
         {
            @Override
            public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result)
            {
               if (result.getMessage().equals("Main Class org.example.Main was created"))
               {
                  flag.set(true);
               }
            }
         });
         controller.execute();
         Assert.assertTrue("Class not created", flag.get());
      }
      JavaResource javaResource = project.getFacet(JavaSourceFacet.class).getJavaResource("org.example.Main");
      Assert.assertTrue(javaResource.exists());
      WildFlySwarmFacet facet = project.getFacet(WildFlySwarmFacet.class);
      Assert.assertEquals("org.example.Main", facet.getConfiguration().getMainClass());
   }

}
