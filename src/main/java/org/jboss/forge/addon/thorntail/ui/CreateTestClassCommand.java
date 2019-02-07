package org.jboss.forge.addon.thorntail.ui;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.parser.java.converters.PackageRootConverter;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.thorntail.config.ThorntailConfiguration;
import org.jboss.forge.addon.thorntail.facet.ThorntailFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.facets.HintsFacet;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.util.Types;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a sample Arquillian Test.
 * It adds Arquillian dependency as well.
 *
 * @author <a href="mailto:asotobue@redhat.com">Alex Soto</a>
 */
@FacetConstraint(ThorntailFacet.class)
public class CreateTestClassCommand extends AbstractThorntailCommand {

    public static final Dependency SWARM_ARQUILLIAN_DEPENDENCY = DependencyBuilder
            .create().setGroupId("io.thorntail")
            .setArtifactId("arquillian")
            .setScopeType("test");

    public static final List<String> ARCHIVE_TYPES = Arrays.asList("JAR", "WAR");

    // Mandatory fields
    private UIInput<String> targetPackage;
    private UIInput<String> named;

    // Optional fields
    private UIInput<Boolean> asClient;
    private UISelectOne<String> archiveType;

    @Override
    public boolean isEnabled(UIContext context) {
        boolean result = super.isEnabled(context);
        return result;
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), getClass()).name("Thorntail: New Test")
                .description("Create new Arquillian test for WildFly Swarm")
                .category(Categories.create("Thorntail"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        InputComponentFactory inputFactory = builder.getInputComponentFactory();

        targetPackage = inputFactory.createInput("targetPackage", String.class)
                .setLabel("Package Name")
                .setRequired(true)
                .setDescription("The package name where this type will be created");

        targetPackage.getFacet(HintsFacet.class).setInputType(InputType.JAVA_PACKAGE_PICKER);
        targetPackage.setValueConverter(new PackageRootConverter(getProjectFactory(), builder));

        targetPackage.setDefaultValue(calculateDefaultPackage(builder.getUIContext()));

        named = inputFactory.createInput("named", String.class)
                .setLabel("Type Name").setRequired(true)
                .setDescription("The type name");

        named.addValidator((context) -> {
            if (!Types.isSimpleName(named.getValue()))
                context.addValidationError(named, "Invalid java type name.");
        });

        asClient = inputFactory.createInput(ThorntailConfiguration.TEST_AS_CLIENT_FLAG_CONFIGURATION_ELEMENT, Boolean.class)
                .setLabel("As Client").setDescription("Sets test mode to as client.");

        archiveType = inputFactory.createSelectOne(ThorntailConfiguration.TEST_TYPE_CONFIGURATION_ELEMENT, String.class)
                .setLabel("Archive Type")
                .setDescription("Sets type of default archive")
                .setValueChoices(ARCHIVE_TYPES);

        builder.add(targetPackage)
                .add(named)
                .add(asClient)
                .add(archiveType);
    }

    protected String calculateDefaultPackage(UIContext context) {
        String packageName;
        Project project = getSelectedProject(context);
        if (project != null) {
            packageName = project.getFacet(JavaSourceFacet.class).getBasePackage();
        } else {
            packageName = "";
        }
        return packageName;
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);

        final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        if (!isArquillianWildflySwarmDependencyInstalled(dependencyFacet)) {
            installArquillianWildflySwarmDependency(dependencyFacet);
        }

        JavaClassSource test = Roaster.create(JavaClassSource.class)
                .setPackage(targetPackage.getValue())
                .setName(named.getValue());

        addArquillianRunner(test);
        addDefaultDeploymentAnnotation(test, project);
        addArquillianResourceEnricher(test);
        addTestMethod(test);

        JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
        facet.saveTestJavaSource(test);

        return Results.success(String.format("Test Class %s.%s was created", targetPackage.getValue(), named.getValue()));
    }

    private void addArquillianResourceEnricher(JavaClassSource test) {
        if (asClient.hasValue()) {
            test.addImport("org.jboss.arquillian.test.api.ArquillianResource");
            final FieldSource<JavaClassSource> urlField = test.addField();
            urlField
                    .setName("url")
                    .setType(URL.class)
                    .setPrivate();

            urlField.addAnnotation("ArquillianResource");

        }
    }

    private void addTestMethod(JavaClassSource test) {

        test.addImport("org.junit.Test");

        MethodSource<JavaClassSource> testMethod = test.addMethod()
                .setPublic()
                .setReturnTypeVoid()
                .setName("should_start_service")
                .setBody("");
        testMethod.addAnnotation("Test");
    }

    private void addArquillianRunner(JavaClassSource test) {
        test.addImport("org.junit.runner.RunWith");
        test.addImport("org.jboss.arquillian.junit.Arquillian");

        test.addAnnotation("RunWith").setLiteralValue("Arquillian.class");
    }

    private void addDefaultDeploymentAnnotation(JavaClassSource test, Project project) throws ClassNotFoundException, IOException {
        test.addImport("org.wildfly.swarm.arquillian.DefaultDeployment");
        final AnnotationSource<JavaClassSource> defaultDeploymentAnnotation = test.addAnnotation("DefaultDeployment");
        if (asClient.hasValue()) {
            defaultDeploymentAnnotation.setLiteralValue("testable", "false");
        }

        if (archiveType.hasValue()) {
            defaultDeploymentAnnotation.setLiteralValue("type", String.format("DefaultDeployment.Type.%s", archiveType.getValue()));
        }
    }

    private void installArquillianWildflySwarmDependency(DependencyFacet dependencyFacet) {
        dependencyFacet.addDirectDependency(SWARM_ARQUILLIAN_DEPENDENCY);
    }

    private boolean isArquillianWildflySwarmDependencyInstalled(DependencyFacet dependencyFacet) {
        return dependencyFacet.getDependencies().stream()
                .map(dependency -> dependency.getCoordinate())
                .anyMatch(coordinate ->
                        "io.thorntail".equals(coordinate.getGroupId()) &&
                                "arquillian".equals(coordinate.getArtifactId()));
    }

}
