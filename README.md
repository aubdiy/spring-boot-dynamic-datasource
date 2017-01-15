# 介绍
* 这是一个支持单库事务的，使用APO方式实现的，适用于 spring-boot 的`动态数据源`工具，支持如下场景：

	1. 需要访问多个数据库，例如：单个工程需要同时访问 A 库、B 库。
	2. 需要一组读写分离，一个主库，多个从库，例如：写操作都访问 Aw 库，读操作都访问 Ar1 、 Ar2 ... Arn 库
	3. 需要多组读写分离，例如：两组数据库 Aw Ar1 Ar2 、 Bw Br1 Br2,  

* 不支持双写主（这种场景很少遇见）

## 安装
1. 安装到本地

	```shell
	git clone git@github.com:aubdiy/spring-boot-dynamic-datasource.git
	cd spring-boot-dynamic-datasource
	mvn install
	```

2. maven依赖引入

	```xml
 	<dependency>
		<groupId>aub.product</groupId>
		<artifactId>spring-boot-dynamic-datasource</artifactId>
		<version>1.0-SNAPSHOT</version>
	</dependency>
	```	

## 快速开始
### 配置数据源

在 `application.properties` 中配置数据源，

数据源均以 `spring.dynamic.datasource.` 开头
	
spring-boot原生数据源是以 `spring.datasource.`  开头

```
spring.dynamic.datasource.${库名}.${参数}=${值}
```

1. 配置主库

	```propertis
	#配置 1 个名字为 writer 的主库数据源
	
	#数据库类型(必填，默认：com.zaxxer.hikari.HikariDataSource)
	spring.dynamic.datasource.writer.type=com.zaxxer.hikari.HikariDataSource
	#数据库 URL
	spring.dynamic.datasource.writer.url=jdbc:mysql://127.0.0.1:3306/spring-boot-writer?useUnicode=true&amp;characterEncoding=utf-8
	#数据库账号
	spring.dynamic.datasource.writer.username=root
	#数据库密码
	spring.dynamic.datasource.writer.password=123
	#数据库驱动
	spring.dynamic.datasource.writer.driver-class-name=com.mysql.jdbc.Driver
	#当前主库的包含的所有从库，如果多个，使用 ',' 分割
	spring.dynamic.datasource.writer.slave=reader1,reader2
	#...其他数据连接信息配置
	```

2. 配置从库

	```propertis
	#配置 2 个从主库数据源：reader1, reader2
	

	#数据库类型(必填，默认：com.zaxxer.hikari.HikariDataSource)
	spring.dynamic.datasource.reader1.type=com.zaxxer.hikari.HikariDataSource
	#数据库 URL
	spring.dynamic.datasource.reader1.url=jdbc:mysql://127.0.0.1:3306/spring-boot-reader1?useUnicode=true&amp;characterEncoding=utf-8
	#数据库账号
	spring.dynamic.datasource.reader1.username=root
	#数据库密码
	spring.dynamic.datasource.reader1.password=123
	#数据库驱动
	spring.dynamic.datasource.reader1.driver-class-name=com.mysql.jdbc.Driver
	#当从库所属的主库
	spring.dynamic.datasource.reader1.master=writer
	#...其他数据连接信息配置
	
	
	#数据库类型(必填，默认：com.zaxxer.hikari.HikariDataSource)
	spring.dynamic.datasource.reader2.type=com.zaxxer.hikari.HikariDataSource
	#数据库 URL
	spring.dynamic.datasource.reader2.url=jdbc:mysql://127.0.0.1:3306/spring-boot-reader2?useUnicode=true&amp;characterEncoding=utf-8
	#数据库账号
	spring.dynamic.datasource.reader2.username=root
	#数据库密码
	spring.dynamic.datasource.reader2.password=123
	#数据库驱动
	spring.dynamic.datasource.reader2.driver-class-name=com.mysql.jdbc.Driver
	#当从库所属的主库
	spring.dynamic.datasource.reader2.master=writer
	#...其他数据连接信息配置
	```
	

### 使用数据源
在需要操作数据库的方法上，增加 `@TargetDataSource` 注解，用于切换数据源 

1. 操作主库

	```java
	@TargetDataSource(master = "writer")
    public List<XXX> selectMaster() {
        return this.sqlSessionTemplate.selectList("x.selectAll");
    }
	
	```
	
2. 操作从库，多个从库的时候，目前只支持随机选取一个从库

	```java
	@TargetDataSource(master = "writer", slavePolicy = DynamicDataSourcePolicy.RANDOM)
    public List<XXX> selectMaster() {
        return this.sqlSessionTemplate.selectList("x.selectAll");
    }
	
	```

### 待完善功能点
1. 增加必要的日志打印
2. 增加例子工程
3. 增加从库选取策略

### 参考资料
1. [sunng / multi-datasource-with-transaction](https://github.com/sunng/multi-datasource-with-transaction)
2. [Spring Boot 动态数据源（多数据源自动切换）](http://blog.csdn.net/catoop/article/details/50575038)