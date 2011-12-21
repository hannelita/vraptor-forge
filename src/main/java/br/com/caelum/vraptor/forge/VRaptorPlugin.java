package br.com.caelum.vraptor.forge;

import javax.inject.Inject;

import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Current;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.spec.javaee.PersistenceFacet;
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.PersistenceDescriptor;

public @Alias("vraptor")
class VRaptorPlugin implements org.jboss.forge.shell.plugins.Plugin {

	@Inject
	private ShellPrompt prompt;

	@Inject
	@Current
	private Resource<?> currentResource;

	@Inject
	private Project project;

	@Inject
	private XMLParser parser;

	@DefaultCommand
	public void defaultCommand(PipeOut out) {
		out.println("Welcome to VRaptor Forge Plugin");
	}

	@Command("setup")
	public void setup(PipeOut out) {
		DependencyFacet deps = project.getFacet(DependencyFacet.class);

		DependencyBuilder springAsm = DependencyBuilder
				.create("org.springframework:spring-asm:3.1.0.RC1");
		deps.addDependency(springAsm);

		// Add the Spring beans dependency
		DependencyBuilder springBeans = DependencyBuilder
				.create("org.springframework:spring-beans:3.1.0.RC1");
		deps.addDependency(springBeans);

		out.println("VRaptor dependencies to pom.xml.");

		ResourceFacet resources = project.getFacet(ResourceFacet.class);
		Node webapp = new Node("web-app");
		webapp.attribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		webapp.attribute("xmlns", "http://java.sun.com/xml/ns/javaee");
		webapp.attribute("xmlns:jsp", "http://java.sun.com/xml/ns/javaee/jsp");
		webapp.attribute("xmlns:web",
				"http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd");
		webapp.attribute(
				"xsi:schemaLocation",
				"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd");
		webapp.attribute("version", "2.5");

		
		MetadataFacet meta = project.getFacet(MetadataFacet.class);
		String projectName = meta.getProjectName();		
		Node displayName = new Node("display", webapp);
		displayName.text(projectName);
		Node contextParam = new Node("context-param", webapp);
		Node contextConfig = new Node("param-name", contextParam);
		contextConfig.text("br.com.caelum.vraptor.packages");
		Node configLocation = new Node("param-value", contextParam);
		configLocation.text("br.com.caelum.vraptor.util.jpa");

		
		Node contextParam2 = new Node("context-param", webapp);
		Node contextConfig2 = new Node("param-name", contextParam2);
		contextConfig2.text("br.com.caelum.vraptor.encoding");
		Node configLocation2 = new Node("param-value", contextParam2);
		configLocation2.text("UTF-8");
		
		Node filter = new Node("filter", webapp);
		Node filterName = new Node("filter-name", filter);
		filterName.text("sitemesh");
		Node filterClass = new Node("filter-class", filter);
		filterClass.text("com.opensymphony.sitemesh.webapp.SiteMeshFilter");
		
		Node filter2 = new Node("filter", webapp);
		Node filterName2 = new Node("filter-name", filter2);
		filterName2.text("vraptor");
		Node filterClass2 = new Node("filter-class", filter2);
		filterClass2.text("br.com.caelum.vraptor.VRaptor");
		
		
		String file = parser.toXMLString(webapp);
		resources.createResource(file.toCharArray(),
				"../webapp/WEB-INF/web.xml");
	}

	@Command("persistence")
	public void vraptorPersistence(PipeOut out) {
		ResourceFacet resources = project.getFacet(ResourceFacet.class);

		// Use a MetadataFacet object to retrieve the project's name.
		MetadataFacet meta = project.getFacet(MetadataFacet.class);
		String projectName = meta.getProjectName();

		/*
		 * Use a PersistenceFacet object to retrieve the project's persistence
		 * configuration. This persistence configuration can then be used to
		 * retrieve the appropriate persistence unit name (for a JNDI lookup).
		 */
		PersistenceFacet jpa = project.getFacet(PersistenceFacet.class);
		PersistenceDescriptor config = jpa.getConfig();
		String unitName = config.listUnits().get(0).getName();
		out.println(unitName);

		// Use the XMLParser provided by Forge to create an
		// applicationContext.xml file.

		// The top-level element of the XML file, <beans>, will contain schema
		// inclusions.
		Node beans = new Node("beans");
		beans.setComment(false);

		// Add each schema as a separate attribute of the <beans> element.
		beans.attribute("xmlns", "http://www.springframework.org/schema/beans");
		beans.attribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		beans.attribute("xmlns:tx", "http://www.springframework.org/schema/tx");
		beans.attribute("xmlns:jee",
				"http://www.springframework.org/schema/jee");

		// schemaLoc contains the locations of each schema file.
		String schemaLoc = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd";
		schemaLoc += " http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee.xsd";
		schemaLoc += " http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd";
		beans.attribute("xsi:schemaLocation", schemaLoc);

		// Perform a JNDI lookup to retrieve an EntityManagerFactory, of type
		// javax.persistence.EntityManagerFactory.
		Node emf = new Node("jee:jndi-lookup", beans);
		emf.setComment(false);
		emf.attribute("id", "entityManagerFactory");
		emf.attribute("jndi-name", "java:comp/env/persistence/" + unitName);
		emf.attribute("expected-type", "javax.persistence.EntityManager");

		/*
		 * Add the <tx:annotation-driven/> element for use of the @Transactional
		 * annotation. This is not necessary unless we choose to annotate
		 * controller methods as @Transactional.
		 */
		Node tx = new Node("tx:annotation-driven", beans);

		// Write the XML tree to a file, using the <beans> root node.
		String file = parser.toXMLString(beans);
		resources.createResource(file.toCharArray(),
				"META-INF/applicationContext.xml");

		// Create a web.xml file and define the persistence unit in web.xml.
		Node webapp = new Node("webapp");
		webapp.attribute("version", "3.0");
		webapp.attribute("xmlns", "http://java.sun.com/xml/ns/javaee");
		webapp.attribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		webapp.attribute(
				"xsi:schemaLocation",
				"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd");
		webapp.attribute("metadata-complete", "true");

		// Add the project name as an attribute of web.xml.
		Node displayName = new Node("display");
		displayName.text(projectName);

		// Include the files containing the web application's context.
		Node contextParam = new Node("context-param", displayName);
		Node contextConfig = new Node("param-name", contextParam);
		contextConfig.text("contextConfigLocation");
		Node configLocation = new Node("param-value", contextConfig);
		configLocation.text("classpath:/META-INF/applicationContext.xml");

		// Define a ContextLoaderListener.
		Node listener = new Node("listener", webapp);
		Node cll = new Node("listener-class", listener);
		cll.text("org.springframework.web.context.ContextLoaderListener");

		// Define a persistence unit to be referenced in the application
		// context.
		Node persistenceContextRef = new Node("persistence-context-ref", webapp);
		Node persistenceContextRefName = new Node(
				"persistence-context-ref-name", persistenceContextRef);
		persistenceContextRefName.text("persistence/" + unitName
				+ "/entityManager");
		Node persistenceUnitName = new Node("persistence-unit-name",
				persistenceContextRef);
		persistenceUnitName.text(unitName);

		file = parser.toXMLString(webapp);
		resources.createResource(file.toCharArray(),
				"../webapp/WEB-INF/web.xml");
	}

	@Command("web-mvc")
	public void setupMVC(
			PipeOut out,
			@Option(required = true, name = "package", description = "Package containing Spring controllers") final String mvcPackage) {
		// Use a ResourceFacet object to retrieve and update XML context files.
		ResourceFacet resources = this.project.getFacet(ResourceFacet.class);

		// Use a MetadataFacet object to retrieve the project's name.
		MetadataFacet meta = this.project.getFacet(MetadataFacet.class);
		String projectName = meta.getProjectName();

		/*
		 * Ensure that the META-INF/applicationContext.xml file exists. If it
		 * does not exist, tell the user that they may need to execute 'spring
		 * persistence'.
		 */
		if (!resources.getResource("../webapp/WEB-INF/web.xml").exists()) {
			out.println("The file 'WEB-INF/web.xml' does not exist.  Have you executed 'spring persistence' yet?");
			return;
		}

		// Create a new mvc-context.xml file for the application.
		Node beans = new Node("beans");

		// Add the appropriate Schema references.
		beans.attribute("xmlns", "http://www.springframework.org/schema/beans");
		beans.attribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		beans.attribute("xmlns:mvc",
				"http://www.springframework.org/schema/mvc");
		beans.attribute("xmlns:context",
				"http://www.springframework.org/schema/context");

		// Add the schema files for the <context> and <mvc> namespaces.
		String schemaLoc = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd";
		schemaLoc += " http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd";
		schemaLoc += " http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd";
		beans.attribute("xsi:schemaLocation", schemaLoc);

		// Scan the given package for any classes with MVC annotations.
		Node contextScan = new Node("context:component-scan", beans);
		contextScan.attribute("base-package", mvcPackage);

		// Indicate that we will use Spring MVC annotations, such as @Controller
		// or @RequestMapping.
		Node mvcAnnotation = new Node("mvc:annotation-driven", beans);

		// Use the Spring MVC default servlet handler.
		Node mvcServlet = new Node("mvc:default-servlet-handler", beans);

		// Unnecessary if there is no static content, but harmless.
		Node mvcStatic = new Node("mvc:resources", beans);
		mvcStatic.attribute("mapping", "/static/**");
		mvcStatic.attribute("location", "/");

		// Write the mvc-context.xml file.
		String file = parser.toXMLString(beans);
		String filename = projectName.toLowerCase().replace(' ', '-');
		resources.createResource(file.toCharArray(), "../webapp/WEB-INF/"
				+ filename + "-mvc-context.xml");

		// Retrieve the WEB-INF/web.xml file to be edited.

		FileResource<?> webXML = resources
				.getResource("../webapp/WEB-INF/web.xml");
		Node webapp = parser.parse(webXML.getResourceInputStream());

		// Define a Dispatcher servlet, named after the project.
		Node servlet = new Node("servlet", webapp);
		String servName = projectName.replace(' ', (char) 0);
		Node servletName = new Node("servlet-name", servlet);
		servletName.text(servName);
		Node servletClass = new Node("servlet-class", servlet);
		servletClass.text("org.springframework.web.servlet.DispatcherServlet");
		Node initParam = new Node("init-param", servlet);
		Node paramName = new Node("param-name", initParam);
		paramName.text("contextConfigLocation");
		Node paramValue = new Node("param-value", initParam);
		paramValue.text("/WEB-INF/" + filename + ".xml");
		Node loadOnStartup = new Node("load-on-startup", servlet);
		loadOnStartup.text(1);

		Node servletMapping = new Node("servlet-mapping", webapp);
		Node servletNameRepeat = new Node("servlet-name", servletMapping);
		servletNameRepeat.text(projectName.replace(' ', (char) 0));
		Node url = new Node("url-pattern", servletMapping);
		url.text('/');

		file = parser.toXMLString(webapp);
		resources.createResource(file.toCharArray(),
				"../webapp/WEB-INF/web.xml");
	}

}