package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends CrudRepository <Assignment, Integer> {

	@Query("select a from Assignment a where a.needsGrading=1 and a.dueDate < current_date and a.course.instructor= :email order by a.id")
	List<Assignment> findNeedGradingByEmail(@Param("email") String email);
	
	@Query("select a from Assignment a where a.id = :id and a.name = :name")
	Assignment findAssignmentByIdAndName(@Param("id")int id, @Param("name")String name);
	
   @Query("select a from Assignment a where a.name = :name")
   Assignment findAssignmentByName(@Param("name")String name);
	
	@Modifying
	@Query("delete from Assignment a where a.name = :name")
	void deleteByName(@Param("name") String name);
	
   void deleteById(Integer id);
}