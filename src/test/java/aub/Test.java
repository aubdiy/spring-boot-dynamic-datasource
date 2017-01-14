package aub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Created by liujinxin on 2017/1/13.
 */
@Configuration
public class Test {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dynamicDataSource() {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.dynamic.datasource.");
        Map<String, Object> subProperties = propertyResolver.getSubProperties("");

        System.out.println(subProperties);
        return null;
    }
}
