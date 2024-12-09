const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendEventNotification = functions.database
  .ref("/events/{eventId}")
  .onCreate((snapshot, context) => {
    const event = snapshot.val();
    const title = event.title || "New Event";
    const message = `New event: ${event.title} on ${event.date} at ${event.location}.`;

    // Create the notification payload
    const payload = {
      notification: {
        title: title,
        body: message,
      },
    };

    // Send notification to all users
    return admin.messaging().sendToTopic("events", payload)
      .then((response) => {
        console.log("Notification sent successfully:", response);
      })
      .catch((error) => {
        console.log("Error sending notification:", error);
      });
  });
