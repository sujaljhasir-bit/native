package fit.hbplus.access;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
@RestController @RequestMapping("/api") public class AuthController {
 private final AuthService auth;
 public AuthController(AuthService auth){this.auth=auth;}
 public record GoogleRequest(String idToken){}
 @PostMapping("/auth/google") public AuthService.Login login(@RequestBody GoogleRequest body){if(body.idToken()==null||body.idToken().length()>10000||body.idToken().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A Google identity token is required.");return auth.login(body.idToken());}
 @GetMapping("/me") public AuthService.Profile me(@AuthenticationPrincipal AuthService.Profile p){return p;}
 @PostMapping("/auth/logout") public ResponseEntity<Void> logout(HttpServletRequest r){auth.logout(SecurityConfig.bearer(r));return ResponseEntity.noContent().build();}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","ok");}
 @GetMapping("/admin/summary") public Map<String,Long> admin(){return Map.of("members",auth.members.count());}
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,String>> error(ResponseStatusException ex){return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",ex.getReason()==null?"Request failed.":ex.getReason()));}
}
