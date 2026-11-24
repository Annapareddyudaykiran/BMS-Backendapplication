ALTER TABLE inventory_items
  ADD COLUMN name VARCHAR(180), ADD COLUMN category VARCHAR(80),
  ADD COLUMN quantity NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN minimum NUMERIC(14,2) NOT NULL DEFAULT 0, ADD COLUMN unit VARCHAR(32);
UPDATE inventory_items SET name=payload->>'name', category=payload->>'category',
  quantity=COALESCE(NULLIF(payload->>'quantity','')::numeric,0),
  minimum=COALESCE(NULLIF(payload->>'minimum','')::numeric,0), unit=payload->>'unit';
ALTER TABLE inventory_items DROP COLUMN payload;

ALTER TABLE purchase_requests
  ADD COLUMN item VARCHAR(180), ADD COLUMN quantity NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN supplier VARCHAR(180), ADD COLUMN unit_cost NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN required_by DATE, ADD COLUMN reason TEXT,
  ADD COLUMN status VARCHAR(40) NOT NULL DEFAULT 'Awaiting approval';
UPDATE purchase_requests SET item=payload->>'item',
  quantity=COALESCE(NULLIF(payload->>'quantity','')::numeric,0), supplier=payload->>'supplier',
  unit_cost=COALESCE(NULLIF(payload->>'unitCost','')::numeric,0),
  required_by=NULLIF(payload->>'requiredBy','')::date, reason=payload->>'reason',
  status=COALESCE(payload->>'status','Awaiting approval');
ALTER TABLE purchase_requests DROP COLUMN payload;
