package fit.hbplus.access;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
@Service public class GoogleIdentity {
 private final GoogleIdTokenVerifier verifier;
 public GoogleIdentity(@Value("${app.google-client-ids}") String clients){var audiences=Arrays.stream(clients.split(",")).map(String::trim).filter(s->!s.isBlank()).toList();if(audiences.isEmpty())throw new IllegalArgumentException("Google client IDs are required");verifier=new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),GsonFactory.getDefaultInstance()).setAudience(audiences).build();}
 public GoogleIdToken.Payload verify(String token){try{var verified=verifier.verify(token);if(verified==null)throw new IllegalArgumentException();return verified.getPayload();}catch(Exception ex){throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Google identity could not be verified. Please sign in again.");}}
}
