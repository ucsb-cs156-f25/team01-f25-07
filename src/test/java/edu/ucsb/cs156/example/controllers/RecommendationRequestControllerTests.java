package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
}
