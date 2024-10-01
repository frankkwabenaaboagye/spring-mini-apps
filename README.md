# General Info

To import into your IDE, import the parent pom `lab/pom.xml` as Maven projects or `lab/build.gradle` as Gradle projects.


# Note
 Some Tips 
 - credit: spring docs, spring academy, spring team


# Content
1. [Configuration](#configuration)
2. [Component Scanning](#component-scanning)
3. [Spring Container](#spring-container)
4. [AOP](#aop)
5. [JDBC](#jdbc)
6. [Transaction](#transaction)
7. [SpringBoot](#spring-boot)

## Configuration

TODO

## Component Scanning

TODO

## Spring Container

TODO

## AOP
- There are some generic functionalities that are needed in many places in any application code
- With this in mind, there is the need to avoid code tangling and eliminate code scattering
- what can we do? Modularize🧩? Yes!
- AOP helps to do that 💡
- AOP Technologies - AspectJ, Spring AOP
- Core concepts
    - `Join Point`: A point in the execution of a program - Method calls  or exception
    - `Pointcut` : Expression that selects one or more `join point`
    - `Advice` : Code to be executed at each selected `join point`
    - `Aspect` : Module that encapsulated `Pointcuts` &  `Advice`
    - `Weaving` : Combining `Aspects` with main code
- Defining a pointcut
    - Spring AOP uses AspectJ expression language for selecting where to apply advice
    - When defining pointcut, we're defining designators
    - Designators - `execution`, e.t.c

```java
// format
execution( <method pattern> )

// chain together with &&, ||, !
execution( <pattern 1> ) && execution( <pattern 2> )

// method pattern
[modifiers] ReturnType [ClassType] MethodName(Arguments) [throws ExceptionType]
    // two mandatory things: 
        // ReturnType and 
        // MethodName with Arguments
    
```

- Advice Types
    - `Before` ⏩
        - Proxy delegates to the advice before delegating to the target⚙️
        - do whatever you want in the advice before executing the business logic
        - if you happen to put an exception on the advice, you prevent the target from executing
            - good for security🔒 use cases 
    - `AfterReturning` ↩️
        - Proxy first delegates to the target⚙️
        - then the proxy delegates to the advice
            - This only happens when there is a successful return from the target
            - there will be more information here because, you will  have
                - the context information
                - with the return from the target
    - `AfterThrowing` ⚠️
        - Proxy first delegate to the target⚙️
        - then the proxy delegates to the advice
            - This only happens when there is an exception thrown from the target
            - you will have the exception available to you
            - you can throw another exception or allow it to propagate
                - there is a work around this
    - `After` 🔚
        - Proxy delegate to the target⚙️
        - then proxy delegate to the advice
        - whether there is a success reutrn or exception, it does not matter
        - advice is excuted after the target⚙️
    - `Around` 🔄
        - Proxy delegates to the advice
        - it is your responsibility to call `proceed` method to the target⚙️
        - in this way, you can excute things before and after the target - cool! 😎
- Limitations of spring aop
    - can only advise non-private methods
    - can only advise spring beans
    - inner method call inside of a target would not get advised
- Note: AspectJ does not have this limitations

## JDBC
- There are issues with plain Jdbc
    - boilerplate code
    - forced to catch certain exceptions
    - forced to close resources
- Spring JDBC Template solves these issues
    - with a simple statement, spring will be able to handle for us
        - connection
        - statement execution
        - result set processsing
        - exceptions
        - release of connection
- Note: When creating the Jdbc, we need a `datasource`
- Basic Usage
    - For simple Types
    - For Generic Maps
    - For Domain Objects

```java
// for simple types

jdbcTemplate.queryForObject(the_sql, the_return_class); 

String sql = "select count(*) from PERSON";
jdbcTemplate.queryForObject(sql, Long.class); 

    // you can bind variables too
    String sql = "insert into PERSON(first_name, last_name) values(?, ?)";
    jdbcTemplate.update(sql, "Kay", "Lee");
        /* Note: Use the `update` method
             to perfom - insert, update, delete
        */

// for generic maps
    // can return each row of a ResultSet as a map
jdbcTemplate.queryForList(...);
jdbcTemplate.queryForMap(...);   // watch out for memory consumption

    // example
    String sql = "select * from PERSON where id=?";
    int id = 1;
    jdbcTemplate.queryForMap(sql, id);  // this returns: Map<String, Object>

    String sql = "select * from PERSON";
    jdbcTemplate.queryForList(sql); // returns: List<Map<String, Object>>


// Domain Object queries
    // you have to use call back approach
        - RowMapper
        - ResultSetExtractor
        - RowCallbackHandler


jdbcTemplate
.queryForObject(
    String sql, 
    RowMapper<T> rowMapper, 
    Object... args
)
.queryForObject(
    String sql, 
    RowMapper<T> rowMapper
)


    // examples
        // single row
            String sql = "select first_name, last_name from PERSON where id=?";

                // here we define row mapper using lambda
            jdbc.queryForObject(
                sql, 
                (rs, rowNum)-> new Person(rs.getString("first_name"), rs.getString("last_name")), 
                id
            );
                // the above returns `Person` domain object

        // multiple rows
            String sql = "select first_name, last_name from PERSON";

                // here we define row mapper using lambda also
            jdbc.queryForObject(
                sql, 
                (rs, rowNum)-> new Person(rs.getString("first_name"), rs.getString("last_name"))
            );
                // the above returns `List<Person>` domain object



```

```java
// Examples

// before - plain jdbc
public Restaurant findByMerchantNumber(String merchantNumber) {
    String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE, BENEFIT_AVAILABILITY_POLICY"
            + " from T_RESTAURANT where MERCHANT_NUMBER = ?";
    Restaurant restaurant = null;

    try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql) ){
        ps.setString(1, merchantNumber);
        ResultSet rs = ps.executeQuery();
        advanceToNextRow(rs);
        restaurant = mapRestaurant(rs);
    } catch (SQLException e) {
        throw new RuntimeException("SQL exception occurred finding by merchant number", e);
    }

    return restaurant;
}

// after - refactored

	public Restaurant findByMerchantNumber(String merchantNumber) {
		String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE, BENEFIT_AVAILABILITY_POLICY"
				+ " from T_RESTAURANT where MERCHANT_NUMBER = ?";

		Restaurant restaurant = jdbcTemplate.queryForObject(
            sql, 
            (rs, rowNum) -> mapRestaurant(rs), 
            merchantNumber
        );

		return restaurant;
	}

//--------------------------------

// before
public void updateBeneficiaries(Account account) {
    String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";
    Connection conn = null;
    PreparedStatement ps = null;
    try {
        conn = dataSource.getConnection();
        ps = conn.prepareStatement(sql);
        for (Beneficiary beneficiary : account.getBeneficiaries()) {
            ps.setBigDecimal(1, beneficiary.getSavings().asBigDecimal());
            ps.setLong(2, account.getEntityId());
            ps.setString(3, beneficiary.getName());
            ps.executeUpdate();
        }
    } catch (SQLException e) {
        throw new RuntimeException("SQL exception occurred updating beneficiary savings", e);
    } finally {
        if (ps != null) {
            try {
                // Close to prevent database cursor exhaustion
                ps.close();
            } catch (SQLException ex) {
            }
        }
        if (conn != null) {
            try {
                // Close to prevent database connection exhaustion
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }
}

// after

public void updateBeneficiaries(Account account) {
    String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";


    for (Beneficiary beneficiary : account.getBeneficiaries()) {
        jdbcTemplate.update(sql, beneficiary.getSavings().asBigDecimal(), account.getEntityId(), beneficiary.getName());
    }
    
}



```

```java

// utilise lambda

    // consider
    jdbcTemplate.queryForObject(sql, new RowMapper<Restaurant>() {
        @Override
        public Restaurant mapRow(ResultSet rs, int rowNum) throws SQLException {
            return mapRestaurant(rs);
        }
    });

    // same as

    jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRestaurant(rs)); // much better


// another example

    // consider
    jdbcTemplate.query(
        sql, 
        new ResultSetExtractor<Account>() {
			@Override
			public Account extractData(ResultSet rs) throws SQLException, DataAccessException {
				return mapAccount(rs);
			}
		}, 
        creditCardNumber
    );

    // same as
    jdbcTemplate.query(
        sql, 
        rs -> {
            return mapAccount(rs);
        }, 
        creditCardNumber
    );

    // same as
    jdbcTemplate.query(
        sql, 
        this::mapAccount, 
        creditCardNumber
    );


// by the way; the mapAcount method is shown below
    /*
	private Account mapAccount(ResultSet rs) throws SQLException {
		Account account = null;
		while (rs.next()) {
			if (account == null) {
				String number = rs.getString("ACCOUNT_NUMBER");
				String name = rs.getString("ACCOUNT_NAME");
				account = new Account(number, name);
				// set internal entity identifier (primary key)
				account.setEntityId(rs.getLong("ID"));
			}
			account.restoreBeneficiary(mapBeneficiary(rs));
		}
		if (account == null) {
			// no rows returned - throw an empty result exception
			throw new EmptyResultDataAccessException(1);
		}
		return account;
	}
    */

```

## Transaction
- We have to adhere to the ACID 
    {Atomicity, Consistency, Isolation, Durable} 
principles when it comes to the data access layer right?
- When running non transactionally, there could be issues like:
    - separate connections for separate method calls
    - partial failures might be a problem too
- we need deal with this
    - with transactions
        - we become very efficient, because same connection is used for each operation
        - also operations complete as an atomic unit
- Spring Transaction Management
    1. Declare a `PlatformTransactionManager` bean
        - note that there are several implementations availbel
            - DataSourceTM, JmsTM, JpaTM , e.t.c.
    2. Declare the transactional methods 
        - you can use annotations(recommended), or go the programmatic way, or even do both
    3. Add the `@EnableTransactionManagement` to a configuration class

```java

// examples

	@Bean
	public DataSource dataSource(){
        ...
        ...
	}	

	@Bean
	public PlatformTransactionManager transactionManager(){
		return new DataSourceTransactionManager(dataSource());
	}

    //----

    @Configuration
    @EnableTransactionManagement
    public class RewardsConfig {
        ...
        ...
    }

```

## Spring Boot
In a Spring application, we typically need to:
- Configure dependencies in the pom.xml file
- Set up configurations in the application context

However, many of these components can be predicted, so why not let Spring Boot handle them for us?
- Spring Boot automates low-level configurations
    - This requires it to make certain decisions (hence, it has its own opinions)
        - Essentially, Spring Boot takes an opinionated approach to the Spring framework and third-party libraries
        - However, we still have the ability to override these defaults.
    - Important note:
        - Spring Boot is not a code generator; all configurations happen at runtime.