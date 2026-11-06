package fit.hbplus.access;
import jakarta.persistence.*;
@Entity public class Member {
 @Id String subject;
 @Column(nullable=false) String email;
 @Column(nullable=false) String name;
 protected Member(){}
 Member(String subject,String email,String name){this.subject=subject;this.email=email;this.name=name;}
}
