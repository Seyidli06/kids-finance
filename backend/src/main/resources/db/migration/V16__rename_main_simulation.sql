ALTER TABLE main_simulation_runs
    RENAME TO simulation_runs;


ALTER TABLE simulation_runs
    RENAME CONSTRAINT fk_main_simulation_runs_child
    TO fk_simulation_runs_child;


ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_start
    TO chk_simulation_start;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_allocations
    TO chk_simulation_allocations;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_allocation_total
    TO chk_simulation_allocation_total;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_event
    TO chk_simulation_event;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_ending
    TO chk_simulation_ending;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_xp
    TO chk_simulation_xp;


ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_saving_score
    TO chk_simulation_saving_score;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_budgeting_score
    TO chk_simulation_budgeting_score;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_risk_score
    TO chk_simulation_risk_score;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_investment_score
    TO chk_simulation_investment_score;

ALTER TABLE simulation_runs
    RENAME CONSTRAINT chk_main_simulation_decision_score
    TO chk_simulation_decision_score;


ALTER INDEX idx_main_simulation_runs_child
    RENAME TO idx_simulation_runs_child;