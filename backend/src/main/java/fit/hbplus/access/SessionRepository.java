package fit.hbplus.access;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
public interface SessionRepository extends JpaRepository<Session,String>{long deleteByExpiresBefore(Instant now);}
