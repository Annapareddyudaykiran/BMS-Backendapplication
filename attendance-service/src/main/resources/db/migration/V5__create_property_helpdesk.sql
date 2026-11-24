CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(160) NOT NULL,
    city VARCHAR(100) NOT NULL, state VARCHAR(100) NOT NULL, property_type VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE property_towers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), property_id UUID NOT NULL REFERENCES properties(id),
    name VARCHAR(80) NOT NULL, floor_count INTEGER NOT NULL CHECK (floor_count > 0),
    UNIQUE(property_id, name)
);
CREATE TABLE property_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tower_id UUID NOT NULL REFERENCES property_towers(id),
    floor_number INTEGER NOT NULL CHECK (floor_number > 0), unit_number VARCHAR(40) NOT NULL,
    space_type VARCHAR(40) NOT NULL, occupancy VARCHAR(20) NOT NULL DEFAULT 'Vacant', occupant VARCHAR(160),
    UNIQUE(tower_id, unit_number)
);
CREATE TABLE helpdesk_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), ticket_number BIGSERIAL UNIQUE,
    description VARCHAR(1000) NOT NULL, tower_id UUID NOT NULL REFERENCES property_towers(id),
    floor_area VARCHAR(100) NOT NULL, unit_id UUID REFERENCES property_units(id),
    category VARCHAR(60) NOT NULL, priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Open', created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE work_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), ticket_id UUID NOT NULL UNIQUE REFERENCES helpdesk_tickets(id),
    owner_worker_id UUID REFERENCES attendance_workers(id), status VARCHAR(24) NOT NULL DEFAULT 'Unassigned',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO properties(name,city,state,property_type) VALUES ('Lakeview Tech & Residences','Hyderabad','Telangana','Mixed-use');
