package com.ninjaone.dundie_awards.config;

// import com.atomikos.icatch.jta.UserTransactionImp;
// import com.atomikos.icatch.jta.UserTransactionManager;
// import com.atomikos.spring.AtomikosConnectionFactoryBean;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
@EnableTransactionManagement
public class TransactionManagementConfig {
    // @Bean(name = "userTransaction")
    // public UserTransaction userTransaction() throws Throwable {
    //     UserTransactionImp userTransactionImp = new UserTransactionImp();
    //     userTransactionImp.setTransactionTimeout(10000);
    //     return userTransactionImp;
    // }

    // @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    // public TransactionManager atomikosTransactionManager() throws Throwable {
    //     UserTransactionManager userTransactionManager = new UserTransactionManager();
    //     userTransactionManager.setForceShutdown(false);

    //     AtomikosJtaPlatform.transactionManager = userTransactionManager;

    //     return userTransactionManager;
    // }

    // @Bean(name = "transactionManager")
    // @DependsOn({ "userTransaction", "atomikosTransactionManager" })
    // public PlatformTransactionManager transactionManager() throws Throwable {
    //     UserTransaction userTransaction = userTransaction();

    //     AtomikosJtaPlatform.transaction = userTransaction;

    //     TransactionManager atomikosTransactionManager = atomikosTransactionManager();
    //     return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
    // }

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
