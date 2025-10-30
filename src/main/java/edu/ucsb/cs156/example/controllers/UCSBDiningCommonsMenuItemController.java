package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommonsMenuItems */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB Dining Commons Menu Items
   *
   * @return an iterable of UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "List all UCSB Dining Commons Menu Items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> ucsbDiningCommonsMenuItems =
        ucsbDiningCommonsMenuItemRepository.findAll();
    return ucsbDiningCommonsMenuItems;
  }

  /**
   * Create a new UCSB Dining Commons Menu Item
   *
   * @param diningCommonsCode the dining commons code of the item
   * @param name the name of the item
   * @param station the station of the item
   * @return the saved UCSB Dining Commons Menu Item
   */
  @Operation(summary = "Create a new UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = new UCSBDiningCommonsMenuItem();
    ucsbDiningCommonsMenuItem.setDiningCommonsCode(diningCommonsCode);
    ucsbDiningCommonsMenuItem.setName(name);
    ucsbDiningCommonsMenuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedUcsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

    return savedUcsbDiningCommonsMenuItem;
  }

  /**
   * Get a single menu item by id
   *
   * @param id the id of the menu item
   * @return a UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Get a single menu item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return ucsbDiningCommonsMenuItem;
  }

  /**
   * Update a single UCSB Dining Commons Menu Item
   *
   * @param id id of the item to update
   * @param incoming the new item data
   * @return the updated item
   */
  @Operation(summary = "Update a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    ucsbDiningCommonsMenuItem.setName(incoming.getName());
    ucsbDiningCommonsMenuItem.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

    return ucsbDiningCommonsMenuItem;
  }

  /**
   * Delete a UCSBDiningCommonsMenuItem
   *
   * @param id the id of the item to delete
   * @return a message indicating the item was deleted
   */
  @Operation(summary = "Delete a UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItemRepository.delete(ucsbDiningCommonsMenuItem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }
}
