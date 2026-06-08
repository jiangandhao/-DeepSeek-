-- 创建饮食记录表
CREATE TABLE IF NOT EXISTS diet_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(20) NOT NULL COMMENT 'breakfast:早餐, lunch:午餐, dinner:晚餐, snack:加餐',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
    food_amount DECIMAL(8,2) COMMENT '食物用量(g)',
    calorie DECIMAL(8,2) COMMENT '热量(kcal)',
    carbohydrate DECIMAL(8,2) COMMENT '碳水化合物(g)',
    protein DECIMAL(8,2) COMMENT '蛋白质(g)',
    fat DECIMAL(8,2) COMMENT '脂肪(g)',
    record_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建运动记录表
CREATE TABLE IF NOT EXISTS exercise_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL COMMENT '运动类型',
    duration INT COMMENT '运动时长(分钟)',
    calorie_consumed DECIMAL(8,2) COMMENT '消耗热量(kcal)',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    note TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(1, 'breakfast', 'oatmeal', 200, 150, 30, 5, 3, '2026-06-04'),
(1, 'lunch', 'brown rice', 150, 180, 35, 4, 2, '2026-06-04'),
(1, 'dinner', 'grilled chicken', 120, 200, 0, 25, 10, '2026-06-04');

INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(1, 'walking', 30, 150, '2026-06-04 07:00:00', '2026-06-04 07:30:00', 'morning walk'),
(1, 'running', 20, 200, '2026-06-04 18:00:00', '2026-06-04 18:20:00', 'evening run');
