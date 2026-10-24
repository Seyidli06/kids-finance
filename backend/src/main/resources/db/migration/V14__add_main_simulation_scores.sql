ALTER TABLE main_simulation_runs
    ADD COLUMN saving_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN budgeting_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN risk_management_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN investment_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN decision_making_score INTEGER NOT NULL DEFAULT 0;


ALTER TABLE main_simulation_runs
    ADD CONSTRAINT chk_main_simulation_saving_score
        CHECK (saving_score BETWEEN 0 AND 100),

    ADD CONSTRAINT chk_main_simulation_budgeting_score
        CHECK (budgeting_score BETWEEN 0 AND 100),

    ADD CONSTRAINT chk_main_simulation_risk_score
        CHECK (risk_management_score BETWEEN 0 AND 100),

    ADD CONSTRAINT chk_main_simulation_investment_score
        CHECK (investment_score BETWEEN 0 AND 100),

    ADD CONSTRAINT chk_main_simulation_decision_score
        CHECK (decision_making_score BETWEEN 0 AND 100);