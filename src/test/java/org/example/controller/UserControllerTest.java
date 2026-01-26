package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.UserDTO;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Используем стандартный ObjectMapper из Spring
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllUsers() throws Exception {
        UserDTO user = new UserDTO(1L, "Ivan", "ivan@mail.com", 20);
        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                // В HATEOAS (CollectionModel) данные лежат в _embedded.userDTOList
                .andExpect(jsonPath("$._embedded.userDTOList.length()").value(1))
                .andExpect(jsonPath("$._embedded.userDTOList[0].name").value("Ivan"))
                // Проверяем наличие ссылок в коллекции
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldReturnUserById() throws Exception {
        UserDTO user = new UserDTO(1L, "Ivan", "ivan@mail.com", 20);
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@mail.com"))
                // Проверяем HATEOAS ссылки для конкретного юзера
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/users/1"))
                .andExpect(jsonPath("$._links.delete.href").exists())
                .andExpect(jsonPath("$._links.all-users.href").exists());
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Если твой сервис кидает RuntimeException, когда юзер не найден
        when(userService.findById(99L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserDTO inputUser = UserDTO.builder().name("New").email("new@mail.com").age(25).build();
        UserDTO savedUser = UserDTO.builder().id(1L).name("New").email("new@mail.com").age(25).build();

        when(userService.createUser(any(UserDTO.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}