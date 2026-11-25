package com.microbms.workorders;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/work-orders")
public class ModuleController {
    private final JdbcTemplate db;
    public ModuleController(JdbcTemplate db) { this.db = db; }

    @GetMapping public List<Map<String,Object>> list(@RequestAttribute("authenticatedUserEmail") String owner) {
        return db.queryForList("SELECT id,source,related_ticket AS \"relatedTicket\",task,location,priority,trade,assigned_to AS \"assignedTo\",status,created_at AS \"createdAt\" FROM work_orders WHERE owner_email=? ORDER BY created_at DESC",owner);
    }

    @PostMapping public Map<String,Object> create(@RequestAttribute("authenticatedUserEmail") String owner,@RequestBody Map<String,Object> body) {
        String task = required(body,"task"), location = required(body,"location");
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO work_orders(id,source,related_ticket,task,location,priority,trade,assigned_to,status,owner_email) VALUES (?,?,?,?,?,?,?,?,?,?)",
                id, value(body,"source","Manual"), value(body,"relatedTicket",null), task, location,
                value(body,"priority","Medium"), value(body,"trade",null), value(body,"assignedTo",null),
                value(body,"status","Awaiting assignment"),owner);
        return db.queryForMap("SELECT id,source,related_ticket AS \"relatedTicket\",task,location,priority,trade,assigned_to AS \"assignedTo\",status,created_at AS \"createdAt\" FROM work_orders WHERE id=? AND owner_email=?",id,owner);
    }
    private static String required(Map<String,Object> body,String key){String v=value(body,key,null);if(v==null||v.isBlank())throw new IllegalArgumentException(key+" is required");return v;}
    private static String value(Map<String,Object> body,String key,String fallback){Object v=body.get(key);return v==null||v.toString().isBlank()?fallback:v.toString().trim();}
}
