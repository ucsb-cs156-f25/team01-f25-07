package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockBean RecommendationRequestRepository recommendationRequestRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/recommendationrequests/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(200)); // logged in user ok
  }

  // Authorization tests for /api/recommendationrequests/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/recommendationrequests/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // ---------- Tests with mocks for database actions (only for /all and /post) ----------

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

    // arrange
    LocalDateTime r1Req = LocalDateTime.parse("2022-04-20T09:30:00");
    LocalDateTime r1Need = LocalDateTime.parse("2022-05-01T23:59:00");
    RecommendationRequest rr1 = new RecommendationRequest();
    rr1.setRequesterEmail("student1@ucsb.edu");
    rr1.setProfessorEmail("prof1@ucsb.edu");
    rr1.setExplanation("Applying for research program");
    rr1.setDateRequested(r1Req);
    rr1.setDateNeeded(r1Need);
    rr1.setDone(false);

    LocalDateTime r2Req = LocalDateTime.parse("2023-01-01T00:00:00");
    LocalDateTime r2Need = LocalDateTime.parse("2023-02-01T00:00:00");
    RecommendationRequest rr2 = new RecommendationRequest();
    rr2.setRequesterEmail("student2@ucsb.edu");
    rr2.setProfessorEmail("prof2@ucsb.edu");
    rr2.setExplanation("Graduate school application");
    rr2.setDateRequested(r2Req);
    rr2.setDateNeeded(r2Need);
    rr2.setDone(true);

    var expected = new ArrayList<RecommendationRequest>();
    expected.addAll(Arrays.asList(rr1, rr2));

    when(recommendationRequestRepository.findAll()).thenReturn(expected);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
    // arrange
    LocalDateTime dateRequested = LocalDateTime.parse("2022-04-20T09:30:00");
    LocalDateTime dateNeeded = LocalDateTime.parse("2022-05-01T23:59:00");

    RecommendationRequest toSave = new RecommendationRequest();
    toSave.setRequesterEmail("student@ucsb.edu");
    toSave.setProfessorEmail("advisor@ucsb.edu");
    toSave.setExplanation("Recommendation for scholarship");
    toSave.setDateRequested(dateRequested);
    toSave.setDateNeeded(dateNeeded);
    toSave.setDone(true);

    when(recommendationRequestRepository.save(eq(toSave))).thenReturn(toSave);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("professorEmail", "advisor@ucsb.edu")
                    .param("explanation", "Recommendation for scholarship")
                    .param("dateRequested", "2022-04-20T09:30:00")
                    .param("dateNeeded", "2022-05-01T23:59:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(eq(toSave));
    String expectedJson = mapper.writeValueAsString(toSave);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime dr = LocalDateTime.parse("2025-01-31T13:45:00");
    LocalDateTime dn = LocalDateTime.parse("2025-02-15T23:59:00");

    RecommendationRequest rr =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("advisor@ucsb.edu")
            .explanation("Recommendation for scholarship")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(rr));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(rr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_existing_recommendationrequest_kills_mutations() throws Exception {

    LocalDateTime dr1 = LocalDateTime.parse("2025-01-31T13:45:00");
    LocalDateTime dn1 = LocalDateTime.parse("2025-02-15T23:59:00");

    RecommendationRequest rrOrig =
        RecommendationRequest.builder()
            .requesterEmail("old@ucsb.edu")
            .professorEmail("oldprof@ucsb.edu")
            .explanation("old")
            .dateRequested(dr1)
            .dateNeeded(dn1)
            .done(false)
            .build();

    LocalDateTime dr2 = LocalDateTime.parse("2025-03-01T09:30:00");
    LocalDateTime dn2 = LocalDateTime.parse("2025-03-20T23:59:00");

    RecommendationRequest rrEdited =
        RecommendationRequest.builder()
            .requesterEmail("new@ucsb.edu")
            .professorEmail("newprof@ucsb.edu")
            .explanation("new explanation")
            .dateRequested(dr2)
            .dateNeeded(dn2)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(rrEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(rrOrig));

    when(recommendationRequestRepository.save(any(RecommendationRequest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);

    ArgumentCaptor<RecommendationRequest> captor =
        ArgumentCaptor.forClass(RecommendationRequest.class);
    verify(recommendationRequestRepository, times(1)).save(captor.capture());
    RecommendationRequest saved = captor.getValue();

    assertEquals(rrEdited.getRequesterEmail(), saved.getRequesterEmail());
    assertEquals(rrEdited.getProfessorEmail(), saved.getProfessorEmail());
    assertEquals(rrEdited.getExplanation(), saved.getExplanation());
    assertEquals(rrEdited.getDateRequested(), saved.getDateRequested());
    assertEquals(rrEdited.getDateNeeded(), saved.getDateNeeded());
    assertEquals(rrEdited.getDone(), saved.getDone());

    String expectedJson = mapper.writeValueAsString(saved);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
    // arrange
    LocalDateTime dr = LocalDateTime.parse("2025-01-31T13:45:00");
    LocalDateTime dn = LocalDateTime.parse("2025-02-15T23:59:00");

    RecommendationRequest rrEdited =
        RecommendationRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .professorEmail("advisor@ucsb.edu")
            .explanation("Recommendation for scholarship")
            .dateRequested(dr)
            .dateNeeded(dn)
            .done(false)
            .build();

    String requestBody = mapper.writeValueAsString(rrEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_recommendation_request() throws Exception {
    // arrange
    RecommendationRequest recommendationRequest =
        RecommendationRequest.builder()
            .requesterEmail("student@example.edu")
            .professorEmail("prof@example.edu")
            .explanation("Requesting recommendation for CS internship")
            .dateRequested(LocalDateTime.parse("2022-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-02-01T00:00:00"))
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(15L)))
        .thenReturn(Optional.of(recommendationRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void
      admin_tries_to_delete_non_existant_recommendation_request_and_gets_right_error_message()
          throws Exception {
    // arrange
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }
}
