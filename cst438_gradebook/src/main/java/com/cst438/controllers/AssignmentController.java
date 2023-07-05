package com.cst438.controllers;

//import java.util.ArrayList;
//import java.util.List;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.services.RegistrationService;

@RestController
public class AssignmentController
{
   
   @Autowired
   AssignmentRepository assignmentRepository;
   
   @Autowired
   AssignmentGradeRepository assignmentGradeRepository;
   
   @Autowired
   CourseRepository courseRepository;
   
   @Autowired
   RegistrationService registrationService;
   
   @PostMapping("/gradebook/{course_id}")
   @Transactional
   public void addAssignment (@RequestBody AssignmentListDTO.AssignmentDTO assignmentDTO, 
                              @PathVariable("course_id") int courseId ) {
      // Check to see if the course exists
      Course course = courseRepository.findById(courseId).orElse(null);
      if ( course == null) {
         throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Course not found.");
      }
      // Creates the new assignment object and assigns the name from JSON body
      Assignment assignment = new Assignment();
      assignment.setName(assignmentDTO.assignmentName);
      
      // Manipulate the date datatype in order to parse it correctly to SQL* not working for yyyyy, may need to use regex
      SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-mm-dd");
      Date dueDate;
      try {
         dueDate = simpleDate.parse(assignmentDTO.dueDate);
         java.sql.Date sqlDate = new java.sql.Date(dueDate.getTime());
         assignment.setDueDate(sqlDate);
      } catch (ParseException pe) {
         throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Invalid date format, should be: yyyy-mm-dd");
      }
      // sets the Course for the new assignment and sets NeedsGrading to 1
      assignment.setCourse(course);
      assignment.setNeedsGrading(1); // 0 = false,  1= true (past due date and not all students have grades)

      assignmentRepository.save(assignment);
   }
   
   @PatchMapping("/gradebook/{id}/{name}")
   @Transactional
   public void updateAssignmentName(@PathVariable("id") Integer assignmentId, @PathVariable("name") String newName) {
      // the {name} refers to the updated name 
      // The checkAssignment method will throw the bad request if assignment not found
      String email = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
      Assignment assignment = checkAssignment(assignmentId, email);  // check that user name matches instructor email of the course.     

      assignment.setName(newName);
      System.out.printf("%s\n", assignment.toString());
      
      assignmentRepository.save(assignment);
   } 
   
   @DeleteMapping("/gradebook/{assignment_id}/{course_id}")
   @Transactional
   public void removeAssignment(@PathVariable("assignment_id") Integer assignment_id, 
                                @PathVariable("course_id") Integer course_id) {    
      // The checkAssignment method will throw the bad request if assignment not found
      String email = "dwisneski@csumb.edu";  // user name (should be instructor's email) 
      Assignment assignment = checkAssignment(assignment_id, email);  // check that user name matches instructor email of the course.
      
      // Checks to see if course exists
      Course course = courseRepository.findById(course_id).orElse(null);
      if ( course == null) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
      }
      
      // checks to see if grades exist, cannot delete assignment with grades
      boolean gradeCheck = assignmentGradeRepository.existsByAssignment(assignment);
      if (gradeCheck) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete assignment with existing grades. AssignmentId:"+assignment_id );
      }
      
      assignmentRepository.delete(assignment);
      /* Kevin notes
      I updated the Assignment.java file to include import javax.persistence.CascadeType; and
      @OneToMany(mappedBy="assignment", cascade = CascadeType.REMOVE)
      */
   }
   
   private Assignment checkAssignment(int assignmentId, String email) {
      // get assignment 
      Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
      if (assignment == null) {
         throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment not found. "+assignmentId );
      }
      // check that user is the course instructor
      if (!assignment.getCourse().getInstructor().equals(email)) {
         throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
      }      
      return assignment;
   }
}
