/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.impl.vsphere;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.util.StringUtils;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

/**
 * Wrapper over the vSphere API
 * 
 * @author Yuval Kohavi <yuval.kohavi@intigua.com>
 * 
 */
public class VSphereClient implements Closeable {

	public static final String TYPE_SERVICE_INSTANCE = "ServiceInstance";
	private static Logger log = LoggerFactory.getLogger(VSphereClient.class);

	private final String server;
	private final String user;
	private final String password;

	private ServiceContent serviceContent;
	private VimPortType vimPort;

	private boolean connected = false;

	private Integer port;

	public String getServer() {
		return server;
	}
	
	public boolean isConnected() {
		return connected;
	}

	public VSphereClient(String server, String user, String password) {
		this.server = server;
		this.user = user;
		this.password = password;
	}

	protected String getVSphereServiceUrl() {
		if (StringUtils.isNullOrEmpty((server))) {
			log.warn("Can't construct vim service url, vSphere host name is empty");
			throw new IllegalArgumentException();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("https://");
		builder.append(server);
		if (port != null) {
			builder.append(":");
			builder.append(port);
		}
		builder.append("/sdk/vimService");
		return builder.toString();
	}

	/**
	 * Establishes session with the vSphere server.
	 * 
	 * @return true if connected successfully
	 * @throws RuntimeFaultFaultMsg
	 * @throws InvalidLoginFaultMsg
	 * @throws InvalidLocaleFaultMsg
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void connect() throws RuntimeFaultFaultMsg,
			InvalidLocaleFaultMsg, InvalidLoginFaultMsg {
		String connectionUrl = getVSphereServiceUrl();

		doTrust();

		ManagedObjectReference serviceInstanceMOR = getServiceInstance();

		VimService vimService = new VimService();

		log.trace("Getting vimPort from vimService");
		vimPort = vimService.getVimPort();
		log.trace("vimPort is gotT successfully");
		log.trace("Getting context from vimPort");
		Map<String, Object> requestContext = ((BindingProvider) vimPort)
				.getRequestContext();
		log.trace("Context from vimPort is got successfully");

		log.trace("URL to connect to vSphere host '{}'", connectionUrl);
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				connectionUrl);
		requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		// do trust ? TODO
/*		requestContext.put(JAXWSProperties.SSL_SOCKET_FACTORY, getSSLContext()
				.getSocketFactory());
		HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		};
		requestContext.put(JAXWSProperties.HOSTNAME_VERIFIER, hv);
*/
		log.trace("Retrieving service content");

		serviceContent = vimPort.retrieveServiceContent(serviceInstanceMOR);

		log.trace("Service content retrieved successfully");
		log.trace("Logging in to vSphere host '{}'", server);
		UserSession userSession = vimPort.login(serviceContent.getSessionManager(), user,
				password, null);
		log.trace("Logged in successfully to vSphere host '{}'", server);
		connected = true;
	}

	public ManagedObjectReference getServiceInstance() {
		ManagedObjectReference serviceInstanceMOR = new ManagedObjectReference();
		serviceInstanceMOR.setType(TYPE_SERVICE_INSTANCE);
		serviceInstanceMOR.setValue(TYPE_SERVICE_INSTANCE);
		return serviceInstanceMOR;
	}

	private void doTrust() {
		HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		};
		try {
			trustAllHttpsCertificates();
		} catch (KeyManagementException e) {

			throw new IllegalStateException("SSL init problems", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SSL init problems", e);
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	@Override
	public void close() throws IOException {
		try {
			disconnect();
		} catch (RuntimeFaultFaultMsg e) {
			throw new IOException(e);
		}
	}

	/**
	 * Disconnects the user session.
	 * 
	 * @throws RuntimeFaultFaultMsg
	 * 
	 * @throws Exception
	 */
	public void disconnect() throws RuntimeFaultFaultMsg {
		if (connected) {
			log.trace("Logging out from vSphere host '{}'", server);
			vimPort.logout(serviceContent.getSessionManager());
			log.trace("Logged out successfully from vSphere host '{}'", server);
		}
		connected = false;
	}

	public ManagedObjectReference findVmByUuid(String uuid, boolean instanceUuid)
			throws RuntimeFaultFaultMsg {
		return vimPort.findByUuid(this.serviceContent.getSearchIndex(), null,
				uuid, true, instanceUuid);
	}
	
	public ManagedObjectReference findVmByIp(String ip)
			throws RuntimeFaultFaultMsg {
		return vimPort.findByIp(this.serviceContent.getSearchIndex(), null,
				ip, true);
	}

	/* taken from vmware samples */
	public Object[] getProperties(ManagedObjectReference moRef,
			String... properties) throws RemoteException,
			InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		// PropertySpec specifies what properties to
		// retrieve and from type of Managed Object
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType(moRef.getType());
		pSpec.getPathSet().addAll(Arrays.asList(properties));

		// ObjectSpec specifies the starting object and
		// any TraversalSpecs used to specify other objects
		// for consideration
		ObjectSpec oSpec = new ObjectSpec();
		oSpec.setObj(moRef);

		// PropertyFilterSpec is used to hold the ObjectSpec and
		// PropertySpec for the call
		PropertyFilterSpec pfSpec = new PropertyFilterSpec();
		pfSpec.getPropSet().addAll(Arrays.asList(new PropertySpec[] { pSpec }));
		pfSpec.getObjectSet().addAll(Arrays.asList(new ObjectSpec[] { oSpec }));

		// retrieveProperties() returns the properties
		// selected from the PropertyFilterSpec
		List<ObjectContent> ocs = vimPort.retrieveProperties(
				serviceContent.getPropertyCollector(),
				Arrays.asList(new PropertyFilterSpec[] { pfSpec }));

		// Return value, one object for each property specified
		Object[] ret = new Object[properties.length];

		if (ocs != null) {
			for (ObjectContent oc : ocs) {

				List<DynamicProperty> dps = oc.getPropSet();
				if (dps != null) {
					for (DynamicProperty dp : dps) {
						// find property path index
						for (int p = 0; p < ret.length; ++p) {
							if (properties[p].equals(dp.getName())) {
								ret[p] = dp.getVal();
							}
						}
					}
				}
			}
		}
		return ret;
	}

	private void trustAllHttpsCertificates() throws NoSuchAlgorithmException,
			KeyManagementException {
		javax.net.ssl.SSLContext sc = getSSLContext();
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}

	private javax.net.ssl.SSLContext getSSLContext() {
		// Create a trust manager that does not validate certificate chains:
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
		trustAllCerts[0] = tm;

		try {

			javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
					.getInstance("SSL");
			javax.net.ssl.SSLSessionContext sslsc = sc
					.getServerSessionContext();
			sslsc.setSessionTimeout(0);
			sc.init(null, trustAllCerts, null);
			return sc;
		} catch (KeyManagementException e) {

			throw new IllegalStateException("SSL init problems", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SSL init problems", e);
		}
	}

	private class TrustAllTrustManager implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {

		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		@Override
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

	public VimPortType getVimPort() {
		return this.vimPort;
	}

	public ServiceContent getServiceContent() {

		return this.serviceContent;
	}

}
