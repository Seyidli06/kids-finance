INSERT INTO challenges (
    code,
    learning_topic_id,
    title,
    prompt,
    explanation,
    xp_reward,
    display_order
)
SELECT
    'SAVING_01',
    id,
    'Save First',
    '100 AZN pulun var və gələcək üçün 20 AZN yığmaq istəyirsən. Ən düzgün yanaşma hansıdır?',
    'Yığımı əvvəlcədən ayırmaq pulun hamısını təsadüfi xərcləmək riskini azaldır.',
    10,
    1
FROM learning_topics
WHERE code = 'SAVING';


INSERT INTO challenges (
    code,
    learning_topic_id,
    title,
    prompt,
    explanation,
    xp_reward,
    display_order
)
SELECT
    'BUDGETING_01',
    id,
    'Plan Your Budget',
    '100 AZN büdcənin 60 AZN-i ehtiyaclara, 20 AZN-i isə yığıma ayrılıb. Digər xərclər üçün maksimum nə qədər qalır?',
    '100 - 60 - 20 = 20 AZN.',
    10,
    2
FROM learning_topics
WHERE code = 'BUDGETING';


INSERT INTO challenges (
    code,
    learning_topic_id,
    title,
    prompt,
    explanation,
    xp_reward,
    display_order
)
SELECT
    'SPENDING_01',
    id,
    'Need or Want?',
    'Məktəb üçün dəftərə ehtiyacın var, eyni zamanda yeni oyun aksesuarı istəyirsən. Büdcən yalnız birinə çatır. Hansını prioritet seçmək daha düzgündür?',
    'Vacib ehtiyaclar adətən istəklərdən əvvəl prioritetləşdirilməlidir.',
    10,
    3
FROM learning_topics
WHERE code = 'SPENDING';


INSERT INTO challenges (
    code,
    learning_topic_id,
    title,
    prompt,
    explanation,
    xp_reward,
    display_order
)
SELECT
    'INVESTING_01',
    id,
    'Risk and Return',
    'İnvestisiya barədə hansı fikir daha düzgündür?',
    'İnvestisiyada gəlir zəmanətli deyil. Potensial gəlirlə yanaşı risk də nəzərə alınmalıdır.',
    10,
    4
FROM learning_topics
WHERE code = 'INVESTING';


INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'SAVE_FIRST',
    'Əvvəl 20 AZN-i yığım üçün ayırmaq',
    TRUE,
    1
FROM challenges
WHERE code = 'SAVING_01';

INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'SPEND_FIRST',
    'Əvvəl istədiyim qədər xərcləyib qalanını yığmaq',
    FALSE,
    2
FROM challenges
WHERE code = 'SAVING_01';


INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'REMAINING_20',
    '20 AZN',
    TRUE,
    1
FROM challenges
WHERE code = 'BUDGETING_01';

INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'REMAINING_40',
    '40 AZN',
    FALSE,
    2
FROM challenges
WHERE code = 'BUDGETING_01';

INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'REMAINING_60',
    '60 AZN',
    FALSE,
    3
FROM challenges
WHERE code = 'BUDGETING_01';


INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'BUY_NOTEBOOK',
    'Məktəb üçün dəftəri almaq',
    TRUE,
    1
FROM challenges
WHERE code = 'SPENDING_01';

INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'BUY_GAME_ACCESSORY',
    'Oyun aksesuarını almaq',
    FALSE,
    2
FROM challenges
WHERE code = 'SPENDING_01';


INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'RISK_EXISTS',
    'Potensial gəlirlə yanaşı pul itirmək riski də ola bilər',
    TRUE,
    1
FROM challenges
WHERE code = 'INVESTING_01';

INSERT INTO challenge_options (
    challenge_id,
    code,
    text,
    correct,
    display_order
)
SELECT
    id,
    'PROFIT_GUARANTEED',
    'İnvestisiyada qazanc həmişə zəmanətlidir',
    FALSE,
    2
FROM challenges
WHERE code = 'INVESTING_01';