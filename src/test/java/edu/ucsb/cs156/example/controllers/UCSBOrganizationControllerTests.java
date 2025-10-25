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
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {
  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/UCSBOrganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/UCSBOrganization/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/UCSBOrganization/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganization() throws Exception {

    // arrange
    UCSBOrganization ucsborganization1 =
        UCSBOrganization.builder()
            .orgCode("UCSBbadminton")
            .orgTranslationShort("Single")
            .orgTranslation("Mix")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedUCSBOrganization = new ArrayList<>();
    expectedUCSBOrganization.addAll(Arrays.asList(ucsborganization1));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedUCSBOrganization);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedUCSBOrganization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_commons() throws Exception {
    // arrange

    UCSBOrganization ucsborganization1 =
        UCSBOrganization.builder()
            .orgCode("UCSBbadminton")
            .orgTranslationShort("Single")
            .orgTranslation("Mix")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(ucsborganization1))).thenReturn(ucsborganization1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post?orgCode=UCSBbadminton&orgTranslationShort=Single&orgTranslation=Mix&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(eq(ucsborganization1));
    String expectedJson = mapper.writeValueAsString(ucsborganization1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBOrganization?orgCode=123"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    UCSBOrganization ucsborganization1 =
        UCSBOrganization.builder()
            .orgCode("UCSB")
            .orgTranslationShort("Single")
            .orgTranslation("Mix")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("UCSB")))
        .thenReturn(Optional.of(ucsborganization1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization?orgCode=UCSB"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById(eq("UCSB"));
    String expectedJson = mapper.writeValueAsString(ucsborganization1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exists()
      throws Exception {

    // arrange
    // UCSBOrganization ucsborganization1 = UCSBOrganization.builder()
    //     .orgCode("aaa")
    //     .orgTranslationShort("bbb")
    //     .orgTranslation("vvv")
    //     .inactive(true)
    //     .build();
    when(ucsbOrganizationRepository.findById(eq("123"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization?orgCode=123"))
            .andExpect(status().isNotFound()) // this executes the orElseThrow lambda
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById(eq("123"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id 123 not found", json.get("message"));
  }

  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_can_edit_an_existing_organization() throws Exception {
  //   // arrange
  //   UCSBOrganization ucsborganization1 =
  //       UCSBOrganization.builder()
  //           .orgCode("UCSB")
  //           .orgTranslationShort("Single")
  //           .orgTranslation("Mix")
  //           .inactive(true)
  //           .build();

  //   UCSBOrganization editeducsborganization1 =
  //       UCSBOrganization.builder()
  //           .orgCode("UCSB")
  //           .orgTranslationShort("SingleLady")
  //           .orgTranslation("Mix")
  //           .inactive(false)
  //           .build();

  //   String requestBody = mapper.writeValueAsString(editeducsborganization1);

  //   when(ucsbOrganizationRepository.findById(eq("UCSB")))
  //       .thenReturn(Optional.of(ucsborganization1));

  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(
  //               put("/api/UCSBOrganization?orgCode=UCSB")
  //                   .contentType(MediaType.APPLICATION_JSON)
  //                   .characterEncoding("utf-8")
  //                   .content(requestBody)
  //                   .with(csrf()))
  //           .andExpect(status().isOk())
  //           .andReturn();

  //   // assert
  //   verify(ucsbOrganizationRepository, times(1)).findById("UCSB");
  //   verify(ucsbOrganizationRepository, times(1))
  //       .save(editeducsborganization1); // should be saved with updated info
  //   String responseString = response.getResponse().getContentAsString();
  //   assertEquals(requestBody, responseString);
  // }

  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
  //   // arrange

  //   UCSBOrganization editeducsborganization1 =
  //       UCSBOrganization.builder()
  //           .orgCode("Yuchao")
  //           .orgTranslationShort("YuchaoZheng")
  //           .orgTranslation("Mix")
  //           .inactive(true)
  //           .build();

  //   String requestBody = mapper.writeValueAsString(editeducsborganization1);

  //   when(ucsbOrganizationRepository.findById(eq("Yuchao"))).thenReturn(Optional.empty());

  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(
  //               put("/api/UCSBOrganization?orgCode=Yuchao")
  //                   .contentType(MediaType.APPLICATION_JSON)
  //                   .characterEncoding("utf-8")
  //                   .content(requestBody)
  //                   .with(csrf()))
  //           .andExpect(status().isNotFound())
  //           .andReturn();

  //   // assert
  //   verify(ucsbOrganizationRepository, times(1)).findById("Yuchao");
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("UCSBOrganization with id Yuchao not found", json.get("message"));
  // }
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange
    UCSBOrganization orgOrig =
        UCSBOrganization.builder()
            .orgCode("UCSB")
            .orgTranslationShort("Single")
            .orgTranslation("Mix")
            .inactive(true)
            .build();

    UCSBOrganization orgEdited =
        UCSBOrganization.builder()
            .orgCode("UCSB")
            .orgTranslationShort("SingleLady")
            .orgTranslation("M")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(orgEdited);

    when(ucsbOrganizationRepository.findById(eq("UCSB"))).thenReturn(Optional.of(orgOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization?orgCode=UCSB")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("UCSB");
    verify(ucsbOrganizationRepository, times(1))
        .save(orgEdited); // should be saved with updated info

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange
    UCSBOrganization Editedorg =
        UCSBOrganization.builder()
            .orgCode("Yuchao")
            .orgTranslationShort("YuchaoZheng")
            .orgTranslation("M")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(Editedorg);

    when(ucsbOrganizationRepository.findById(eq("Yuchao"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization?orgCode=Yuchao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("Yuchao");

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id Yuchao not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_organization() throws Exception {
    // arrange
    UCSBOrganization ucsbOrganization1 =
        UCSBOrganization.builder()
            .orgCode("UCSB")
            .orgTranslationShort("Single")
            .orgTranslation("Mix")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("UCSB")))
        .thenReturn(Optional.of(ucsbOrganization1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization?orgCode=UCSB").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("UCSB");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id UCSB deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    // arrange
    when(ucsbOrganizationRepository.findById(eq("Yuchao"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization?orgCode=Yuchao").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("Yuchao");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id Yuchao not found", json.get("message"));
  }
}
