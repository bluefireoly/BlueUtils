package net.axay.blueutils.database.mongodb

import com.mongodb.client.MongoCollection
import net.axay.blueutils.database.DatabaseLoginInformation
import org.litote.kmongo.getCollection

/**
 * An instance of this class represents a connection to a singke
 * MongoDB database.
 *
 * @param kMongo set this to true if you are working with KMongo (for kotlin)
 * @param spigot set this to true if you are working with spigot (a minecraft server api)
 */
class SyncMongoDB(
    loginInformation: DatabaseLoginInformation,
    kMongo: Boolean = true,
    spigot: Boolean = false,
) : MongoDB<com.mongodb.client.MongoClient, com.mongodb.client.MongoDatabase>(
    loginInformation, kMongo, spigot,

    clientCreator = { clientSettings, ifKMongo ->
        if (ifKMongo) org.litote.kmongo.KMongo.createClient(clientSettings)
        else com.mongodb.client.MongoClients.create(clientSettings)
    },

    databaseCreator = { client, databaseName ->
        client.getDatabase(databaseName)
    }
) {

    /**
     * Tries to get the given collection and creates a new one
     * if no collection could be found.
     *
     * @param name the name of the collection
     * @param onCreate an optional callback, which should be invoked if a new
     * collection was created
     */
    inline fun <reified T : Any> getCollectionOrCreate(
        name: String,
        noinline onCreate: ((MongoCollection<T>) -> Unit)? = null
    ): MongoCollection<T> {

        val ifNew = createCollection(name)

        val collection = database.getCollection<T>(name)
        if (ifNew) onCreate?.invoke(collection)

        return collection

    }

    /**
     * Creates a new collection.
     *
     * @param name the name of the new collection
     * @return true, if a new collection was created
     */
    override fun createCollection(name: String) =
        if (!database.listCollectionNames().contains(name)) {
            database.createCollection(name)
            true
        } else false

    override fun close() = mongoClient.close()

}