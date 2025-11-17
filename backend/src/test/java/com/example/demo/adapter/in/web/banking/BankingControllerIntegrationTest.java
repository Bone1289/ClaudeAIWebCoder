package com.example.demo.adapter.in.web.banking;

import com.example.demo.adapter.in.web.banking.dto.CreateAccountRequest;
import com.example.demo.adapter.in.web.banking.dto.DepositRequest;
import com.example.demo.adapter.in.web.banking.dto.UpdateAccountRequest;
import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.domain.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BankingController
 * Tests the complete integration of Controller -> Service -> Repository
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Banking Controller Integration Tests")
class BankingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Create Account Integration Tests")
    class CreateAccountIntegrationTests {

        @Test
        @DisplayName("Should create account and return 201 Created")
        void shouldCreateAccountAndReturn201() throws Exception {
            // Given
            CreateAccountRequest request = new CreateAccountRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setNationality("United States");
            request.setAccountType("CHECKING");

            // When & Then
            mockMvc.perform(post("/api/banking/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.nationality").value("United States"))
                .andExpect(jsonPath("$.data.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.data.balance").value(0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should return 400 when creating account with invalid account type")
        void shouldReturn400WhenCreatingAccountWithInvalidAccountType() throws Exception {
            // Given
            CreateAccountRequest request = new CreateAccountRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setNationality("USA");
            request.setAccountType("INVALID");

            // When & Then
            mockMvc.perform(post("/api/banking/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 400 when creating account with missing required fields")
        void shouldReturn400WhenCreatingAccountWithMissingFields() throws Exception {
            // Given - Request with missing lastName
            CreateAccountRequest request = new CreateAccountRequest();
            request.setFirstName("John");
            request.setNationality("USA");
            request.setAccountType("CHECKING");

            // When & Then
            mockMvc.perform(post("/api/banking/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Account Integration Tests")
    class GetAccountIntegrationTests {

        @Test
        @DisplayName("Should get all accounts")
        void shouldGetAllAccounts() throws Exception {
            // Given - Create two accounts first
            createTestAccount("Alice", "Johnson");
            createTestAccount("Bob", "Smith");

            // When & Then
            mockMvc.perform(get("/api/banking/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("Should get account by ID")
        void shouldGetAccountById() throws Exception {
            // Given - Create an account
            String accountId = createTestAccount("Jane", "Doe");

            // When & Then
            mockMvc.perform(get("/api/banking/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(accountId))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
        }

        @Test
        @DisplayName("Should return 404 when account not found")
        void shouldReturn404WhenAccountNotFound() throws Exception {
            // Given
            String nonExistentId = "00000000-0000-0000-0000-000000000000";

            // When & Then
            mockMvc.perform(get("/api/banking/accounts/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Update Account Integration Tests")
    class UpdateAccountIntegrationTests {

        @Test
        @DisplayName("Should update account type successfully")
        void shouldUpdateAccountTypeSuccessfully() throws Exception {
            // Given - Create an account
            String accountId = createTestAccount("John", "Doe");

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setAccountType("SAVINGS");

            // When & Then
            mockMvc.perform(put("/api/banking/accounts/" + accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountType").value("SAVINGS"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent account")
        void shouldReturn404WhenUpdatingNonExistentAccount() throws Exception {
            // Given
            String nonExistentId = "00000000-0000-0000-0000-000000000000";
            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setAccountType("SAVINGS");

            // When & Then
            mockMvc.perform(put("/api/banking/accounts/" + nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Deposit Integration Tests")
    class DepositIntegrationTests {

        @Test
        @DisplayName("Should deposit money successfully")
        void shouldDepositMoneySuccessfully() throws Exception {
            // Given - Create an account
            String accountId = createTestAccount("John", "Doe");

            DepositRequest request = new DepositRequest();
            request.setAmount(new BigDecimal("1000.00"));

            // When & Then
            mockMvc.perform(post("/api/banking/accounts/" + accountId + "/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(1000.00));
        }

        @Test
        @DisplayName("Should return 400 when depositing negative amount")
        void shouldReturn400WhenDepositingNegativeAmount() throws Exception {
            // Given
            String accountId = createTestAccount("John", "Doe");

            DepositRequest request = new DepositRequest();
            request.setAmount(new BigDecimal("-100.00"));

            // When & Then
            mockMvc.perform(post("/api/banking/accounts/" + accountId + "/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Delete Account Integration Tests")
    class DeleteAccountIntegrationTests {

        @Test
        @DisplayName("Should delete account with zero balance")
        void shouldDeleteAccountWithZeroBalance() throws Exception {
            // Given
            String accountId = createTestAccount("John", "Doe");

            // When & Then
            mockMvc.perform(delete("/api/banking/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account deleted successfully"));

            // Verify account is deleted
            mockMvc.perform(get("/api/banking/accounts/" + accountId))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when deleting account with non-zero balance")
        void shouldReturn400WhenDeletingAccountWithNonZeroBalance() throws Exception {
            // Given - Create account and deposit money
            String accountId = createTestAccount("John", "Doe");

            DepositRequest depositRequest = new DepositRequest();
            depositRequest.setAmount(new BigDecimal("1000.00"));

            mockMvc.perform(post("/api/banking/accounts/" + accountId + "/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)));

            // When & Then - Try to delete
            mockMvc.perform(delete("/api/banking/accounts/" + accountId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete account with non-zero balance. Please transfer or withdraw all funds first."));
        }
    }

    // Helper method to create a test account and return its ID
    private String createTestAccount(String firstName, String lastName) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setNationality("United States");
        request.setAccountType("CHECKING");

        MvcResult result = mockMvc.perform(post("/api/banking/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        // Parse the response to extract the account ID
        // This is a simplified extraction - in real scenarios, you might use JsonPath
        int idIndex = jsonResponse.indexOf("\"id\":\"") + 6;
        int endIndex = jsonResponse.indexOf("\"", idIndex);
        return jsonResponse.substring(idIndex, endIndex);
    }
}
