package com.siemens.internship;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper objectMapper;

    // Test the POST request for valid input
    @Test
    public void testCreateItem_ValidInput_ReturnsCreated() throws Exception {
        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setStatus("unprocessed");
        item.setEmail("test@gmail.com");

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName(item.getName());
        savedItem.setDescription(item.getDescription());
        savedItem.setStatus(item.getStatus());
        savedItem.setEmail(item.getEmail());

        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("test item"));
    }

    // Test the POST request for incorrect email address
    @Test
    public void testCreateItem_InvalidEmail_ReturnsBadRequest() throws Exception {
        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setStatus("unprocessed");
        item.setEmail("invalid.email"); // Should fail the validate() check

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    //Test POST request for
    @Test
    public void testCreateItem_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        Item item = new Item();

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetItemById_Found() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setName("test item");
        item.setDescription("test description");
        item.setStatus("unprocessed");
        item.setEmail("test@gmail.com");

        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("test item"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    public void testGetItemById_NotFound() throws Exception {
        when(itemService.findById(23L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/23")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateItem_ExistingItem_ReturnsCreated() throws Exception {
        Long itemId = 1L;

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("test item");
        existingItem.setDescription("test description");
        existingItem.setStatus("unprocessed");
        existingItem.setEmail("test@gmail.com");

        Item updatedItem = new Item();
        updatedItem.setName("new test item");
        updatedItem.setDescription("new test description");
        updatedItem.setStatus("new unprocessed");
        updatedItem.setEmail("newTest@gmail.com.com");

        Item savedItem = new Item();
        savedItem.setId(itemId);
        savedItem.setName(updatedItem.getName());
        savedItem.setDescription(updatedItem.getDescription());
        savedItem.setStatus(updatedItem.getStatus());
        savedItem.setEmail(updatedItem.getEmail());

        when(itemService.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        mockMvc.perform(put("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("new test item"))
                .andExpect(jsonPath("$.email").value("newTest@gmail.com.com"));
    }

    @Test
    public void testUpdateItem_NotFound_ReturnsNoContent() throws Exception {
        Long itemId = 23L;

        Item updatedItem = new Item();
        updatedItem.setName("new test item");
        updatedItem.setDescription("new test description");
        updatedItem.setStatus("new unprocessed");
        updatedItem.setEmail("newTest@gmail.com.com");

        when(itemService.findById(itemId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteItem_ExistingItem_ReturnsOk() throws Exception {
        Long itemId = 1L;

        Item item = new Item();
        item.setId(itemId);

        when(itemService.findById(itemId)).thenReturn(Optional.of(item));
        doNothing().when(itemService).deleteById(itemId);

        mockMvc.perform(delete("/api/items/{id}", itemId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteById(itemId);
    }

    @Test
    public void testDeleteItem_NotFound_ReturnsNoContent() throws Exception {
        Long itemId = 23L;

        when(itemService.findById(itemId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/items/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(itemService, never()).deleteById(anyLong());
    }



}
