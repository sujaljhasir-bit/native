package fit.hbplus.access;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component public class DomainPolicy {
 private final String domain; private final Set<String> admins;
 public DomainPolicy(@Value("${app.allowed-domain}") String domain,@Value("${app.admin-emails}") String adminEmails){this.domain=domain.trim().toLowerCase(Locale.ROOT);if(!this.domain.matches("[a-z0-9.-]+\\.[a-z]{2,}"))throw new IllegalArgumentException("Invalid allowed domain");admins=new HashSet<>();for(String s:adminEmails.split(","))if(!s.isBlank())admins.add(s.trim().toLowerCase(Locale.ROOT));}
 public boolean allows(String email,boolean verified){if(email==null||!verified)return false;String[] parts=email.toLowerCase(Locale.ROOT).split("@",-1);return parts.length==2&&!parts[0].isBlank()&&parts[1].equals(domain);}
 public String role(String email){return admins.contains(email.toLowerCase(Locale.ROOT))?"ADMIN":"USER";}
}
