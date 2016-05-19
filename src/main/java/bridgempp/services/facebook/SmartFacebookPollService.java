package bridgempp.services.facebook;

import java.util.Collection;
import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;

import bridgempp.data.User;

import bridgempp.messageformat.MessageFormat;

import com.restfb.Connection;
import com.restfb.Parameter;
import com.restfb.types.Post;

public class SmartFacebookPollService implements Runnable
{

	private FacebookService service;

	public SmartFacebookPollService(FacebookService facebookService)
	{
		this.service = facebookService;
	}

	@Override
	public void run()
	{
		// Check that BridgeMPP is ready to do stuff
		BridgeMPP.readLock();
		BridgeMPP.readUnlock();
		Parameter checkSince = Parameter.with("since", service.getLastUpdate());
		Collection<Endpoint> endpoints = service.getEndpoints();

		endpoints.forEach(e -> {
			Connection<Post> connection = service.getFacebook().fetchConnection(e.getIdentifier(), Post.class, checkSince);
			connection.getData().forEach(post -> {
				if (post.getMessage() != null)
				{
					User user = DataManager.getOrNewUserForIdentifier(post.getFrom().getId() + "@facebook.com", e);
					if (!user.hasAlias())
					{
						user.setName(post.getFrom().getName());
					}
					service.receiveMessage(new Message(user, e, post.getMessage(), MessageFormat.PLAIN_TEXT));
				} else
				{
					ShadowManager.log(Level.WARNING, "Encountered unknown Facebook Type: " + post.getType());
				}
			});
		});
		service.setLastUpdate();
	}

}
