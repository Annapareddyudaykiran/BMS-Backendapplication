package com.microbms.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PropertyHelpdeskController {
    private final JdbcTemplate db;
    private final String adminEmail;
    public PropertyHelpdeskController(JdbcTemplate db, @Value("${app.attendance.admin-email}") String adminEmail) { this.db = db; this.adminEmail=adminEmail.trim().toLowerCase(); }
    public record TowerRequest(@NotBlank @Size(max=80) String name, @Min(1) @Max(100) int floorCount) {}
    public record PropertyRequest(@NotBlank @Size(max=160) String name, @NotBlank @Size(max=100) String city,
                                  @NotBlank @Size(max=100) String state, @NotBlank @Size(max=40) String propertyType) {}
    public record UnitRequest(@NotNull UUID towerId, @Min(1) int floorNumber, @NotBlank @Size(max=40) String unitNumber,
                              @NotBlank @Size(max=40) String spaceType, @NotBlank @Pattern(regexp="Occupied|Vacant") String occupancy,
                              @Size(max=160) String occupant) {}
    public record TicketRequest(@NotBlank @Size(max=1000) String description, @NotNull UUID towerId,
                                @NotBlank @Size(max=100) String floorArea, UUID unitId,
                                @NotBlank @Size(max=60) String category,
                                @NotBlank @Pattern(regexp="Critical|High|Medium|Low") String priority) {}

    @GetMapping("/property") public Map<String,Object> property() {
        var p = db.queryForMap("SELECT id,name,city,state,property_type AS \"propertyType\" FROM properties ORDER BY created_at LIMIT 1");
        var towers = db.queryForList("SELECT t.id,t.name,t.floor_count AS \"floorCount\", count(u.id) AS \"unitCount\", count(u.id) FILTER (WHERE u.occupancy='Occupied') AS occupied FROM property_towers t LEFT JOIN property_units u ON u.tower_id=t.id WHERE t.property_id=? GROUP BY t.id ORDER BY t.name", p.get("id"));
        var units = db.queryForList("SELECT u.id,u.tower_id AS \"towerId\",u.floor_number AS \"floorNumber\",u.unit_number AS \"unitNumber\",u.space_type AS \"spaceType\",u.occupancy,u.occupant FROM property_units u JOIN property_towers t ON t.id=u.tower_id WHERE t.property_id=? ORDER BY t.name,u.floor_number,u.unit_number", p.get("id"));
        return Map.of("property",p,"towers",towers,"units",units);
    }
    @PutMapping("/property")
    public Map<String,Object> updateProperty(@RequestAttribute("authenticatedUserEmail") String actor, @Valid @RequestBody PropertyRequest r) {
        requireAdmin(actor);
        UUID propertyId = db.queryForObject("SELECT id FROM properties ORDER BY created_at LIMIT 1", UUID.class);
        db.update("UPDATE properties SET name=?,city=?,state=?,property_type=? WHERE id=?", r.name().trim(), r.city().trim(), r.state().trim(), r.propertyType().trim(), propertyId);
        return Map.of("id",propertyId,"name",r.name().trim(),"city",r.city().trim(),"state",r.state().trim(),"propertyType",r.propertyType().trim());
    }
    @PostMapping("/property/towers") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createTower(@RequestAttribute("authenticatedUserEmail") String actor, @Valid @RequestBody TowerRequest r) {
        requireAdmin(actor); UUID property = db.queryForObject("SELECT id FROM properties ORDER BY created_at LIMIT 1", UUID.class);
        UUID id = UUID.randomUUID(); db.update("INSERT INTO property_towers(id,property_id,name,floor_count) VALUES(?,?,?,?)",id,property,r.name().trim(),r.floorCount());
        return Map.of("id",id,"name",r.name().trim(),"floorCount",r.floorCount());
    }
    @PostMapping("/property/units") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createUnit(@RequestAttribute("authenticatedUserEmail") String actor, @Valid @RequestBody UnitRequest r) {
        requireAdmin(actor); Integer floors = db.queryForObject("SELECT floor_count FROM property_towers WHERE id=?",Integer.class,r.towerId());
        if (floors==null || r.floorNumber()>floors) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a valid tower and floor");
        UUID id=UUID.randomUUID(); db.update("INSERT INTO property_units(id,tower_id,floor_number,unit_number,space_type,occupancy,occupant) VALUES(?,?,?,?,?,?,?)",id,r.towerId(),r.floorNumber(),r.unitNumber().trim(),r.spaceType(),r.occupancy(),r.occupant());
        return Map.of("id",id,"towerId",r.towerId(),"floorNumber",r.floorNumber(),"unitNumber",r.unitNumber(),"spaceType",r.spaceType(),"occupancy",r.occupancy());
    }
    @GetMapping("/helpdesk/tickets") public List<Map<String,Object>> tickets(@RequestAttribute("authenticatedUserEmail") String actor) {
        requireAdmin(actor); return db.queryForList("SELECT tk.id, 'TKT-'||tk.ticket_number AS \"ticketNumber\",tk.description,tk.floor_area AS \"floorArea\",tk.category,tk.priority,tk.status,tk.created_at AS \"createdAt\",t.name AS tower,u.unit_number AS \"unitNumber\",w.status AS \"workOrderStatus\",worker.name AS \"ownerName\" FROM helpdesk_tickets tk JOIN property_towers t ON t.id=tk.tower_id LEFT JOIN property_units u ON u.id=tk.unit_id JOIN work_orders w ON w.ticket_id=tk.id LEFT JOIN attendance_workers worker ON worker.id=w.owner_worker_id ORDER BY tk.created_at DESC");
    }
    @PostMapping("/helpdesk/tickets") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createTicket(@RequestAttribute("authenticatedUserId") UUID userId,@RequestAttribute("authenticatedUserEmail") String actor,@Valid @RequestBody TicketRequest r) {
        requireAdmin(actor);
        if (r.unitId()!=null && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM property_units WHERE id=? AND tower_id=?)",Boolean.class,r.unitId(),r.towerId()))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unit must belong to the selected tower");
        UUID id=UUID.randomUUID(); db.update("INSERT INTO helpdesk_tickets(id,description,tower_id,floor_area,unit_id,category,priority,created_by) VALUES(?,?,?,?,?,?,?,?)",id,r.description().trim(),r.towerId(),r.floorArea(),r.unitId(),r.category(),r.priority(),userId);
        db.update("INSERT INTO work_orders(ticket_id) VALUES(?)",id);
        return db.queryForMap("SELECT id,'TKT-'||ticket_number AS \"ticketNumber\",description,floor_area AS \"floorArea\",category,priority,status FROM helpdesk_tickets WHERE id=?",id);
    }
    private void requireAdmin(String email) { if(email==null || !email.trim().equalsIgnoreCase(adminEmail)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the attendance administrator can manage property and helpdesk"); }
}
