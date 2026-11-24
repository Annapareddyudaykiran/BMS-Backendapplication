package com.microbms.assets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/assets")
public class ModuleController {
    private final JdbcTemplate db;
    public ModuleController(JdbcTemplate db) { this.db = db; }

    @GetMapping public List<Map<String,Object>> list() {
        return db.queryForList("SELECT id,record_type AS \"recordType\",name,category,location,condition,maintenance_task AS \"maintenanceTask\",frequency,next_due AS \"nextDue\",created_at AS \"createdAt\" FROM assets ORDER BY created_at DESC");
    }

    @PostMapping public Map<String,Object> create(@RequestBody Map<String,Object> body) {
        String name=required(body,"name"), location=required(body,"location");
        String type=value(body,"recordType","Asset");
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO assets(id,record_type,name,category,location,condition,maintenance_task,frequency,next_due) VALUES (?,?,?,?,?,?,?,?,?)",
                id,type,name,value(body,"category",null),location,value(body,"condition","Operational"),
                value(body,"maintenanceTask",null),value(body,"frequency",null),date(body.get("nextDue")));
        return db.queryForMap("SELECT id,record_type AS \"recordType\",name,category,location,condition,maintenance_task AS \"maintenanceTask\",frequency,next_due AS \"nextDue\",created_at AS \"createdAt\" FROM assets WHERE id=?",id);
    }
    private static Date date(Object value){return value==null||value.toString().isBlank()?null:Date.valueOf(value.toString());}
    private static String required(Map<String,Object> body,String key){String v=value(body,key,null);if(v==null||v.isBlank())throw new IllegalArgumentException(key+" is required");return v;}
    private static String value(Map<String,Object> body,String key,String fallback){Object v=body.get(key);return v==null||v.toString().isBlank()?fallback:v.toString().trim();}
}
