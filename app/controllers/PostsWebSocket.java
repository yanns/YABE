package controllers;

import models.Post;
import models.StatefulModel;
import play.Logger;
import play.mvc.WebSocketController;

public class PostsWebSocket extends WebSocketController {
 
    public static void listen() {
        // send the initial count of posts
        outbound.send("" + Post.count());
        
        while (inbound.isOpen()) {
            String event = await(StatefulModel.instance.event.nextEvent());
            // send the updated count of posts
            outbound.send(event);
        }
    }

}
