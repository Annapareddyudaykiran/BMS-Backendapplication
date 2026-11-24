package com.microbms.safety;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/safety")
public class SafetyController {
    private final JdbcTemplate db;
    public SafetyController(JdbcTemplate db){this.db=db;}

    @GetMapping("/reports") public List<Map<String,Object>> reports(){return db.queryForList("SELECT id,'HSE-'||LPAD(report_no::text,3,'0') AS \"reportCode\",report_type AS type,title,location,severity,details,status,corrective_work_order AS \"workOrder\",created_at AS \"createdAt\" FROM safety_reports ORDER BY created_at DESC");}
    @PostMapping("/reports") public Map<String,Object> report(@RequestBody Map<String,Object> b){UUID id=UUID.randomUUID();db.update("INSERT INTO safety_reports(id,report_type,title,location,severity,details) VALUES(?,?,?,?,?,?)",id,required(b,"type"),required(b,"title"),required(b,"location"),value(b,"severity","Low"),value(b,"details",null));return db.queryForMap("SELECT id,'HSE-'||LPAD(report_no::text,3,'0') AS \"reportCode\",report_type AS type,title,location,severity,details,status,corrective_work_order AS \"workOrder\",created_at AS \"createdAt\" FROM safety_reports WHERE id=?",id);}
    @PatchMapping("/reports/{id}/status") public Map<String,Object> reportStatus(@PathVariable UUID id,@RequestBody Map<String,Object> b){db.update("UPDATE safety_reports SET status=?,updated_at=now() WHERE id=?",required(b,"status"),id);return db.queryForMap("SELECT id,'HSE-'||LPAD(report_no::text,3,'0') AS \"reportCode\",report_type AS type,title,location,severity,details,status,corrective_work_order AS \"workOrder\" FROM safety_reports WHERE id=?",id);}
    @PatchMapping("/reports/{id}/work-order") public Map<String,Object> linkWorkOrder(@PathVariable UUID id,@RequestBody Map<String,Object>b){db.update("UPDATE safety_reports SET corrective_work_order=?,updated_at=now() WHERE id=?",required(b,"workOrder"),id);return db.queryForMap("SELECT id,'HSE-'||LPAD(report_no::text,3,'0') AS \"reportCode\",status,corrective_work_order AS \"workOrder\" FROM safety_reports WHERE id=?",id);}
    @GetMapping("/inspections") public List<Map<String,Object>> inspections(){return db.queryForList("SELECT id,inspection_name AS name,location,due_date AS \"dueDate\",status,result,created_at AS \"createdAt\" FROM safety_inspections ORDER BY due_date");}
    @PostMapping("/inspections") public Map<String,Object> inspection(@RequestBody Map<String,Object> b){UUID id=UUID.randomUUID();db.update("INSERT INTO safety_inspections(id,inspection_name,location,due_date) VALUES(?,?,?,?)",id,required(b,"name"),required(b,"location"),Date.valueOf(required(b,"dueDate")));return db.queryForMap("SELECT id,inspection_name AS name,location,due_date AS \"dueDate\",status,result FROM safety_inspections WHERE id=?",id);}
    @PatchMapping("/inspections/{id}/result") public Map<String,Object> inspectionResult(@PathVariable UUID id,@RequestBody Map<String,Object> b){db.update("UPDATE safety_inspections SET result=?,status='Completed',updated_at=now() WHERE id=?",required(b,"result"),id);return db.queryForMap("SELECT id,inspection_name AS name,location,due_date AS \"dueDate\",status,result FROM safety_inspections WHERE id=?",id);}
    private static String required(Map<String,Object>b,String k){String v=value(b,k,null);if(v==null||v.isBlank())throw new IllegalArgumentException(k+" is required");return v;}
    private static String value(Map<String,Object>b,String k,String d){Object v=b.get(k);return v==null||v.toString().isBlank()?d:v.toString().trim();}
}
