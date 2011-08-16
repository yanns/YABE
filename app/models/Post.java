package models;
 
import java.util.*;

import javax.persistence.*;

import org.joda.time.DateTime;
 
import play.data.validation.*;
import play.db.jpa.*;
 
@Entity
public class Post extends Model {
 
    @Required
    public String title;

    @Required
    public Date postedAt;
    
    @Lob
    @Required
    @MaxSize(10000)
    public String content;
    
    @ManyToOne
    @Required
    public User author;
    
    @OneToMany(mappedBy="post", cascade=CascadeType.ALL)
    public List<Comment> comments;
    
    @ManyToMany(cascade=CascadeType.PERSIST)
    public Set<Tag> tags;
    
    public Post(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.postedAt = new Date();
    	this.comments = new ArrayList<Comment>();
    	this.tags = new TreeSet<Tag>();
    }
    
    @PostPersist void onPostPersist() {
        StatefulModel.instance.event.publish("" + count());
    }
    
    @PostRemove void onPostRemove() {
        StatefulModel.instance.event.publish("" + (count() - 1));
    }
    
    public Post addComment(String author, String content) {
        Comment newComment = new Comment(this, author, content).save();
        this.comments.add(newComment);
        this.save();
        return this;
    }
    
    public Post previous() {
        return Post.find("postedAt < ? order by postedAt desc", postedAt).first();
    }
     
    public Post next() {
        return Post.find("postedAt > ? order by postedAt asc", postedAt).first();
    }
    
    public static Post findByIdAndEmail(Object id, String authorEmail) {
        if (authorEmail == null) return null;
        return Post.find("byIdAndAuthor.email", id, authorEmail).first();
    }
    
    public Post tagItWith(String name) {
        tags.add(Tag.findOrCreateByName(name));
        return this;
    }
    
    public static List<Post> findTaggedWith(String tag) {
        return Post.find(
            "select distinct p from Post p join p.tags as t where t.name = ?", tag
        ).fetch();
    }
    
    public static List<Post> findTaggedWith(String... tags) {
        return Post.find(
                "select distinct p from Post p join p.tags as t where t.name in (:tags) group by p.id, p.author, p.title, p.content,p.postedAt having count(t.id) = :size"
        ).bind("tags", tags).bind("size", tags.length).fetch();
    }
    
    
    public static List<Post> findPostFromInactivUsers() {
        DateTime forOneYear = new DateTime().minusYears(1);
        return Post.find(
                "select p from Post p where p.author not in (select pp.author from Post pp where pp.postedAt > :oldDate) group by p.author, p.postedAt"
        ).bind("oldDate", forOneYear.toDate()).fetch();
    }

    @Override
    public String toString() {
        return title + " (" + author + ")";
    }

}