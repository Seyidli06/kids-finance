INSERT INTO wallets (
    child_profile_id,
    balance,
    currency
)
SELECT
    cp.id,
    1000.00,
    'AZN'
FROM child_profiles cp
WHERE NOT EXISTS (
    SELECT 1
    FROM wallets w
    WHERE w.child_profile_id = cp.id
);