# EShop Project

这是一个使用 Spring Boot 构建的电商平台项目。

## 功能特点

本项目旨在提供一个功能全面的在线购物体验，涵盖用户、商家和管理员三种角色的核心需求。

**通用功能:**
*   **用户注册与登录:** 支持个人用户和商家用户注册，包含基于图片的随机验证码，并由管理员审核注册请求。
*   **商品门户:** 无需登录即可展示在售的各类商品。
*   **商品搜索与展示:** 用户可以根据商品名称搜索，并可按价格、好评度和销量自定义排序规则。
*   **优惠券:** 提供限时抵扣券，在支付时自动扣减。
*   **购物车:** 支持将多个卖家的多个商品暂存购物车，并可一键下单。
*   **用户积分:** 消费一元积一分，100积分可抵扣1元现金。

**用户端功能:**
*   **基本信息管理:** 用户可以修改密码、头像、个人介绍、邮箱、微信、收货地址等。
*   **浏览商品:** 查看商品详细信息（价格、图片、描述、交易方式）、评价、销量、库存等。
*   **购买商品:** 支持快递或线下交易，扣除账户余额（模拟），积分抵扣。
*   **确认收货:** 确认后，货款将划转至卖家账户。
*   **订单管理:** 查看历史购买记录和在途商品交易状态（已发货、待收货、运输中、已收货、交易完成）。
*   **钱包功能:** 查看账户剩余金额（暂不支持直接支付）、充值记录、支付记录。
*   **商品评价:** 确认收货后可对商品和交易进行文字和打分评价，形成商品好评率。
*   **查看评价:** 查看自己的历史评价以及卖家对自己的评价。
*   **商家服务态度评价:** 对交易过程中的商家服务态度进行评价。
*   **申请退货:** 收货后24小时内可申请退货，需商家审核。

**商家端功能:**
*   **发布商品:** 发布商品详细信息（介绍、图片、文字、数量、价格、交易方式等）。
*   **卖家等级:** 默认等级为5，根据交易额和数量调整等级，可能影响平台费用。
*   **商品橱窗:** 统一展示店铺内所有发布的商品，显示库存、历史销量和评价分数。
*   **发货管理:** 买家付款后进行发货操作。
*   **收款管理:** 买家确认收货后，收到货款（扣除平台费用）。
*   **商品管理:** 可下架在售商品。
*   **退货审核:** 处理买家的退货申请，可批准或驳回。
*   **评价买家:** 对买家的购买/退货行为进行文字和打分评价，形成买家的商家好评率。

**管理员后台管理功能:**
*   **用户管理:** 审核用户（个人/商家）注册请求；管理用户信息（禁用、修改、查看）。
*   **商品管理:** 审核商家发布的商品；管理平台所有商品，可强制下架商品。
*   **订单与交易管理:** 查看平台交易记录。
*   **平台交易费用管理:** 根据费率表对每笔交易收取费用。
*   **卖家等级调整:** 根据卖家交易情况手动调整商家等级。
*   **内容管理:** 管理首页轮播图，用于商品推荐和广告宣传。

## 技术栈

*   Java 17
*   Spring Boot 3.2.5
*   Spring Security (用于用户认证和授权)
*   Spring Data JPA (用于数据持久化)
*   Thymeleaf (模板引擎)
*   MySQL (数据库)
*   Maven (项目管理和构建工具)
*   Lombok (简化Java代码)
*   Kaptcha (用于生成图片验证码)

## 安装与运行

1.  **克隆仓库:**
    ```bash
    git clone https://github.com/your-username/eshop.git
    cd eshop
    ```
2.  **数据库配置:**
    *   确保已安装并运行 MySQL。
    *   创建一个数据库，例如 `your_database_name` (可自定义)。
    *   更新 `src/main/resources/application.properties` 文件中的数据库连接信息：
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
        spring.datasource.username=your_username
        spring.datasource.password=your_password
        ```
    *   应用程序将使用 `src/main/resources/schema.sql` 文件自动初始化数据库表结构。

3.  **文件上传配置:**
    *   应用程序默认将上传文件存储在 `E:/eshop/uploads` 目录。
    *   您可以在 `src/main/resources/application.properties` 中修改此路径：
        ```properties
        spring.file.upload-dir=your/preferred/upload/directory
        ```
    *   请确保指定的目录存在且应用程序具有写入权限。以下子目录将被使用：
        *   `avatars` (用户头像)
        *   `products` (商品图片)
        *   `idcards` (身份认证图片)
        *   `licenses` (商家营业执照图片)

4.  **构建项目:**
    ```bash
    mvn clean install
    ```

5.  **运行应用程序:**
    ```bash
    mvn spring-boot:run
    ```
    应用程序将运行在 `http://localhost:8080`。

## 默认管理员凭证

*   **用户名:** admin
*   **密码:** 111111

(可以在 `application.properties` 文件中修改这些默认凭证，或者直接往数据库的user表中插入一条角色为ROLE_ADMIN的记录)

## 项目结构

```
eshop/
├── pom.xml                # Maven 项目配置文件
├── src/
│   ├── main/
│   │   ├── java/            # Java 源代码
│   │   │   └── com/example/eshop/ # 项目主包
│   │   ├── resources/       # 应用程序资源文件
│   │   │   ├── application.properties # 应用配置文件
│   │   │   ├── schema.sql       # 数据库初始化脚本
│   │   │   ├── static/          # 静态资源 (CSS, JS, 图片等)
│   │   │   └── templates/       # Thymeleaf 模板文件
├── uploads/               # 文件上传目录 (需手动创建或配置路径)
│   ├── avatars/
│   ├── products/
│   ├── idcards/
│   └── licenses/
├── README.md              # 项目说明文档
```

## 贡献指南

欢迎为此项目做出贡献！如果您有任何想法、建议或发现错误，请随时提出 Issue 或提交 Pull Request。

## 许可证

本项目采用 [MIT许可证](https://opensource.org/licenses/MIT)。

## 联系方式

如果在使用过程中遇到任何问题或有任何建议，可以通过以下方式联系我：
*   微信: 15718046681
