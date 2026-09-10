package br.com.exemplo.crud;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
class ProductIT {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Test void completeLifecycle() throws Exception {
        String body = mvc.perform(post("/api/products").contentType("application/json")
            .content("{\"name\":\"Teclado\",\"price\":100.00}"))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();
        mvc.perform(get("/api/products/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Teclado"));
        mvc.perform(put("/api/products/" + id).contentType("application/json")
            .content("{\"name\":\"Mouse\",\"price\":50.00}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Mouse"));
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        mvc.perform(delete("/api/products/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }
    @Test void rejectsInvalidPagination() throws Exception {
        mvc.perform(get("/api/products?size=0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products?size=101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products?page=-1")).andExpect(status().isBadRequest());
    }
    @Test void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/products").contentType("application/json")
            .content("{\"name\":\"\",\"price\":-1}"))
            .andExpect(status().isBadRequest());
    }
}
