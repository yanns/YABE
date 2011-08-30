import java.util.List;

import org.apache.commons.mail.SimpleEmail;

import models.Post;
import models.User;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Mail;


@Every("cron.emailReminder.period")
public class EmailReminder extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Email reminder job");
        List<Post> postsFromInactivUsers = Post.findPostFromInactivUsers();
        for (Post post : postsFromInactivUsers) {
            User inactivAuthor = post.author;
            SimpleEmail email = new SimpleEmail();
            email.setFrom("sender@yabe.com");
            email.addTo(inactivAuthor.email);
            email.setSubject("Lost inspiration?");
            String url = Play.configuration.getProperty("application.baseUrl") + "posts/" + post.id;
            
            String emailMsg = "Are you too much busy reading xkcd.com?\n" +
                    "I remind you your last post:\n" +
                    url;
            email.setMsg(emailMsg);
            Logger.info("Sending email to " + email.getFromAddress() + ":\n" + emailMsg);
            Mail.send(email); 
        }
    }

}
