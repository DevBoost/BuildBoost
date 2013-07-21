package de.devboost.buildboost.buildext.toolproduct.localdns;

import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

public class LocalNameServiceDescriptor implements NameServiceDescriptor {

	private static LocalNameService localNameService = new LocalNameService();

	public NameService createNameService() throws Exception {
		return localNameService;
	}

	public String getType() {
		System.out.println("LocalNameServiceDescriptor.getType()");
		return "dns";
	}

	public String getProviderName() {
		System.out.println("LocalNameServiceDescriptor.getProviderName()");
		return "localdns";
	}
}
