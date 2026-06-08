-- 24岁正常男生模拟数据脚本
-- 用于展示健康管理系统各项功能

USE health_management;

-- ============================================
-- 1. 用户数据（24岁，男性）
-- ============================================
INSERT INTO users (username, password, phone, email, real_name, age, gender, avatar, status) VALUES
('young_male', '123456', '13900139000', 'young@example.com', '张小明', 24, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=young_male', 1);

-- 获取刚插入的用户ID（假设为2）
SET @user_id = LAST_INSERT_ID();

-- ============================================
-- 2. 健康档案（正常BMI）
-- ============================================
INSERT INTO health_profiles (user_id, height, weight, blood_type, allergy_history, medical_history, family_disease_history, birthday) VALUES
(@user_id, 175.00, 70.00, 'O', '无', '无', '无', '2002-03-15');

-- ============================================
-- 3. 七天血糖记录（正常范围）
-- ============================================
-- 空腹血糖正常范围：3.9-6.1 mmol/L
-- 餐后2小时血糖正常范围：<7.8 mmol/L

-- 第1天（2024-01-15）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 5.2, 'fasting', '2024-01-15 07:30:00', '早起空腹'),
(@user_id, 6.8, 'after', '2024-01-15 09:30:00', '早餐后2小时'),
(@user_id, 5.4, 'fasting', '2024-01-15 12:00:00', '午餐前'),
(@user_id, 7.2, 'after', '2024-01-15 14:00:00', '午餐后2小时'),
(@user_id, 5.6, 'fasting', '2024-01-15 18:00:00', '晚餐前'),
(@user_id, 7.0, 'after', '2024-01-15 20:00:00', '晚餐后2小时');

-- 第2天（2024-01-16）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 5.0, 'fasting', '2024-01-16 07:15:00', '早起空腹'),
(@user_id, 6.5, 'after', '2024-01-16 09:15:00', '早餐后2小时'),
(@user_id, 5.3, 'fasting', '2024-01-16 12:30:00', '午餐前'),
(@user_id, 7.5, 'after', '2024-01-16 14:30:00', '午餐后2小时'),
(@user_id, 5.5, 'fasting', '2024-01-16 18:30:00', '晚餐前'),
(@user_id, 7.3, 'after', '2024-01-16 20:30:00', '晚餐后2小时');

-- 第3天（2024-01-17）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 4.9, 'fasting', '2024-01-17 07:00:00', '早起空腹'),
(@user_id, 6.2, 'after', '2024-01-17 09:00:00', '早餐后2小时'),
(@user_id, 5.1, 'fasting', '2024-01-17 12:00:00', '午餐前'),
(@user_id, 6.9, 'after', '2024-01-17 14:00:00', '午餐后2小时'),
(@user_id, 5.4, 'fasting', '2024-01-17 18:00:00', '晚餐前'),
(@user_id, 7.1, 'after', '2024-01-17 20:00:00', '晚餐后2小时');

-- 第4天（2024-01-18）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 5.3, 'fasting', '2024-01-18 07:20:00', '早起空腹'),
(@user_id, 6.7, 'after', '2024-01-18 09:20:00', '早餐后2小时'),
(@user_id, 5.2, 'fasting', '2024-01-18 12:15:00', '午餐前'),
(@user_id, 7.4, 'after', '2024-01-18 14:15:00', '午餐后2小时'),
(@user_id, 5.7, 'fasting', '2024-01-18 18:45:00', '晚餐前'),
(@user_id, 7.6, 'after', '2024-01-18 20:45:00', '晚餐后2小时');

-- 第5天（2024-01-19）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 5.1, 'fasting', '2024-01-19 07:10:00', '早起空腹'),
(@user_id, 6.4, 'after', '2024-01-19 09:10:00', '早餐后2小时'),
(@user_id, 5.0, 'fasting', '2024-01-19 12:00:00', '午餐前'),
(@user_id, 6.8, 'after', '2024-01-19 14:00:00', '午餐后2小时'),
(@user_id, 5.3, 'fasting', '2024-01-19 18:30:00', '晚餐前'),
(@user_id, 7.0, 'after', '2024-01-19 20:30:00', '晚餐后2小时');

-- 第6天（2024-01-20）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 5.4, 'fasting', '2024-01-20 07:25:00', '早起空腹'),
(@user_id, 6.9, 'after', '2024-01-20 09:25:00', '早餐后2小时'),
(@user_id, 5.5, 'fasting', '2024-01-20 12:30:00', '午餐前'),
(@user_id, 7.7, 'after', '2024-01-20 14:30:00', '午餐后2小时'),
(@user_id, 5.8, 'fasting', '2024-01-20 18:15:00', '晚餐前'),
(@user_id, 7.5, 'after', '2024-01-20 20:15:00', '晚餐后2小时');

-- 第7天（2024-01-21）
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(@user_id, 5.2, 'fasting', '2024-01-21 07:05:00', '早起空腹'),
(@user_id, 6.6, 'after', '2024-01-21 09:05:00', '早餐后2小时'),
(@user_id, 5.3, 'fasting', '2024-01-21 12:00:00', '午餐前'),
(@user_id, 7.2, 'after', '2024-01-21 14:00:00', '午餐后2小时'),
(@user_id, 5.6, 'fasting', '2024-01-21 18:00:00', '晚餐前'),
(@user_id, 7.4, 'after', '2024-01-21 20:00:00', '晚餐后2小时');

-- ============================================
-- 4. 七天饮食记录
-- ============================================

-- 第1天（2024-01-15）- 早餐
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '全麦面包', 150, 350, 45, 12, 8, '2024-01-15'),
(@user_id, 'breakfast', '鸡蛋', 100, 155, 1.1, 13, 11, '2024-01-15'),
(@user_id, 'breakfast', '牛奶', 250, 135, 12, 8, 8, '2024-01-15'),
(@user_id, 'lunch', '米饭', 200, 230, 50, 4, 0.5, '2024-01-15'),
(@user_id, 'lunch', '清蒸鱼', 150, 180, 0, 25, 8, '2024-01-15'),
(@user_id, 'lunch', '炒青菜', 200, 80, 8, 4, 4, '2024-01-15'),
(@user_id, 'dinner', '面条', 200, 280, 55, 10, 2, '2024-01-15'),
(@user_id, 'dinner', '红烧肉', 100, 250, 5, 15, 20, '2024-01-15'),
(@user_id, 'dinner', '凉拌黄瓜', 150, 45, 8, 2, 1, '2024-01-15');

-- 第2天（2024-01-16）
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '燕麦粥', 200, 180, 30, 6, 4, '2024-01-16'),
(@user_id, 'breakfast', '香蕉', 120, 105, 27, 1.3, 0.4, '2024-01-16'),
(@user_id, 'breakfast', '酸奶', 200, 140, 20, 7, 4, '2024-01-16'),
(@user_id, 'lunch', '糙米饭', 200, 220, 45, 5, 1, '2024-01-16'),
(@user_id, 'lunch', '宫保鸡丁', 180, 280, 15, 20, 18, '2024-01-16'),
(@user_id, 'lunch', '西红柿蛋汤', 200, 80, 8, 5, 3, '2024-01-16'),
(@user_id, 'dinner', '馒头', 150, 330, 65, 10, 2, '2024-01-16'),
(@user_id, 'dinner', '清炒虾仁', 150, 180, 5, 25, 8, '2024-01-16'),
(@user_id, 'dinner', '蒜蓉西兰花', 200, 70, 8, 5, 3, '2024-01-16');

-- 第3天（2024-01-17）
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '包子', 200, 400, 60, 12, 12, '2024-01-17'),
(@user_id, 'breakfast', '豆浆', 300, 120, 15, 8, 4, '2024-01-17'),
(@user_id, 'lunch', '米饭', 200, 230, 50, 4, 0.5, '2024-01-17'),
(@user_id, 'lunch', '红烧排骨', 150, 350, 10, 25, 25, '2024-01-17'),
(@user_id, 'lunch', '清炒时蔬', 200, 60, 6, 3, 3, '2024-01-17'),
(@user_id, 'dinner', '小米粥', 250, 120, 25, 3, 1, '2024-01-17'),
(@user_id, 'dinner', '蒸蛋', 150, 120, 2, 10, 8, '2024-01-17'),
(@user_id, 'dinner', '凉拌木耳', 150, 50, 8, 3, 1, '2024-01-17');

-- 第4天（2024-01-18）
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '三明治', 200, 350, 35, 15, 18, '2024-01-18'),
(@user_id, 'breakfast', '橙汁', 250, 110, 26, 1.5, 0.5, '2024-01-18'),
(@user_id, 'lunch', '米饭', 200, 230, 50, 4, 0.5, '2024-01-18'),
(@user_id, 'lunch', '鱼香肉丝', 180, 280, 15, 18, 18, '2024-01-18'),
(@user_id, 'lunch', '紫菜蛋花汤', 200, 60, 5, 5, 3, '2024-01-18'),
(@user_id, 'dinner', '饺子', 250, 400, 50, 18, 15, '2024-01-18'),
(@user_id, 'dinner', '醋溜白菜', 200, 60, 8, 2, 3, '2024-01-18');

-- 第5天（2024-01-19）
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '煎蛋', 100, 155, 1.1, 13, 11, '2024-01-19'),
(@user_id, 'breakfast', '吐司', 100, 250, 45, 8, 4, '2024-01-19'),
(@user_id, 'breakfast', '牛奶', 250, 135, 12, 8, 8, '2024-01-19'),
(@user_id, 'lunch', '面条', 200, 280, 55, 10, 2, '2024-01-19'),
(@user_id, 'lunch', '炸鸡腿', 150, 350, 15, 25, 22, '2024-01-19'),
(@user_id, 'lunch', '蔬菜沙拉', 200, 80, 10, 3, 4, '2024-01-19'),
(@user_id, 'dinner', '米饭', 200, 230, 50, 4, 0.5, '2024-01-19'),
(@user_id, 'dinner', '清蒸鲈鱼', 200, 200, 0, 30, 8, '2024-01-19'),
(@user_id, 'dinner', '炒豆角', 200, 70, 10, 4, 2, '2024-01-19');

-- 第6天（2024-01-20）
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '油条', 150, 400, 45, 10, 22, '2024-01-20'),
(@user_id, 'breakfast', '豆腐脑', 250, 80, 8, 6, 3, '2024-01-20'),
(@user_id, 'lunch', '米饭', 200, 230, 50, 4, 0.5, '2024-01-20'),
(@user_id, 'lunch', '糖醋里脊', 150, 320, 25, 18, 18, '2024-01-20'),
(@user_id, 'lunch', '香菇青菜', 200, 60, 6, 4, 3, '2024-01-20'),
(@user_id, 'dinner', '馄饨', 200, 250, 35, 12, 8, '2024-01-20'),
(@user_id, 'dinner', '凉拌海带', 150, 40, 8, 2, 1, '2024-01-20');

-- 第7天（2024-01-21）
INSERT INTO diet_records (user_id, meal_type, food_name, food_amount, calorie, carbohydrate, protein, fat, record_date) VALUES
(@user_id, 'breakfast', '鸡蛋灌饼', 200, 350, 40, 12, 15, '2024-01-21'),
(@user_id, 'breakfast', '豆浆', 300, 120, 15, 8, 4, '2024-01-21'),
(@user_id, 'lunch', '米饭', 200, 230, 50, 4, 0.5, '2024-01-21'),
(@user_id, 'lunch', '回锅肉', 150, 300, 10, 18, 22, '2024-01-21'),
(@user_id, 'lunch', '番茄蛋汤', 200, 80, 8, 5, 3, '2024-01-21'),
(@user_id, 'dinner', '炒饭', 250, 350, 55, 10, 12, '2024-01-21'),
(@user_id, 'dinner', '蒜蓉菠菜', 200, 50, 6, 3, 2, '2024-01-21');

-- ============================================
-- 5. 七天运动记录
-- ============================================

-- 第1天（2024-01-15）- 晨跑
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '晨跑', 30, 350, '2024-01-15 06:30:00', '2024-01-15 07:00:00', '公园慢跑，感觉良好');

-- 第2天（2024-01-16）- 健身
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '力量训练', 45, 280, '2024-01-16 18:00:00', '2024-01-16 18:45:00', '上肢训练为主');

-- 第3天（2024-01-17）- 游泳
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '游泳', 40, 400, '2024-01-17 17:30:00', '2024-01-17 18:10:00', '自由泳为主');

-- 第4天（2024-01-18）- 骑行
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '骑行', 60, 450, '2024-01-18 16:00:00', '2024-01-18 17:00:00', '城市骑行，路况良好');

-- 第5天（2024-01-19）- 瑜伽
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '瑜伽', 50, 200, '2024-01-19 19:00:00', '2024-01-19 19:50:00', '放松身心，缓解压力');

-- 第6天（2024-01-20）- 篮球
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '篮球', 90, 600, '2024-01-20 15:00:00', '2024-01-20 16:30:00', '和朋友打球，运动量较大');

-- 第7天（2024-01-21）- 散步
INSERT INTO exercise_records (user_id, exercise_type, duration, calorie_consumed, start_time, end_time, note) VALUES
(@user_id, '散步', 45, 150, '2024-01-21 19:30:00', '2024-01-21 20:15:00', '饭后散步，放松心情');

-- ============================================
-- 6. 血压记录（正常范围）
-- ============================================
-- 正常血压：收缩压 90-140 mmHg，舒张压 60-90 mmHg
INSERT INTO blood_pressure_records (user_id, systolic, diastolic, heart_rate, measure_time, note) VALUES
(@user_id, 118, 75, 68, '2024-01-15 08:00:00', '晨起测量'),
(@user_id, 122, 78, 72, '2024-01-16 08:15:00', '晨起测量'),
(@user_id, 115, 72, 65, '2024-01-17 07:45:00', '晨起测量'),
(@user_id, 120, 76, 70, '2024-01-18 08:00:00', '晨起测量'),
(@user_id, 118, 74, 68, '2024-01-19 08:30:00', '晨起测量'),
(@user_id, 125, 80, 75, '2024-01-20 08:00:00', '运动后测量'),
(@user_id, 116, 73, 66, '2024-01-21 08:00:00', '晨起测量');

-- ============================================
-- 7. 预警阈值设置
-- ============================================
INSERT INTO warning_thresholds (user_id, threshold_type, threshold_value, unit) VALUES
(@user_id, 'blood_sugar_high', 7.0, 'mmol/L'),
(@user_id, 'blood_sugar_low', 3.9, 'mmol/L'),
(@user_id, 'blood_pressure_high', 140, 'mmHg'),
(@user_id, 'heart_rate_high', 100, 'bpm'),
(@user_id, 'bmi_high', 24, '');

-- ============================================
-- 8. 健康目标
-- ============================================
INSERT INTO health_goals (user_id, goal_name, target_value, current_value, progress, status) VALUES
(@user_id, '保持健康体重', 22.0, 22.86, 85, 1),
(@user_id, '每周运动3次', 3.0, 2.0, 67, 1),
(@user_id, '血糖稳定', 6.1, 5.2, 95, 1),
(@user_id, '血压正常', 130.0, 118.0, 100, 2);

-- ============================================
-- 9. 预警记录（少量，因为是健康状态）
-- ============================================
INSERT INTO warning_records (user_id, warning_type, warning_level, title, content, is_read) VALUES
(@user_id, 'exercise', 'low', '运动量提醒', '本周运动次数不足3次，建议增加运动量', 0),
(@user_id, 'diet', 'low', '饮食均衡提醒', '今日蛋白质摄入略低，建议增加优质蛋白', 1);

-- ============================================
-- 10. 体检报告（正常）
-- ============================================
INSERT INTO checkup_reports (user_id, report_no, checkup_date, hospital_name, summary, risk_level) VALUES
(@user_id, 'RPT20240115001', '2024-01-15', '市第一人民医院', '体检结果正常，各项指标均在正常范围内。建议保持健康生活方式，定期体检。', 0);

-- ============================================
-- 11. 风险评估（低风险）
-- ============================================
INSERT INTO risk_assessments (user_id, risk_type, risk_level, risk_score, description, suggestions) VALUES
(@user_id, '糖尿病', 1, 15.5, '年龄较轻，BMI正常，无家族史，糖尿病风险较低', '保持健康饮食，定期运动，每年体检'),
(@user_id, '心血管疾病', 1, 12.3, '血压正常，血脂正常，无不良生活习惯，心血管风险较低', '戒烟限酒，保持规律运动，控制体重'),
(@user_id, '肥胖', 1, 8.7, 'BMI 22.86，处于正常范围，肥胖风险较低', '保持当前饮食和运动习惯');

-- 完成
SELECT '24岁正常男生模拟数据插入完成！' AS message;