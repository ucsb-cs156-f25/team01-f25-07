package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller that exposes endpoints for {@link MenuItemReview} records. */
@Tag(name = "Menu Item Reviews")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired private MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all menu item reviews
   *
   * @return Iterable<MenuItemReview>
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allReviews() {
    return menuItemReviewRepository.findAll();
  }

  /** Create a new menu item review */
  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postReview(
      @Parameter(name = "itemId") @RequestParam long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "dateReviewed", description = "ISO datetime, e.g. 2025-10-25T13:45:00")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments) {
    MenuItemReview mir = new MenuItemReview();
    mir.setItemId(itemId);
    mir.setReviewerEmail(reviewerEmail);
    mir.setStars(stars);
    mir.setDateReviewed(dateReviewed);
    mir.setComments(comments);

    MenuItemReview savedReview = menuItemReviewRepository.save(mir);
    return savedReview;
  }
}
