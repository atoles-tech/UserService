package atl.web.user_service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import atl.web.user_service.dto.CardInfoDto;
import atl.web.user_service.dto.CardInfoResponseDto;
import atl.web.user_service.dto.UserDto;
import atl.web.user_service.dto.UserResponseDto;
import atl.web.user_service.repositories.CardInfoRepository;
import atl.web.user_service.repositories.UserRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CardInfoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17")
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
    void clearDb() {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Long createTestUser(String email) throws Exception {
        UserDto userDto = UserDto.builder()
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1000, 1, 1))
                .email(email)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andReturn();

        UserResponseDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class);
        return user.getId();
    }

    @Test
    @DisplayName("Should create card for user")
    @WithMockUser(roles = "ADMIN")
    void createCard_ShouldCreateCardForUser() throws Exception {
        Long userId = createTestUser("user@gmail.com");
        CardInfoDto cardDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isOk())
                .andReturn();

        CardInfoResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.getId() > 0);
        assertEquals("1234567812345678", response.getNumber());
        assertEquals("name surname", response.getHolder());
        assertEquals(LocalDate.of(2027, 12, 12), response.getExpirationDate());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should return card by id")
    @WithMockUser(roles = "ADMIN")
    void getCardById_ShouldReturnCard_WhenCardExists() throws Exception {
        Long userId = createTestUser("user@gmail.com");
        CardInfoDto cardDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)))
                .andReturn();

        CardInfoResponseDto createdCard = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        MvcResult getResult = mockMvc.perform(get("/api/v1/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andReturn();

        CardInfoResponseDto response = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        assertEquals(createdCard.getId(), response.getId());
        assertEquals("1234567812345678", response.getNumber());
        assertEquals("name surname", response.getHolder());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should return all cards for user")
    @WithMockUser(roles = "ADMIN")
    void getCardsByUserId_ShouldReturnUserCards() throws Exception {
        Long userId = createTestUser("user@gmail.com");

        CardInfoDto card1 = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        CardInfoDto card2 = CardInfoDto.builder()
                .number("8765432187654321")
                .holder("name surname")
                .expirationDate(LocalDate.of(2026, 6, 6))
                .build();

        mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(card1)));

        mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(card2)));

        MvcResult result = mockMvc.perform(get("/api/v1/users/{userId}/cards", userId))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        List<CardInfoResponseDto> cards = objectMapper.readValue(
                responseJson,
                new TypeReference<List<CardInfoResponseDto>>() {});

        assertEquals(2, cards.size());
        
        boolean hasCard1 = cards.stream().anyMatch(card -> "1234567812345678".equals(card.getNumber()));
        boolean hasCard2 = cards.stream().anyMatch(card -> "8765432187654321".equals(card.getNumber()));
        
        assertTrue(hasCard1);
        assertTrue(hasCard2);
        
        for (CardInfoResponseDto card : cards) {
            assertEquals(userId, card.getUserId());
        }
    }

    @Test
    @DisplayName("Should return paginated cards for user")
    @WithMockUser(roles = "ADMIN")
    void getCardsByUserId_ShouldReturnPaginatedCards() throws Exception {
        Long userId = createTestUser("user@gmail.com");

        CardInfoDto card1 = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        CardInfoDto card2 = CardInfoDto.builder()
                .number("8765432187654321")
                .holder("name surname")
                .expirationDate(LocalDate.of(2026, 6, 6))
                .build();

        mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(card1)));

        mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(card2)));

        MvcResult result = mockMvc.perform(get("/api/v1/users/{userId}/cards?page=0&size=1", userId))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        assertNotNull(responseJson);
    }

    @Test
    @DisplayName("Should return card by number for user")
    @WithMockUser(roles = "ADMIN")
    void getCardsByUserId_ShouldReturnCardByNumber() throws Exception {
        Long userId = createTestUser("user@gmail.com");

        CardInfoDto cardDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)));

        MvcResult result = mockMvc.perform(get("/api/v1/users/{userId}/cards?number=1234567812345678", userId))
                .andExpect(status().isOk())
                .andReturn();

        CardInfoResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        assertEquals("1234567812345678", response.getNumber());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should return card by number globally")
    @WithMockUser(roles = "ADMIN")
    void getCardByNumber_ShouldReturnCard() throws Exception {
        Long userId = createTestUser("user@gmail.com");

        CardInfoDto cardDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)));

        MvcResult result = mockMvc.perform(get("/api/v1/cards?number=1234567812345678"))
                .andExpect(status().isOk())
                .andReturn();

        CardInfoResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        assertEquals("1234567812345678", response.getNumber());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should update card")
    @WithMockUser(roles = "ADMIN")
    void updateCard_ShouldUpdateCard() throws Exception {
        Long userId = createTestUser("user@gmail.com");
        CardInfoDto createDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name surname")
                .expirationDate(LocalDate.of(2027, 10, 10))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();

        CardInfoResponseDto createdCard = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        CardInfoDto updateDto = CardInfoDto.builder()
                .number("8765432187654321")
                .holder("nam surnam")
                .expirationDate(LocalDate.of(2027, 6, 6))
                .build();

        MvcResult updateResult = mockMvc.perform(put("/api/v1/cards/{id}", createdCard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn();

        CardInfoResponseDto response = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        assertEquals(createdCard.getId(), response.getId());
        assertEquals("8765432187654321", response.getNumber());
        assertEquals("nam surnam", response.getHolder());
        assertEquals(LocalDate.of(2027, 6, 6), response.getExpirationDate());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should delete card")
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldDeleteCard() throws Exception {
        Long userId = createTestUser("user@gmail.com");
        CardInfoDto cardDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("user surname")
                .expirationDate(LocalDate.of(2027, 12, 12))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/users/{userId}/cards", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)))
                .andReturn();

        CardInfoResponseDto createdCard = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CardInfoResponseDto.class);

        mockMvc.perform(delete("/api/v1/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/cards/{id}", createdCard.getId()))
                .andExpect(status().isNotFound());
    }

}