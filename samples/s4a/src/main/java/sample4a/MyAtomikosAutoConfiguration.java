package sample4a;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.spring.AtomikosAutoConfiguration;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

@Configuration(proxyBeanMethods = false)
@Import(AtomikosAutoConfiguration.class)

public class MyAtomikosAutoConfiguration implements BeanDefinitionRegistryPostProcessor {
  private static final String ATM_NAME = "transactionManager";
  private static final String MY_ATM_NAME = "myTransactionManager";

  @Bean(name = MY_ATM_NAME)
  JtaTransactionManager transactionManager(UserTransaction ut, TransactionManager tm,
      ObjectProvider<TransactionManagerCustomizers> tmc) {
    JtaTransactionManager jtm = new JtaTransactionManager(ut, tm);
    tmc.ifAvailable((customizers) -> customizers.customize(jtm));
    return jtm;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    registry.removeBeanDefinition(ATM_NAME);
    BeanDefinition bd = registry.getBeanDefinition(MY_ATM_NAME);
    registry.removeBeanDefinition(MY_ATM_NAME);
    registry.registerBeanDefinition(ATM_NAME,bd);

  }

}
