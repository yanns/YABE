package controllers;

import play.*;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.F;
import play.libs.Images;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.*;

import java.util.*;

import org.w3c.dom.Document;

import models.*;

public class Application extends Controller {

    @Before
    static void addDefaults() {
        renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
        renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
    }

    public static void index() {
        Post frontPost = Post.find("order by postedAt desc").first();
        List<Post> olderPosts = Post.find(
            "order by postedAt desc"
        ).from(1).fetch(10);
        render(frontPost, olderPosts);
    }
    
    public static void country() {
        String IP = request.remoteAddress;
        if (Play.mode.isDev()) {
            IP = "87.193.216.74";
        }
        Logger.info("resolve country for IP: %s", IP);
        String key = "IP" + IP;
        String countryName = (String)Cache.get(key);
        if (countryName == null) {
            Logger.info("...using web service");
            F.Promise<WS.HttpResponse> remoteCall = WS.url("http://www.webservicex.net/geoipservice.asmx/GetGeoIP?IPAddress=" + IP).getAsync();

            HttpResponse post = await(remoteCall);
            if (!post.success()) {
                error();
            }
            Document doc = post.getXml();
            countryName = doc.getElementsByTagName("CountryName").item(0).getTextContent();
            Cache.add(key, countryName, "10mn");
        }
        Logger.info("country for IP: %s is %s", IP, countryName);
        renderJSON("{\"countryName\": \"" + countryName + "\"}");
    }
    
    public static void show(Long id) {
        Post post = Post.findById(id);
        String randomID = Codec.UUID();
        render(post, randomID);
    }
    
    public static void postComment(Long postId, 
    		                       @Required(message="Author is required") String author, 
    		                       @Required(message="A message is required") String content,
    		                       @Required(message="Please type the code") String code,
    		                       String randomID) {
        
        Post post = Post.findById(postId);
        if (!Play.id.equals("test")) {
            validation.equals(
                code, Cache.get(randomID)
            ).message("Invalid code. Please type it again");
        }
        
        if (validation.hasErrors()) {
            render("Application/show.html", post, randomID);
        }
        post.addComment(author, content);
        flash.success("Thanks for posting %s", author);
        Cache.delete(randomID);
        show(postId);
    }
    
    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#E4EAFD");
        Cache.set(id, code, "10mn");
        renderBinary(captcha);
    }
    
    public static void listTagged(String tag) {
        List<Post> posts = Post.findTaggedWith(tag);
        render(tag, posts);
    }
    
    public static void listTags(String tag) {
        notFoundIfNull(tag);
        List<Tag> tags = Tag.find("byNameLike", "%" + tag + "%").fetch();
        renderJSON(tags);
    }
}