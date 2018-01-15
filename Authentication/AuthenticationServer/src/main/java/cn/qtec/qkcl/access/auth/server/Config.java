package cn.qtec.qkcl.access.auth.server;/*
package cn.qtec.qkcl.access.auth.server;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

*/
/**
 * Created by INM(enzo27red@hotmail.com) on2017/12/8.
 *//*

@Configuration(value = "accessConfig")
public class Config {


    @Bean(name = "dataSource")
    public DataSource dataSource(@Value("${jdbc.url}") String url, @Value("${jdbc.driverClassName}") String driverClassName, @Value("${jdbc.username}") String userName, @Value("${jdbc.password}") String password) {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "sessionFactory")
    public LocalSessionFactoryBean sessionFactory(@Value("${hibernate.dialect}") String hibernateDialect, @Qualifier(value = "dataSource") DataSource dataSource) {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", hibernateDialect);
        sessionFactoryBean.setHibernateProperties(properties);
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setPackagesToScan("cn.qtec.qkcl.access.auth.server.entity");
        return sessionFactoryBean;
    }

    @Bean
    public HibernateTransactionManager hibernateTransactionManager(@Qualifier(value = "sessionFactory") SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
*/
