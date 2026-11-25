package com.microbms.amc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final JdbcTemplate db;
    public ContractController(JdbcTemplate db){this.db=db;}
    @GetMapping public List<Map<String,Object>> list(@RequestAttribute("authenticatedUserEmail") String owner){return db.queryForList("SELECT id,'AMC-'||LPAD(contract_no::text,3,'0') AS \"contractCode\",vendor_name AS vendor,covered_asset AS \"coveredAsset\",contract_start AS \"contractStart\",contract_expiry AS \"contractExpiry\",annual_value AS \"annualValue\",service_frequency AS \"serviceFrequency\",vendor_contact AS \"vendorContact\",scope,status,work_order_reference AS \"workOrder\",created_at AS \"createdAt\" FROM amc_contracts WHERE owner_email=? ORDER BY contract_expiry",owner);}
    @PostMapping public Map<String,Object> create(@RequestAttribute("authenticatedUserEmail") String owner,@RequestBody Map<String,Object>b){UUID id=UUID.randomUUID();db.update("INSERT INTO amc_contracts(id,vendor_name,covered_asset,contract_start,contract_expiry,annual_value,service_frequency,vendor_contact,scope,owner_email) VALUES(?,?,?,?,?,?,?,?,?,?)",id,required(b,"vendor"),required(b,"coveredAsset"),Date.valueOf(required(b,"contractStart")),Date.valueOf(required(b,"contractExpiry")),amount(b.get("annualValue")),required(b,"serviceFrequency"),value(b,"vendorContact",null),required(b,"scope"),owner);return db.queryForMap("SELECT id,'AMC-'||LPAD(contract_no::text,3,'0') AS \"contractCode\",vendor_name AS vendor,covered_asset AS \"coveredAsset\",contract_start AS \"contractStart\",contract_expiry AS \"contractExpiry\",annual_value AS \"annualValue\",service_frequency AS \"serviceFrequency\",vendor_contact AS \"vendorContact\",scope,status FROM amc_contracts WHERE id=? AND owner_email=?",id,owner);}
    @PatchMapping("/{id}/status") public Map<String,Object> status(@RequestAttribute("authenticatedUserEmail") String owner,@PathVariable UUID id,@RequestBody Map<String,Object>b){db.update("UPDATE amc_contracts SET status=?,updated_at=now() WHERE id=? AND owner_email=?",required(b,"status"),id,owner);return db.queryForMap("SELECT id,'AMC-'||LPAD(contract_no::text,3,'0') AS \"contractCode\",vendor_name AS vendor,covered_asset AS \"coveredAsset\",contract_start AS \"contractStart\",contract_expiry AS \"contractExpiry\",annual_value AS \"annualValue\",service_frequency AS \"serviceFrequency\",vendor_contact AS \"vendorContact\",scope,status FROM amc_contracts WHERE id=? AND owner_email=?",id,owner);}
    @PatchMapping("/{id}/work-order") public Map<String,Object> linkWorkOrder(@RequestAttribute("authenticatedUserEmail") String owner,@PathVariable UUID id,@RequestBody Map<String,Object>b){db.update("UPDATE amc_contracts SET work_order_reference=? WHERE id=? AND owner_email=?",required(b,"workOrder"),id,owner);return db.queryForMap("SELECT id,work_order_reference AS \"workOrder\" FROM amc_contracts WHERE id=? AND owner_email=?",id,owner);}
    private static BigDecimal amount(Object v){return v==null||v.toString().isBlank()?BigDecimal.ZERO:new BigDecimal(v.toString());}
    private static String required(Map<String,Object>b,String k){String v=value(b,k,null);if(v==null||v.isBlank())throw new IllegalArgumentException(k+" is required");return v;}
    private static String value(Map<String,Object>b,String k,String d){Object v=b.get(k);return v==null||v.toString().isBlank()?d:v.toString().trim();}
}
