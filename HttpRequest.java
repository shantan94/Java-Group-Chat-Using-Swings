import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
class HttpRequest extends Thread implements Runnable
{
	static Socket s;
	static List<BufferedWriter> bws=new ArrayList<BufferedWriter>();
	static List<Long> timers=new ArrayList<Long>();
	static List<String> usernames=new ArrayList<String>();
	/* Set the bufferedWriters, usernames and timers for each client that is connected to the server. */
	HttpRequest(Socket s,List<BufferedWriter> bws,List<Long> timers,List<String> usernames)
	{
		this.s=s;
		this.bws=bws;
		this.timers=timers;
		this.usernames=usernames;
	}
	/* On request from the user processRequest() function will be called. */
	public void run()
	{
		try
		{
			processRequest();
		}
		catch(Exception e){}
	}
	private void processRequest() throws Exception
	{
		/* Create a reader and writer for each client process. */
		BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		/* Iterate in an infinite loop to fullfil each request from the user. */
		while(true){
			// System.out.println(this.usernames);
			/* Get the messages from the stream of data that is sent from the user. */
			String stream="";
			for(int i=0;i<7;i++){
				if(i!=6)
					System.out.println(br.readLine());
				else
					stream=br.readLine();
			}
			System.out.println();
			/* If the user has sent his username the code checks if the username is valid and sends a message. If username is not valid
			the client is requested for reentry of the username. If the username is valid the user has now entered the chat room and can
			send messages in it. Each message has a HTTP format which is displayed by the server. */
			if(stream.split(" ")[1].equals("initial")){
				if(stream.split(" ")[0].equals("")||this.usernames.indexOf(stream.split(" ")[0])!=-1){
					bw.write("Username maybe existing or in invalid format. Please Reenter."+"\r\n");
					bw.flush();
				}
				else{
					this.bws.add(bw);
					this.usernames.add(stream.split(" ")[0]);
					this.timers.add((long)(0));
					for(int i=0;i<this.timers.size();i++) {
						try{
							BufferedWriter bwt=(BufferedWriter)(this.bws.get(i));
							String senddata=stream.split(" ")[0]+" has entered chat room!"+"\r\n";
							SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date=new Date();
							System.out.println("GET / HTTP/1.1");
							System.out.println("Host: "+s.getLocalAddress().getHostName());
							System.out.println("User-Agent: localhost");
							System.out.println("Content-Type: plain/text");
							System.out.println("Content-Length: "+senddata.length());
							System.out.println("Date: "+format.format(date));
							System.out.println();
							bwt.write(senddata);
							bwt.flush();
							this.timers.set(i,System.currentTimeMillis());
						}
						catch(Exception e){}
					}
				}
			}
			/* If the user clicks logout button on the page then his username and bufferedreader will be removed from the list so that
			no more messages will be sent to him thereafter. */
			else if(stream.split(":")[0].equals("Logout")){
				int position=this.usernames.indexOf(stream.split(":")[1].split(" ")[0]);
				this.usernames.remove(position);
				this.bws.remove(position);
				this.timers.remove(position);
				for(int i=0;i<this.timers.size();i++){
					BufferedWriter bwt=(BufferedWriter)(this.bws.get(i));
					bwt.write(stream.split(":")[1]+"\r\n");
					bwt.flush();
				}
			}
			/* If the user sends a message the timer and current time are check and the timer is refreshed. The time is sent based on
			the user time when he first entered and the last time that he has sent the message. */
			else{
				String total="";
				for(int i=0;i<this.timers.size();i++) {
					try{
						if(stream.split(" ")[0].equals(this.usernames.get(i))){
							long end=System.currentTimeMillis();
							long current=(end-this.timers.get(i))/1000;
							int mins=0,secs=0;
							while(current>=60){
								mins+=1;
								current-=60;
							}
							secs=(int)current;
							if(secs>=10)
								total=String.valueOf(mins)+":"+secs;
							else
								total=String.valueOf(mins)+":0"+secs;
							this.timers.set(i,System.currentTimeMillis());
						}
					}
					catch(Exception e){}
				}
				/* Send the message to the client by getting the time difference bwtween the message sent before and the message sent now. */
				for(int i=0;i<this.timers.size();i++){
					try{
						BufferedWriter bwt=(BufferedWriter)(this.bws.get(i));
						String senddata="("+total+") "+stream.split(" ")[0]+" : "+stream.substring(stream.split(" ")[0].length())+"\r\n";
						SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date=new Date();
						System.out.println("GET / HTTP/1.1");
						System.out.println("Host: "+s.getLocalAddress().getHostName());
						System.out.println("User-Agent: localhost");
						System.out.println("Content-Type: plain/text");
						System.out.println("Content-Length: "+senddata.length());
						System.out.println("Date: "+format.format(date));
						System.out.println();
						bwt.write(senddata);
						bwt.flush();
					}
					catch(Exception ex){}
				}
			}
		}
	}
}