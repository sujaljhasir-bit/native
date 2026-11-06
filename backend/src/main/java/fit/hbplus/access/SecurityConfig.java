package fit.hbplus.access;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
@Configuration public class SecurityConfig {
 static String bearer(HttpServletRequest r){String h=r.getHeader("Authorization");return h!=null&&h.startsWith("Bearer ")?h.substring(7):null;}
 @Bean SecurityFilterChain security(HttpSecurity http,AuthService auth,@Value("${app.frontend-origins}") String origins)throws Exception{
 CorsConfiguration cors=new CorsConfiguration();cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());cors.setAllowedMethods(List.of("GET","POST","OPTIONS"));cors.setAllowedHeaders(List.of("Content-Type","Authorization"));cors.setAllowCredentials(false);UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",cors);
 return http.cors(c->c.configurationSource(source)).csrf(c->c.disable()).sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 .authorizeHttpRequests(c->c.requestMatchers("/api/auth/google","/api/health").permitAll().requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
 .exceptionHandling(c->c.authenticationEntryPoint((r,s,e)->{s.setStatus(401);s.setContentType("application/json");s.getWriter().write("{\"message\":\"Please sign in to continue.\"}");}).accessDeniedHandler((r,s,e)->{s.setStatus(403);s.setContentType("application/json");s.getWriter().write("{\"message\":\"Administrator access required.\"}");}))
 .addFilterBefore(new OncePerRequestFilter(){@Override protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain chain)throws ServletException,IOException{
 s.setHeader("Cache-Control","no-store");auth.authenticate(bearer(r)).ifPresent(p->SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p,null,List.of(new SimpleGrantedAuthority("ROLE_"+p.role())))));chain.doFilter(r,s);
 }},UsernamePasswordAuthenticationFilter.class).build();
 }
}
