package hanwha.meta;

import javax.sql.DataSource;

import oracle.jdbc.OracleDriver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
@ComponentScan
class Config {
	@Bean
	DataSource dataSource() {
		return new SimpleDriverDataSource(new OracleDriver(),
		               "jdbc:oracle:thin:@localhost:1521:XE", "system", "sys");
	}
}
