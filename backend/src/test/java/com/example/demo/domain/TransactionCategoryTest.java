package com.example.demo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionCategory domain entity
 */
@DisplayName("TransactionCategory Domain Tests")
class TransactionCategoryTest {

    @Nested
    @DisplayName("Category Creation Tests")
    class CategoryCreationTests {

        @Test
        @DisplayName("Should create income category with valid data")
        void shouldCreateIncomeCategoryWithValidData() {
            // When
            TransactionCategory category = TransactionCategory.create(
                "Salary",
                "Monthly salary income",
                TransactionCategory.CategoryType.INCOME,
                "#2ecc71"
            );

            // Then
            assertNotNull(category);
            assertEquals("SALARY", category.getName()); // Name should be uppercase
            assertEquals("Monthly salary income", category.getDescription());
            assertEquals(TransactionCategory.CategoryType.INCOME, category.getType());
            assertEquals("#2ecc71", category.getColor());
            assertTrue(category.isActive());
            assertNull(category.getId()); // ID is null before persistence
            assertNotNull(category.getCreatedAt());
        }

        @Test
        @DisplayName("Should create expense category with valid data")
        void shouldCreateExpenseCategoryWithValidData() {
            // When
            TransactionCategory category = TransactionCategory.create(
                "Groceries",
                "Food and household items",
                TransactionCategory.CategoryType.EXPENSE,
                "#e74c3c"
            );

            // Then
            assertNotNull(category);
            assertEquals("GROCERIES", category.getName());
            assertEquals(TransactionCategory.CategoryType.EXPENSE, category.getType());
            assertEquals("#e74c3c", category.getColor());
        }

        @Test
        @DisplayName("Should use default color when color is null")
        void shouldUseDefaultColorWhenColorIsNull() {
            // When
            TransactionCategory category = TransactionCategory.create(
                "Bonus",
                "Yearly bonus",
                TransactionCategory.CategoryType.INCOME,
                null
            );

            // Then
            assertEquals("#3498db", category.getColor()); // Default color
        }

        @Test
        @DisplayName("Should convert name to uppercase")
        void shouldConvertNameToUppercase() {
            // When
            TransactionCategory category = TransactionCategory.create(
                "salary",
                "Monthly salary",
                TransactionCategory.CategoryType.INCOME,
                "#2ecc71"
            );

            // Then
            assertEquals("SALARY", category.getName());
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCategory.create(
                    null,
                    "Description",
                    TransactionCategory.CategoryType.INCOME,
                    "#000000"
                )
            );
            assertEquals("Category name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCategory.create(
                    "  ",
                    "Description",
                    TransactionCategory.CategoryType.INCOME,
                    "#000000"
                )
            );
            assertEquals("Category name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when name exceeds 50 characters")
        void shouldThrowExceptionWhenNameExceeds50Characters() {
            // Given
            String longName = "A".repeat(51);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCategory.create(
                    longName,
                    "Description",
                    TransactionCategory.CategoryType.INCOME,
                    "#000000"
                )
            );
            assertEquals("Category name cannot exceed 50 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionCategory.create(
                    "Salary",
                    "Description",
                    null,
                    "#000000"
                )
            );
            assertEquals("Category type cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept name with exactly 50 characters")
        void shouldAcceptNameWithExactly50Characters() {
            // Given
            String exactName = "A".repeat(50);

            // When & Then
            assertDoesNotThrow(() -> TransactionCategory.create(
                exactName,
                "Description",
                TransactionCategory.CategoryType.INCOME,
                "#000000"
            ));
        }
    }

    @Nested
    @DisplayName("Category Update Tests")
    class CategoryUpdateTests {

        @Test
        @DisplayName("Should update category details")
        void shouldUpdateCategoryDetails() {
            // Given
            TransactionCategory category = createTestCategory();

            // When
            TransactionCategory updatedCategory = category.update(
                "NewName",
                "New description",
                "#ff0000"
            );

            // Then
            assertEquals("NEWNAME", updatedCategory.getName());
            assertEquals("New description", updatedCategory.getDescription());
            assertEquals("#ff0000", updatedCategory.getColor());
            assertEquals(category.getType(), updatedCategory.getType()); // Type should not change
            assertEquals(category.isActive(), updatedCategory.isActive()); // Active status should not change
        }

        @Test
        @DisplayName("Should keep existing color when new color is null")
        void shouldKeepExistingColorWhenNewColorIsNull() {
            // Given
            TransactionCategory category = createTestCategory();
            String originalColor = category.getColor();

            // When
            TransactionCategory updatedCategory = category.update(
                "UpdatedName",
                "Updated description",
                null
            );

            // Then
            assertEquals(originalColor, updatedCategory.getColor());
        }

        @Test
        @DisplayName("Should throw exception when updating with null name")
        void shouldThrowExceptionWhenUpdatingWithNullName() {
            // Given
            TransactionCategory category = createTestCategory();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> category.update(null, "Description", "#000000")
            );
            assertEquals("Category name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating with name exceeding 50 characters")
        void shouldThrowExceptionWhenUpdatingWithLongName() {
            // Given
            TransactionCategory category = createTestCategory();
            String longName = "A".repeat(51);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> category.update(longName, "Description", "#000000")
            );
            assertEquals("Category name cannot exceed 50 characters", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Category Activation Tests")
    class CategoryActivationTests {

        @Test
        @DisplayName("Should deactivate active category")
        void shouldDeactivateActiveCategory() {
            // Given
            TransactionCategory category = createTestCategory();
            assertTrue(category.isActive());

            // When
            TransactionCategory deactivatedCategory = category.deactivate();

            // Then
            assertFalse(deactivatedCategory.isActive());
            assertEquals(category.getName(), deactivatedCategory.getName());
            assertEquals(category.getType(), deactivatedCategory.getType());
        }

        @Test
        @DisplayName("Should activate inactive category")
        void shouldActivateInactiveCategory() {
            // Given
            TransactionCategory category = createTestCategory().deactivate();
            assertFalse(category.isActive());

            // When
            TransactionCategory activatedCategory = category.activate();

            // Then
            assertTrue(activatedCategory.isActive());
            assertEquals(category.getName(), activatedCategory.getName());
        }

        @Test
        @DisplayName("Should be idempotent when deactivating already inactive category")
        void shouldBeIdempotentWhenDeactivatingInactiveCategory() {
            // Given
            TransactionCategory category = createTestCategory().deactivate();

            // When
            TransactionCategory deactivatedAgain = category.deactivate();

            // Then
            assertFalse(deactivatedAgain.isActive());
        }

        @Test
        @DisplayName("Should be idempotent when activating already active category")
        void shouldBeIdempotentWhenActivatingActiveCategory() {
            // Given
            TransactionCategory category = createTestCategory();

            // When
            TransactionCategory activatedAgain = category.activate();

            // Then
            assertTrue(activatedAgain.isActive());
        }
    }

    @Nested
    @DisplayName("Category Reconstitution Tests")
    class CategoryReconstitutionTests {

        @Test
        @DisplayName("Should reconstitute category from persistence")
        void shouldReconstituteCategoryFromPersistence() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

            // When
            TransactionCategory category = TransactionCategory.reconstitute(
                id,
                "SALARY",
                "Monthly salary",
                TransactionCategory.CategoryType.INCOME,
                "#2ecc71",
                true,
                createdAt
            );

            // Then
            assertNotNull(category);
            assertEquals(id, category.getId());
            assertEquals("SALARY", category.getName());
            assertEquals("Monthly salary", category.getDescription());
            assertEquals(TransactionCategory.CategoryType.INCOME, category.getType());
            assertEquals("#2ecc71", category.getColor());
            assertTrue(category.isActive());
            assertEquals(createdAt, category.getCreatedAt());
        }

        @Test
        @DisplayName("Should reconstitute inactive category")
        void shouldReconstituteInactiveCategory() {
            // When
            TransactionCategory category = TransactionCategory.reconstitute(
                UUID.randomUUID(),
                "OLDCATEGORY",
                "Old category",
                TransactionCategory.CategoryType.EXPENSE,
                "#000000",
                false,
                LocalDateTime.now()
            );

            // Then
            assertFalse(category.isActive());
        }
    }

    @Nested
    @DisplayName("Category Immutability Tests")
    class CategoryImmutabilityTests {

        @Test
        @DisplayName("Should create new instance when updating")
        void shouldCreateNewInstanceWhenUpdating() {
            // Given
            TransactionCategory original = createTestCategory();

            // When
            TransactionCategory updated = original.update(
                "NewName",
                "New description",
                "#ff0000"
            );

            // Then
            assertNotSame(original, updated);
            assertEquals("SALARY", original.getName()); // Original unchanged
            assertEquals("NEWNAME", updated.getName());
        }

        @Test
        @DisplayName("Should create new instance when deactivating")
        void shouldCreateNewInstanceWhenDeactivating() {
            // Given
            TransactionCategory original = createTestCategory();

            // When
            TransactionCategory deactivated = original.deactivate();

            // Then
            assertNotSame(original, deactivated);
            assertTrue(original.isActive()); // Original unchanged
            assertFalse(deactivated.isActive());
        }

        @Test
        @DisplayName("Should create new instance when activating")
        void shouldCreateNewInstanceWhenActivating() {
            // Given
            TransactionCategory original = createTestCategory().deactivate();

            // When
            TransactionCategory activated = original.activate();

            // Then
            assertNotSame(original, activated);
            assertFalse(original.isActive()); // Original unchanged
            assertTrue(activated.isActive());
        }
    }

    // Helper method
    private TransactionCategory createTestCategory() {
        return TransactionCategory.create(
            "Salary",
            "Monthly salary income",
            TransactionCategory.CategoryType.INCOME,
            "#2ecc71"
        );
    }
}
