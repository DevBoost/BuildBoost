package de.devboost.buildboost.buildext.toolproduct.localdns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.spi.nameservice.NameService;

/**
 * A {@link LocalNameService} can be used to resolve all DNS requests to one
 * particular host. The address of this host can be set in the field
 * {@link #HOST}.
 */
public class LocalNameService implements NameService {
	
	/**
	 * The IP address of the host to which all DNS requests shall be resolved.
	 */
	public static byte[] HOST;

	/**
	 * Returns {@link #HOST} regardless of the name.
	 */
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

	/**
	 * Returns null as reverse look up is not supported.
	 */
	@Override
	public String getHostByAddr(byte[] bs) {
		return null;
	}
}
