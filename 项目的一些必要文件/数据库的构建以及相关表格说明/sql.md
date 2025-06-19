```angular2html

CREATE DATABASE IF NOT EXISTS yyzx  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yyzx ;
-- ==========================================
-- 东软颐养中心数据库创建脚本
-- ==========================================



-- ==========================================
-- 1. 角色表 (roles)
-- ==========================================
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码',
    description TEXT COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. 菜单表 (menus)
-- ==========================================
DROP TABLE IF EXISTS menus;
CREATE TABLE menus (
    id INT PRIMARY KEY AUTO_INCREMENT,
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(20) NOT NULL UNIQUE COMMENT '菜单编号',
    menu_path VARCHAR(100) COMMENT '菜单路径',
    menu_level INT DEFAULT 1 COMMENT '菜单层级',
    parent_id INT DEFAULT 0 COMMENT '父菜单ID',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. 用户表 (users)
-- ==========================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(15) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    role_id INT NOT NULL COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ==========================================
-- 4. 房间表 (rooms)
-- ==========================================
DROP TABLE IF EXISTS rooms;
CREATE TABLE rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) NOT NULL UNIQUE COMMENT '房间号',
    floor_number INT NOT NULL COMMENT '楼层',
    room_type VARCHAR(20) DEFAULT '标准间' COMMENT '房间类型',
    max_beds INT DEFAULT 2 COMMENT '最大床位数',
    description TEXT COMMENT '房间描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. 床位表 (beds)
-- ==========================================
DROP TABLE IF EXISTS beds;
CREATE TABLE beds (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bed_number VARCHAR(20) NOT NULL UNIQUE COMMENT '床位编号',
    room_id INT NOT NULL COMMENT '房间ID',
    bed_type VARCHAR(20) DEFAULT '普通床' COMMENT '床位类型',
    status ENUM('空闲', '占用', '维修', '预留') DEFAULT '空闲' COMMENT '床位状态',
    daily_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '日费用',
    description TEXT COMMENT '床位描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- ==========================================
-- 6. 退住信息表 (checkout_info)
-- ==========================================
DROP TABLE IF EXISTS checkout_info;
CREATE TABLE checkout_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    checkout_type VARCHAR(20) NOT NULL COMMENT '退住类型',
    checkout_reason TEXT COMMENT '退住原因',
    checkout_date DATE NOT NULL COMMENT '退住日期',
    remarks TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 7. 客户表 (customers)
-- ==========================================
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    id_card VARCHAR(18) UNIQUE COMMENT '身份证号',
    gender ENUM('男', '女') COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(15) COMMENT '联系电话',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(15) COMMENT '紧急联系电话',
    address TEXT COMMENT '家庭住址',
    medical_history TEXT COMMENT '病史',
    allergies TEXT COMMENT '过敏史',
    bed_id INT COMMENT '床位ID',
    admission_date DATE COMMENT '入住日期',
    checkout_info_id INT COMMENT '退住信息ID',
    audit_status ENUM('待审核', '已通过', '已拒绝') DEFAULT '待审核' COMMENT '审核状态',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在住，0-已退住',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bed_id) REFERENCES beds(id),
    FOREIGN KEY (checkout_info_id) REFERENCES checkout_info(id)
);

-- ==========================================
-- 8. 护理协议表 (nursing_agreements)
-- ==========================================
DROP TABLE IF EXISTS nursing_agreements;
CREATE TABLE nursing_agreements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL COMMENT '客户ID',
    level_name VARCHAR(50) NOT NULL COMMENT '护理等级名称',
    level_code VARCHAR(20) NOT NULL COMMENT '护理等级编码',
    level_status ENUM('生效', '暂停', '终止') DEFAULT '生效' COMMENT '等级状态',
    monthly_fee DECIMAL(10,2) NOT NULL COMMENT '月费用',
    service_content TEXT COMMENT '服务内容',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- ==========================================
-- 9. 护理项目表 (nursing_projects)
-- ==========================================
DROP TABLE IF EXISTS nursing_projects;
CREATE TABLE nursing_projects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    project_category VARCHAR(50) COMMENT '项目分类',
    description TEXT COMMENT '项目描述',
    standard_duration INT COMMENT '标准时长(分钟)',
    caregiver_id INT COMMENT '护理人ID',
    frequency VARCHAR(20) COMMENT '执行频率',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (caregiver_id) REFERENCES users(id)
);

-- ==========================================
-- 10. 护理记录表 (nursing_records)
-- ==========================================
DROP TABLE IF EXISTS nursing_records;
CREATE TABLE nursing_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL COMMENT '客户ID',
    project_id INT NOT NULL COMMENT '护理项目ID',
    caregiver_id INT NOT NULL COMMENT '护理人ID',
    execution_date DATE NOT NULL COMMENT '执行日期',
    execution_time TIME COMMENT '执行时间',
    duration INT COMMENT '实际时长(分钟)',
    execution_status ENUM('已完成', '部分完成', '未执行', '异常') DEFAULT '已完成' COMMENT '执行状态',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (project_id) REFERENCES nursing_projects(id),
    FOREIGN KEY (caregiver_id) REFERENCES users(id)
);

-- ==========================================
-- 11. 食品表 (foods)
-- ==========================================
DROP TABLE IF EXISTS foods;
CREATE TABLE foods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    food_name VARCHAR(100) NOT NULL COMMENT '食品名称',
    food_category VARCHAR(50) COMMENT '食品分类',
    unit VARCHAR(10) DEFAULT '份' COMMENT '计量单位',
    calories_per_unit DECIMAL(8,2) COMMENT '单位热量(卡路里)',
    protein_content DECIMAL(8,2) COMMENT '蛋白质含量(g)',
    fat_content DECIMAL(8,2) COMMENT '脂肪含量(g)',
    carb_content DECIMAL(8,2) COMMENT '碳水化合物含量(g)',
    allergen_info TEXT COMMENT '过敏源信息',
    storage_requirements TEXT COMMENT '储存要求',
    status TINYINT DEFAULT 1 COMMENT '状态：1-可用，0-停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 12. 膳食日记表 (meal_diaries)
-- ==========================================
DROP TABLE IF EXISTS meal_diaries;
CREATE TABLE meal_diaries (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL COMMENT '客户ID',
    meal_date DATE NOT NULL COMMENT '用餐日期',
    meal_type ENUM('早餐', '午餐', '晚餐', '加餐') NOT NULL COMMENT '餐次',
    food_id INT NOT NULL COMMENT '食品ID',
    quantity DECIMAL(8,2) DEFAULT 1.00 COMMENT '食用份量',
    notes TEXT COMMENT '备注',
    caregiver_id INT COMMENT '记录人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (food_id) REFERENCES foods(id),
    FOREIGN KEY (caregiver_id) REFERENCES users(id)
);

-- ==========================================
-- 13. 外出信息表 (outing_records)
-- ==========================================
DROP TABLE IF EXISTS outing_records;
CREATE TABLE outing_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL COMMENT '客户ID',
    outing_date DATE NOT NULL COMMENT '外出日期',
    outing_time TIME NOT NULL COMMENT '外出时间',
    return_time TIME COMMENT '返回时间',
    destination VARCHAR(200) COMMENT '外出目的地',
    companion VARCHAR(100) COMMENT '陪同人员',
    reason TEXT COMMENT '外出原因',
    approval_status ENUM('待批准', '已批准', '已拒绝') DEFAULT '待批准' COMMENT '审批状态',
    actual_return_time TIME COMMENT '实际返回时间',
    notes TEXT COMMENT '备注',
    caregiver_id INT COMMENT '登记人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (caregiver_id) REFERENCES users(id)
);

-- ==========================================
-- 14. 角色菜单关联表 (role_menu_relations)
-- ==========================================
DROP TABLE IF EXISTS role_menu_relations;
CREATE TABLE role_menu_relations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL COMMENT '角色ID',
    menu_id INT NOT NULL COMMENT '菜单ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (menu_id) REFERENCES menus(id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
);

-- ==========================================
-- 15. 客户护理人员分配表 (customer_caregiver_assignments)
-- ==========================================
DROP TABLE IF EXISTS customer_caregiver_assignments;
CREATE TABLE customer_caregiver_assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL COMMENT '客户ID',
    caregiver_id INT NOT NULL COMMENT '护理人员ID',
    assignment_date DATE NOT NULL COMMENT '分配日期',
    end_date DATE COMMENT '结束日期',
    primary_caregiver TINYINT DEFAULT 0 COMMENT '是否主要护理人：1-是，0-否',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-无效',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (caregiver_id) REFERENCES users(id)
);

-- ==========================================
-- 数据插入部分
-- ==========================================

-- 1. 插入角色数据
INSERT INTO roles (role_name, role_code, description) VALUES
('系统管理员', 'ADMIN', '系统最高权限管理员，负责系统整体管理'),
('健康管家', 'CAREGIVER', '护理人员，负责客户日常护理和健康监护'),
('医护人员', 'NURSE', '专业医护人员，负责医疗护理');

-- 2. 插入菜单数据
INSERT INTO menus (menu_name, menu_code, menu_path, menu_level, parent_id, icon, sort_order) VALUES
('系统管理', 'SYSTEM', '/system', 1, 0, 'system', 1),
('用户管理', 'USER_MANAGE', '/system/users', 2, 1, 'user', 1),
('角色管理', 'ROLE_MANAGE', '/system/roles', 2, 1, 'role', 2),
('床位管理', 'BED_MANAGE', '/bed', 1, 0, 'bed', 2),
('床位示意图', 'BED_OVERVIEW', '/bed/overview', 2, 4, 'overview', 1),
('床位管理', 'BED_LIST', '/bed/list', 2, 4, 'list', 2),
('客户管理', 'CUSTOMER', '/customer', 1, 0, 'customer', 3),
('入住登记', 'CUSTOMER_CHECKIN', '/customer/checkin', 2, 7, 'checkin', 1),
('外出登记', 'CUSTOMER_OUTING', '/customer/outing', 2, 7, 'outing', 2),
('退住登记', 'CUSTOMER_CHECKOUT', '/customer/checkout', 2, 7, 'checkout', 3),
('护理管理', 'NURSING', '/nursing', 1, 0, 'nursing', 4),
('护理级别', 'NURSING_LEVEL', '/nursing/level', 2, 11, 'level', 1),
('护理项目', 'NURSING_PROJECT', '/nursing/project', 2, 11, 'project', 2),
('护理需求', 'NURSING_NEEDS', '/nursing/needs', 2, 11, 'needs', 3),
('护理记录', 'NURSING_RECORD', '/nursing/record', 2, 11, 'record', 4),
('健康管家', 'HEALTH_MANAGER', '/health', 1, 0, 'health', 5),
('服务对象', 'SERVICE_ASSIGNMENT', '/health/assignment', 2, 16, 'assignment', 1),
('服务关注', 'SERVICE_FOCUS', '/health/focus', 2, 16, 'focus', 2);

-- 3. 插入用户数据
INSERT INTO users (username, password, real_name, phone, email, role_id, last_login_time) VALUES
('admin', MD5('admin123'), '系统管理员', '13800138000', 'admin@nursing.com', 1, '2024-06-19 08:00:00'),
('zhang_nurse', MD5('zhang123'), '张护士', '13800138001', 'zhang@nursing.com', 2, '2024-06-19 07:30:00'),
('li_caregiver', MD5('li123'), '李护工', '13800138002', 'li@nursing.com', 2, '2024-06-19 07:45:00'),
('wang_caregiver', MD5('wang123'), '王护工', '13800138003', 'wang@nursing.com', 2, '2024-06-18 18:30:00'),
('zhao_nurse', MD5('zhao123'), '赵护士', '13800138004', 'zhao@nursing.com', 3, '2024-06-19 08:15:00'),
('qian_caregiver', MD5('qian123'), '钱护工', '13800138005', 'qian@nursing.com', 2, '2024-06-19 07:00:00');

-- 4. 插入房间数据
INSERT INTO rooms (room_number, floor_number, room_type, max_beds, description) VALUES
('A101', 1, '双人间', 2, '一楼普通双人间，朝南，采光好'),
('A102', 1, '双人间', 2, '一楼普通双人间，朝南'),
('A103', 1, '单人间', 1, '一楼单人间，适合特殊护理'),
('A104', 1, '双人间', 2, '一楼双人间，近护士站'),
('A105', 1, '三人间', 3, '一楼经济型三人间'),
('B201', 2, '双人间', 2, '二楼双人间，安静环境'),
('B202', 2, '三人间', 3, '二楼三人间，性价比高'),
('B203', 2, '双人间', 2, '二楼双人间，配套设施齐全'),
('B204', 2, '单人间', 1, '二楼高级单人间'),
('C301', 3, '双人间', 2, '三楼双人间，视野开阔');

-- 5. 插入床位数据
INSERT INTO beds (bed_number, room_id, bed_type, status, daily_price, description) VALUES
('A101-1', 1, '普通床', '占用', 150.00, '靠窗床位'),
('A101-2', 1, '普通床', '空闲', 150.00, '靠门床位'),
('A102-1', 2, '普通床', '占用', 150.00, '靠窗床位'),
('A102-2', 2, '普通床', '占用', 150.00, '靠门床位'),
('A103-1', 3, '电动床', '维修', 200.00, '电动调节护理床'),
('A104-1', 4, '普通床', '占用', 150.00, '靠窗床位'),
('A104-2', 4, '普通床', '预留', 150.00, '靠门床位'),
('A105-1', 5, '普通床', '占用', 120.00, '三人间床位1'),
('A105-2', 5, '普通床', '空闲', 120.00, '三人间床位2'),
('A105-3', 5, '普通床', '空闲', 120.00, '三人间床位3'),
('B201-1', 6, '普通床', '占用', 160.00, '二楼靠窗床位'),
('B201-2', 6, '普通床', '占用', 160.00, '二楼靠门床位'),
('B202-1', 7, '普通床', '占用', 130.00, '三人间床位1'),
('B202-2', 7, '普通床', '空闲', 130.00, '三人间床位2'),
('B202-3', 7, '普通床', '空闲', 130.00, '三人间床位3'),
('B203-1', 8, '护理床', '维修', 180.00, '专业护理床'),
('B203-2', 8, '普通床', '空闲', 160.00, '普通床位'),
('B204-1', 9, '电动床', '空闲', 220.00, '高级单人间电动床'),
('C301-1', 10, '普通床', '空闲', 170.00, '三楼靠窗床位'),
('C301-2', 10, '普通床', '空闲', 170.00, '三楼靠门床位');

-- 6. 插入客户数据
INSERT INTO customers (name, id_card, gender, birth_date, phone, emergency_contact, emergency_phone, address, medical_history, allergies, bed_id, admission_date, audit_status, status) VALUES
('张三', '110101195001011234', '男', '1950-01-01', '13901234567', '张小明', '13801234567', '北京市朝阳区某街道123号', '高血压、糖尿病', '青霉素过敏', 1, '2024-01-15', '已通过', 1),
('李四', '110101194005051234', '女', '1940-05-05', '13902234567', '李小红', '13802234567', '北京市海淀区某路456号', '冠心病、关节炎', '无', 3, '2024-02-20', '已通过', 1),
('王五', '110101193010101234', '男', '1930-10-10', '13903234567', '王小强', '13803234567', '北京市西城区某胡同789号', '脑梗、帕金森', '磺胺类药物过敏', 4, '2024-03-10', '已通过', 1),
('赵六', '110101195512121234', '女', '1955-12-12', '13904234567', '赵小华', '13804234567', '北京市东城区某街101号', '轻度认知障碍', '无', 8, '2024-04-05', '已通过', 1),
('钱七', '110101194808081234', '男', '1948-08-08', '13905234567', '钱小伟', '13805234567', '北京市丰台区某路202号', '慢性支气管炎', '无', 11, '2024-05-01', '已通过', 1),
('孙八', '110101196003031234', '女', '1960-03-03', '13906234567', '孙小丽', '13806234567', '北京市石景山区某街303号', '骨质疏松', '海鲜过敏', 12, '2024-05-15', '已通过', 1),
('周九', '110101195207071234', '男', '1952-07-07', '13907234567', '周小军', '13807234567', '北京市门头沟区某村404号', '轻度抑郁', '无', 13, '2024-06-01', '已通过', 1),
('吴十', '110101194411111234', '女', '1944-11-11', '13908234567', '吴小芳', '13808234567', '北京市房山区某镇505号', '风湿性关节炎', '阿司匹林过敏', NULL, '2024-06-18', '待审核', 0);

-- 7. 插入护理协议数据
INSERT INTO nursing_agreements (customer_id, level_name, level_code, monthly_fee, service_content, start_date) VALUES
(1, '二级护理', 'L002', 5000.00, '日常护理、健康监测、生活协助、血糖监测', '2024-01-15'),
(2, '一级护理', 'L001', 8000.00, '24小时专业护理、医疗监护、生活照料、康复训练', '2024-02-20'),
(3, '一级护理', 'L001', 8000.00, '24小时专业护理、认知训练、安全防护', '2024-03-10'),
(4, '三级护理', 'L003', 3000.00, '基础护理、生活照料、健康指导', '2024-04-05'),
(5, '二级护理', 'L002', 5000.00, '日常护理、呼吸道护理、康复训练', '2024-05-01'),
(6, '三级护理', 'L003', 3000.00, '基础护理、营养指导、心理关怀', '2024-05-15'),
(7, '二级护理', 'L002', 5000.00, '日常护理、心理疏导、社交活动', '2024-06-01');

-- 8. 插入护理项目数据
INSERT INTO nursing_projects (project_name, project_category, description, standard_duration, caregiver_id, frequency, status) VALUES
('生命体征监测', '健康监护', '测量血压、心率、体温、血氧饱和度', 15, 2, '每日3次', 1),
('血糖检测', '健康监护', '使用血糖仪测量血糖值', 10, 2, '每日2次', 1),
('协助用餐', '生活照料', '协助客户进食，监督营养摄入', 30, 3, '每日3次', 1),
('翻身拍背', '护理照料', '预防褥疮，促进血液循环', 10, 3, '每2小时1次', 1),
('康复训练', '功能训练', '肢体功能训练，语言康复训练', 45, 5, '每日1次', 1),
('个人卫生护理', '生活照料', '协助洗漱、更衣、清洁', 25, 4, '每日2次', 1),
('药物管理', '医疗护理', '按时给药，观察药物反应', 10, 2, '按医嘱', 1),
('心理疏导', '心理关怀', '倾听交流，缓解负面情绪', 20, 6, '每周3次', 1),
('呼吸道护理', '医疗护理', '吸痰、雾化治疗', 20, 5, '每日2次', 1),
('认知训练', '功能训练', '记忆力训练，思维能力训练', 30, 2, '每日1次', 1);

-- 9. 插入护理记录数据
INSERT INTO nursing_records (customer_id, project_id, caregiver_id, execution_date, execution_time, duration, execution_status, notes) VALUES
(1, 1, 2, '2024-06-19', '09:00:00', 15, '已完成', '血压130/85，心率72，体温36.5℃，血氧98%'),
(1, 2, 2, '2024-06-19', '14:00:00', 10, '已完成', '血糖值7.2mmol/L，偏高'),
(2, 1, 2, '2024-06-19', '09:30:00', 15, '已完成', '血压140/90，心率80，体温36.8℃'),
(2, 4, 3, '2024-06-19', '11:00:00', 10, '已完成', '翻身顺利，皮肤状况良好'),
(3, 1, 2, '2024-06-19', '10:00:00', 15, '已完成', '血压150/95，需要关注'),
(3, 10, 2, '2024-06-19', '15:00:00', 30, '已完成', '认知训练配合度一般'),
(4, 3, 3, '2024-06-19', '12:00:00', 25, '部分完成', '食欲不佳，只吃了一半'),
(5, 9, 5, '2024-06-19', '08:00:00', 20, '已完成', '雾化治疗完成，呼吸顺畅'),
(6, 8, 6, '2024-06-19', '16:00:00', 20, '已完成', '情绪稳定，交流良好'),
(7, 8, 6, '2024-06-19', '10:30:00', 20, '已完成', '心情较好，参与度高');

-- 10. 插入食品数据
INSERT INTO foods (food_name, food_category, unit, calories_per_unit, protein_content, fat_content, carb_content, allergen_info, storage_requirements) VALUES
('白粥', '主食', '碗', 120, 2.5, 0.3, 25.0, '无', '常温保存，及时食用'),
('蒸蛋羹', '蛋类', '份', 150, 12.0, 8.0, 2.0, '鸡蛋', '冷藏保存'),
('青菜汤', '蔬菜', '碗', 35, 2.0, 0.2, 6.0, '无', '现做现吃'),
('瘦肉粥', '主食', '碗', 180, 8.0, 3.0, 28.0, '无', '常温保存，及时食用'),
('红烧鱼', '荤菜', '份', 200, 18.0, 10.0, 5.0, '鱼类', '冷藏保存'),
('炒青菜', '蔬菜', '份', 45, 3.0, 2.0, 6.0, '无', '现做现吃'),
('小馒头', '主食', '个', 80, 2.8, 0.5, 16.0, '小麦', '常温保存'),
('银耳莲子汤', '汤品', '碗', 65, 1.5, 0.1, 15.0, '无', '冷藏保存'),
('蒸南瓜', '蔬菜', '份', 55, 1.0, 0.1, 13.0, '无', '常温保存'),
('鸡蛋面条', '主食', '碗', 220, 10.0, 4.0, 38.0, '鸡蛋、小麦', '现做现吃');

-- 11. 插入膳食日记数据
INSERT INTO meal_diaries (customer_id, meal_date, meal_type, food_id, quantity, notes, caregiver_id) VALUES
(1, '2024-06-19', '早餐', 1, 1.0, '食欲正常', 3),
(1, '2024-06-19', '早餐', 2, 1.0, '全部吃完', 3),
(1, '2024-06-19', '午餐', 4, 1.0, '少糖版本', 3),
(1, '2024-06-19', '午餐', 6, 1.0, '清淡口味', 3),
(2, '2024-06-19', '早餐', 1, 0.8, '食量减少', 3),
(2, '2024-06-19', '午餐', 5, 1.0, '喜欢吃鱼', 3),
(2, '2024-06-19', '晚餐', 8, 1.0, '睡前汤品', 4),
(3, '2024-06-19', '早餐', 7, 2.0, '胃口不错', 4),
(3, '2024-06-19', '午餐', 10, 1.0, '软烂易消化', 4),
(4, '2024-06-19', '早餐', 9, 1.0, '营养丰富', 3),
(4, '2024-06-19', '午餐', 3, 1.0, '清淡蔬菜', 3);

-- 12. 插入外出记录数据
INSERT INTO outing_records (customer_id, outing_date, outing_time, return_time, destination, companion, reason, approval_status, actual_return_time, caregiver_id) VALUES
(1, '2024-06-18', '09:00:00', '17:00:00', '北京协和医院', '张小明(儿子)', '复查糖尿病', '已批准', '16:30:00', 2),
(4, '2024-06-18', '14:00:00', '18:00:00', '家属住所', '赵小华(女儿)', '家庭聚餐', '已批准', '17:45:00', 3),
(5, '2024-06-19', '10:00:00', '16:00:00', '中山公园', '钱小伟(儿子)', '散步休闲', '已批准', NULL, 4),
(6, '2024-06-19', '08:30:00', '12:00:00', '附近超市', '孙小丽(女儿)', '购买日用品', '已批准', '11:30:00', 3),
(7, '2024-06-19', '15:00:00', '20:00:00', '朋友家', '周小军(儿子)', '探访老友', '待批准', NULL, 2);

-- 13. 插入退住信息数据
INSERT INTO checkout_info (checkout_type, checkout_reason, checkout_date, remarks) VALUES
('正常退住', '家属接回家中照料', '2024-06-15', '客户身体状况稳定，家属有能力照料'),
('医疗转院', '病情需要专科治疗', '2024-06-10', '转至专科医院继续治疗'),
('自愿退住', '客户要求回家', '2024-06-12', '客户坚持要求回家');

-- 14. 插入角色菜单关联数据
INSERT INTO role_menu_relations (role_id, menu_id) VALUES
-- 系统管理员拥有所有菜单权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18),
-- 健康管家拥有部分菜单权限
(2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17), (2, 18),
-- 医护人员拥有护理相关权限
(3, 11), (3, 12), (3, 13), (3, 14), (3, 15), (3, 16), (3, 17), (3, 18);

-- 15. 插入客户护理人员分配数据
INSERT INTO customer_caregiver_assignments (customer_id, caregiver_id, assignment_date, primary_caregiver, status) VALUES
(1, 2, '2024-01-15', 1, 1),  -- 张三 -> 张护士(主要)
(1, 3, '2024-01-15', 0, 1),  -- 张三 -> 李护工(辅助)
(2, 2, '2024-02-20', 1, 1),  -- 李四 -> 张护士(主要)
(2, 4, '2024-02-20', 0, 1),  -- 李四 -> 王护工(辅助)
(3, 5, '2024-03-10', 1, 1),  -- 王五 -> 赵护士(主要)
(3, 2, '2024-03-10', 0, 1),  -- 王五 -> 张护士(辅助)
(4, 3, '2024-04-05', 1, 1),  -- 赵六 -> 李护工(主要)
(4, 6, '2024-04-05', 0, 1),  -- 赵六 -> 钱护工(辅助)
(5, 5, '2024-05-01', 1, 1),  -- 钱七 -> 赵护士(主要)
(6, 6, '2024-05-15', 1, 1),  -- 孙八 -> 钱护工(主要)
(7, 6, '2024-06-01', 1, 1);  -- 周九 -> 钱护工(主要)

-- ==========================================
-- 创建索引以提高查询性能
-- ==========================================

-- 用户表索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role_id ON users(role_id);

-- 床位表索引
CREATE INDEX idx_beds_status ON beds(status);
CREATE INDEX idx_beds_room_id ON beds(room_id);

-- 客户表索引
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_bed_id ON customers(bed_id);
CREATE INDEX idx_customers_audit_status ON customers(audit_status);

-- 护理记录表索引
CREATE INDEX idx_nursing_records_customer_id ON nursing_records(customer_id);
CREATE INDEX idx_nursing_records_date ON nursing_records(execution_date);
CREATE INDEX idx_nursing_records_caregiver_id ON nursing_records(caregiver_id);

-- 外出记录表索引
CREATE INDEX idx_outing_records_customer_id ON outing_records(customer_id);
CREATE INDEX idx_outing_records_date ON outing_records(outing_date);

-- 膳食日记表索引
CREATE INDEX idx_meal_diaries_customer_id ON meal_diaries(customer_id);
CREATE INDEX idx_meal_diaries_date ON meal_diaries(meal_date);

-- ==========================================
-- 创建视图以便查询
-- ==========================================

-- 床位使用情况视图
CREATE VIEW view_bed_status AS
SELECT
    b.bed_number,
    r.room_number,
    r.floor_number,
    b.bed_type,
    b.status,
    c.name as customer_name,
    b.daily_price
FROM beds b
LEFT JOIN rooms r ON b.room_id = r.id
LEFT JOIN customers c ON b.id = c.bed_id AND c.status = 1;

-- 客户护理信息视图
CREATE VIEW view_customer_care AS
SELECT
    c.name as customer_name,
    c.gender,
    YEAR(CURDATE()) - YEAR(c.birth_date) as age,
    b.bed_number,
    na.level_name,
    na.monthly_fee,
    u.real_name as primary_caregiver
FROM customers c
LEFT JOIN beds b ON c.bed_id = b.id
LEFT JOIN nursing_agreements na ON c.id = na.customer_id AND na.level_status = '生效'
LEFT JOIN customer_caregiver_assignments cca ON c.id = cca.customer_id AND cca.primary_caregiver = 1 AND cca.status = 1
LEFT JOIN users u ON cca.caregiver_id = u.id
WHERE c.status = 1;

-- 护理工作统计视图
CREATE VIEW view_nursing_stats AS
SELECT
    u.real_name as caregiver_name,
    COUNT(nr.id) as total_records,
    SUM(CASE WHEN nr.execution_status = '已完成' THEN 1 ELSE 0 END) as completed_count,
    SUM(CASE WHEN nr.execution_status = '未执行' THEN 1 ELSE 0 END) as pending_count,
    DATE(nr.execution_date) as record_date
FROM nursing_records nr
JOIN users u ON nr.caregiver_id = u.id
WHERE nr.execution_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY u.id, DATE(nr.execution_date);

COMMIT;

```