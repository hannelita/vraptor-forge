package br.com.caelum.vraptor.forge.config.xml;

public enum ServletVersion {
	VERSION2_5("2.5"){
		@Override
		String getVersionSanitized() {
			return "2_5";
		}
	}, VERSION_3_0("3.0"){
		@Override
		String getVersionSanitized() {
			return "3_0";
		}
	};

	private String version;
	
	private ServletVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}

	abstract String getVersionSanitized();
	
	public String getSchemaLocation() {
		return "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_"+getVersionSanitized()+".xsd";
	}
}
