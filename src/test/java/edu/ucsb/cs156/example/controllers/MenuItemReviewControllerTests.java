package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockBean private MenuItemReviewRepository menuItemReviewRepository;
  @MockBean private UserRepository userRepository;

  // ---------- Auth checks ----------
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/menuitemreview/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/menuitemreview/post")).andExpect(status().is(403));
  }

  // ---------- GET /all ----------
  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_reviews() throws Exception {
    LocalDateTime t1 = LocalDateTime.parse("2025-10-25T10:00:00");
    LocalDateTime t2 = LocalDateTime.parse("2025-10-24T09:30:00");

    MenuItemReview r1 =
        MenuItemReview.builder()
            .itemId(100L)
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .dateReviewed(t1)
            .comments("Excellent!")
            .build();

    MenuItemReview r2 =
        MenuItemReview.builder()
            .itemId(101L)
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .dateReviewed(t2)
            .comments("Good")
            .build();

    when(menuItemReviewRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(r1, r2)));

    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(Arrays.asList(r1, r2));
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // ---------- POST /post ----------
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_review() throws Exception {
    LocalDateTime dt = LocalDateTime.parse("2025-10-25T20:15:00");

    MenuItemReview newReview =
        MenuItemReview.builder()
            .itemId(99L)
            .reviewerEmail("admin@ucsb.edu")
            .stars(5)
            .dateReviewed(dt)
            .comments("Perfect!")
            .build();

    when(menuItemReviewRepository.save(any(MenuItemReview.class))).thenReturn(newReview);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post?itemId=99&reviewerEmail=admin@ucsb.edu&stars=5&dateReviewed=2025-10-25T20:15:00&comments=Perfect!")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // 捕获并验证传入的保存实体，确保每个 setter 被执行
    ArgumentCaptor<MenuItemReview> captor = ArgumentCaptor.forClass(MenuItemReview.class);
    verify(menuItemReviewRepository, times(1)).save(captor.capture());
    MenuItemReview saved = captor.getValue();

    assertEquals(99L, saved.getItemId());
    assertEquals("admin@ucsb.edu", saved.getReviewerEmail());
    assertEquals(5, saved.getStars());
    assertEquals(LocalDateTime.parse("2025-10-25T20:15:00"), saved.getDateReviewed());
    assertEquals("Perfect!", saved.getComments());

    String expectedJson = mapper.writeValueAsString(newReview);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // ---------- GET by id ----------
  @WithMockUser(roles = {"USER"})
  @Test
  public void user_can_get_by_id_when_exists() throws Exception {
    LocalDateTime t = LocalDateTime.parse("2025-10-25T10:00:00");
    MenuItemReview mir =
        MenuItemReview.builder()
            .itemId(100L)
            .reviewerEmail("a@ucsb.edu")
            .stars(4)
            .dateReviewed(t)
            .comments("ok")
            .build();

    assertEquals(99L, saved.getItemId());
    assertEquals("admin@ucsb.edu", saved.getReviewerEmail());
    assertEquals(5, saved.getStars());
    assertEquals(LocalDateTime.parse("2025-10-25T20:15:00"), saved.getDateReviewed());
    assertEquals("Perfect!", saved.getComments());

    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview?id=7")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findById(7L);
    String expectedJson = mapper.writeValueAsString(mir);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void user_get_by_id_returns_404_when_not_exists() throws Exception {
    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreview?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(7L);
    var json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReview with id 7 not found", json.get("message"));
  }

  // ---------- PUT (update) ----------
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_existing_menuitemreview() throws Exception {
    LocalDateTime t1 = LocalDateTime.parse("2025-10-25T10:00:00");
    LocalDateTime t2 = LocalDateTime.parse("2025-11-01T12:34:56");

    MenuItemReview orig =
        MenuItemReview.builder()
            .itemId(100L)
            .reviewerEmail("a@ucsb.edu")
            .stars(3)
            .dateReviewed(t1)
            .comments("ok")
            .build();

    MenuItemReview edited =
        MenuItemReview.builder()
            .itemId(101L)
            .reviewerEmail("b@ucsb.edu")
            .stars(5)
            .dateReviewed(t2)
            .comments("great")
            .build();

    String requestBody = mapper.writeValueAsString(edited);
    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(orig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(67L);
    verify(menuItemReviewRepository, times(1)).save(edited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menuitemreview_when_id_not_found() throws Exception {
    LocalDateTime t = LocalDateTime.parse("2025-10-25T10:00:00");
    MenuItemReview body =
        MenuItemReview.builder()
            .itemId(100L)
            .reviewerEmail("a@ucsb.edu")
            .stars(4)
            .dateReviewed(t)
            .comments("nice")
            .build();

    String requestBody = mapper.writeValueAsString(body);
    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(67L);
    var json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReview with id 67 not found", json.get("message"));
  }
    String expectedJson = mapper.writeValueAsString(newReview);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
