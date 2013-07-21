package de.devboost.buildboost.buildext.toolproduct.localdns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.spi.nameservice.NameService;

public class LocalNameService implements NameService {

	@Override
	public InetAddress[] lookupAllHostAddr(String name) {
		byte[] array = new byte[] {127, 0, 0, 1};
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByAddress(array);
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
