package aub.dao;

import aub.product.spring.boot.dynamic.datasource.DynamicDataSourcePolicy;
import aub.product.spring.boot.dynamic.datasource.TargetDataSource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDao {

    @Resource(name = "sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @TargetDataSource(master = "writer")
    public List<Map<String, Object>> selectMaster() {
        return this.sqlSessionTemplate.selectList("x.selectAll");
    }

    @TargetDataSource(master = "reader", slavePolicy = DynamicDataSourcePolicy.RANDOM)
    public List<Map<String, Object>> selectSlave() {
        return this.sqlSessionTemplate.selectList("x.selectAll");
    }


    @Transactional
    @TargetDataSource(master = "ds1")
    public void insert(String name, String password) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userName", name);
        map.put("password", password);
        map.put("createTime", System.currentTimeMillis());
        this.sqlSessionTemplate.insert("x.insert", map);
        //throw new RuntimeException();
    }

}
