import java.net.*;
import java.io.*;
import java.util.*;
class Server
{
	/* Create a multi threaded server to keep accepting the requests from client and each client process runs of different thread.
	For each client a timer will be instatiated along with a bufferedreader so that messages can be sent throught it. */
	public static void main(String[] args)
	{
		try
		{
        int port=8000;
		ServerSocket ss=new ServerSocket(port);
		System.out.println("Server listening on port "+port);
		List<BufferedWriter> bws=new ArrayList<BufferedWriter>();
		List<Long> timers=new ArrayList<Long>();
		List<String> usernames=new ArrayList<String>();
		while(true){
			Socket s=ss.accept();
			// BufferedWriter dos=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			// BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
			// System.out.println(br.toString());
			// bws.add(dos);
			// timers.add((long)0);
			// brs.add(br);
			HttpRequest hr=new HttpRequest(s,bws,timers,usernames);
			Thread t=new Thread(hr);
			t.start();
			}
		}
		catch(Exception e)
		{
			System.out.println("Got an error"+e.getMessage());
		}
	}
}
