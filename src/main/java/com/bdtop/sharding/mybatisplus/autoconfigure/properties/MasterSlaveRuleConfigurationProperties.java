package com.bdtop.sharding.mybatisplus.autoconfigure.properties;

import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Master slave rule configuration properties.
 *
 * @author Whalley
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config.master-slave")
public class MasterSlaveRuleConfigurationProperties extends YamlMasterSlaveRuleConfiguration {
}
