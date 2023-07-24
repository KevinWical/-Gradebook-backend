package com.cst438.controllers;

//import java.util.ArrayList;
//import java.util.List;
//import java.util.Date;
import java.sql.Date;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentListDTO.AssignmentDTO;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.services.RegistrationService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
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
   
   @PostMapping("/gradebook")
   @Transactional
   public AssignmentDTO newAssignment(@RequestBody AssignmentDTO dto) {
      String email = "dwisneski@csumb.edu";
      Course c = courseRepository.findById(dto.courseId).orElse(null);
      if ( c != null && c.getInstructor().equals(email)) {
         Assignment a = new Assignment();
         a.setCourse(c);
         a.setName(dto.assignmentName);
         a.setDueDate(Date.valueOf(dto.dueDate));
         a.setNeedsGrading(1);
         a = assignmentRepository.save(a);
         dto.assignmentId = a.getId();
         return dto;
         
      } else {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invaid course ID");
         
      }
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
