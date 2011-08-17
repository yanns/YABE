package models;
 
import java.util.*;
import javax.persistence.*;

import net.sf.oval.constraint.Assert;
import net.sf.oval.constraint.NotNull;
 
import play.data.validation.*;
import play.db.jpa.*;
import validation.Unique;
 
@Entity
public class User extends Model {
 
    @Email
    @Required
    @Unique
    public String email;

    @Password
    @Required
    public String password;

    public String fullname;
    
    public Date birthday;
    
    @NotNull(when = "groovy:_this.birthday == null")
    @Assert(expr = "_value == null || _this.birthday == null", lang = "groovy", message="Either Age or date of birth should be empty")
    @Min(0)
    public Integer age;
    
    public boolean isAdmin;
    
    public User(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }
 
    public static User connect(String email, String password) {
        return find("byEmailAndPassword", email, password).first();
    }

    @Override
    public String toString() {
        return email;
    }
}