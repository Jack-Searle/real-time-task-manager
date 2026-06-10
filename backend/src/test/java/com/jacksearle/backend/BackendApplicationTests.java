package com.jacksearle.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class BackendApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void registerReturnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jack",
                                  "lastName": "Searle",
                                  "email": "register@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.firstName").value("Jack"))
                .andExpect(jsonPath("$.user.lastName").value("Searle"))
                .andExpect(jsonPath("$.user.email").value("register@test.com"));
    }

    @Test
    void loginReturnsTokenAndInvalidCredentialsFail() throws Exception {
        register("login@test.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("login@test.com"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login@test.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void protectedRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndListBoardsWithDefaultColumns() throws Exception {
        String token = register("boards@test.com");

        mockMvc.perform(post("/api/boards")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "My Board"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("My Board"))
                .andExpect(jsonPath("$.columns", hasSize(3)))
                .andExpect(jsonPath("$.columns[0].name").value("To Do"))
                .andExpect(jsonPath("$.columns[1].name").value("In Progress"))
                .andExpect(jsonPath("$.columns[2].name").value("Done"));

        mockMvc.perform(get("/api/boards")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("My Board"));
    }

    @Test
    void boardMembershipAndOwnerPermissionsAreEnforced() throws Exception {
        String ownerToken = register("owner@test.com");
        String memberToken = register("member@test.com");
        long boardId = createBoard(ownerToken, "Private Board").get("id").asLong();

        mockMvc.perform(get("/api/boards/" + boardId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void createMoveAndDeleteTask() throws Exception {
        String token = register("tasks@test.com");
        JsonNode board = createBoard(token, "Task Board");
        long todoColumnId = board.get("columns").get(0).get("id").asLong();
        long inProgressColumnId = board.get("columns").get(1).get("id").asLong();

        JsonNode task = performJson(post("/api/columns/" + todoColumnId + "/tasks")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Build MVP",
                          "description": "Ship backend",
                          "priority": "HIGH"
                        }
                        """), 201);

        long taskId = task.get("id").asLong();
        mockMvc.perform(get("/api/boards/" + board.get("id").asLong())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].tasks", hasSize(1)))
                .andExpect(jsonPath("$.columns[0].tasks[0].title").value("Build MVP"));

        mockMvc.perform(put("/api/tasks/" + taskId + "/move")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "columnId": %d,
                                  "position": 0
                                }
                                """.formatted(inProgressColumnId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnId").value(inProgressColumnId))
                .andExpect(jsonPath("$.position").value(0));

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    private String register(String email) throws Exception {
        JsonNode response = performJson(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName": "Test",
                          "lastName": "User",
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(email)), 200);
        return response.get("token").asText();
    }

    private JsonNode createBoard(String token, String name) throws Exception {
        return performJson(post("/api/boards")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "%s"
                        }
                        """.formatted(name)), 201);
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder request, int expectedStatus) throws Exception {
        String response = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
