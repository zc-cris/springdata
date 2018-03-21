package com.zc.cris.jpa.springdata.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import com.zc.cris.jpa.springdata.entities.Dog;
import com.zc.cris.jpa.springdata.repository.DogRepository;
import com.zc.cris.jpa.springdata.service.DogService;

class TestSpringDataJPA {

	private ApplicationContext context = null;
	private DogRepository dogRepository = null;
	private DogService dogService = null;

	{
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		dogRepository = context.getBean(DogRepository.class);
		dogService = context.getBean(DogService.class);
	}

	/*
	 * 测试自定义 repository 方法
	 */
	@Test
	void testMyRepositoryMethod() {
		dogRepository.test();
	}
	
	
	/*
	 * 目的：实现带查询条件的分页：id>5
	 * 调用 JpaSpecificationExecutor 的page<T> findAll(Specification<T> spec, Pageable pageable);
	 * Specification ： 封装了JPA Criteria 查询的查询条件
	 * Pageable：封装了请求分页的信息，例如 pageNo，pageSize，Sort
	 */
	@Test
	void testJpaSpecificationExecutor() {
		// 页数默认从0开始
		int pageNo = 4-1;
		int pageSize = 5;
		
		PageRequest pageable = new PageRequest(pageNo, pageSize);
		
		// 通常使用 Specification 的匿名内部类
		Specification<Dog> specification = new Specification<Dog>() {
			/*
			 * @param root: 代表查询的实体类
			 * @param query: 可以从中得到 root 对象，告知 JPA Criteria 查询要查询哪一个实体类，
			 * 				还可以添加查询条件，还可以结合 EntityManager 对象得到最终查询的 TypedQuery 对象
			 * @param cb: CriteriaBuilder 类型对象，用于创建Criteria 相关对象的工厂，也可以从中获取到 Predicate 对象
			 * @return : Predicate 类型，代表一个查询条件
			 */
			@Override
			public Predicate toPredicate(Root<Dog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path path = root.get("id");
				Predicate predicate = cb.gt(path, 5);
				return predicate;
			}
		};
		
		Page<Dog> page = dogRepository.findAll(specification, pageable);
		
		System.out.println("总记录数：" + page.getTotalElements());
		System.out.println("当前第几页：" + (page.getNumber() + 1));
		System.out.println("总页数：" + page.getTotalPages());
		System.out.println("当前页面的 list 数据：" + page.getContent());
		System.out.println("当前页面的记录数" + page.getNumberOfElements());
	}
	
	
	
	/*
	 * 测试 通过继承 JpaRepository 接口的方法
	 * saveAndFlush 方法类似于 JPA 的 merge 方法
	 */
	@Test
	void testJpaRepository() {
		Dog dog = new Dog();
		dog.setAge(13);
		dog.setName("土狗");
		dog.setId(27);
		dogRepository.saveAndFlush(dog);
	}
	
	
	
	/*
	 * 测试 继承 PagingAndSortingRepository 后的数据分页和排序
	 */
	@Test
	void testPagingAndSortingRepository() {
		
		// 当前页数是从0开始的
		int pageNo = 5;
		int pageSize = 5;
		
		// Pageable 接口通常使用其 PageRequest 实现类，其中封装了需要分页的信息
		// 排序相关的，Sort 封装了排序的信息
		// Order 是具体针对某一个属性进行升序还是降序
		Order order1 = new Order(Direction.DESC, "id");
		Order order2 = new Order(Direction.ASC, "age");
		Sort sort = new Sort(order1, order2);
		
		PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
		Page<Dog> page = dogRepository.findAll(pageable);
		
		System.out.println("总记录数：" + page.getTotalElements());
		System.out.println("当前第几页：" + (page.getNumber() + 1));
		System.out.println("总页数：" + page.getTotalPages());
		System.out.println("当前页面的 list 数据：" + page.getContent());
		System.out.println("当前页面的记录数" + page.getNumberOfElements());
		
	}
	
	
	
	/*
	 * 测试通过继承 CrudRepository 来进行数据的 insert 操作
	 */
	@Test
	void testSave() {
		Dog dog = new Dog();
		dog.setAddressId(1);
		dog.setAge(12);
		dog.setBirth(new Date());
		dog.setName("cris");
		
		dogService.saveDog(dog);
	}
	
	/*
	 * 测试通过继承 CrudRepository 来进行数据的批量 insert 操作
	 * 注意：applicationContext.xml 配置文件中的事务管理器必须名字 为 transactionManager，
	 * 否则继承 CrudRepository 就无法使用 其特定的 save 和 saveAll 方法
	 */
	@Test
	void testSaveDogs() {
		List<Dog> dogs = new ArrayList<>();
		
		for(int i = 'a'; i <= 'z'; i++) {
			Dog dog = new Dog();
			dog.setAddressId(i);
			dog.setAge(i);
			dog.setBirth(new Date());
			dog.setName((char)i + "" + (char)i + "狗");
			dogs.add(dog);
		}
		
		dogService.saveDogs(dogs);
	}
	
	
	/*
	 * 测试 通过 @Query 和 @Modifying 注解结合来进行update 操作
	 */
	@Test
	void testUpdate() {
		dogService.updateDogAge(100, 1);
	}
	
	
	/*
	 * 测试本地sql 查询
	 */
	@Test
	void testNativeQuery() {
		Long count = dogRepository.queryCount();
		System.out.println(count);
	}
	
	
	/*
	 * 测试使用 @Query 注解进行查询
	 */
	@Test
	void testQuery() {
//		Dog dog = dogRepository.findMaxIdDog();
//		System.out.println(dog);
		
//		List<Dog> dogs = dogRepository.testQueryAnnotationParams1("金毛", 12);
//		System.out.println(dogs);
		
		
//		List<Dog> dogs = dogRepository.testQueryAnnotationParams2(12, "金毛");
//		System.out.println(dogs);
		
		List<Dog> dogs = dogRepository.testQueryAnnotationParams3("金毛");
		System.out.println(dogs);
		
		
		
	}
	
	
	
	
	
	
	
	/*
	 * 测试多对一的关系查询
	 */
	@Test
	void testRelationQuery() {
		//List<Dog> dogs = dogRepository.findByAddress_Id(1);
//		System.out.println(dogs);
	}
	
	
	@Test
	void testKeyWord2() {
		List<Dog> dogs = dogRepository.findByAgeInOrBirthLessThan(Arrays.asList(10, 9, 8), new Date());
		System.out.println(dogs);
	}
	
	
	@Test
	void testKeyWord() {
		List<Dog> dogs = dogRepository.findByNameStartingWithAndIdLessThan("狼", 10);
		System.out.println(dogs);
		
		dogs = dogRepository.findByNameEndingWithAndIdLessThan("狗", 10);
		System.out.println(dogs);
	}
	
	
	/*
	 * 测试通过springdata获取数据，ok
	 */
	@Test
	void testSpringdata() {
		
		Dog dog = dogRepository.findByName("阿布拉多");
		System.out.println(dog);
	}

	/*
	 * 测试通过 jpa 生成数据表，ok
	 */
	@Test
	void testJPA() {

	}

	/*
	 * 测试数据库的连接，ok
	 */
	@Test
	void testDataSource() throws SQLException {
		DataSource dataSource = context.getBean(DataSource.class);
		System.out.println(dataSource.getConnection());
	}
}
