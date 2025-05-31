-- 用户表 (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NULL UNIQUE COMMENT '用户名 (可选, 可用于登录)',
    password VARCHAR(100) NOT NULL COMMENT '密码 (存储哈希值)',
    phone VARCHAR(20) NOT NULL COMMENT '手机号 (唯一, 用于登录)',
    email VARCHAR(100) NULL UNIQUE COMMENT '邮箱 (唯一)',
    city VARCHAR(50) NULL COMMENT '所在城市',
    gender ENUM('MALE','FEMALE','UNKNOWN') NULL DEFAULT 'UNKNOWN' COMMENT '性别',
    role VARCHAR(31) NOT NULL,
    status ENUM('PENDING','ACTIVE','BANNED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '账户状态',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间/创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    name VARCHAR(100) NOT NULL COMMENT '用户姓名',
    avatar_url VARCHAR(255) NULL COMMENT '用户头像URL',
    bio TEXT NULL COMMENT '个人介绍',
    wechat VARCHAR(50) NULL COMMENT '微信号'
);

-- 用户详细信息表 (user_profiles)
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY COMMENT '关联的用户ID',
    bank_account_number VARCHAR(25) NULL COMMENT '银行账号',
    id_card_image_url VARCHAR(1024) NULL COMMENT '身份证图片URL',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户地址表 (user_addresses)
CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区/县',
    detailed_address VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否默认地址',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 验证码表 (verification_codes)
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '验证码ID',
    target VARCHAR(100) NOT NULL COMMENT '验证目标(手机号/邮箱)',
    code VARCHAR(6) NOT NULL COMMENT '验证码',
    type ENUM('REGISTER','LOGIN','RESET_PASSWORD') NOT NULL COMMENT '验证码类型',
    expired_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_used BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已使用'
);

-- 优惠券表 (coupons)
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type ENUM('FIXED','PERCENTAGE') NOT NULL COMMENT '优惠类型：固定金额/百分比',
    value DECIMAL(10,2) NOT NULL COMMENT '优惠值',
    min_purchase DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低消费金额',
    start_time TIMESTAMP NOT NULL COMMENT '生效时间',
    end_time TIMESTAMP NOT NULL COMMENT '失效时间',
    quantity INT NOT NULL COMMENT '发放数量',
    remaining INT NOT NULL COMMENT '剩余数量',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户优惠券表 (user_coupons)
CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户优惠券ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status ENUM('UNUSED','USED','EXPIRED') NOT NULL DEFAULT 'UNUSED' COMMENT '使用状态',
    received_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    used_at TIMESTAMP NULL COMMENT '使用时间',
    used_order_id BIGINT NULL COMMENT '使用的订单ID',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (used_order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- 卖家详情表 (seller_details)
CREATE TABLE IF NOT EXISTS seller_details (
    user_id BIGINT PRIMARY KEY COMMENT '商家用户ID',
    store_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
    store_description VARCHAR(500) NULL COMMENT '店铺描述',
    business_license_url VARCHAR(1024) NOT NULL COMMENT '营业执照URL',
    seller_level INT NOT NULL DEFAULT 1 COMMENT '商家等级',
    service_rating DECIMAL(3, 2) NULL DEFAULT 5.00 COMMENT '商家服务平均评分',
    buyer_positive_rating_percent DECIMAL(5, 2) NULL DEFAULT 100.00 COMMENT '买家好评率',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- 商品分类表 (categories)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    display_order INT NULL COMMENT '显示顺序',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    icon_url VARCHAR(255) NULL COMMENT '分类图标URL',
    parent_id BIGINT NULL COMMENT '父分类ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- 商品表 (products)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID，主键，自增',
    seller_id BIGINT NOT NULL COMMENT '卖家用户ID',
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    category VARCHAR(100) NULL COMMENT '商品类别',
    description TEXT NULL COMMENT '商品描述/使用说明',
    original_price DECIMAL(10, 2) NULL COMMENT '原价',
    discount_price DECIMAL(10, 2) NOT NULL COMMENT '实际售价/折扣价',
    size VARCHAR(50) NULL COMMENT '尺寸大小',
    item_condition ENUM('NEW','LIKE_NEW','VERY_GOOD','GOOD','ACCEPTABLE') NULL COMMENT '新旧程度',
    quantity INT NOT NULL COMMENT '库存数量',
    status ENUM('PENDING_APPROVAL','ACTIVE','LOCKED','SOLD_OUT','UNLISTED','REJECTED') NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT '商品状态',
    negotiable TINYINT(1) NULL DEFAULT 0 COMMENT '是否可议价',
    sales_count INT NULL DEFAULT 0 COMMENT '历史销量',
    average_rating DECIMAL(3, 2) NULL COMMENT '商品平均评分',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间/创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    category_id BIGINT NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- 商品图片表 (product_images)
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID，主键，自增',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片存储URL',
    is_primary TINYINT(1) NULL DEFAULT 0 COMMENT '是否为主图',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    image_path VARCHAR(1024) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 订单表 (orders)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID，主键，自增',
    order_number VARCHAR(64) NOT NULL COMMENT '订单号 (系统生成, 唯一)',
    user_id BIGINT NOT NULL COMMENT '下单用户ID (买家)',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '订单总金额',
    status ENUM('PENDING_PAYMENT','PENDING_DELIVERY','IN_TRANSIT','DELIVERED','COMPLETED','CANCELED','RETURN_REQUESTED','RETURN_APPROVED','RETURN_REJECTED','RETURN_COMPLETED','AUTO_COMPLETED') NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
    points_deducted INT NULL DEFAULT 0 COMMENT '本次订单使用的积分数',
    deducted_amount DECIMAL(10, 2) NULL DEFAULT 0.00 COMMENT '积分抵扣的金额',
    agreed_offline_time VARCHAR(100) NULL COMMENT '约定线下交易时间描述',
    agreed_offline_location VARCHAR(255) NULL COMMENT '约定线下交易地点描述',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    payment_time TIMESTAMP NULL COMMENT '支付时间',
    delivery_time TIMESTAMP NULL COMMENT '确认收货时间',
    completion_time TIMESTAMP NULL COMMENT '订单完成时间',
    return_request_time TIMESTAMP NULL COMMENT '发起退货申请时间',
    UNIQUE KEY uk_order_number (order_number),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 订单项表 (order_items)
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID，主键，自增',
    order_id BIGINT NOT NULL COMMENT '所属订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    seller_id BIGINT NOT NULL COMMENT '卖家用户ID',
    quantity INT NOT NULL COMMENT '购买数量',
    price DECIMAL(10, 2) NOT NULL COMMENT '下单时的商品单价',
    product_name VARCHAR(255) NULL COMMENT '下单时的商品名称',
    status ENUM('NORMAL','RETURN_REQUESTED','RETURN_APPROVED','RETURN_REJECTED') NOT NULL DEFAULT 'NORMAL' COMMENT '订单项状态',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 待结算资金表 (pending_funds)
CREATE TABLE IF NOT EXISTS pending_funds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID，主键，自增',
    order_item_id BIGINT NOT NULL COMMENT '关联的订单项ID',
    seller_id BIGINT NOT NULL COMMENT '收款卖家ID',
    amount DECIMAL(12, 2) NOT NULL COMMENT '待结算金额',
    fee_rate DECIMAL(5, 4) NOT NULL COMMENT '成交时的平台费率',
    platform_fee DECIMAL(10, 2) NOT NULL COMMENT '计算出的平台手续费',
    release_due_date TIMESTAMP NOT NULL COMMENT '预计资金释放日期',
    status ENUM('HELD','RELEASED','REFUNDED') NOT NULL DEFAULT 'HELD' COMMENT '资金状态',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pending_order_item (order_item_id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 评价表 (reviews)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID，主键，自增',
    order_item_id BIGINT NOT NULL COMMENT '关联的订单项ID',
    user_id BIGINT NOT NULL COMMENT '评价者用户ID',
    product_id BIGINT NOT NULL COMMENT '被评价商品ID',
    seller_id BIGINT NOT NULL COMMENT '被评价卖家ID',
    product_rating INT NULL COMMENT '商品评分',
    product_comment TINYTEXT NULL,
    seller_service_rating INT NULL COMMENT '卖家服务评分',
    seller_service_comment TINYTEXT NULL,
    seller_reply TEXT NULL COMMENT '卖家回复内容',
    seller_reply_time TIMESTAMP NULL COMMENT '卖家回复时间',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价创建时间',
    UNIQUE KEY uk_review_order_item (order_item_id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 卖家评价买家表 (seller_buyer_reviews)
CREATE TABLE IF NOT EXISTS seller_buyer_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID，主键，自增',
    order_id BIGINT NOT NULL COMMENT '关联的订单ID',
    seller_id BIGINT NOT NULL COMMENT '评价者用户ID',
    buyer_id BIGINT NOT NULL COMMENT '被评价用户ID',
    is_positive TINYINT(1) NULL COMMENT '是否好评',
    comment TINYTEXT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价创建时间',
    UNIQUE KEY uk_seller_review_order (order_id, seller_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 购物车表 (shopping_cart_items)
CREATE TABLE IF NOT EXISTS shopping_cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID，主键，自增',
    user_id BIGINT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '添加数量',
    added_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加到购物车的时间',
    session_id VARCHAR(255) NULL COMMENT '会话ID，用于未登录用户的购物车',
    UNIQUE KEY uk_cart_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 用户积分表 (points)
CREATE TABLE IF NOT EXISTS points (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID',
    points_balance INT NOT NULL DEFAULT 0 COMMENT '积分余额',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '积分最后更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 积分历史记录表 (points_history)
CREATE TABLE IF NOT EXISTS points_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '积分历史记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变动数量',
    description VARCHAR(255) NOT NULL COMMENT '变动描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户钱包表 (wallets)
CREATE TABLE IF NOT EXISTS wallets (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID',
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '余额最后更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 资金流水表 (transactions)
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '流水ID，主键，自增',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    type ENUM('RECHARGE','PURCHASE','SALE_INCOME','WITHDRAWAL','PLATFORM_FEE','REFUND_OUT','REFUND_IN','POINTS_DEDUCTION') NOT NULL COMMENT '交易类型',
    amount DECIMAL(12, 2) NOT NULL COMMENT '交易金额',
    balance_after DECIMAL(12, 2) NOT NULL COMMENT '交易后的账户余额快照',
    related_order_id BIGINT NULL COMMENT '关联订单ID',
    related_pending_fund_id BIGINT NULL COMMENT '关联待结算资金ID',
    description VARCHAR(255) NULL COMMENT '交易描述',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易发生时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (related_order_id) REFERENCES orders(id) ON DELETE SET NULL,
    FOREIGN KEY (related_pending_fund_id) REFERENCES pending_funds(id) ON DELETE SET NULL
);

-- 用户黑名单表 (blacklisted_users)
CREATE TABLE IF NOT EXISTS blacklisted_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '黑名单记录ID，主键，自增',
    user_id BIGINT NOT NULL COMMENT '被拉黑的用户ID',
    blacklisted_by_type ENUM('SELLER','PLATFORM') NOT NULL COMMENT '拉黑发起者类型',
    blacklisted_by_id BIGINT NOT NULL COMMENT '拉黑发起者ID',
    reason TINYTEXT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
    UNIQUE KEY uk_blacklist_user_by (user_id, blacklisted_by_type, blacklisted_by_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blacklisted_by_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 商家费率表 (seller_fee_rates)
CREATE TABLE IF NOT EXISTS seller_fee_rates (
    level INT PRIMARY KEY COMMENT '商家等级',
    fee_rate DECIMAL(5, 4) NOT NULL COMMENT '费率',
    description VARCHAR(100) NOT NULL COMMENT '等级描述',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO `seller_fee_rates` (`level`, `fee_rate`, `description`) VALUES
(1, 0.0010, '等级1，手续费0.1%（最高等级）'),
(2, 0.0020, '等级2，手续费0.2%'),
(3, 0.0050, '等级3，手续费0.5%'),
(4, 0.0075, '等级4，手续费0.75%'),
(5, 0.0100, '等级5，手续费1%（最低等级）');

-- 退货请求表 (return_requests)
CREATE TABLE IF NOT EXISTS return_requests (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货申请ID',
    order_id BIGINT NOT NULL COMMENT '关联订单ID',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    reason ENUM('QUALITY_ISSUE','WRONG_ITEM','DAMAGED','NOT_AS_DESCRIBED','OTHER') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '退货原因',
    description TEXT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '问题描述',
    status ENUM('PENDING','APPROVED','REJECTED','COMPLETED') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '退货状态',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id) USING BTREE,
    INDEX order_id(order_id ASC) USING BTREE,
    INDEX user_id(user_id ASC) USING BTREE,
    CONSTRAINT return_requests_ibfk_1 FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT return_requests_ibfk_2 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- 退货请求项表 (return_request_items)
CREATE TABLE IF NOT EXISTS return_request_items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货商品ID',
    return_request_id BIGINT NOT NULL COMMENT '关联退货申请ID',
    order_item_id BIGINT NOT NULL COMMENT '关联订单商品ID',
    quantity INT NOT NULL COMMENT '退货数量',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id) USING BTREE,
    INDEX return_request_id(return_request_id ASC) USING BTREE,
    INDEX order_item_id(order_item_id ASC) USING BTREE,
    CONSTRAINT return_request_items_ibfk_1 FOREIGN KEY (return_request_id) REFERENCES return_requests(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT return_request_items_ibfk_2 FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

