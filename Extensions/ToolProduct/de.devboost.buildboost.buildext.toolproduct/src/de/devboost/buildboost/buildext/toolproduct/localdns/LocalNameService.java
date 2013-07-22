package de.devboost.buildboost.buildext.toolproduct.localdns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.spi.nameservice.NameService;

public class LocalNameService implements NameService {
	
	public static byte[] HOST;

	@Override
	public InetAddress[] lookupAllHostAddr(String name) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByAddress(HOST);
			return new InetAddress[] {inetAddress};
		} catch (UnknownHostException e) {
			return null;
		}
	}

	@Override
	public String getHostByAddr(byte[] bs) {
		return null;
	}
}
