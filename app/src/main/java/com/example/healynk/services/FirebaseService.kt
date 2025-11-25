package com.example.healynk.services

import com.example.healynk.models.ActivityEntry
import com.example.healynk.models.FoodEntry
import com.example.healynk.models.Measurement
import com.example.healynk.utils.Constants
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private fun userCollection(userId: String, child: String) = firestore
        .collection(Constants.USERS_COLLECTION)
        .document(userId)
        .collection(child)

    fun measurementsFlow(userId: String): Flow<List<Measurement>> = collectionFlow(
        userId,
        Constants.MEASUREMENTS_COLLECTION
    ) { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(Measurement::class.java)?.copy(id = doc.id, userId = userId)
        }
    }

    fun activitiesFlow(userId: String): Flow<List<ActivityEntry>> = collectionFlow(
        userId,
        Constants.ACTIVITIES_COLLECTION
    ) { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(ActivityEntry::class.java)?.copy(id = doc.id, userId = userId)
        }
    }

    fun foodsFlow(userId: String): Flow<List<FoodEntry>> = collectionFlow(
        userId,
        Constants.FOODS_COLLECTION
    ) { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(FoodEntry::class.java)?.copy(id = doc.id, userId = userId)
        }
    }

    private fun <T> collectionFlow(
        userId: String,
        collection: String,
        mapper: (QuerySnapshot) -> List<T>
    ): Flow<List<T>> = callbackFlow {
        val listener = userCollection(userId, collection)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot != null) {
                    trySend(mapper(snapshot))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addMeasurement(userId: String, measurement: Measurement) {
        writeDocument(userId, Constants.MEASUREMENTS_COLLECTION, measurement.id, measurement.toMap())
    }

    suspend fun addActivity(userId: String, activityEntry: ActivityEntry) {
        writeDocument(userId, Constants.ACTIVITIES_COLLECTION, activityEntry.id, activityEntry.toMap())
    }

    suspend fun addFood(userId: String, foodEntry: FoodEntry) {
        writeDocument(userId, Constants.FOODS_COLLECTION, foodEntry.id, foodEntry.toMap())
    }

    suspend fun deleteMeasurement(userId: String, id: String) {
        deleteDocument(userId, Constants.MEASUREMENTS_COLLECTION, id)
    }

    suspend fun deleteActivity(userId: String, id: String) {
        deleteDocument(userId, Constants.ACTIVITIES_COLLECTION, id)
    }

    suspend fun deleteFood(userId: String, id: String) {
        deleteDocument(userId, Constants.FOODS_COLLECTION, id)
    }

    private suspend fun writeDocument(
        userId: String,
        collection: String,
        id: String,
        data: Map<String, Any?>
    ) {
        val collectionRef = userCollection(userId, collection)
        val documentId = id.ifEmpty { collectionRef.document().id }
        val finalData = data.toMutableMap().apply {
            put("id", documentId)
            put("userId", userId)
        }
        collectionRef.document(documentId).set(finalData).await()
    }

    private suspend fun deleteDocument(userId: String, collection: String, id: String) {
        doc(userId, collection, id).delete().await()
    }

    private fun doc(userId: String, collection: String, id: String): DocumentReference {
        val collectionRef = userCollection(userId, collection)
        val documentId = id.ifEmpty { collectionRef.document().id }
        return collectionRef.document(documentId)
    }
}
