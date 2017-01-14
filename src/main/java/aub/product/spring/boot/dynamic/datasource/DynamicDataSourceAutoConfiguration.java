package aub.product.spring.boot.dynamic.datasource;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Configuration
public class DynamicDataSourceAutoConfiguration {
    private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

    private ConversionService conversionService = new DefaultConversionService();

    @Autowired
    private Environment env;

    @Bean
    public DataSource dynamicDataSource() {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.dynamic.datasource.");
        Set<String> dataSourceNames = getDataSourceNames(propertyResolver);
        HashMap<String, List<String>> masterSlaveDataSourceNames = assembleDataSourceNames(propertyResolver, dataSourceNames);
        DynamicDataSourceContextHolder.setMasterSlaveDataSourceNames(masterSlaveDataSourceNames);

        Map<Object, Object> targetDataSources = buidTargetDataSources(propertyResolver, dataSourceNames);
        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setTargetDataSources(targetDataSources);
        //dataSource.setDefaultTargetDataSource(defaultDataSource);

        return dataSource;
    }


    private HashMap<String, List<String>> assembleDataSourceNames(RelaxedPropertyResolver propertyResolver, Set<String> dataSourceNames) {
        HashMap<String, List<String>> masterSlaveDataSourceNames = new HashMap<>();
        for (String datasourceName : dataSourceNames) {
            if (masterSlaveDataSourceNames.containsKey(datasourceName)) {
                continue;
            }
            String masterDataSourceName = propertyResolver.getProperty(datasourceName + ".master");
            String slaveDataSourceName = propertyResolver.getProperty(datasourceName + ".slave");
            if (slaveDataSourceName != null) {
                masterSlaveDataSourceNames.put(datasourceName, new ArrayList<String>());
            } else if (masterDataSourceName != null) {
                List<String> slaveDatasourceNames = masterSlaveDataSourceNames.get(masterDataSourceName);
                if (slaveDatasourceNames == null) {
                    if (!dataSourceNames.contains(masterDataSourceName)) {
                        throw new RuntimeException("dynamic data source config error, cant find master data source: " + masterDataSourceName);
                    } else {
                        slaveDatasourceNames = new ArrayList<>();
                        masterSlaveDataSourceNames.put(masterDataSourceName, slaveDatasourceNames);
                    }
                }
                slaveDatasourceNames.add(datasourceName);
            }
        }
        return masterSlaveDataSourceNames;
    }

    private Set<String> getDataSourceNames(RelaxedPropertyResolver propertyResolver) {
        Map<String, Object> subProperties = propertyResolver.getSubProperties("");
        Set<String> keys = subProperties.keySet();

        Set<String> dataSourceNames = new LinkedHashSet<>(keys.size());
        for (String key : keys) {
            int i = key.indexOf(".");
            String dataSourceName = key.substring(0, i);
            dataSourceNames.add(dataSourceName);
        }
        return dataSourceNames;
    }


    private Map<Object, Object> buidTargetDataSources(RelaxedPropertyResolver propertyResolver, Set<String> dataSourceNames) {
        Map<Object, Object> targetDataSources = new HashMap<>(dataSourceNames.size());
        for (String dataSourceName : dataSourceNames) {
            DataSource dataSource = buidDataSource(dataSourceName, propertyResolver);
            binderExtendConfig(dataSourceName, propertyResolver, dataSource);
            targetDataSources.put(dataSourceName, dataSource);
        }
        return targetDataSources;
    }


    private DataSource buidDataSource(String dataSourceName, RelaxedPropertyResolver propertyResolver) {
        String dataSourceUrl = propertyResolver.getProperty(dataSourceName + ".url");
        String dataSourceType = propertyResolver.getProperty(dataSourceName + ".type");
        String dataSourceUsername = propertyResolver.getProperty(dataSourceName + ".username");
        String dataSourcePassword = propertyResolver.getProperty(dataSourceName + ".password");
        String dataSourcedriverClassName = propertyResolver.getProperty(dataSourceName + ".driver-class-name");

        if (dataSourceType == null) {
            dataSourceType = DATASOURCE_TYPE_DEFAULT;
        }
        Class<? extends DataSource> dataSourceTypeClass;
        try {
            dataSourceTypeClass = (Class<? extends DataSource>) Class.forName(dataSourceType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("dynamic data source config error, ClassNotFoundException:  " + dataSourceType, e);
        }

        DataSourceBuilder factory = DataSourceBuilder.create()
                .driverClassName(dataSourcedriverClassName)
                .url(dataSourceUrl)
                .username(dataSourceUsername)
                .password(dataSourcePassword)
                .type(dataSourceTypeClass);
        return factory.build();
    }

    private void binderExtendConfig(String dataSourceName, RelaxedPropertyResolver propertyResolver, DataSource dataSource) {
        RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
        dataBinder.setConversionService(conversionService);
        dataBinder.setIgnoreNestedProperties(false);//false
        dataBinder.setIgnoreInvalidFields(false);//false
        dataBinder.setIgnoreUnknownFields(true);//true

        Map<String, Object> dataSourceExtendConfigs = propertyResolver.getSubProperties(dataSourceName + ".");
        dataSourceExtendConfigs = new HashMap<>(dataSourceExtendConfigs);

        // 排除已经设置的属性
        dataSourceExtendConfigs.remove("type");
        dataSourceExtendConfigs.remove("driver-class-name");
        dataSourceExtendConfigs.remove("url");
        dataSourceExtendConfigs.remove("username");
        dataSourceExtendConfigs.remove("password");
        dataSourceExtendConfigs.remove("master");
        dataSourceExtendConfigs.remove("slave");
        dataBinder.bind(new MutablePropertyValues(dataSourceExtendConfigs));
    }


    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.remove("aaa");
    }
}
