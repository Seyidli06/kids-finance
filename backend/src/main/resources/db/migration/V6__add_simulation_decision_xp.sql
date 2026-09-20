ALTER TABLE simulation_decisions
    ADD COLUMN xp_earned INTEGER NOT NULL DEFAULT 0;

ALTER TABLE simulation_decisions
    ADD CONSTRAINT chk_simulation_decisions_xp_earned
        CHECK (xp_earned >= 0);