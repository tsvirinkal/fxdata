package com.vts.fxdata.notifications;

import com.niamedtech.expo.exposerversdk.ExpoPushMessage;
import com.niamedtech.expo.exposerversdk.ExpoPushMessageTicketPair;
import com.niamedtech.expo.exposerversdk.ExpoPushReceipt;
import com.niamedtech.expo.exposerversdk.ExpoPushTicket;
import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class NotificationServer {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void send(String recipient, String title, String msgLine1, String msgLine2, Map<String, Object> data) throws PushClientException, InterruptedException {

        if (!PushClient.isExponentPushToken(recipient))
            throw new Error("Token:" + recipient + " is not a valid token.");

        ExpoPushMessage expoPushMessage = new ExpoPushMessage();
        expoPushMessage.getTo().add(recipient);
        expoPushMessage.setTitle(msgLine1);
        //expoPushMessage.setSubtitle(msgLine1);
        expoPushMessage.setBody(msgLine2);
        expoPushMessage.setData(data);

        List<ExpoPushMessage> expoPushMessages = new ArrayList<>();
        expoPushMessages.add(expoPushMessage);

        PushClient client = new PushClient();
        List<List<ExpoPushMessage>> chunks = client.chunkPushNotifications(expoPushMessages);

        executor.submit(() -> {
            try {
                List<CompletableFuture<List<ExpoPushTicket>>> messageRepliesFutures = new ArrayList<>();

                for (List<ExpoPushMessage> chunk : chunks) {
                    messageRepliesFutures.add(client.sendPushNotificationsAsync(chunk));
                }

                // Wait for each completable future to finish
                List<ExpoPushTicket> allTickets = new ArrayList<>();
                for (CompletableFuture<List<ExpoPushTicket>> messageReplyFuture : messageRepliesFutures) {
                    try {
                        for (ExpoPushTicket ticket : messageReplyFuture.get()) {
                            allTickets.add(ticket);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                List<ExpoPushMessageTicketPair<ExpoPushMessage>> zippedMessagesTickets = client.zipMessagesTickets(expoPushMessages, allTickets);

                List<ExpoPushMessageTicketPair<ExpoPushMessage>> okTicketMessages = client.filterAllSuccessfulMessages(zippedMessagesTickets);
                String okTicketMessagesString = okTicketMessages.stream().map(
                        p -> "Title: " + p.message.getTitle() + ", Id:" + p.ticket.getId()
                ).collect(Collectors.joining(","));
                System.out.println("Recieved OK ticket for " +okTicketMessages.size() +" messages: " + okTicketMessagesString);

                List<ExpoPushMessageTicketPair<ExpoPushMessage>> errorTicketMessages = client.filterAllMessagesWithError(zippedMessagesTickets);
                String errorTicketMessagesString = errorTicketMessages.stream().map(
                        p -> "Title: " + p.message.getTitle() + ", Error: " + p.ticket.getDetails().getError()
                ).collect(Collectors.joining(","));
                if (errorTicketMessages.size()>0) {
                    System.out.println("Recieved ERROR ticket for " + errorTicketMessages.size() + " messages: " + errorTicketMessagesString);
                }

                // Countdown 10s
                Thread.sleep(1000);

                System.out.println("Fetching reciepts...");

                List<String> ticketIds = (client.getTicketIdsFromPairs(okTicketMessages));
                CompletableFuture<List<ExpoPushReceipt>> receiptFutures = client.getPushNotificationReceiptsAsync(ticketIds);

                List<ExpoPushReceipt> receipts = new ArrayList<>();
                try {
                    receipts = receiptFutures.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Recieved " + receipts.size() + " receipts:");
                for (ExpoPushReceipt reciept : receipts) {
                    System.out.println("Receipt for id: " +reciept.getId() +" had status: " +reciept.getStatus());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Pushed notification to: "+recipient);
    }
}
