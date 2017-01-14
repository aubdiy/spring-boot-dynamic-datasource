package aub.product.spring.boot.dynamic.datasource;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<DynamicDataSourceGene> contextHolder = new ThreadLocal<>();

    private static Map<String, List<String>> masterSlaveDataSourceNames;

    public static void setDataSourceType(String master, DynamicDataSourcePolicy slavePolicy) {
        contextHolder.set(new DynamicDataSourceGene(master, slavePolicy));
    }

    public static String getDataSourceType() {
        DynamicDataSourceGene dynamicDataSourceGene = contextHolder.get();
        //todo 可增加master是否存在的校验
        String master = dynamicDataSourceGene.getMaster();
        //todo 可增加 slave是否存在的校验
        DynamicDataSourcePolicy slavePolicy = dynamicDataSourceGene.getSlavePolicy();
        if (slavePolicy == DynamicDataSourcePolicy.NULL) {
            return master;
        } else {
            //todo 随机策略,可增加其他策略
            List<String> slaves = masterSlaveDataSourceNames.get(master);
            return slaves.get(new Random().nextInt(slaves.size()));
        }
    }

    public static void clearDataSourceType() {
        contextHolder.remove();
    }

    public static void setMasterSlaveDataSourceNames(Map<String, List<String>> masterSlaveDataSourceNames) {
        DynamicDataSourceContextHolder.masterSlaveDataSourceNames = masterSlaveDataSourceNames;
    }
}
