package com.ninjaone.dundie_awards.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.UserTransaction;

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
    public PlatformTransactionManager transactionManager(
            @Qualifier("userTransaction") UserTransaction userTransaction,
            @Qualifier("atomikosTransactionManager") UserTransactionManager atomikosTransactionManager)
            throws Throwable {
        return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
