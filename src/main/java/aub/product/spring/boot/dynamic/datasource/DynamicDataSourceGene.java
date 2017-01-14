package aub.product.spring.boot.dynamic.datasource;


public class DynamicDataSourceGene {
    private String master;
    private DynamicDataSourcePolicy slavePolicy;

    public DynamicDataSourceGene(String master, DynamicDataSourcePolicy slavePolicy) {
        this.master = master;
        this.slavePolicy = slavePolicy;
    }

    public String getMaster() {
        return master;
    }

    public DynamicDataSourcePolicy getSlavePolicy() {
        return slavePolicy;
    }
}
