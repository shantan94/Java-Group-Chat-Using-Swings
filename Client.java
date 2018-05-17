import javax.swing.*;
import javax.swing.text.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
class Client
{
	static JTextArea area;
	static Socket s;
	static JTextField tf;
	static boolean init=false;
	static String username;
	static BufferedWriter bw=null;
	static BufferedReader br=null;
	static JFrame frame;
	public static void main(String[] args) throws Exception
	{
		/* Create a frame with a chat messages viewer, a textfield to type in the messages, a send button to send the messages
		and a loutout button to log the user out. Provide a scroll for the chat message viewer to scroll on incoming messages.*/
		s=new Socket();
		frame=new JFrame();
		JPanel main=new JPanel(new GridLayout(3,2));
		JPanel panelchat=new JPanel();
		area=new JTextArea(30,30);
		area.setEditable(false);
		JScrollPane scroll=new JScrollPane(area);
		DefaultCaret crList=(DefaultCaret)area.getCaret();
		crList.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JPanel panel=new JPanel();
		tf=new JTextField("",25);
		tf.add(scroll);
		JButton button=new JButton("Send");
		JButton logout=new JButton("Log Out");
		panelchat.add(scroll);
		panel.add(tf);
		panel.add(button);
		main.add(panelchat);
		main.add(panel);
		main.add(logout);
		frame.add(main);
		frame.setSize(400,400);
		frame.setVisible(true);
		area.append("Enter a username:\n");

		/* Create a socket connecting the server on port 8000 with a timeout of 10 seconds and create an reader and writer
		for the socket. */
		s.connect(new InetSocketAddress("localhost",8000),10000);
		br=new BufferedReader(new InputStreamReader(s.getInputStream()));
		bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

		/* Create a thread to keep listening to the server to check if any new message has been sent by other clients.
		At the start of the communication the client is asked to enter username and if the format of the username is
		incorrect the a variable init will be set to false which will be used later in the send button click listener 
		function in order to proceed and allow the user to chat or make the user re-enter a username and re-check it. */
		new Thread(new Runnable() {
			public void run() {
				try{
					String msg="";
					while((msg=br.readLine())!=null) {
						if(msg.equals("Username maybe existing or in invalid format Reenter"))
							init=false;
						else if(msg.equals(username+" has entered chat room!")){
							init=true;
							area.append("Hello you have entered the chat room with username: "+username+"\n");
						}
						System.out.println(msg);
						area.append(msg+"\n");
					}
				}
				catch(Exception ex){}
			}
		}).start();

		/* On click of the send button at the start of the chatting process the user is allowed to register a username
		with the server and depending on the above thread which runs in the background will make the user re-enter the
		username if it entered invalide and if valid username is entered init is set to true the user is allowed to chat. 
		Each message from then on will be sent to the sever which sends it to the rest of the clients. */
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				try{
					if(!init){
						username=tf.getText().toString();
						// bw=new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);
						SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date=new Date();
						bw.write("POST / HTTP/1.1"+"\r\n");
						bw.write("Host: "+s.getLocalAddress().getHostName()+"\r\n");
						bw.write("User-Agent: localhost"+"\r\n");
						bw.write("Content-Type: plain/text"+"\r\n");
						bw.write("Content-Length: "+username.length()+"\r\n");
						bw.write("Date: "+format.format(date)+"\r\n");
						bw.write(username+" initial"+"\r\n");
						bw.flush();
					}
					else{
						String chatmsg=tf.getText().toString();
						// bw=new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);
						SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date=new Date();
						bw.write("POST / HTTP/1.1"+"\r\n");
						bw.write("Host: "+s.getLocalAddress().getHostName()+"\r\n");
						bw.write("User-Agent: localhost"+"\r\n");
						bw.write("Content-Type: plain/text"+"\r\n");
						bw.write("Content-Length: "+chatmsg.length()+"\r\n");
						bw.write("Date: "+format.format(date)+"\r\n");
						bw.write(username+" "+chatmsg+"\r\n");
						bw.flush();
					}
				}
				catch(Exception ex){
					
				}
			}
		});

		/* On click logout button the user is logged out, the server is sent a message and the server logs out client. Along with
		the server loggin out the frame is disposed and socket is closed. */
		logout.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				try{
					SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String senddata="Logout:"+username+" has logged out"+"\r\n";
					Date date=new Date();
					bw.write("POST / HTTP/1.1"+"\r\n");
					bw.write("Host: "+s.getLocalAddress().getHostName()+"\r\n");
					bw.write("User-Agent: localhost"+"\r\n");
					bw.write("Content-Type: plain/text"+"\r\n");
					bw.write("Content-Length: "+senddata.length()+"\r\n");
					bw.write("Date: "+format.format(date)+"\r\n");
					bw.write(senddata);
					bw.flush();
					bw.close();
					s.close();
					frame.dispose();
				}
				catch(Exception ex){
					
				}
			}
		});
	}
}