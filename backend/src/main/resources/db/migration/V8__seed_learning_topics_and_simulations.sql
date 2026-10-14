INSERT INTO learning_topics (
    code,
    title,
    description,
    display_order
)
VALUES
    (
        'SAVING',
        'Saving',
        'Saving money and building financial discipline',
        1
    ),
    (
        'BUDGETING',
        'Budgeting',
        'Planning income and expenses',
        2
    ),
    (
        'SPENDING',
        'Spending Decisions',
        'Making responsible spending decisions',
        3
    ),
    (
        'INVESTING',
        'Investing Basics',
        'Understanding basic investment concepts',
        4
    );


INSERT INTO simulation_scenarios (
    code,
    learning_topic_id,
    title,
    description,
    display_order
)
SELECT
    'SAVE_OR_SPEND_01',
    id,
    'İndi alım, yoxsa pulumu saxlayım?',
    '150 AZN-lik qulaqlıq görmüsən. Onu indi almaq və ya pulunu saxlamaq arasında seçim et.',
    1
FROM learning_topics
WHERE code = 'SPENDING';


INSERT INTO simulation_scenarios (
    code,
    learning_topic_id,
    title,
    description,
    display_order
)
SELECT
    'PLAN_PURCHASE_02',
    id,
    'Bu gün hansını alım?',
    'Məktəb çantan köhnəlib. Yeni çanta 80 AZN, bəyəndiyin oyun aksesuarı isə 120 AZN-dir. Bu gün yalnız birini almağı seçirsən.',
    2
FROM learning_topics
WHERE code = 'SPENDING';


INSERT INTO simulation_options (
    scenario_id,
    code,
    title,
    description,
    cost,
    display_order
)
SELECT
    id,
    'BUY_HEADPHONES',
    'Qulaqlığı al',
    'İndi 150 AZN xərcləyərək qulaqlığı alırsan.',
    150.00,
    1
FROM simulation_scenarios
WHERE code = 'SAVE_OR_SPEND_01';


INSERT INTO simulation_options (
    scenario_id,
    code,
    title,
    description,
    cost,
    display_order
)
SELECT
    id,
    'KEEP_SAVINGS',
    'Pulunu saxla',
    'Qulaqlığı indi almırsan və pulunu gələcək məqsədin üçün saxlayırsan.',
    0.00,
    2
FROM simulation_scenarios
WHERE code = 'SAVE_OR_SPEND_01';


INSERT INTO simulation_options (
    scenario_id,
    code,
    title,
    description,
    cost,
    display_order
)
SELECT
    id,
    'BUY_SCHOOL_BAG',
    'Məktəb çantasını al',
    '80 AZN xərcləyərək yeni məktəb çantası alırsan.',
    80.00,
    1
FROM simulation_scenarios
WHERE code = 'PLAN_PURCHASE_02';


INSERT INTO simulation_options (
    scenario_id,
    code,
    title,
    description,
    cost,
    display_order
)
SELECT
    id,
    'BUY_GAME_ACCESSORY',
    'Oyun aksesuarını al',
    '120 AZN xərcləyərək oyun aksesuarı alırsan.',
    120.00,
    2
FROM simulation_scenarios
WHERE code = 'PLAN_PURCHASE_02';