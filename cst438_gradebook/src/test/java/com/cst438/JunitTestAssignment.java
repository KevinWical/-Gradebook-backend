package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.cst438.controllers.AssignmentController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
//import com.cst438.domain.Enrollment;
//import com.cst438.domain.GradebookDTO;
import com.cst438.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ContextConfiguration;

/* 
 * Example of using Junit with Mockito for mock objects
 *  the database repositories are mocked with test data.
 *  
 * Mockmvc is used to test a simulated REST call to the RestController
 * 
 * the http response and repository is verified.
 * 
 *   Note: This tests uses Junit 5.
 *  ContextConfiguration identifies the controller class to be tested
 *  addFilters=false turns off security.  (I could not get security to work in test environment.)
 *  WebMvcTest is needed for test environment to create Repository classes.
 */
@ContextConfiguration(classes = { AssignmentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestAssignment {

   static final String URL = "http://localhost:8080";
   public static final int TEST_COURSE_ID_VALID = 40442;
   public static final int TEST_COURSE_ID_INVALID = 999999;
   public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
   public static final String TEST_STUDENT_NAME = "test student name";
   public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
   public static final int TEST_YEAR = 2021;
   public static final String TEST_SEMESTER = "Fall";
   public static final String TEST_DUE_DATE = "2023-12-25";
   public static final int TEST_ASSIGNMET_ID_VALID = 1;
   public static final int TEST_ASSIGNMET_ID_INVALID = 99999;
   public static final String TEST_ASSIGNMENT_NAME = "Test Assignment name";
   public static final String TEST_ASSIGNMENT_NAME_OLD = "Old Assignment Name";
   
   @MockBean
   AssignmentRepository assignmentRepository;

   @MockBean
   AssignmentGradeRepository assignmentGradeRepository;

   @MockBean
   CourseRepository courseRepository; // must have this to keep Spring test happy

   @MockBean
   RegistrationService registrationService; // must have this to keep Spring test happy

   @Autowired
   private MockMvc mvc;
   // Kevins API tests
   @Test
   public void testAddAssignmentSuccess() throws Exception {
      // Given proper courseId, name, and dueDate

      // Mock
      AssignmentListDTO.AssignmentDTO assignmentDTO = new AssignmentListDTO.AssignmentDTO();
      assignmentDTO.dueDate = TEST_DUE_DATE;
      assignmentDTO.assignmentName = "TestAddAssignmentSuccess";
      Course course = new Course();
      course.setCourse_id(TEST_COURSE_ID_VALID);

      given(courseRepository.findById(TEST_COURSE_ID_VALID)).willReturn(Optional.of(course));

      // Convert assignmentDTO to JSON string
      String requestBody = asJsonString(assignmentDTO);

      System.out.println("Request Body: " + requestBody);
      // Perform the request
      MvcResult result = mvc.perform(MockMvcRequestBuilders
            .post("/gradebook/{id}", TEST_COURSE_ID_VALID)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andReturn();
      
      assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
      verify(assignmentRepository, times(1)).save(any());
   }
   
   @Test
   public void testAddAssignmentCourseNotFound() throws Exception{
      // given improper TEST_COURSE_ID
      
      // Mock
      AssignmentListDTO.AssignmentDTO assignmentDTO = new AssignmentListDTO.AssignmentDTO();
      assignmentDTO.assignmentName = TEST_ASSIGNMENT_NAME;
      assignmentDTO.dueDate = TEST_DUE_DATE;
      
      given(courseRepository.findById(TEST_COURSE_ID_INVALID)).willReturn(Optional.empty());
      
      // perform request
      mvc.perform(MockMvcRequestBuilders.post("/gradebook/{id}", TEST_COURSE_ID_INVALID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(assignmentDTO) ) )
            .andExpect(status().is4xxClientError());
      
      verify(assignmentRepository, times(0)).save(any());
   }
   
   @Test
   public void testUpdateAssignmentNameSuccess() throws Exception{
      // Given proper assignment ID and newName
      
      // Mock 
      Assignment assignment = new Assignment();
      assignment.setId(TEST_COURSE_ID_VALID);
      assignment.setName(TEST_ASSIGNMENT_NAME_OLD);    
      Course course = new Course();
      course.setInstructor(TEST_INSTRUCTOR_EMAIL);
      assignment.setCourse(course);
      
      given(assignmentRepository.findById(TEST_COURSE_ID_VALID)).willReturn(Optional.of(assignment));

      // Peform request
      mvc.perform(MockMvcRequestBuilders.patch("/gradebook/{id}/{name}", TEST_COURSE_ID_VALID, TEST_ASSIGNMENT_NAME)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
      
      Assignment updatedAssignment = assignmentRepository.findById(TEST_COURSE_ID_VALID).orElse(null);
      
      assertNotNull(updatedAssignment);
      assertEquals(TEST_ASSIGNMENT_NAME, updatedAssignment.getName());
      verify(assignmentRepository, times(1)).save(any());
   }
   
   @Test
   public void testUpdateAssignmentNameNotFound() throws Exception{
      // Given invalid assignmentId
      
      // Mock 
      Assignment assignment = new Assignment();
      assignment.setId(TEST_COURSE_ID_INVALID);
      assignment.setName(TEST_ASSIGNMENT_NAME_OLD);
      
      given(assignmentRepository.findById(TEST_COURSE_ID_INVALID)).willReturn(Optional.empty());
      
      // Perform request
      mvc.perform(MockMvcRequestBuilders.patch("/gradebook/{id}/{name}", TEST_COURSE_ID_INVALID, TEST_ASSIGNMENT_NAME)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
      
      verify(assignmentRepository, times(0)).save(any());
   }
   
   @Test
   public void testUpdateAssignmentNameEmptyName() throws Exception{
      // Given an empty string for a new name
      
      // Mock 
      Assignment assignment = new Assignment();
      assignment.setId(TEST_ASSIGNMET_ID_VALID);
      assignment.setName(TEST_ASSIGNMENT_NAME_OLD);
      
      // Perform request
      mvc.perform(MockMvcRequestBuilders.patch("/gradebook/{id}", TEST_ASSIGNMET_ID_VALID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
      
      verify(assignmentRepository, times(0)).save(any());
   }
     
   @Test
   public void testDeleteAssignmentSuccess() throws Exception {
      // given proper assignmentId and TEST_COURSE_ID
      
      // Mock
      Assignment assignment = new Assignment();
      assignment.setId(TEST_ASSIGNMET_ID_VALID);
      Course course = new Course();
      course.setInstructor(TEST_INSTRUCTOR_EMAIL);
      assignment.setCourse(course);
      
      given(assignmentRepository.findById(TEST_ASSIGNMET_ID_VALID)).willReturn(Optional.of(assignment));
      given(courseRepository.findById(TEST_COURSE_ID_VALID)).willReturn(Optional.of(course));
      given(assignmentGradeRepository.existsByAssignment(assignment)).willReturn(false);

      // Peform request
      mvc.perform(MockMvcRequestBuilders.delete("/gradebook/{assignment_id}/{course_id}", TEST_ASSIGNMET_ID_VALID, TEST_COURSE_ID_VALID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
      
      verify(assignmentRepository, times(1)).delete(any());
   }
   
   @Test
   public void testDeleteAssignmentAssignmentNotFound() throws Exception{
      // given invalid assignmentid 
      
      // Mock
      Assignment assignment = new Assignment();
      assignment.setId(TEST_ASSIGNMET_ID_INVALID);
      Course course = new Course();
      course.setInstructor(TEST_INSTRUCTOR_EMAIL);
      assignment.setCourse(course);
      
      given(assignmentRepository.findById(TEST_ASSIGNMET_ID_INVALID)).willReturn(Optional.empty());
      given(courseRepository.findById(TEST_COURSE_ID_VALID)).willReturn(Optional.of(course));
      given(assignmentGradeRepository.existsByAssignment(assignment)).willReturn(false);
      
      // perform request
      mvc.perform(MockMvcRequestBuilders.delete("/gradebook/{assignment_id}/{course_id}", TEST_ASSIGNMET_ID_INVALID, TEST_COURSE_ID_VALID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
      
      verify(assignmentRepository, times(0)).delete(any());
   }
   
   @Test
   public void testDeleteAssignmentCourseNotFound() throws Exception{
      // given invalid TEST_COURSE_ID
      
      // Mock
      Assignment assignment = new Assignment();
      assignment.setId(TEST_ASSIGNMET_ID_VALID);
      Course course = new Course();
      course.setInstructor(TEST_INSTRUCTOR_EMAIL);
      assignment.setCourse(course);
      
      given(assignmentRepository.findById(TEST_ASSIGNMET_ID_VALID)).willReturn(Optional.of(assignment));
      given(courseRepository.findById(TEST_COURSE_ID_INVALID)).willReturn(Optional.empty());
      given(assignmentGradeRepository.existsByAssignment(assignment)).willReturn(false);
      
      // perform request
      mvc.perform(MockMvcRequestBuilders.delete("/gradebook/{assignment_id}/{course_id}", TEST_ASSIGNMET_ID_VALID, TEST_COURSE_ID_INVALID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
      
      verify(assignmentRepository, times(0)).delete(any());
   }
   
   @Test
   public void testDeleteAssignmentGivenExistingGrades() throws Exception{
      // given existing grades
      
      // Mock
      Assignment assignment = new Assignment();
      assignment.setId(TEST_COURSE_ID_VALID);
      Course course = new Course();
      course.setInstructor(TEST_INSTRUCTOR_EMAIL);
      assignment.setCourse(course);  
      
      given(assignmentRepository.findById(TEST_COURSE_ID_VALID)).willReturn(Optional.of(assignment));
      given(courseRepository.findById(TEST_ASSIGNMET_ID_VALID)).willReturn(Optional.of(course));
      given(assignmentGradeRepository.existsByAssignment(assignment)).willReturn(true);

      // perform request
      mvc.perform(MockMvcRequestBuilders.delete("/gradebook/{assignment_id}/{course_id}", TEST_COURSE_ID_VALID, TEST_ASSIGNMET_ID_VALID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
      
      verify(assignmentRepository, times(0)).delete(any());
   }

   private static String asJsonString(final Object obj) {
      try {

         return new ObjectMapper().writeValueAsString(obj);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

//   private static <T> T fromJsonString(String str, Class<T> valueType) {
//      try {
//         return new ObjectMapper().readValue(str, valueType);
//      } catch (Exception e) {
//         throw new RuntimeException(e);
//      }
//   }

}
