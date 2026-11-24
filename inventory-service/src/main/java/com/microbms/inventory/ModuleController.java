package com.microbms.inventory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ModuleController {
    private final JdbcTemplate db;
    public ModuleController(JdbcTemplate db) { this.db=db; }

    @GetMapping("/inventory") public List<Map<String,Object>> inventory(){
        return db.queryForList("SELECT id,name,category,quantity,minimum,unit,created_at AS \"createdAt\" FROM inventory_items ORDER BY name");
    }
    @PostMapping("/inventory") public Map<String,Object> addInventory(@RequestBody Map<String,Object> body){
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO inventory_items(id,name,category,quantity,minimum,unit) VALUES (?,?,?,?,?,?)",id,
                required(body,"name"),value(body,"category",null),number(body.get("quantity")),number(body.get("minimum")),value(body,"unit","pcs"));
        return db.queryForMap("SELECT id,name,category,quantity,minimum,unit,created_at AS \"createdAt\" FROM inventory_items WHERE id=?",id);
    }
    @GetMapping("/procurement") public List<Map<String,Object>> procurement(){
        return db.queryForList("SELECT id,item,quantity,supplier,unit_cost AS \"unitCost\",required_by AS \"requiredBy\",reason,status,created_at AS \"createdAt\" FROM purchase_requests ORDER BY created_at DESC");
    }
    @PostMapping("/procurement") public Map<String,Object> createRequest(@RequestBody Map<String,Object> body){
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO purchase_requests(id,item,quantity,supplier,unit_cost,required_by,reason,status) VALUES (?,?,?,?,?,?,?,?)",id,
                required(body,"item"),number(body.get("quantity")),required(body,"supplier"),number(body.get("unitCost")),
                date(body.get("requiredBy")),value(body,"reason",null),value(body,"status","Awaiting approval"));
        return db.queryForMap("SELECT id,item,quantity,supplier,unit_cost AS \"unitCost\",required_by AS \"requiredBy\",reason,status,created_at AS \"createdAt\" FROM purchase_requests WHERE id=?",id);
    }
    private static BigDecimal number(Object v){if(v==null||v.toString().isBlank())return BigDecimal.ZERO;return new BigDecimal(v.toString());}
    private static Date date(Object v){return v==null||v.toString().isBlank()?null:Date.valueOf(v.toString());}
    private static String required(Map<String,Object> b,String key){String v=value(b,key,null);if(v==null||v.isBlank())throw new IllegalArgumentException(key+" is required");return v;}
    private static String value(Map<String,Object> b,String key,String fallback){Object v=b.get(key);return v==null||v.toString().isBlank()?fallback:v.toString().trim();}
}
