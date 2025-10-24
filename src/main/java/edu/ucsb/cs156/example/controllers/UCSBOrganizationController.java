package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
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

/** This is a REST controller for UCSBOrganization */
@Tag(name = "UCSBOrganization")
@RequestMapping("/api/UCSBOrganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * THis method returns a list of all ucsborganization.
   *
   * @return a list of all ucsborganization
   */
  @Operation(summary = "List all UCSB organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allOrganizations() {
    Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
    return orgs;
  }

  /**
   * This method creates a new diningcommons. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode the organization orgCode
   * @param orgTranslationShort the organization orgTranslation
   * @param orgTranslation
   * @param inactive
   * @return the save
   */
  @Operation(summary = "Create a new UCSBOrganization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {
    UCSBOrganization org = new UCSBOrganization();
    org.setOrgCode(orgCode);
    org.setOrgTranslationShort(orgTranslationShort);
    org.setOrgTranslation(orgTranslation);
    org.setInactive(inactive);

    UCSBOrganization savedOrg = ucsbOrganizationRepository.save(org);
    return savedOrg;
  }

  /**
   * This method returns a single organization.
   *
   * @param orgCode code of the organization
   * @return a single organization
   */
  @Operation(summary = "Get a single UCSBOrganization by orgCode")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    return org;
  }

  /**
   * Update a single ucsborganization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the ucsborganization
   * @param incoming the new commons contents
   * @return the updated commons object
   */
  @Operation(summary = "Update a single UCSBOrganization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @RequestBody @Valid UCSBOrganization incoming) {

    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    org.setOrgTranslationShort(incoming.getOrgTranslationShort());
    org.setOrgTranslation(incoming.getOrgTranslation());
    org.setInactive(incoming.getInactive());

    ucsbOrganizationRepository.save(org);
    return org;
  }

  /**
   * Delete a ucsborganization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode orgCode of the organization
   * @return a message indiciating the commons was deleted
   */
  @Operation(summary = "Delete a UCSBOrganization by orgCode")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteOrganization(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    ucsbOrganizationRepository.delete(org);
    return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
  }
}
