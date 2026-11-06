package fit.hbplus.access;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"app.google-client-ids=test-client","spring.datasource.url=jdbc:h2:mem:test","app.admin-emails=admin@hbplus.fit"}) @AutoConfigureMockMvc
class AccessTests {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper; @MockitoBean GoogleIdentity google;
 @Test void exactDomain(){var p=new DomainPolicy("hbplus.fit","");assertTrue(p.allows("person@hbplus.fit",true));assertTrue(p.allows("PERSON@HBPLUS.FIT",true));for(String email:new String[]{"x@gmail.com","x@hbplus.fit.evil.com","x@sub.hbplus.fit","x@@hbplus.fit","@hbplus.fit"})assertFalse(p.allows(email,true));assertFalse(p.allows("x@hbplus.fit",false));}
 @Test void anonymousCannotAccess()throws Exception{mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());mvc.perform(get("/api/admin/summary")).andExpect(status().isUnauthorized());}
 @Test void wrongDomainRejected()throws Exception{when(google.verify("outsider")).thenReturn(payload("outsider","x@gmail.com"));mvc.perform(post("/api/auth/google").contentType("application/json").content("{\"idToken\":\"outsider\"}")).andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Access restricted. Please login using your official domain email."));}
 @Test void sessionAndLogout()throws Exception{when(google.verify("good")).thenReturn(payload("member","member@hbplus.fit"));String body=mvc.perform(post("/api/auth/google").contentType("application/json").content("{\"idToken\":\"good\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();String token=mapper.readTree(body).get("token").asText();mvc.perform(get("/api/me").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("USER"));mvc.perform(get("/api/admin/summary").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());mvc.perform(post("/api/auth/logout").header("Authorization","Bearer "+token)).andExpect(status().isNoContent());mvc.perform(get("/api/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());}
 private GoogleIdToken.Payload payload(String sub,String email){var p=new GoogleIdToken.Payload();p.setSubject(sub);p.setEmail(email);p.setEmailVerified(true);p.set("name","Team Member");return p;}
}
