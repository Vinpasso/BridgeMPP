package bridgempp.services.facebook;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import bridgempp.ShadowManager;

import com.restfb.Connection;
import com.restfb.types.Post;

public class FacebookPollService implements Runnable {

	private static final long POLL_FREQUENCY = 900000;
	private Hashtable<String, Integer> connections;
	private FacebookService service;

	public FacebookPollService(FacebookService service)
	{
		connections = new Hashtable<>();
		this.service = service;
	}
	
	public void addConnection(String connection)
	{
		connections.put(connection, -1);
	}

	public void run()
	{
		try
		{
			while(true)
			{
				Enumeration<String> connectionKeys = connections.keys();
				while(connectionKeys.hasMoreElements())
				{
					String nextElement = connectionKeys.nextElement();
					Connection<Post> connection = service.getFacebook().fetchConnection(nextElement, Post.class);
					List<Post> posts = connection.getData();
					int lastSeen = connections.get(nextElement);
					int lastUnreadPost = -1;
					while(posts.get(lastUnreadPost + 1).hashCode() != lastSeen)
					{
						lastUnreadPost++;
						if(lastUnreadPost >= posts.size() - 1)
						{
							break;
						}
					}
					for(int i = lastUnreadPost; i >= 0; i--)
					{
						service.processPost(nextElement, posts.get(i));
						connections.remove(nextElement);
						connections.put(nextElement, posts.get(i).hashCode());
					}
				}
				Thread.sleep(POLL_FREQUENCY);
			}
		} catch(InterruptedException e)
		{
			ShadowManager.log(Level.SEVERE, "Facebook Poll interrupted! Shutting down");
		}
	}

}
