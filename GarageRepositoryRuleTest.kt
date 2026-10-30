package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.GarageItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class GarageRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveItem_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = GarageRepository(firestore)
        val itemId = "item_${UUID.randomUUID().toString().replace("-", "")}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createItem(
                GarageItem(
                    id = itemId,
                    userId = aliceUid,
                    title = "Шины Michelin 205/55 R16",
                    description = "Комплект зимних шин в отличном состоянии",
                    price = 24000L,
                    quantity = 4L,
                    unitLabel = "шт. (комплект)",
                    zone = "Склад 1",
                    shelfLocation = "Сектор Б-2, полка 3",
                    qrCode = "#GAR-112-II",
                    barcode = "4607112883491"
                )
            )
        }
        assertTrue(createResult.isSuccess)

        val emittedItems = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeItems(aliceUid).first { list -> list.any { it.id == itemId } }
        }
        assertTrue(emittedItems.any { it.id == itemId && it.title == "Шины Michelin 205/55 R16" })
    }

    @Test
    fun getItemById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = GarageRepository(firestore)
        val itemId = "item_${UUID.randomUUID().toString().replace("-", "")}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createItem(
                GarageItem(
                    id = itemId,
                    userId = aliceUid,
                    title = "Домкрат подкатной 2т",
                    price = 4500L,
                    zone = "Гараж",
                    shelfLocation = "Стеллаж А1, на полу",
                    qrCode = "#GAR-205-I"
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = GarageRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getItemById(aliceUid, itemId)
        }
        assertTrue(result.isFailure)
        val fsEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeItems_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = GarageRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeItems(aliceUid).first()
            }
            fail("Expected exception for unauthenticated user")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@garage.test"
        const val BOB_EMAIL = "bob@garage.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
