package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Controller for MenuItemReview */
@Tag(name = "Menu Item Reviews")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired private MenuItemReviewRepository menuItemReviewRepository;

  /** List all menu item reviews */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allReviews() {
    return menuItemReviewRepository.findAll();
  }

  /** Get a single menu item review by id */
  @Operation(summary = "Get a single menu item review by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(@Parameter(name = "id") @RequestParam Long id) {
    return menuItemReviewRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));
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

    return menuItemReviewRepository.save(mir);
  }

  /** Update a single menu item review */
  @Operation(summary = "Update a single menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody MenuItemReview incoming) {

    MenuItemReview existing =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    existing.setItemId(incoming.getItemId());
    existing.setReviewerEmail(incoming.getReviewerEmail());
    existing.setStars(incoming.getStars());
    existing.setDateReviewed(incoming.getDateReviewed());
    existing.setComments(incoming.getComments());

    menuItemReviewRepository.save(existing);
    return existing;
  }

  /** Delete a single menu item review by id */
  @Operation(summary = "Delete a single menu item review by id")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public ResponseEntity<Object> deleteMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id) {

    var mirOpt = menuItemReviewRepository.findById(id);

    if (mirOpt.isEmpty()) {
      // Not found -> 404 with message "record {id} not found"
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(genericMessage(String.format("record %d not found", id)));
    }

    // Found -> delete and return 200 with message "record {id} deleted"
    menuItemReviewRepository.delete(mirOpt.get());
    return ResponseEntity.ok(genericMessage(String.format("record %d deleted", id)));
  }
}
