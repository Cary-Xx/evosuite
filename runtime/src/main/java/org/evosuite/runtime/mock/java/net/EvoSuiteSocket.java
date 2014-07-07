package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.runtime.vnet.NativeTcp;
import org.evosuite.runtime.vnet.VirtualNetwork;

/*
 * An actual implementation is 
 * 
 * SocksSocketImpl -> PlainSocketImpl -> AbstractPlainSocketImpl -> SocketImpl
 */

public class EvoSuiteSocket extends MockSocketImpl{

	private final Map<Integer,Object> options;
	
	private NativeTcp openedConnection;
	
	private boolean isClosed;
	
	public EvoSuiteSocket(){
		options = new ConcurrentHashMap<>();
		initOptions();
		isClosed = false;
	}
	
	private void initOptions(){
		//options.put(SocketOptions, );
		//TODO
	}
	
	@Override
	public void setOption(int optID, Object value) throws SocketException {
		options.put(optID, value);
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		return options.get(optID);
	}

	@Override
	protected void create(boolean stream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connect(String host, int port) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connect(SocketAddress address, int timeout)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void bind(InetAddress host, int port) throws IOException {		
		//TODO: need to check special cases like multicast and 0.0.0.0
		boolean opened = VirtualNetwork.getInstance().openTcpServer(host.getHostAddress(), port);
		if(!opened){
			throw new IOException("Failed to opened TCP port");
		}
	}

	@Override
	protected void listen(int backlog) throws IOException {
		// TODO 		
	}

	@Override
	protected void accept(SocketImpl s) throws IOException {
		
		if(! (s instanceof MockSocketImpl)){
			throw new IOException("Can only hanlded mocked socketsa");
		}
		
		MockSocketImpl mock = (MockSocketImpl) s;
		
		/*
		 * If the test case has set up an incoming connection, then
		 * simulate an immediate connection.
		 * If not, there is no point in blocking the SUT for
		 * a connection that will never arrive: just throw an exception
		 */
		
		String localAddress = serverSocket.getInetAddress().getHostAddress();
		int localPort = serverSocket.getLocalPort();
		
		NativeTcp tcp = VirtualNetwork.getInstance().pullTcpConnection(
				localAddress,localPort);
		if(tcp == null){
			throw new IOException("Simulated exception on waiting server");
		} else {
			openedConnection = tcp;
			mock.setLocalPort(localPort);
			mock.setRemoteAddress(InetAddress.getByName(tcp.getRemoteEndPoint().getHost()));
			mock.setRemotePort(tcp.getRemoteEndPoint().getPort());
		}
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int available() throws IOException {
		checkIfClosed();
		if(openedConnection==null){
			return 0;
		}
		return openedConnection.getAmountOfDataInLocalBuffer();
	}

	@Override
	protected void close() throws IOException {
		isClosed = true;
	}

	@Override
	protected void sendUrgentData(int data) throws IOException {
		// TODO this is off by default, but can be activated with the option SO_OOBINLINE
		checkIfClosed();
	}

	private void checkIfClosed() throws IOException{
		if(isClosed){
			throw new IOException("Connection is closed");
		}
	}
}