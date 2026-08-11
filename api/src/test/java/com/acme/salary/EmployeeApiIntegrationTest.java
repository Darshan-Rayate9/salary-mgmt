package com.acme.salary;

import com.acme.salary.dto.EmployeeCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full Spring MVC stack (real security filter chain, real query-param
 * binding, real JSON) via MockMvc - in-process, so it stays fast and, with
 * @Transactional, correctly rolls back per test method (a real socket-based
 * HTTP client would run the request on a different thread, outside the
 * test's transaction, tried first and dropped for exactly that reason).
 *
 * Auth is HTTP Basic (see SecurityConfig); {@code httpBasic(...)} attaches the
 * in-memory HR Manager credentials from src/test/resources/application.yml.
 *
 * Added after actually curling the running app and finding that ?limit= was
 * silently ignored (Spring's own default page-size param name is "size", not
 * "limit") - nothing below this layer could have caught that, since every
 * other test calls the service/repository layer directly with an
 * already-constructed Pageable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeApiIntegrationTest {

    private static final String USER = "test.admin";
    private static final String PASSWORD = "TestPassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listEmployees_withoutAuth_returns401WithErrorContract() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    @Test
    void listEmployees_limitQueryParam_isActuallyRespected() throws Exception {
        createEmployee("E-API001");
        createEmployee("E-API002");
        createEmployee("E-API003");

        // Three employees exist; a limit smaller than that must cap the page.
        mockMvc.perform(get("/api/v1/employees?page=1&limit=2").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.limit").value(2));
    }

    @Test
    void createThenGetEmployee_roundTripsThroughRealHttp() throws Exception {
        String created = createEmployee("E-API100");
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/v1/employees/" + id).with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("E-API100"))
                .andExpect(jsonPath("$.currentSalaryAmount").doesNotExist());
    }

    @Test
    void createEmployee_duplicateCode_returns409WithErrorContract() throws Exception {
        createEmployee("E-API200");

        mockMvc.perform(post("/api/v1/employees")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest("E-API200"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_ENTRY"));
    }

    @Test
    void getEmployee_unknownId_returns404WithErrorContract() throws Exception {
        mockMvc.perform(get("/api/v1/employees/999999").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void getEmployee_nonNumericId_returns400NotA500() throws Exception {
        mockMvc.perform(get("/api/v1/employees/not-a-number").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST_PARAM"));
    }

    @Test
    void unmappedUrl_returns404NotA500() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/outliers").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    // A GET to a POST-only endpoint used to fall through to the generic Exception handler and come
    // back as a bare 500 - the catch-all was swallowing Spring's own HttpRequestMethodNotSupportedException
    // instead of letting it be a 405 (see GlobalExceptionHandler).
    @Test
    void unsupportedHttpMethod_returns405NotA500() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/summary").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
    }

    // Same category: a missing/malformed JSON body was also falling through to a bare 500.
    @Test
    void malformedJsonBody_returns400NotA500() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST_BODY"));
    }

    private String createEmployee(String code) throws Exception {
        return mockMvc.perform(post("/api/v1/employees")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest(code))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private EmployeeCreateRequest sampleCreateRequest(String code) {
        return new EmployeeCreateRequest(
                code, "Test", "Employee", code.toLowerCase() + "@acme.test", "Engineering", "Engineer",
                "L3", "United States", "USD", LocalDate.of(2022, 1, 1));
    }
}
