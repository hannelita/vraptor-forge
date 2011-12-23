package br.com.caelum.vraptor.forge.config.xml;

import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.MetadataFacet;

public class WebApp {
	private final Node webapp;
	private Project project;

	public WebApp(ServletVersion version, Project project) {
		this.project = project;
		this.webapp = new Node("web-app");

		createRootNamespaces(version);
		createDisplayNameTag();
	}
	
	public Node get() {
		return this.webapp;
	}

	public WebApp addContextParam(String paraName, String paramValue) {
		Node contextParam = new Node("context-param", webapp);
		
		new Node("param-name", contextParam).text(paraName);
		new Node("param-value", contextParam).text(paramValue);
		return this;
	}

	public WebApp addFilter(String filterName, String filterClass) {
		Node filter = new Node("filter", webapp);
		new Node("filter-name", filter).text(filterName);
		new Node("filter-class", filter).text(filterClass);
		
		return this;
	}
	
	public WebApp addFilter(String filterClass) {
		return addFilter(filterClass.replaceAll("\\.", "_"), filterClass);
	}
	
	private void createDisplayNameTag() {
		Node displayName = new Node("display", webapp);
		MetadataFacet meta = project.getFacet(MetadataFacet.class);
		displayName.text(meta.getProjectName());
	}

	private void createRootNamespaces(ServletVersion version) {
		webapp.attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		webapp.attribute("xmlns", "http://java.sun.com/xml/ns/javaee");
		webapp.attribute("xmlns:jsp", "http://java.sun.com/xml/ns/javaee/jsp");
		webapp.attribute("xmlns:web", "http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd");
		webapp.attribute("xsi:schemaLocation", version.getSchemaLocation());
		webapp.attribute("version", version.getVersion());
	}
}
