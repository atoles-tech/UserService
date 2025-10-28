package atl.web.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;

import atl.web.user_service.dto.UserDto;
import atl.web.user_service.dto.UserResponseDto;
import atl.web.user_service.repositories.UserRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = 
            new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.cache.type", () -> "none");
    }

    @AfterAll
    static void closeContainer() {
        postgreSQLContainer.close();
    }

    @AfterEach
    void clearDb(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create and return user")
    void createUser_ShouldCreateAndReturnUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Name")
                .surname("Surname")
                .birthDate(LocalDate.of(1000, 1, 1))
                .email("email@gmail.com")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn();

        UserResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertNotNull(response);
        assertEquals("Name", response.getName());
        assertEquals("Surname", response.getSurname());
        assertEquals("email@gmail.com", response.getEmail());
        assertEquals(LocalDate.of(1000, 1, 1), response.getBirthDate());
    }

    @Test
    @DisplayName("Should return user if it exists")
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Name")
                .surname("Surname")
                .birthDate(LocalDate.of(1000, 1, 1))
                .email("email@gmail.com")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn();

        UserResponseDto createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        MvcResult getResult = mockMvc.perform(get("/api/v1/users/{id}", createdUser.getId()))
                .andReturn();

        UserResponseDto response = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertEquals(createdUser.getId(), response.getId());
        assertEquals("Name", response.getName());
        assertEquals("Surname", response.getSurname());
        assertEquals("email@gmail.com", response.getEmail());
        assertEquals(LocalDate.of(1000, 1, 1), response.getBirthDate());
    }

    @Test
    @DisplayName("Should return errorResponse if user not exists")
    void getUserById_ShouldReturnError_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/id/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return user by email")
    void getUserByEmail_ShouldReturnUser_WhenEmailExists() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1000, 1, 1))
                .email("email@gmail.com")
                .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)));

        MvcResult result = mockMvc.perform(get("/api/v1/users")
                .param("email","email@gmail.com"))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertEquals("email@gmail.com", response.getEmail());
    }

    @Test
    @DisplayName("Should update user if it exists")
    void updateUser_ShouldUpdateAndReturnUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1000, 1, 1))
                .email("email@gmail.com")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn();

        UserResponseDto createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        UserDto updateDto = UserDto.builder()
                .name("name")
                .surname("sname")
                .birthDate(LocalDate.of(2000, 1, 1))
                .email("email@gmail.com")
                .build();

        MvcResult updateResult = mockMvc.perform(put("/api/v1/users/{id}", createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto response = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        assertEquals(createdUser.getId(), response.getId());
        assertEquals("name", response.getName());
        assertEquals("email@gmail.com", response.getEmail());
        assertEquals(LocalDate.of(2000, 1, 1), response.getBirthDate());
    }

    @Test
    @DisplayName("Should delete user if it exists")
    void deleteUser_ShouldDeleteUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1000, 1, 1))
                .email("email@gmail.com")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn();

        UserResponseDto createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                UserResponseDto.class);

        mockMvc.perform(delete("/api/v1/users/{id}", createdUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/id/{id}", createdUser.getId()))
                .andExpect(status().isNotFound());
    }

    
        @Test
        @DisplayName("Should return paginated users sorted by name ascending")
        void findUsersByPage_ShouldReturnPaginatedList_SortedAsc() throws Exception {
            for (int i = 1; i <= 5; i++) {
                UserDto user = UserDto.builder()
                        .name("User" + i)
                        .surname("Surname" + i)
                        .birthDate(LocalDate.of(2000, 1, i))
                        .email("user" + i + "@example.com")
                        .build();

                mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                        .andExpect(status().isCreated());
            }

            MvcResult result = mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "3")
                .param("sortBy", "name")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            assertTrue(jsonResponse.contains("User1"));
            assertTrue(jsonResponse.contains("User2"));
            assertTrue(jsonResponse.contains("User3"));
        }

        @Test
        @DisplayName("Should return paginated users sorted by name descending")
        void findUsersByPage_ShouldReturnPaginatedList_SortedDesc() throws Exception {
            for (int i = 1; i <= 3; i++) {
                        UserDto user = UserDto.builder()
                                .name("User" + i)
                                .surname("Surname" + i)
                                .birthDate(LocalDate.of(2000, 1, i))
                                .email("user" + i + "@example.com")
                                .build();

                        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isCreated());
            }

            MvcResult result = mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "3")
                .param("sortBy", "name")
                .param("direction", "desc"))
                .andExpect(status().isOk())
                .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            assertTrue(jsonResponse.contains("User1"));
            assertTrue(jsonResponse.contains("User2"));
            assertTrue(jsonResponse.contains("User3"));
        }
}