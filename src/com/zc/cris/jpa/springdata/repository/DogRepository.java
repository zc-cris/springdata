package com.zc.cris.jpa.springdata.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.zc.cris.jpa.springdata.dao.DogDao;
import com.zc.cris.jpa.springdata.entities.Dog;


/*
 * 1. Repository 是一个空接口，即标识接口
 * 2. 若我们的接口继承了 Repository 或其子接口，那么 该接口将被 ioc容器标示为一个 repository bean
 * 		并纳入到 IOC 容器中，进而可以在该接口中定义满足一定规范的方法
 * 
 * 3. 实际上，我们也可以通过 @RepositoryDefinition 注解来替代继承 Repository 接口
 * 	
 * - 如何在 repository 中声明方法：
 * 	1. 方法不是随便声明的，必须遵循一定的规范
 * 	2. 查询方法以 get/read/find 开头
 * 	3. 设计查询条件的时候，条件的属性需要用条件关键字连接
 * 	4. 注意：条件属性首字母大写
 * 
 * - 支持属性的级联查询，若当前类有符合条件的属性，那么优先使用当前类的属性，而不使用级联属性（dog有一个addressId的属性和Address的id属性重叠）
 * 		若需要使用级联属性，方法名字的属性之间需要使用 _ 分割开来；还有就是尽量避免类的成员属性名和级联对象的属性名重叠
 * 
 */
//@RepositoryDefinition(domainClass=Dog.class, idClass=Integer.class)
public interface DogRepository extends
	JpaRepository<Dog, Integer>, JpaSpecificationExecutor<Dog>, DogDao{
	
	// 根据Dog类的名字获取对应的dog数据
	Dog findByName(String name);
	
	// where name like ?% and id < ?
	List<Dog> findByNameStartingWithAndIdLessThan(String name, Integer id); 
	
	// where name like %? and id < ?
	List<Dog> findByNameEndingWithAndIdLessThan(String name, Integer id); 
	
	// where age in (?,?,?) or birth < ?
	List<Dog> findByAgeInOrBirthLessThan(List<Integer> ages, Date birth); 
	
	// where address.id = ?
	List<Dog> findByAddressId(Integer id);
	
	// 查询id值最大的那个 Person（子查询）
	// 使用 @Query 注解可以自定义 JPQL 语句得以实现更加灵活的查询
	@Query("select d from Dog d where d.id = (select max(d2.id) from Dog d2)")
	Dog findMaxIdDog();
	

	// 使用占位符的方式将方法的参数传递到 jpql 语句中，但是位置固定，强烈不建议这么做
	@Query(" select d from Dog d where d.name = ?1 and d.age = ?2")
	List<Dog> testQueryAnnotationParams1(String name, Integer age);
	
	// 使用命名参数的形式为 jpql 语句传递参数，推荐
	@Query(" select d from Dog d where d.name = :name and d.age = :age")
	List<Dog> testQueryAnnotationParams2(@Param("age")Integer age, @Param("name")String name);
	
	
	// 使用 @Query 注解进行模糊查询
	@Query("select d from Dog d where d.name like %:name%")
	List<Dog> testQueryAnnotationParams3(@Param("name") String name);
	
	// 使用 本地sql 进行查询
	@Query(value="select count(id) from JS_DOGS", nativeQuery=true)
	Long queryCount();

	
	// 修改数据，通过自定义的 jpql 完成update 和delete 操作，但是jpql 不支持 insert操作
	// 在 @Query 注解中写 jpql 语句，但是必须使用 @Modifying 通知springdata 使用的是update或者delete操作
	// 并且update 或 delete 操作必须在 service层的事务方法上才可以操作
	// 默认情况下，springdata的每个方法上都有事务，但是这个事务是只读性质的，无法完成数据修改操作！
	@Modifying
	@Query("update Dog d set d.age = :age where d.id = :id")
	void updateDogAge(@Param("age") Integer age, @Param("id") Integer id);
	

}





