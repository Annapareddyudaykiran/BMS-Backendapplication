ALTER TABLE work_orders
  ADD COLUMN source VARCHAR(40),
  ADD COLUMN related_ticket VARCHAR(80),
  ADD COLUMN task TEXT,
  ADD COLUMN location VARCHAR(240),
  ADD COLUMN priority VARCHAR(20),
  ADD COLUMN trade VARCHAR(80),
  ADD COLUMN assigned_to VARCHAR(160),
  ADD COLUMN status VARCHAR(40) NOT NULL DEFAULT 'Awaiting assignment';
UPDATE work_orders SET
  source=payload->>'source', related_ticket=payload->>'relatedTicket', task=payload->>'task',
  location=payload->>'location', priority=payload->>'priority', trade=payload->>'trade',
  assigned_to=payload->>'assignedTo', status=COALESCE(payload->>'status','Awaiting assignment');
ALTER TABLE work_orders DROP COLUMN payload;
