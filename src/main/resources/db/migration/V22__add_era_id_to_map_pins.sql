ALTER TABLE map_pins
    ADD COLUMN era_id UUID REFERENCES eras(id);
