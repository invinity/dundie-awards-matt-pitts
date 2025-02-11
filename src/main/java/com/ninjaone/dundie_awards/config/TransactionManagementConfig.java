package com.ninjaone.dundie_awards.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.spring.AtomikosConnectionFactoryBean;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
public class TransactionManagementConfig {
    @Bean
    @Qualifier("userTransaction")
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(10);
        return userTransactionImp;
    }

    @Qualifier("atomikosTransactionManager")
    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("userTransaction") UserTransaction userTransaction, @Qualifier("atomikosTransactionManager") UserTransactionManager atomikosTransactionManager) throws Throwable {
        return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    // @Bean
    // public DefaultMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
    //         PlatformTransactionManager transactionManager) {
    //     DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setTransactionManager(transactionManager);
    //     container.setDestinationName("test");
    //     // Configure other properties like destination, message listener etc.
    //     return container;
    // }

    // @Bean
    // public JmsTemplate jmsTemplate() throws Throwable {
    //     return new JmsTemplate(connectionFactory());
    // }

    // @Bean(initMethod = "init", destroyMethod = "close")
    // public ConnectionFactory connectionFactory() throws JMSException {
    //     ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
    //     // activeMQXAConnectionFactory.setBrokerURL(jmsUrl);
    //     // activeMQXAConnectionFactory.setUser(jmsUserName);
    //     // activeMQXAConnectionFactory.setPassword(jmsPassport);
    //     AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
    //     atomikosConnectionFactoryBean.setUniqueResourceName("xamq");
    //     atomikosConnectionFactoryBean.setLocalTransactionMode(false);
    //     atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
    //     return atomikosConnectionFactoryBean;
    // }
}
