package com.priyank.drdelivery.shipmentDetails.data.remote

import android.accounts.Account
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class GetEmails {
    // This function will be improvement in future I have just hacked my way around to make things work right now
    suspend fun getEmails(
        applicationContext: Context,
        gsa: GoogleSignInAccount?
    ): List<Message> {

        val messageList: MutableList<Message> = mutableListOf()
        lateinit var emailList: Deferred<ListMessagesResponse?>
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, listOf(GmailScopes.GMAIL_READONLY)
        )
            .setBackOff(ExponentialBackOff())
            .setSelectedAccount(
                Account(
                    gsa!!.email, "Dr.Delivery"

                )
            )

        val service = Gmail.Builder(
            NetHttpTransport(), AndroidJsonFactory.getDefaultInstance(), credential
        )
            .setApplicationName("DocDelivery")
            .build()

        val getEmailList = GlobalScope.launch {
            try {
                emailList =
                    async {
                        service.users().messages()?.list("me")?.execute()
                    }
            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
            }
        }

        getEmailList.join()

        val job = GlobalScope.launch(Dispatchers.IO) {

            for (i in 0 until emailList.await()?.messages!!.size) {
                val email =

                    async {
                        service.users().messages().get("me", emailList.await()!!.messages[i].id)
                            .setFormat("Full").execute()
                    }
                messageList.add(email.await())
            }
        }

        getEmailList.join()
        job.join()

        return messageList
    }
}