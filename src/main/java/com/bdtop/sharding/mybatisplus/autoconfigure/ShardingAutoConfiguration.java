package com.bdtop.sharding.mybatisplus.autoconfigure;

import com.bdtop.sharding.mybatisplus.autoconfigure.properties.MasterSlaveRuleConfigurationProperties;
import com.bdtop.sharding.mybatisplus.autoconfigure.properties.ShardingRuleConfigurationProperties;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import io.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import io.shardingsphere.shardingjdbc.util.DataSourceUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Spring boot sharding and master-slave configuration.
 *
 * @author Whalley
 */
@Configuration
@EnableConfigurationProperties({ShardingRuleConfigurationProperties.class, MasterSlaveRuleConfigurationProperties.class})
@Getter
public class ShardingAutoConfiguration implements EnvironmentAware {

    @Autowired
    private ShardingRuleConfigurationProperties shardingProperties;

    @Autowired
    private MasterSlaveRuleConfigurationProperties masterSlaveProperties;

    private final Map<String, DataSource> dataSourceMap = new HashMap<>();

    private final Properties props = new Properties();

    private final Map<String, Object> configMap = ConfigMapContext.getInstance().getConfigMap();

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() throws SQLException {
        if (null == masterSlaveProperties.getMasterDataSourceName()) {
            ShardingRuleConfiguration shardingRuleConfiguration = shardingProperties.getShardingRuleConfiguration();
            return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfiguration, configMap, props);
        }
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = masterSlaveProperties.getMasterSlaveRuleConfiguration();
        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfiguration, configMap, props);
    }

    @Override
    public void setEnvironment(final Environment environment) {
        setDataSourceMap(environment);
        setShardingProperties(environment);
    }

    private void setDataSourceMap(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.datasource.");
        String[] dataSources = propertyResolver.getProperty("names").split(",");
        for (String each : dataSources) {
            try {
                Map<String, Object> dataSourceProps = propertyResolver.getSubProperties(each + ".");
                Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
                DataSource dataSource = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
                dataSourceMap.put(each, dataSource);
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingConfigurationException("Can't find datasource type!", ex);
            }
        }
    }

    private void setShardingProperties(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.config.sharding.props.");
        String showSQL = propertyResolver.getProperty(ShardingPropertiesConstant.SQL_SHOW.getKey());
        if (!Strings.isNullOrEmpty(showSQL)) {
            props.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), showSQL);
        }
        String executorSize = propertyResolver.getProperty(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey());
        if (!Strings.isNullOrEmpty(executorSize)) {
            props.setProperty(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey(), executorSize);
        }
    }
}
