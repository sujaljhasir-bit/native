package fit.hbplus.access;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="access_sessions") public class Session {
 @Id String hash;
 @Column(nullable=false) String subject;
 @Column(nullable=false) Instant expires;
 protected Session(){}
 Session(String hash,String subject,Instant expires){this.hash=hash;this.subject=subject;this.expires=expires;}
}
