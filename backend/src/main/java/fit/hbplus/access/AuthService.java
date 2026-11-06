package fit.hbplus.access;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
@Service public class AuthService {
 final SessionRepository sessions; final MemberRepository members; final DomainPolicy policy; final GoogleIdentity google;
 public record Profile(String email,String name,String role){}
 public record Login(String token,long expiresIn){}
 public AuthService(SessionRepository s,MemberRepository m,DomainPolicy p,GoogleIdentity g){sessions=s;members=m;policy=p;google=g;}
 static String hash(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
 @Transactional public Login login(String idToken){
 var p=google.verify(idToken);
 if(!policy.allows(p.getEmail(),Boolean.TRUE.equals(p.getEmailVerified())))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Access restricted. Please login using your official domain email.");
 String name=Objects.toString(p.get("name"),p.getEmail().split("@")[0]);
 members.save(new Member(p.getSubject(),p.getEmail().toLowerCase(Locale.ROOT),name));
 byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
 sessions.save(new Session(hash(token),p.getSubject(),Instant.now().plusSeconds(3600)));return new Login(token,3600);
 }
 public Optional<Profile> authenticate(String token){if(token==null||!token.matches("[A-Za-z0-9_-]{43}"))return Optional.empty();return sessions.findById(hash(token)).filter(s->s.expires.isAfter(Instant.now())).flatMap(s->members.findById(s.subject)).filter(m->policy.allows(m.email,true)).map(m->new Profile(m.email,m.name,policy.role(m.email)));}
 @Transactional public void logout(String token){if(token!=null)sessions.deleteById(hash(token));}
 @Scheduled(fixedDelay=600000) @Transactional public void cleanup(){sessions.deleteByExpiresBefore(Instant.now());}
}
