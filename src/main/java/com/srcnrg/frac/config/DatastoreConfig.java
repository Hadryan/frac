package com.srcnrg.frac.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatastoreConfig 
{
    @Bean(name = "agensDB")
    @ConfigurationProperties(prefix = "formula.ds.agens")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "agensJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("agensDB") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
