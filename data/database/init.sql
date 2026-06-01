-- DeepSeek 健康管理系统 - 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS health_management
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE health_management;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    real_name VARCHAR(50),
    age INT,
    gender TINYINT DEFAULT 0 COMMENT '0:未知, 1:男, 2:女',
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康档案表
CREATE TABLE IF NOT EXISTS health_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    blood_type VARCHAR(5) COMMENT '血型',
    allergy_history TEXT COMMENT '过敏史',
    medical_history TEXT COMMENT '病史',
    family_disease_history TEXT COMMENT '家族病史',
    birthday DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 血糖记录表
CREATE TABLE IF NOT EXISTS blood_sugar_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    value DECIMAL(4,1) NOT NULL COMMENT '血糖值(mmol/L)',
    type VARCHAR(20) NOT NULL COMMENT 'fasting:空腹, before:餐前, after:餐后',
    measure_time DATETIME NOT NULL COMMENT '测量时间',
    note TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_measure_time (measure_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 体检报告表
CREATE TABLE IF NOT EXISTS checkup_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_no VARCHAR(50) NOT NULL UNIQUE COMMENT '报告编号',
    checkup_date DATE NOT NULL COMMENT '体检日期',
    hospital_name VARCHAR(100) COMMENT '医院名称',
    report_url VARCHAR(255) COMMENT '报告文件URL',
    summary TEXT COMMENT '报告摘要',
    risk_level TINYINT DEFAULT 0 COMMENT '0:正常, 1:低风险, 2:中风险, 3:高风险',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_report_no (report_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康风险评估表
CREATE TABLE IF NOT EXISTS risk_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    risk_type VARCHAR(50) NOT NULL COMMENT '风险类型',
    risk_level TINYINT NOT NULL COMMENT '1:低风险, 2:中风险, 3:高风险',
    risk_score DECIMAL(5,2) COMMENT '风险评分',
    description TEXT COMMENT '风险描述',
    suggestions TEXT COMMENT '建议措施',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_risk_type (risk_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 饮食记录表
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

-- 运动记录表
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

-- 体检预约表
CREATE TABLE IF NOT EXISTS checkup_appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    center_id BIGINT COMMENT '体检中心ID',
    center_name VARCHAR(100) NOT NULL COMMENT '体检中心名称',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    package_type VARCHAR(50) COMMENT '套餐类型',
    phone VARCHAR(20) COMMENT '联系电话',
    status VARCHAR(20) DEFAULT '已预约' COMMENT '预约状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_appointment_date (appointment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 疾病档案表
CREATE TABLE IF NOT EXISTS disease_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    disease_name VARCHAR(100) NOT NULL COMMENT '疾病名称',
    diagnose_date DATE COMMENT '确诊时间',
    control_level VARCHAR(20) COMMENT '控制水平: 控制良好/需关注/控制不佳',
    last_checkup DATE COMMENT '最近复查时间',
    next_checkup DATE COMMENT '下次复查时间',
    medications JSON COMMENT '用药信息',
    status TINYINT DEFAULT 1 COMMENT '0:已结束, 1:进行中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_disease_name (disease_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 疾病监测记录表
CREATE TABLE IF NOT EXISTS disease_monitor_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    disease_id BIGINT NOT NULL,
    indicator VARCHAR(100) NOT NULL COMMENT '监测指标',
    value VARCHAR(50) NOT NULL COMMENT '检测值',
    unit VARCHAR(20) COMMENT '单位',
    status VARCHAR(20) COMMENT '状态: 正常/偏高/偏低',
    record_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (disease_id) REFERENCES disease_records(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_disease_id (disease_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预警记录表
CREATE TABLE IF NOT EXISTS warning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    level VARCHAR(20) NOT NULL COMMENT '预警级别: critical/warning/info',
    title VARCHAR(200) NOT NULL COMMENT '预警标题',
    description TEXT COMMENT '预警描述',
    indicator VARCHAR(100) COMMENT '相关指标',
    current_value VARCHAR(50) COMMENT '当前数值',
    normal_range VARCHAR(50) COMMENT '正常范围',
    suggestions JSON COMMENT '处理建议',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_level (level),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预警设置表
CREATE TABLE IF NOT EXISTS alert_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    methods JSON COMMENT '预警方式: app/sms/email/phone',
    time_range_start TIME COMMENT '预警时段开始',
    time_range_end TIME COMMENT '预警时段结束',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 影像分析记录表
CREATE TABLE IF NOT EXISTS image_analysis_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    image_type VARCHAR(50) NOT NULL COMMENT '影像类型',
    image_url VARCHAR(255) COMMENT '影像文件URL',
    risk_level VARCHAR(20) COMMENT '风险等级',
    findings JSON COMMENT '检查发现',
    suggestion TEXT COMMENT 'AI建议',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_image_type (image_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT INTO users (username, password, phone, email, real_name, age, gender) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 'test@example.com', '测试用户', 35, 1);

INSERT INTO health_profiles (user_id, height, weight, blood_type, birthday) VALUES
(1, 175.00, 70.00, 'A', '1990-01-01');

INSERT INTO blood_sugar_records (user_id, value, type, measure_time) VALUES
(1, 5.8, 'fasting', '2024-01-15 07:00:00'),
(1, 7.2, 'after', '2024-01-15 09:00:00'),
(1, 5.6, 'fasting', '2024-01-16 07:00:00'),
(1, 7.8, 'after', '2024-01-16 09:00:00'),
(1, 6.0, 'fasting', '2024-01-17 07:00:00'),
(1, 7.5, 'after', '2024-01-17 09:00:00');

-- 疾病档案测试数据
INSERT INTO disease_records (user_id, disease_name, diagnose_date, control_level, last_checkup, next_checkup, medications) VALUES
(1, '2型糖尿病', '2022-05-15', '控制良好', '2024-01-10', '2024-04-10', '[{"name":"二甲双胍","dosage":"500mg","frequency":"每日两次","time":"早晚餐后","note":"随餐服用"}]'),
(1, '高血压', '2023-08-20', '需关注', '2024-01-05', '2024-03-05', '[{"name":"氨氯地平","dosage":"5mg","frequency":"每日一次","time":"早晨","note":"不可突然停药"}]');

-- 预警记录测试数据
INSERT INTO warning_records (user_id, level, title, description, indicator, current_value, normal_range, is_read) VALUES
(1, 'critical', '血糖异常升高', '您最近一次空腹血糖检测值为 8.5mmol/L，远超正常范围（3.9-6.1mmol/L）', '空腹血糖', '8.5mmol/L', '3.9-6.1mmol/L', 0),
(1, 'warning', '血压波动较大', '您近一周血压波动较大，收缩压在125-145mmHg之间', '收缩压', '145mmHg', '90-139mmHg', 0),
(1, 'info', '复查提醒', '距离您上次甲状腺检查已过6个月，建议进行复查', '甲状腺B超', '-', '-', 1);
