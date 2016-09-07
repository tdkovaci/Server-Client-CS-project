import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;


public class ServerTest {

	@Test
	public void isPortTestf() {
		String port ="0000";
		boolean t = false;
		boolean isP = SafeWalkServer.isPortValid(port);
		assertEquals(isP,t);
	}
	
	@Test
	public void isPortTestf2() {
		String port ="70000";
		boolean t = false;
		boolean isP = SafeWalkServer.isPortValid(port);
		assertEquals(isP,t);
	}
	
	@Test
	public void isPortTestt() {
		String port ="8888";
		boolean t = true;
		boolean isP = SafeWalkServer.isPortValid(port);
		assertEquals(isP,t);
	}
	@Test
	public void NumExcep() {
		String port ="00a3";
		boolean t = false;
		boolean isP = SafeWalkServer.isPortValid(port);
		assertEquals(isP,t);
	}
	@Test
	public void conPortTest() {
		try {
			SafeWalkServer ss = new SafeWalkServer(9000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO still okay!
	}
	
	@Test(timeout = 2000)
	public void connectionTest() {
		System.out.println("Running connection: ");
		SafeWalkServer ss = null;
		try {
			ss = new SafeWalkServer(1500);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Thread t = new Thread(ss);
		t.start();
		
		Client c = new Client("localhost",1500,"Tom Riddle,LWSN,PUSH");
		
		Thread t2 = new Thread(c);
		t2.start();
		
		Client c2 = new Client("localhost",1500,":RESET");
		
		Thread t3 = new Thread(c2);
		t3.start();
		
		Client c3 = new Client("localhost",1500,":SHUTDOWN");
		
		Thread t4 = new Thread(c3);
		t4.start();
		
			try {
				t.join();
			} catch (InterruptedException e) {
				
			}
		
			System.out.println("Client 1: " + c.getResult());
			System.out.println("Client 2: " + c2.getResult());
			System.out.println("Client 3: " + c3.getResult());
			
}
	@Test
	public void conPortTest1() {
		try {
			SafeWalkServer ss = new SafeWalkServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO still okay!
	}
}
